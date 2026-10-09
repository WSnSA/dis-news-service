package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.OilChangeLogRepository;
import mn.usug.dis_news_service.DAO.VehicleRepository;
import mn.usug.dis_news_service.Entity.OilChangeLog;
import mn.usug.dis_news_service.Entity.Vehicle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Тос тосолгооны бүртгэл — машины засвараас (vehicle_repair) тусдаа, зөвхөн
 * тээврийн хэрэгсэл, жолооч, огноо, гүйлт, зарцуулсан тосыг (литр) хурдан бичихэд зориулсан лог.
 */
@RestController
@RequestMapping("/repair/oil-change")
@RequiredArgsConstructor
public class OilChangeLogController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    private final OilChangeLogRepository repository;
    private final VehicleRepository vehicleRepository;

    @GetMapping("/getAll")
    public List<OilChangeLog> getAll() {
        return enrich(repository.findByActiveFlagOrderByChangeDateDescIdDesc(ACTIVE));
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody OilChangeLog body) {
        if (body.getVehicleId() == null)  return badRequest("Машин сонгоно уу");
        if (body.getChangeDate() == null) return badRequest("Огноо оруулна уу");
        if (body.getOdometerKm() == null || body.getOdometerKm() <= 0) return badRequest("Гүйлт оруулна уу");
        if (body.getOilLiters() == null || body.getOilLiters().signum() <= 0) return badRequest("Зарцуулсан тос (литр) оруулна уу");

        Vehicle vehicle = vehicleRepository.findById(body.getVehicleId()).orElse(null);
        if (vehicle == null) return badRequest("Машин олдсонгүй");

        body.setId(null);
        body.setPlateNumber(vehicle.getPlateNumber());
        body.setActiveFlag(ACTIVE);
        return ResponseEntity.ok(repository.save(body));
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

    /** Машины марк, загварыг нэг удаагийн map-аар нөхнө. */
    private List<OilChangeLog> enrich(List<OilChangeLog> rows) {
        if (rows.isEmpty()) return rows;
        Map<Long, Vehicle> vehicleMap = vehicleRepository.findAll().stream()
                .collect(Collectors.toMap(Vehicle::getId, v -> v, (a, b) -> a));
        rows.forEach(r -> {
            Vehicle v = vehicleMap.get(r.getVehicleId());
            if (v != null) {
                r.setBrand(v.getBrand());
                r.setModel(v.getModel());
            }
        });
        return rows;
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}
