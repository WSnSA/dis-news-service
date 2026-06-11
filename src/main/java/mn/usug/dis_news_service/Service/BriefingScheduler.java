package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.*;
import mn.usug.dis_news_service.Entity.BriefingCycle;
import mn.usug.dis_news_service.Entity.BriefingFulfillment;
import mn.usug.dis_news_service.Entity.BriefingTask;
import mn.usug.dis_news_service.Entity.BriefingTaskDelegate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Шуурхай хурлын автомат сануулга (§3.6).
 *  - Баасан өглөө: биелэлт оруулаагүй нэгжүүдэд "хугацаа дуусах гэж байна".
 *  - Баасан 14:00: "2 цагийн дараа дуусна".
 *  - Даваа өглөө: дүгнэх шаардлагатай удирдлагуудад "биелэлт шалгах шаардлагатай".
 *
 * Cron-уудыг application.properties-ээр тохируулна (briefing.cron.*).
 * Бүх ажил Улаанбаатарын цагаар ажиллана.
 */
@Component
@RequiredArgsConstructor
public class BriefingScheduler {

    private static final String UB = "Asia/Ulaanbaatar";
    private static final ZoneId UB_ZONE = ZoneId.of(UB);

    private final BriefingCycleRepository cycleRepo;
    private final BriefingFulfillmentRepository fulRepo;
    private final BriefingTaskRepository taskRepo;
    private final BriefingTaskDelegateRepository delegateRepo;
    private final NotificationService notificationService;

    /** Идэвхтэй (устгаагүй, бүрэн биелээгүй) үүрэг эсэх */
    private BriefingTask activeTask(Integer taskId) {
        BriefingTask t = taskRepo.findById(taskId).orElse(null);
        if (t == null) return null;
        if (!Integer.valueOf(1).equals(t.getActiveFlag())) return null;
        if (Integer.valueOf(1).equals(t.getStatus())) return null;   // бүрэн биелсэн
        return t;
    }

    private Set<Integer> pendingDepts(BriefingCycle c) {
        return fulRepo.findByCycleId(c.getId()).stream()
                .filter(f -> f.getSubmittedAt() == null)
                .map(BriefingFulfillment::getDepartmentId)
                .collect(Collectors.toSet());
    }

    // ── Баасан өглөө 09:00 — биелэлт оруулах сануулга ─────────────────────────────
    @Scheduled(cron = "${briefing.cron.submit-reminder:0 0 9 ? * FRI}", zone = UB)
    public void submitDeadlineReminder() {
        LocalDate today = LocalDate.now(UB_ZONE);
        List<BriefingCycle> cycles = cycleRepo.findByScoreIsNullAndSubmitDeadlineBetween(
                today.atStartOfDay(), today.atTime(23, 59, 59));
        for (BriefingCycle c : cycles) {
            BriefingTask t = activeTask(c.getTaskId());
            if (t == null) continue;
            Set<Integer> depts = pendingDepts(c);
            if (!depts.isEmpty())
                notificationService.notifyBriefingDepts(depts, "Биелэлт оруулах сануулга",
                        "Биелэлт оруулах хугацаа өнөөдөр 16:00 цагт дуусах гэж байна: " + t.getDescription());
        }
    }

    // ── Баасан 14:00 — 2 цагийн дотор дуусах сануулга ─────────────────────────────
    @Scheduled(cron = "${briefing.cron.submit-soon:0 0 14 ? * FRI}", zone = UB)
    public void submitDeadlineSoon() {
        LocalDateTime now = LocalDateTime.now(UB_ZONE);
        List<BriefingCycle> cycles = cycleRepo.findByScoreIsNullAndSubmitDeadlineBetween(now, now.plusHours(3));
        for (BriefingCycle c : cycles) {
            BriefingTask t = activeTask(c.getTaskId());
            if (t == null) continue;
            Set<Integer> depts = pendingDepts(c);
            if (!depts.isEmpty())
                notificationService.notifyBriefingDepts(depts, "Хугацаа дуусч байна",
                        "Биелэлт оруулах хугацаа 2 цагийн дараа дуусна: " + t.getDescription());
        }
    }

    // ── Даваа өглөө 09:00 — дүгнэх сануулга ───────────────────────────────────────
    @Scheduled(cron = "${briefing.cron.score-reminder:0 0 9 ? * MON}", zone = UB)
    public void scoreDeadlineReminder() {
        LocalDate today = LocalDate.now(UB_ZONE);
        List<BriefingCycle> cycles = cycleRepo.findByScoreIsNullAndScoreDeadlineBetween(
                today.atStartOfDay(), today.atTime(23, 59, 59));
        for (BriefingCycle c : cycles) {
            BriefingTask t = activeTask(c.getTaskId());
            if (t == null) continue;
            boolean anySubmitted = fulRepo.findByCycleId(c.getId()).stream()
                    .anyMatch(f -> f.getSubmittedAt() != null);
            if (!anySubmitted) continue;   // биелэлт байхгүй бол дүгнэх юм алга (auto-0)
            Set<Integer> reviewers = new HashSet<>();
            if (t.getAssignerId() != null) reviewers.add(t.getAssignerId());
            delegateRepo.findByTaskId(t.getId()).forEach(d -> reviewers.add(d.getUserId()));
            notificationService.notifyBriefingUsers(reviewers, "Биелэлт шалгах сануулга",
                    "Биелэлт шалгаж дүгнэх шаардлагатай: " + t.getDescription());
        }
    }
}
