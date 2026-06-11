package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.BriefingTaskDelegateRepository;
import mn.usug.dis_news_service.DAO.BriefingUserRoleRepository;
import mn.usug.dis_news_service.Entity.BriefingTask;
import mn.usug.dis_news_service.Entity.BriefingUserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Шуурхай хурлын модулийн эрхийн (RBAC) шийдвэрлэлт.
 *
 * 5 дүр (briefing_user_role.role_key):
 *   BRIEFING_ADMIN     — хэрэглэгч/дүр удирдах, бүх хандалт
 *   BRIEFING_SECRETARY — хурал/үүрэг бүртгэх, нэгтгэл, хэвлэх (= хуучин canRecord)
 *   BRIEFING_UNIT      — биелэлт/шуурхайн мэдээ оруулах, файл хавсаргах
 *   BRIEFING_MANAGER   — биелэлт хянах, дүгнэх, тайлбар, буцаах (= хуучин assigner)
 *   BRIEFING_VIEWER    — зөвхөн харах (mutation бүхэн 403)
 *
 * Зан төлвийн зарчим: тусгай дүргүй хэрэглэгч өөрийн албаны биелэлт/мэдээ оруулах
 * хуучин боломжоо хадгална. Харин VIEWER дүр нь уг боломжийг хааж зөвхөн харагч болгоно.
 */
@Service
@RequiredArgsConstructor
public class BriefingAccessService {

    public static final String ADMIN     = "BRIEFING_ADMIN";
    public static final String SECRETARY = "BRIEFING_SECRETARY";
    public static final String UNIT      = "BRIEFING_UNIT";
    public static final String MANAGER   = "BRIEFING_MANAGER";
    public static final String VIEWER    = "BRIEFING_VIEWER";

    public static final List<String> ALL_ROLES = List.of(ADMIN, SECRETARY, UNIT, MANAGER, VIEWER);

    private final BriefingUserRoleRepository roleRepo;
    private final BriefingTaskDelegateRepository delegateRepo;

    // ── Дүр унших ────────────────────────────────────────────────────────────────

    public Set<String> rolesOf(Integer userId) {
        if (userId == null) return Set.of();
        return roleRepo.findByUserId(userId).stream()
                .map(BriefingUserRole::getRoleKey)
                .collect(Collectors.toSet());
    }

    public boolean isAdmin(Integer u)     { return rolesOf(u).contains(ADMIN); }
    public boolean isSecretary(Integer u) { Set<String> r = rolesOf(u); return r.contains(ADMIN) || r.contains(SECRETARY); }
    public boolean isManager(Integer u)   { Set<String> r = rolesOf(u); return r.contains(ADMIN) || r.contains(MANAGER); }
    public boolean isUnit(Integer u)      { Set<String> r = rolesOf(u); return r.contains(ADMIN) || r.contains(UNIT); }

    /** VIEWER эрхтэй боловч бусад идэвхтэй дүргүй — зөвхөн харагч */
    public boolean isViewerOnly(Integer u) {
        Set<String> r = rolesOf(u);
        boolean anyActive = r.contains(ADMIN) || r.contains(SECRETARY) || r.contains(UNIT) || r.contains(MANAGER);
        return !anyActive && r.contains(VIEWER);
    }

    /** Биелэлт/мэдээ оруулах боломжтой эсэх (viewer-only биш бол боломжтой) */
    public boolean canSubmit(Integer u) { return !isViewerOnly(u); }

    /** Assigner болгож сонгох боломжтой хэрэглэгчдийн id (MANAGER эсвэл ADMIN дүртэй) */
    public List<Integer> assignerUserIds() {
        return roleRepo.findUserIdsByRoleKeys(List.of(ADMIN, MANAGER));
    }

    /** Тухайн үүргийг дүгнэх эрхтэй эсэх — assigner өөрөө, delegate, эсвэл admin */
    public boolean canScoreTask(Integer userId, BriefingTask task) {
        if (userId == null || task == null) return false;
        if (isAdmin(userId)) return true;
        if (Objects.equals(userId, task.getAssignerId())) return true;
        return delegateRepo.existsByTaskIdAndUserId(task.getId(), userId);
    }

    // ── Guard-ууд (mutation хамгаалалт) ──────────────────────────────────────────

    public void requireNotViewer(Integer u) {
        if (isViewerOnly(u))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Зөвхөн харах эрхтэй (Viewer)");
    }

    public void requireSecretary(Integer u) {
        if (!isSecretary(u))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Шуурхайн нарийн бичгийн эрх шаардлагатай");
    }

    public void requireAdmin(Integer u) {
        if (!isAdmin(u))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Системийн админ эрх шаардлагатай");
    }
}
