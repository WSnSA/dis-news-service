package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.BriefingUserRoleRepository;
import mn.usug.dis_news_service.DAO.DepartmentDAO;
import mn.usug.dis_news_service.DAO.UserDAO;
import mn.usug.dis_news_service.DAO.VehicleApprovalSkipRepository;
import mn.usug.dis_news_service.Entity.BriefingUserRole;
import mn.usug.dis_news_service.Entity.Department;
import mn.usug.dis_news_service.Entity.User;
import mn.usug.dis_news_service.Entity.VehicleApprovalSkip;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * "Эрхийн удирдлага" цэсний нэгдсэн сервис — системийн тусгай (хатуу заасан)
 * хэрэглэгчийн эрхүүдийг нэг цонхноос удирдана:
 *   1. Шуурхай хурлын дүрүүд (briefing_user_role)
 *   2. Машин захиалгын албаны баталгаажуулалт алгасах хэрэглэгчид (vehicle_approval_skip)
 *
 * Хандалтыг стандарт menu permission (canView/canEdit)-ээр хязгаарлана тул энд
 * BRIEFING_ADMIN шаардахгүй — ингэснээр анхны админыг нэмэх bootstrap асуудал шийдэгдэнэ.
 * ("Үүрэг өгөх эрх"/can_assign_task нь одоо байгаа ref/user endpoint-оор удирдагдана.)
 */
@Service
@RequiredArgsConstructor
public class AccessAdminService {

    private static final ZoneId UB = ZoneId.of("Asia/Ulaanbaatar");

    private final BriefingUserRoleRepository roleRepo;
    private final VehicleApprovalSkipRepository skipRepo;
    private final UserDAO userRepo;
    private final DepartmentDAO departmentRepo;

    private static final Map<String, String> ROLE_LABELS = Map.of(
            BriefingAccessService.ADMIN,     "Системийн админ",
            BriefingAccessService.SECRETARY, "Шуурхайн нарийн бичиг (бүртгэх)",
            BriefingAccessService.MANAGER,   "Үүрэг өгсөн удирдлага (хянах)",
            BriefingAccessService.UNIT,      "Зохион байгуулалтын нэгж",
            BriefingAccessService.VIEWER,    "Зөвхөн харах");

    // ── Нэр / алба туслах ────────────────────────────────────────────────────────

    private Map<Integer, User> usersById() {
        return userRepo.findAll().stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    private Map<Integer, String> depNames() {
        return departmentRepo.findAll().stream()
                .collect(Collectors.toMap(Department::getDepId, Department::getDepName, (a, b) -> a));
    }

    /** Овгийн эхний үсэг + нэр (урд/хойд зай цэвэрлэсэн) */
    private String fullName(User u) {
        if (u == null) return null;
        String last = u.getLastName() != null ? u.getLastName().trim() : "";
        String ln = !last.isEmpty() ? last.charAt(0) + ". " : "";
        String fn = u.getFirstName() != null ? u.getFirstName().trim() : "";
        return (ln + fn).trim();
    }

    private Map<String, Object> userInfo(Integer userId, Map<Integer, User> byId, Map<Integer, String> deps) {
        User u = byId.get(userId);
        Map<String, Object> m = new HashMap<>();
        m.put("userId", userId);
        m.put("userName", u != null ? fullName(u) : "#" + userId);
        m.put("depName", (u != null && u.getDepartmentId() != null) ? deps.get(u.getDepartmentId()) : null);
        return m;
    }

    // ── Шуурхай хурлын дүрүүд ─────────────────────────────────────────────────────

    public List<Map<String, Object>> roleCatalog() {
        return BriefingAccessService.ALL_ROLES.stream().map(k -> {
            Map<String, Object> m = new HashMap<>();
            m.put("key", k);
            m.put("label", ROLE_LABELS.getOrDefault(k, k));
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> listBriefingUserRoles() {
        Map<Integer, User> byId = usersById();
        Map<Integer, String> deps = depNames();
        Map<Integer, List<String>> byUser = roleRepo.findAll().stream()
                .collect(Collectors.groupingBy(BriefingUserRole::getUserId,
                        Collectors.mapping(BriefingUserRole::getRoleKey, Collectors.toList())));
        return byUser.entrySet().stream().map(e -> {
            Map<String, Object> m = userInfo(e.getKey(), byId, deps);
            m.put("roles", e.getValue());
            return m;
        }).sorted(Comparator.comparing(m -> String.valueOf(m.get("userName"))))
          .collect(Collectors.toList());
    }

    @Transactional
    public void assignBriefingRole(Integer userId, String roleKey) {
        if (userId == null || roleKey == null || !BriefingAccessService.ALL_ROLES.contains(roleKey))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Буруу хэрэглэгч эсвэл дүр");
        if (roleRepo.existsByUserIdAndRoleKey(userId, roleKey)) return;
        BriefingUserRole r = new BriefingUserRole();
        r.setUserId(userId);
        r.setRoleKey(roleKey);
        r.setCreatedBy(UserContext.getUserId());
        r.setCreatedDate(LocalDateTime.now(UB));
        roleRepo.save(r);
    }

    @Transactional
    public void revokeBriefingRole(Integer userId, String roleKey) {
        roleRepo.deleteByUserIdAndRoleKey(userId, roleKey);
    }

    // ── Машин захиалгын баталгаажуулалт алгасах хэрэглэгчид ─────────────────────────

    public List<Map<String, Object>> listVehicleSkip() {
        Map<Integer, User> byId = usersById();
        Map<Integer, String> deps = depNames();
        return skipRepo.findAll().stream()
                .map(VehicleApprovalSkip::getUserId)
                .distinct()
                .map(uid -> userInfo(uid, byId, deps))
                .sorted(Comparator.comparing(m -> String.valueOf(m.get("userName"))))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addVehicleSkip(Integer userId) {
        if (userId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Хэрэглэгч сонгоно уу");
        if (skipRepo.existsByUserId(userId)) return;
        VehicleApprovalSkip s = new VehicleApprovalSkip();
        s.setUserId(userId);
        s.setCreatedBy(UserContext.getUserId());
        s.setCreatedDate(LocalDateTime.now(UB));
        skipRepo.save(s);
    }

    @Transactional
    public void removeVehicleSkip(Integer userId) {
        skipRepo.deleteByUserId(userId);
    }
}
