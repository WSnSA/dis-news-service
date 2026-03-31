package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.MenuDAO;
import mn.usug.dis_news_service.DAO.NotificationRepository;
import mn.usug.dis_news_service.DAO.PermissionDAO;
import mn.usug.dis_news_service.DAO.UserDAO;
import mn.usug.dis_news_service.DTO.NotificationDto;
import mn.usug.dis_news_service.Entity.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messaging;
    private final NotificationRepository notifRepo;
    private final PermissionDAO permissionDAO;
    private final MenuDAO menuDAO;
    private final UserDAO userDAO;

    // ── Component paths ────────────────────────────────────────────────────────
    private static final
    String COMP_REPORT      = "pages/report/report.component";
    private static final String COMP_FULFILLMENT = "pages/task-fulfillment/task-fulfillment.component";
    private static final String PATH_DASHBOARD   = "dashboard";

    // ── Public notify methods ──────────────────────────────────────────────────

    /** Ус хангамжийн цагийн бүртгэл — тайлан/хянах самбар эрхтэй хүнд */
    public void notifyWsHourly(String stationName, int hour) {
        Set<Integer> targets = reportDashboardUsers();
        broadcast(targets, "ws",
                "Цагийн бүртгэл",
                stationName + " — " + hour + ":00 цагийн бүртгэл хадгалагдлаа",
                "pi-clock");
    }

    /** Ажлын мэдээ — тайлан/хянах самбар эрхтэй хүнд */
    public void notifyWorkNews(String title) {
        Set<Integer> targets = reportDashboardUsers();
        broadcast(targets, "news", "Ажлын мэдээ нэмэгдлээ", title, "pi-file-edit");
    }

    /** Машин захиалга — тайлан/хянах самбар эрхтэй хүнд + хуваарилагдсан алба */
    public void notifyVehicleOrder(String description, Integer assignedDepartmentId) {
        String msg = truncate(description, 100);
        Set<Integer> targets = reportDashboardUsers();
        // Хуваарилагдсан алба дахь хэрэглэгчдийг нэмнэ
        if (assignedDepartmentId != null && assignedDepartmentId > 0) {
            userDAO.findUsersByFilter(assignedDepartmentId, 0).stream()
                    .filter(u -> Boolean.TRUE.equals(u.getActiveFlag()))
                    .forEach(u -> targets.add(u.getId()));
        }
        broadcast(targets, "vehicle-order", "Машин захиалга", msg, "pi-car");
    }

    /** Ажилд гарах машин — тайлан/хянах самбар эрхтэй хүнд */
    public void notifyVehicleOut(String info) {
        Set<Integer> targets = reportDashboardUsers();
        broadcast(targets, "vehicle-out", "Ажилд гарах машин", info, "pi-truck");
    }

    /** Ажлын захиалга шинээр бүртгэгдэх — гүйцэтгэх алба (departmentId) дахь хэрэглэгчид */
    public void notifyWorkOrderNew(String location, Integer executorDeptId) {
        String msg = truncate(location, 100);
        Set<Integer> targets = userIdsInDept(executorDeptId);
        broadcast(targets, "work-order", "Шинэ ажлын захиалга", msg, "pi-file-plus");
    }

    /** Ажлын захиалга биелэгдсэн — захиалга үүсгэсэн алба (assignedDeptId) дахь хэрэглэгчид */
    public void notifyWorkOrderDone(String location, Integer assignedDeptId) {
        String msg = truncate(location, 100);
        Set<Integer> targets = userIdsInDept(assignedDeptId);
        broadcast(targets, "work-order", "Ажлын захиалга биелэгдлээ", msg, "pi-check-circle");
    }

    /**
     * Үүрэг даалгавар — биелэлт оруулах эрхтэй (canEdit=1) тохирох хэрэглэгчид.
     * depId / positionId → UserDAO.findUsersByFilter-р шүүнэ.
     */
    public void notifyTask(String description, Integer depId, Integer positionId) {
        String msg = truncate(description, 100);

        // Биелэлт оруулах цонхны canEdit=1 эрхтэй userId-ийн жагсаалт
        Set<Integer> editableUsers = usersWithMenuEditPermission(COMP_FULFILLMENT);

        // Тохирох алба/албан тушаалтай хэрэглэгчид
        Set<Integer> matchedUsers = userDAO
                .findUsersByFilter(depId == null ? 0 : depId,
                                   positionId == null ? 0 : positionId)
                .stream()
                .filter(u -> Boolean.TRUE.equals(u.getActiveFlag()))
                .map(u -> u.getId())
                .collect(Collectors.toSet());

        // Огтлолцол: биелэлт оруулах эрхтэй ба тохирох хэрэглэгч
        Set<Integer> targets = new HashSet<>(editableUsers);
        targets.retainAll(matchedUsers);

        // Огтлолцол хоосон бол тохирох хэрэглэгч бүгдэд илгээнэ (fallback)
        if (targets.isEmpty()) targets = matchedUsers;

        broadcast(targets, "task", "Шинэ үүрэг даалгавар", msg, "pi-check-square");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Тайлан болон хянах самбар эрхтэй бүх идэвхтэй хэрэглэгчдийн ID */
    private Set<Integer> reportDashboardUsers() {
        Set<Integer> menuIds = new HashSet<>();
        menuDAO.findByComponent(COMP_REPORT).forEach(m -> menuIds.add(m.getId()));
        menuDAO.findByPath(PATH_DASHBOARD).forEach(m -> menuIds.add(m.getId()));

        Set<Integer> userIds = permissionDAO.findAll().stream()
                .filter(p -> menuIds.contains(p.getMenuId())
                          && p.getCanView() != null && p.getCanView() == 1)
                .map(p -> p.getUserId())
                .collect(Collectors.toSet());

        // Идэвхгүй хэрэглэгч хасах
        Set<Integer> activeUsers = userDAO.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActiveFlag()))
                .map(u -> u.getId())
                .collect(Collectors.toSet());
        userIds.retainAll(activeUsers);
        return userIds;
    }

    /** Тухайн цонхны canEdit=1 эрхтэй userId-ийн жагсаалт */
    private Set<Integer> usersWithMenuEditPermission(String component) {
        Set<Integer> menuIds = menuDAO.findByComponent(component).stream()
                .map(m -> m.getId())
                .collect(Collectors.toSet());
        if (menuIds.isEmpty()) return Collections.emptySet();

        return permissionDAO.findAll().stream()
                .filter(p -> menuIds.contains(p.getMenuId())
                          && p.getCanEdit() != null && p.getCanEdit() == 1)
                .map(p -> p.getUserId())
                .collect(Collectors.toSet());
    }

    /** DB-д хадгалж, WebSocket-ээр broadcast хийнэ */
    private void broadcast(Set<Integer> userIds, String category,
                           String title, String message, String icon) {
        if (userIds.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        List<Notification> records = userIds.stream().map(uid -> {
            Notification n = new Notification();
            n.setUserId(uid);
            n.setCategory(category);
            n.setTitle(title);
            n.setMessage(message);
            n.setIcon(icon);
            n.setIsRead(0);
            n.setCreatedAt(now);
            return n;
        }).toList();
        notifRepo.saveAll(records);

        messaging.convertAndSend(
                "/topic/notify/" + category,
                new NotificationDto(category, title, message, icon, System.currentTimeMillis())
        );
    }

    /** Тухайн алба дахь идэвхтэй хэрэглэгчдийн ID */
    private Set<Integer> userIdsInDept(Integer deptId) {
        if (deptId == null || deptId == 0) return Collections.emptySet();
        return userDAO.findUsersByFilter(deptId, 0).stream()
                .filter(u -> Boolean.TRUE.equals(u.getActiveFlag()))
                .map(u -> u.getId())
                .collect(Collectors.toSet());
    }

    /** WS станцуудын menuId жагсаалт */
    private Set<Integer> wsMenuIdSet() {
        return menuDAO.findByWS().stream()
                .map(m -> m.getId())
                .collect(Collectors.toSet());
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
