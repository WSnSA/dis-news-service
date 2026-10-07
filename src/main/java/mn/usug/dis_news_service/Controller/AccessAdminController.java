package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.Service.AccessAdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * "Эрхийн удирдлага" цэс — системийн тусгай хэрэглэгчийн эрхүүдийг удирдах.
 * Хандалтыг frontend-ийн menu permission (canView/canEdit)-ээр хязгаарлана.
 */
@RestController
@RequestMapping("/ref/access-admin")
@RequiredArgsConstructor
public class AccessAdminController {

    private final AccessAdminService service;

    // ── Шуурхай хурлын дүрүүд ─────────────────────────────────────────────────────

    @GetMapping("/briefing/catalog")
    public List<Map<String, Object>> briefingCatalog() {
        return service.roleCatalog();
    }

    @GetMapping("/briefing/users")
    public List<Map<String, Object>> briefingUserRoles() {
        return service.listBriefingUserRoles();
    }

    @PostMapping("/briefing/assign")
    public void assignBriefingRole(@RequestBody Map<String, Object> body) {
        Integer userId = body.get("userId") != null ? Integer.valueOf(body.get("userId").toString()) : null;
        String roleKey = body.get("roleKey") != null ? body.get("roleKey").toString() : null;
        service.assignBriefingRole(userId, roleKey);
    }

    @DeleteMapping("/briefing/revoke")
    public void revokeBriefingRole(@RequestParam Integer userId, @RequestParam String roleKey) {
        service.revokeBriefingRole(userId, roleKey);
    }

    // ── Машин захиалгын баталгаажуулалт алгасах хэрэглэгчид ─────────────────────────

    @GetMapping("/vehicle-skip")
    public List<Map<String, Object>> vehicleSkip() {
        return service.listVehicleSkip();
    }

    @PostMapping("/vehicle-skip")
    public void addVehicleSkip(@RequestBody Map<String, Object> body) {
        Integer userId = body.get("userId") != null ? Integer.valueOf(body.get("userId").toString()) : null;
        service.addVehicleSkip(userId);
    }

    @DeleteMapping("/vehicle-skip")
    public void removeVehicleSkip(@RequestParam Integer userId) {
        service.removeVehicleSkip(userId);
    }
}
