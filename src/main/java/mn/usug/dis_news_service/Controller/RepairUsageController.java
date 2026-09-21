package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.*;
import mn.usug.dis_news_service.Entity.*;
import mn.usug.dis_news_service.Model.RepairUsageDto;
import mn.usug.dis_news_service.Model.RepairWorkerHistoryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final VehicleRepairRepository repairRepo;
    private final VehicleRepository vehicleRepo;
    private final RepairCategoryRepository categoryRepo;

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

    /**
     * Сэлбэгийн мөр нэмнэ. Үнэ өгөгдөөгүй бол лавлахаас хуулж авна.
     *
     * Лавлах дээр үлдэгдэл хөтөлсөн бол (stock_qty NULL биш) зарцуулсан тоогоор
     * хасна. Хүрэлцэхгүй бол бүртгэлийг зогсооно — үлдэгдэл хасах тал руу
     * явбал тоо баримт утгагүй болно.
     */
    @PostMapping("/part")
    @Transactional
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

        if (ref.getStockQty() != null) {
            BigDecimal left = ref.getStockQty().subtract(line.getQty());
            if (left.signum() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Үлдэгдэл хүрэлцэхгүй байна. \"" + ref.getName() + "\" — үлдэгдэл "
                        + ref.getStockQty().stripTrailingZeros().toPlainString()
                        + ", зарцуулах гэж буй " + line.getQty().stripTrailingZeros().toPlainString()
                        + ". Сэлбэгийн лавлах дээр үлдэгдлээ шинэчилнэ үү.");
            }
            ref.setStockQty(left);
            partRepo.save(ref);
        }
        return partLineRepo.save(line);
    }

    /** Мөр хасахад үлдэгдлийг буцааж нэмнэ */
    @DeleteMapping("/part/{id}")
    @Transactional
    public ResponseEntity<Void> removePart(@PathVariable Long id) {
        partLineRepo.findById(id).ifPresent(line -> {
            // Идэвхтэй мөрийг л буцаана — давхар дуудлагад үлдэгдэл хөөрөхгүй
            if (!ACTIVE.equals(line.getActiveFlag())) return;
            line.setActiveFlag(INACTIVE);
            partLineRepo.save(line);

            partRepo.findById(line.getRepairPartId()).ifPresent(ref -> {
                if (ref.getStockQty() == null) return;
                BigDecimal qty = line.getQty() != null ? line.getQty() : BigDecimal.ZERO;
                ref.setStockQty(ref.getStockQty().add(qty));
                partRepo.save(ref);
            });
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

    /**
     * Ажилтны ажлын түүх — хэн аль машинд, ямар засварт, хэдэн цаг ажилласан.
     *
     * from/to өгвөл засварын ЭХЛЭХ огноогоор шүүнэ. Ажилтан бүр нэг мөр болж,
     * дотроо оролцсон засваруудаа (сэлбэгийн жагсаалттай нь) агуулна.
     */
    @GetMapping("/worker-history")
    public List<RepairWorkerHistoryDto> workerHistory(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate   = parseDate(to);

        Map<Long, RepairWorker> workers = workerRepo.findAll().stream()
                .collect(Collectors.toMap(RepairWorker::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> specialties = specialtyRepo.findAll().stream()
                .collect(Collectors.toMap(RepairSpecialty::getId, RepairSpecialty::getName, (a, b) -> a));
        Map<Long, RepairPart> parts = partRepo.findAll().stream()
                .collect(Collectors.toMap(RepairPart::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> partTypes = partTypeRepo.findAll().stream()
                .collect(Collectors.toMap(RepairPartType::getId, RepairPartType::getName, (a, b) -> a));
        Map<Long, Vehicle> vehicles = vehicleRepo.findAll().stream()
                .collect(Collectors.toMap(Vehicle::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> categories = categoryRepo.findAll().stream()
                .collect(Collectors.toMap(RepairCategory::getId, RepairCategory::getName, (a, b) -> a));

        // Огнооны мужид багтах засварууд
        Map<Long, VehicleRepair> repairs = new LinkedHashMap<>();
        for (VehicleRepair r : repairRepo.findAll()) {
            if (!ACTIVE.equals(r.getActiveFlag())) continue;
            LocalDate start = r.getStartDate();
            if (fromDate != null && (start == null || start.isBefore(fromDate))) continue;
            if (toDate != null && (start == null || start.isAfter(toDate))) continue;
            repairs.put(r.getId(), r);
        }

        // Засвар бүрийн сэлбэг — нэг удаа уншаад бүлэглэнэ
        Map<Long, List<RepairWorkerHistoryDto.PartUse>> partsByRepair = new LinkedHashMap<>();
        Map<Long, BigDecimal> partsAmountByRepair = new LinkedHashMap<>();
        for (VehicleRepairPart line : partLineRepo.findAll()) {
            if (!ACTIVE.equals(line.getActiveFlag())) continue;
            if (!repairs.containsKey(line.getVehicleRepairId())) continue;

            RepairPart ref = parts.get(line.getRepairPartId());
            BigDecimal qty   = line.getQty() != null ? line.getQty() : BigDecimal.ZERO;
            BigDecimal price = line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal amount = qty.multiply(price);

            partsByRepair.computeIfAbsent(line.getVehicleRepairId(), k -> new ArrayList<>())
                    .add(RepairWorkerHistoryDto.PartUse.builder()
                            .partName(ref != null ? ref.getName() : "—")
                            .partTypeName(ref != null ? partTypes.getOrDefault(ref.getPartTypeId(), "") : "")
                            .qty(qty)
                            .unit(ref != null ? ref.getUnit() : null)
                            .amount(amount)
                            .build());
            partsAmountByRepair.merge(line.getVehicleRepairId(), amount, BigDecimal::add);
        }

        // Ажилтан бүрийн мөрүүдийг цуглуулна
        Map<Long, List<RepairWorkerHistoryDto.Job>> jobsByWorker = new LinkedHashMap<>();
        Map<Long, BigDecimal> hoursByWorker = new LinkedHashMap<>();
        for (VehicleRepairWorker line : workerLineRepo.findAll()) {
            if (!ACTIVE.equals(line.getActiveFlag())) continue;
            VehicleRepair r = repairs.get(line.getVehicleRepairId());
            if (r == null) continue;

            Vehicle v = r.getVehicleId() != null ? vehicles.get(r.getVehicleId()) : null;
            String vehicleText = v == null ? "" :
                    ((v.getBrand() == null ? "" : v.getBrand()) + " " + (v.getModel() == null ? "" : v.getModel())).trim();

            jobsByWorker.computeIfAbsent(line.getRepairWorkerId(), k -> new ArrayList<>())
                    .add(RepairWorkerHistoryDto.Job.builder()
                            .repairId(r.getId())
                            .plateNumber(r.getPlateNumber() != null ? r.getPlateNumber()
                                    : (v != null ? v.getPlateNumber() : "—"))
                            .vehicleText(vehicleText)
                            .categoryName(categories.getOrDefault(r.getRepairCategoryId(), ""))
                            .location(r.getLocation())
                            .faultDescription(r.getFaultDescription())
                            .startDate(r.getStartDate())
                            .endDate(r.getEndDate())
                            .status(r.getStatus())
                            .hours(line.getHours())
                            .parts(partsByRepair.getOrDefault(r.getId(), List.of()))
                            .build());

            hoursByWorker.merge(line.getRepairWorkerId(),
                    line.getHours() != null ? line.getHours() : BigDecimal.ZERO, BigDecimal::add);
        }

        List<RepairWorkerHistoryDto> out = new ArrayList<>();
        for (Map.Entry<Long, List<RepairWorkerHistoryDto.Job>> e : jobsByWorker.entrySet()) {
            RepairWorker w = workers.get(e.getKey());
            List<RepairWorkerHistoryDto.Job> jobs = e.getValue();
            jobs.sort((a, b) -> {
                LocalDate x = a.getStartDate(), y = b.getStartDate();
                if (x == null && y == null) return 0;
                if (x == null) return 1;
                if (y == null) return -1;
                return y.compareTo(x);          // сүүлийн засвар эхэнд
            });

            // Нэг засварт хоёр мөр бүртгэсэн ч машиныг давхар тоолохгүй
            long vehicleCount = jobs.stream().map(RepairWorkerHistoryDto.Job::getPlateNumber).distinct().count();
            BigDecimal partsAmount = jobs.stream()
                    .map(RepairWorkerHistoryDto.Job::getRepairId)
                    .distinct()
                    .map(id -> partsAmountByRepair.getOrDefault(id, BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            out.add(RepairWorkerHistoryDto.builder()
                    .workerId(e.getKey())
                    .workerName(w != null ? w.getName() : "—")
                    .specialtyName(w != null ? specialties.getOrDefault(w.getSpecialtyId(), "") : "")
                    .activeFlag(w != null ? w.getActiveFlag() : 1)
                    .repairCount(jobs.size())
                    .vehicleCount((int) vehicleCount)
                    .totalHours(hoursByWorker.getOrDefault(e.getKey(), BigDecimal.ZERO))
                    .partsAmount(partsAmount)
                    .jobs(jobs)
                    .build());
        }

        out.sort((a, b) -> b.getTotalHours().compareTo(a.getTotalHours()));
        return out;
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception ignored) {
            return null;
        }
    }
}
