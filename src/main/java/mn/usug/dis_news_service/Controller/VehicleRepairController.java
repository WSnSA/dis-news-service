package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.RepairCategoryRepository;
import mn.usug.dis_news_service.DAO.VehicleRepairRepository;
import mn.usug.dis_news_service.DAO.VehicleRepository;
import mn.usug.dis_news_service.Entity.RepairCategory;
import mn.usug.dis_news_service.Entity.Vehicle;
import mn.usug.dis_news_service.Entity.VehicleRepair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Засварын бүртгэл — машин тус бүрийн засварын тэмдэглэл.
 *
 * Нээлттэй бичлэгтэй (status=0) машиныг "Машин хуваарилалт" дэлгэц сонголтоос хасна.
 */
@RestController
@RequestMapping("/repair/vehicle")
@RequiredArgsConstructor
public class VehicleRepairController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    private final VehicleRepairRepository  repository;
    private final VehicleRepository        vehicleRepository;
    private final RepairCategoryRepository categoryRepository;
    private final mn.usug.dis_news_service.DAO.RepairWorkerRepository workerRepository;

    /** Бүх засварын бүртгэл. `status` өгвөл (0=засварт, 1=дууссан) шүүнэ. */
    @GetMapping("/getAll")
    public List<VehicleRepair> getAll(@RequestParam(required = false) Integer status) {
        List<VehicleRepair> rows = (status == null)
                ? repository.findByActiveFlagOrderByStartDateDescIdDesc(ACTIVE)
                : repository.findByActiveFlagAndStatusOrderByStartDateDescIdDesc(ACTIVE, status);
        return enrich(rows);
    }

    /**
     * Одоо засварт байгаа (status=0) бүх бичлэг.
     * Машин хуваарилалт эндээс уншаад захиалгын хугацаатай давхцаж буй машиныг хасна.
     */
    @GetMapping("/active")
    public List<VehicleRepair> getActive() {
        return enrich(repository.findByActiveFlagAndStatusOrderByStartDateDescIdDesc(ACTIVE, VehicleRepair.STATUS_IN_REPAIR));
    }

    /** Нэг машины засварын түүх — автобаазын "Засварын түүх" таб */
    @GetMapping("/by-vehicle")
    public List<VehicleRepair> getByVehicle(@RequestParam String plate) {
        if (plate == null || plate.isBlank()) return List.of();
        return enrich(repository.findByPlate(plate.trim()));
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody VehicleRepair body) {
        if (body.getVehicleId() == null)        return badRequest("Машин сонгоно уу");
        if (body.getRepairCategoryId() == null) return badRequest("Засварын ангилал сонгоно уу");
        if (body.getStartDate() == null)        return badRequest("Эхлэх огноо оруулна уу");
        if (body.getEndDate() != null && body.getEndDate().isBefore(body.getStartDate())) {
            return badRequest("Дуусах огноо эхлэх огнооноос өмнө байж болохгүй");
        }

        Vehicle vehicle = vehicleRepository.findById(body.getVehicleId()).orElse(null);
        if (vehicle == null) return badRequest("Машин олдсонгүй");

        // Нэг машинд нэг л нээлттэй засвар байна — давхар бүртгэлээс сэргийлнэ
        boolean alreadyOpen = repository
                .findByActiveFlagAndStatusOrderByStartDateDescIdDesc(ACTIVE, VehicleRepair.STATUS_IN_REPAIR)
                .stream()
                .anyMatch(r -> r.getVehicleId().equals(body.getVehicleId()));
        if (alreadyOpen) {
            return badRequest(vehicle.getPlateNumber() + " дугаартай машин аль хэдийн засварт бүртгэлтэй байна");
        }

        body.setId(null);
        body.setPlateNumber(vehicle.getPlateNumber());
        body.setStatus(VehicleRepair.STATUS_IN_REPAIR);
        body.setActiveFlag(ACTIVE);
        return ResponseEntity.ok(repository.save(body));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody VehicleRepair body) {
        VehicleRepair existing = repository.findById(id).orElse(null);
        if (existing == null) return badRequest("Бичлэг олдсонгүй");
        if (body.getStartDate() != null && body.getEndDate() != null
                && body.getEndDate().isBefore(body.getStartDate())) {
            return badRequest("Дуусах огноо эхлэх огнооноос өмнө байж болохгүй");
        }

        if (body.getRepairCategoryId() != null) existing.setRepairCategoryId(body.getRepairCategoryId());
        if (body.getStartDate() != null)        existing.setStartDate(body.getStartDate());
        existing.setEndDate(body.getEndDate());
        existing.setNote(body.getNote());
        existing.setOdometerKm(body.getOdometerKm());
        existing.setNextServiceKm(body.getNextServiceKm());
        existing.setResponsibleWorkerId(body.getResponsibleWorkerId());
        existing.setLocation(body.getLocation());
        existing.setExternalOrg(body.getExternalOrg());
        existing.setFaultDescription(body.getFaultDescription());
        existing.setDriverName(body.getDriverName());
        return ResponseEntity.ok(repository.save(existing));
    }

    /** Засвар дууссан — машин дахин захиалгад сонгогдох боломжтой болно. */
    @PutMapping("/finish/{id}")
    public ResponseEntity<?> finish(@PathVariable Long id) {
        VehicleRepair existing = repository.findById(id).orElse(null);
        if (existing == null) return badRequest("Бичлэг олдсонгүй");
        existing.setStatus(VehicleRepair.STATUS_DONE);
        // Дуусах огноо тэмдэглээгүй бол өнөөдрөөр хаана
        if (existing.getEndDate() == null) existing.setEndDate(LocalDate.now());
        return ResponseEntity.ok(repository.save(existing));
    }

    /** Soft delete — түүхэнд үлдээнэ. */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(r -> {
            r.setActiveFlag(INACTIVE);
            repository.save(r);
        });
        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────

    /** Машин + ангиллын нэрийг нэг удаагийн map-аар нөхнө (мөр бүрд query явуулахгүй). */
    private List<VehicleRepair> enrich(List<VehicleRepair> rows) {
        if (rows.isEmpty()) return rows;

        Map<Long, Vehicle> vehicleMap = vehicleRepository.findAll().stream()
                .collect(Collectors.toMap(Vehicle::getId, v -> v, (a, b) -> a));

        Map<Long, RepairCategory> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(RepairCategory::getId, c -> c, (a, b) -> a));

        Map<Long, String> workerMap = workerRepository.findAll().stream()
                .collect(Collectors.toMap(mn.usug.dis_news_service.Entity.RepairWorker::getId,
                        mn.usug.dis_news_service.Entity.RepairWorker::getName, (a, b) -> a));

        rows.forEach(r -> {
            Vehicle v = vehicleMap.get(r.getVehicleId());
            if (v != null) {
                r.setBrand(v.getBrand());
                r.setModel(v.getModel());
            }
            RepairCategory c = categoryMap.get(r.getRepairCategoryId());
            if (c != null) {
                r.setCategoryName(c.getName());
                r.setCategoryCode(c.getCode());
            }
            if (r.getResponsibleWorkerId() != null) {
                r.setResponsibleWorkerName(workerMap.get(r.getResponsibleWorkerId()));
            }
        });
        return rows;
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}
