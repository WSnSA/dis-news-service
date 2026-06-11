package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.BriefingUserRoleRepository;
import mn.usug.dis_news_service.DAO.DepartmentDAO;
import mn.usug.dis_news_service.DAO.UserDAO;
import mn.usug.dis_news_service.Entity.BriefingUserRole;
import mn.usug.dis_news_service.Entity.Department;
import mn.usug.dis_news_service.Entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Шуурхай хурлын дүрийн (RBAC) удирдлага.
 *  - myAccess(): одоогийн хэрэглэгчийн эрхийн мэдээлэл (frontend таб/товч gate).
 *  - listUserRoles()/assign()/revoke(): зөвхөн ADMIN — дүр оноох/хасах.
 */
@Service
@RequiredArgsConstructor
public class BriefingRoleService {

    private static final ZoneId UB = ZoneId.of("Asia/Ulaanbaatar");

    private final BriefingUserRoleRepository roleRepo;
    private final BriefingAccessService access;
    private final UserDAO userRepo;
    private final DepartmentDAO departmentRepo;
    private final BriefingAuditService audit;

    /** Одоогийн хэрэглэгчийн эрхийн мэдээлэл */
    public Map<String, Object> myAccess() {
        Integer uid = UserContext.getUserId();
        Map<String, Object> m = new HashMap<>();
        m.put("userId", uid);
        m.put("roles", access.rolesOf(uid));
        m.put("isAdmin", access.isAdmin(uid));
        m.put("isSecretary", access.isSecretary(uid));
        m.put("isManager", access.isManager(uid));
        m.put("isUnit", access.isUnit(uid));
        m.put("isViewerOnly", access.isViewerOnly(uid));
        m.put("canSubmit", access.canSubmit(uid));
        m.put("canRecord", access.isSecretary(uid));   // хуучин canRecord = нарийн бичиг
        return m;
    }

    /** Боломжит бүх дүрийн жагсаалт (админ UI dropdown) */
    public List<Map<String, Object>> roleCatalog() {
        Map<String, String> labels = Map.of(
                BriefingAccessService.ADMIN,     "Системийн админ",
                BriefingAccessService.SECRETARY, "Шуурхайн нарийн бичиг",
                BriefingAccessService.UNIT,      "Зохион байгуулалтын нэгж",
                BriefingAccessService.MANAGER,   "Үүрэг өгсөн удирдлага",
                BriefingAccessService.VIEWER,    "Зөвхөн харах");
        return BriefingAccessService.ALL_ROLES.stream().map(k -> {
            Map<String, Object> m = new HashMap<>();
            m.put("key", k);
            m.put("label", labels.get(k));
            return m;
        }).collect(Collectors.toList());
    }

    /** Дүр оноогдсон бүх хэрэглэгч (админ UI) */
    public List<Map<String, Object>> listUserRoles() {
        access.requireAdmin(UserContext.getUserId());
        Map<Integer, String> depMap = departmentRepo.findAll().stream()
                .collect(Collectors.toMap(Department::getDepId, Department::getDepName, (a, b) -> a));
        Map<Integer, User> userById = userRepo.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        Map<Integer, List<String>> byUser = roleRepo.findAll().stream()
                .collect(Collectors.groupingBy(BriefingUserRole::getUserId,
                        Collectors.mapping(BriefingUserRole::getRoleKey, Collectors.toList())));

        return byUser.entrySet().stream().map(e -> {
            User u = userById.get(e.getKey());
            Map<String, Object> m = new HashMap<>();
            m.put("userId", e.getKey());
            m.put("userName", u != null ? fullName(u) : "#" + e.getKey());
            m.put("depName", (u != null && u.getDepartmentId() != null) ? depMap.get(u.getDepartmentId()) : null);
            m.put("roles", e.getValue());
            return m;
        }).sorted(Comparator.comparing(m -> String.valueOf(m.get("userName"))))
          .collect(Collectors.toList());
    }

    @Transactional
    public void assign(Integer userId, String roleKey) {
        access.requireAdmin(UserContext.getUserId());
        if (userId == null || roleKey == null || !BriefingAccessService.ALL_ROLES.contains(roleKey))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Буруу хэрэглэгч эсвэл дүр");
        if (roleRepo.existsByUserIdAndRoleKey(userId, roleKey)) return;
        BriefingUserRole r = new BriefingUserRole();
        r.setUserId(userId);
        r.setRoleKey(roleKey);
        r.setCreatedBy(UserContext.getUserId());
        r.setCreatedDate(LocalDateTime.now(UB));
        roleRepo.save(r);
        audit.log("ASSIGN_ROLE", "ROLE", userId, null, "roleKey=" + roleKey);
    }

    @Transactional
    public void revoke(Integer userId, String roleKey) {
        access.requireAdmin(UserContext.getUserId());
        roleRepo.deleteByUserIdAndRoleKey(userId, roleKey);
        audit.log("REVOKE_ROLE", "ROLE", userId, "roleKey=" + roleKey, null);
    }

    private String fullName(User u) {
        String ln = u.getLastName() != null && !u.getLastName().isBlank()
                ? u.getLastName().charAt(0) + ". " : "";
        String fn = u.getFirstName() != null ? u.getFirstName() : "";
        return (ln + fn).trim();
    }
}
