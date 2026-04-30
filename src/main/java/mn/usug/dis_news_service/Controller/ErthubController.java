package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.Entity.VehiclePenalty;
import mn.usug.dis_news_service.DAO.VehiclePenaltyRepository;
import mn.usug.dis_news_service.DAO.VehicleRepository;
import mn.usug.dis_news_service.Entity.Vehicle;
import mn.usug.dis_news_service.Service.VehicleComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/erthub")
@RequiredArgsConstructor
public class ErthubController {

    private final VehicleComplianceService complianceService;
    private final VehiclePenaltyRepository penaltyRepo;
    private final VehicleRepository vehicleRepo;

    // Улсын дугаараар хайх, inspection/insurance DB-д хадгалах
    @PostMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestBody Map<String, String> body) {
        String plate = body.get("plate");
        if (plate == null || plate.isBlank()) {
            return ResponseEntity.badRequest().body("plate is required");
        }
        Map<String, Object> result = complianceService.lookup(plate.trim().toUpperCase());
        if (result.get("registration") == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Тус улсын дугаарт машин олдсонгүй"));
        }
        return ResponseEntity.ok(result);
    }

    // Бүх машины compliance статус (AG Grid-д өнгө гаргахад)
    @GetMapping("/compliance")
    public List<Map<String, Object>> getComplianceStatus() {
        return complianceService.getComplianceStatus();
    }

    // Бүх төлөгдөөгүй торгуул (торгуулийн цонх)
    @GetMapping("/penalties")
    public List<Map<String, Object>> getUnpaidPenalties() {
        List<VehiclePenalty> penalties = penaltyRepo.findByIsPaidFalse();

        // Машины нэр нэмэх (brand + model)
        Map<String, String> nameMap = vehicleRepo.findAll().stream()
            .collect(Collectors.toMap(
                Vehicle::getPlateNumber,
                v -> ((v.getBrand() != null ? v.getBrand() : "") + " " +
                      (v.getModel() != null ? v.getModel() : "")).trim(),
                (a, b) -> a
            ));

        return penalties.stream().map(p -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id",              p.getId());
            m.put("plateNumber",     p.getPlateNumber());
            m.put("vehicleName",     nameMap.getOrDefault(p.getPlateNumber(), ""));
            m.put("barCode",         p.getBarCode());
            m.put("amount",          p.getAmount());
            m.put("localName",       p.getLocalName());
            m.put("reasonType",      p.getReasonType());
            m.put("passDate",        p.getPassDate());
            m.put("checkedAt",       p.getCheckedAt());
            return m;
        }).collect(Collectors.toList());
    }

    // Гараар penalty check дуудах (тест/дахин татах)
    @PostMapping("/check-penalties")
    public ResponseEntity<?> triggerPenaltyCheck() {
        new Thread(() -> {
            try { complianceService.scheduledDailyPenaltyCheck(); }
            catch (Exception e) { /* logged in service */ }
        }).start();
        return ResponseEntity.ok(Map.of("message", "Penalty check started in background"));
    }
}
