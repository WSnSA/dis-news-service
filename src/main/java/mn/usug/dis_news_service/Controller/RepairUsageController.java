package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.*;
import mn.usug.dis_news_service.Entity.*;
import mn.usug.dis_news_service.Model.RepairUsageDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Засварын зарцуулалт — тухайн засварт ямар сэлбэг зарцуулсан, хэн ажилласан.
 *
 * Лавлах жагсаалтууд (repair_part, repair_worker) дээр тулгуурлана. Сэлбэгийн
 * нэгж үнийг мөр нэмэх үед лавлахаас хуулж авна — каталогийн үнэ хожим
 * өөрчлөгдсөн ч бүртгэсэн үеийн дүн хэвээр үлдэнэ.
 */
@RestController
@RequestMapping("/repair/usage")
@RequiredArgsConstructor
public class RepairUsageController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    private final VehicleRepairPartRepository partLineRepo;
    private final VehicleRepairWorkerRepository workerLineRepo;
    private final RepairPartRepository partRepo;
    private final RepairPartTypeRepository partTypeRepo;
    private final RepairWorkerRepository workerRepo;
    private final RepairSpecialtyRepository specialtyRepo;

    /** Нэг засварын бүх зарцуулалт + нийт дүн */
    @GetMapping("/{repairId}")
    public RepairUsageDto get(@PathVariable Long repairId) {
        Map<Long, RepairPart> parts = partRepo.findAll().stream()
                .collect(Collectors.toMap(RepairPart::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> partTypes = partTypeRepo.findAll().stream()
                .collect(Collectors.toMap(RepairPartType::getId, RepairPartType::getName, (a, b) -> a));
        Map<Long, RepairWorker> workers = workerRepo.findAll().stream()
                .collect(Collectors.toMap(RepairWorker::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> specialties = specialtyRepo.findAll().stream()
                .collect(Collectors.toMap(RepairSpecialty::getId, RepairSpecialty::getName, (a, b) -> a));

        List<RepairUsageDto.PartLine> partLines = partLineRepo
                .findByVehicleRepairIdAndActiveFlagOrderByIdAsc(repairId, ACTIVE)
                .stream()
                .map(line -> {
                    RepairPart ref = parts.get(line.getRepairPartId());
                    BigDecimal qty = line.getQty() != null ? line.getQty() : BigDecimal.ZERO;
                    BigDecimal price = line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO;
                    return RepairUsageDto.PartLine.builder()
                            .id(line.getId())
                            .repairPartId(line.getRepairPartId())
                            .partName(ref != null ? ref.getName() : "—")
                            .partTypeName(ref != null ? partTypes.getOrDefault(ref.getPartTypeId(), "") : "")
                            .unit(ref != null ? ref.getUnit() : null)
                            .qty(qty)
                            .unitPrice(price)
                            .amount(qty.multiply(price))
                            .note(line.getNote())
                            .build();
                })
                .toList();

        List<RepairUsageDto.WorkerLine> workerLines = workerLineRepo
                .findByVehicleRepairIdAndActiveFlagOrderByIdAsc(repairId, ACTIVE)
                .stream()
                .map(line -> {
                    RepairWorker ref = workers.get(line.getRepairWorkerId());
                    return RepairUsageDto.WorkerLine.builder()
                            .id(line.getId())
                            .repairWorkerId(line.getRepairWorkerId())
                            .workerName(ref != null ? ref.getName() : "—")
                            .specialtyName(ref != null ? specialties.getOrDefault(ref.getSpecialtyId(), "") : "")
                            .hours(line.getHours())
                            .note(line.getNote())
                            .build();
                })
                .toList();

        return RepairUsageDto.builder()
                .vehicleRepairId(repairId)
                .parts(partLines)
                .workers(workerLines)
                .partsTotal(partLines.stream()
                        .map(RepairUsageDto.PartLine::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .totalHours(workerLines.stream()
                        .map(w -> w.getHours() != null ? w.getHours() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();
    }

    /** Сэлбэгийн мөр нэмнэ. Үнэ өгөгдөөгүй бол лавлахаас хуулж авна. */
    @PostMapping("/part")
    public VehicleRepairPart addPart(@RequestBody VehicleRepairPart line) {
        if (line.getVehicleRepairId() == null || line.getRepairPartId() == null) {
            throw new IllegalArgumentException("Засвар болон сэлбэгээ сонгоно уу");
        }
        RepairPart ref = partRepo.findById(line.getRepairPartId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сэлбэг олдсонгүй"));

        line.setId(null);
        line.setActiveFlag(ACTIVE);
        if (line.getQty() == null || line.getQty().signum() <= 0) {
            line.setQty(BigDecimal.ONE);
        }
        if (line.getUnitPrice() == null) {
            line.setUnitPrice(ref.getUnitPrice() != null ? ref.getUnitPrice() : BigDecimal.ZERO);
        }
        return partLineRepo.save(line);
    }

    @DeleteMapping("/part/{id}")
    public ResponseEntity<Void> removePart(@PathVariable Long id) {
        partLineRepo.findById(id).ifPresent(line -> {
            line.setActiveFlag(INACTIVE);
            partLineRepo.save(line);
        });
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/worker")
    public VehicleRepairWorker addWorker(@RequestBody VehicleRepairWorker line) {
        if (line.getVehicleRepairId() == null || line.getRepairWorkerId() == null) {
            throw new IllegalArgumentException("Засвар болон ажилтнаа сонгоно уу");
        }
        workerRepo.findById(line.getRepairWorkerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ажилтан олдсонгүй"));
        line.setId(null);
        line.setActiveFlag(ACTIVE);
        return workerLineRepo.save(line);
    }

    @DeleteMapping("/worker/{id}")
    public ResponseEntity<Void> removeWorker(@PathVariable Long id) {
        workerLineRepo.findById(id).ifPresent(line -> {
            line.setActiveFlag(INACTIVE);
            workerLineRepo.save(line);
        });
        return ResponseEntity.noContent().build();
    }
}
