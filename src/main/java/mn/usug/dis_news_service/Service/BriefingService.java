package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.*;
import mn.usug.dis_news_service.DTO.BriefingDto;
import mn.usug.dis_news_service.DTO.BriefingSaveDto;
import mn.usug.dis_news_service.Entity.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Шуурхай хурлын үүрэг даалгаврын бизнес логик.
 *  - 7 хоног тутмын мөчлөг (cycle), хатуу хугацааны хязгаар.
 *  - Биелэлт алба бүрд, нотлох баримт folder (file service).
 *  - Дүн = нэг ерөнхий оноо/cycle. 100 болбол үүрэг бүрэн биелсэн.
 */
@Service
@RequiredArgsConstructor
public class BriefingService {

    private final BriefingTaskRepository taskRepo;
    private final BriefingTaskDepartmentRepository taskDepRepo;
    private final BriefingCycleRepository cycleRepo;
    private final BriefingFulfillmentRepository fulRepo;
    private final BriefingEvidenceRepository evidenceRepo;
    private final BriefingMeetingRepository meetingRepo;
    private final DepartmentDAO departmentRepo;
    private final UserDAO userRepo;
    private final NotificationService notificationService;
    private final BriefingAccessService access;
    private final BriefingTaskDelegateRepository delegateRepo;
    private final BriefingAuditService audit;

    private static final ZoneId UB = ZoneId.of("Asia/Ulaanbaatar");

    private LocalDateTime now() { return LocalDateTime.now(UB); }

    // ── Хугацаа / 7 хоногийн логик ──────────────────────────────────────────────

    /** Тухайн 7 хоногийн 2 дахь өдөр (Мягмар) */
    private LocalDate currentMeetingTuesday() {
        return LocalDate.now(UB).with(ChronoField.DAY_OF_WEEK, 2);
    }

    /** Биелэлт оруулах эцсийн хугацаа — тухайн долоо хоногийн Баасан 16:00 (Мягмар + 3 өдөр) */
    private LocalDateTime submitDeadlineOf(LocalDate meetingDate) {
        return meetingDate.plusDays(3).atTime(16, 0);
    }

    /** Дүгнэх эцсийн хугацаа — дараа долоо хоногийн Даваа 14:00 (Мягмар + 6 өдөр) */
    private LocalDateTime scoreDeadlineOf(LocalDate meetingDate) {
        return meetingDate.plusDays(6).atTime(14, 0);
    }

    // ── Assigner жагсаалт (can_assign_task=1) ────────────────────────────────────

    public List<Map<String, Object>> listAssigners() {
        Map<Integer, String> depMap = departmentRepo.findAll().stream()
                .collect(Collectors.toMap(Department::getDepId, Department::getDepName, (a, b) -> a));
        // MANAGER/ADMIN дүртэй хэрэглэгчид — шилжилтийн үед хуучин can_assign_task-тай нэгтгэнэ
        Set<Integer> assignerIds = new HashSet<>(access.assignerUserIds());
        return userRepo.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActiveFlag()))
                .filter(u -> assignerIds.contains(u.getId()) || Boolean.TRUE.equals(u.getCanAssignTask()))
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("name", fullName(u));
                    m.put("depName", u.getDepartmentId() != null ? depMap.get(u.getDepartmentId()) : null);
                    return m;
                })
                .collect(Collectors.toList());
    }

    // ── Унших ────────────────────────────────────────────────────────────────────

    public List<BriefingDto> getAll() {
        finalizeOverdueNoFulfillment();
        return mapTasks(taskRepo.findActive());
    }

    /**
     * Дүгнэх хугацаа дууссан хэрнээ ямар ч алба биелэлт оруулаагүй cycle-уудыг автоматаар 0 болгоно.
     * (Биелэлт оруулаагүй бол тухайн 7 хоногт шууд 0.)
     */
    private void finalizeOverdueNoFulfillment() {
        List<BriefingCycle> overdue = cycleRepo.findByScoreIsNullAndScoreDeadlineBefore(now());
        for (BriefingCycle c : overdue) {
            boolean anySubmitted = fulRepo.findByCycleId(c.getId()).stream()
                    .anyMatch(f -> f.getSubmittedAt() != null);
            if (!anySubmitted) {
                c.setScore(0);
                c.setStatus(1);
                c.setScoredAt(now());
                cycleRepo.save(c);
            }
        }
    }

    public BriefingDto getOne(Integer id) {
        BriefingTask t = taskRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Үүрэг олдсонгүй"));
        List<BriefingDto> list = mapTasks(List.of(t));
        return list.isEmpty() ? null : list.get(0);
    }

    // ── Бүртгэх / засах ────────────────────────────────────────────────────────────

    @Transactional
    public BriefingDto save(BriefingSaveDto dto) {
        access.requireSecretary(UserContext.getUserId());
        if (dto.getAssignerId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Үүрэг өгөх албан тушаалтан заавал сонгоно");
        if (dto.getDescription() == null || dto.getDescription().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Үүрэг даалгаврын тайлбар заавал бичнэ");
        List<Integer> depIds = dto.getDepartmentIds() == null ? List.of()
                : dto.getDepartmentIds().stream().distinct().collect(Collectors.toList());
        if (depIds.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Холбогдох алба 1-с доошгүй сонгоно");

        boolean isNew = dto.getId() == null;
        BriefingTask task;
        if (isNew) {
            task = new BriefingTask();
            task.setStatus(0);
            task.setActiveFlag(1);
            task.setCreatedBy(UserContext.getUserId());
            task.setCreatedDate(now());
        } else {
            task = taskRepo.findById(dto.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Үүрэг олдсонгүй"));
        }
        task.setAssignerId(dto.getAssignerId());
        task.setDescription(dto.getDescription());
        task.setUpdatedBy(UserContext.getUserId());
        task.setUpdatedDate(now());
        BriefingTask saved = taskRepo.save(task);

        if (isNew) {
            // Холбогдох албад
            depIds.forEach(depId -> {
                BriefingTaskDepartment td = new BriefingTaskDepartment();
                td.setTaskId(saved.getId());
                td.setDepartmentId(depId);
                taskDepRepo.save(td);
            });
            // Эхний cycle + хоосон биелэлтүүд
            createCycle(saved.getId(), 1, currentMeetingTuesday(), depIds);
            notificationService.notifyBriefing(saved.getDescription(), new HashSet<>(depIds));
        } else {
            syncDepartments(saved.getId(), depIds);
        }

        audit.log(isNew ? "CREATE_TASK" : "UPDATE_TASK", "TASK", saved.getId(), null,
                "assignerId=" + saved.getAssignerId() + "; depIds=" + depIds + "; desc=" + saved.getDescription());

        return getOne(saved.getId());
    }

    /** Засах үед холбогдох албадыг шинэчилнэ; идэвхтэй cycle-д биелэлт нэмж/хасна. */
    private void syncDepartments(Integer taskId, List<Integer> newDepIds) {
        List<BriefingTaskDepartment> existing = taskDepRepo.findByTaskId(taskId);
        Set<Integer> existingIds = existing.stream()
                .map(BriefingTaskDepartment::getDepartmentId).collect(Collectors.toSet());
        Set<Integer> newSet = new HashSet<>(newDepIds);

        // Хасах
        existing.stream()
                .filter(td -> !newSet.contains(td.getDepartmentId()))
                .forEach(td -> taskDepRepo.deleteById(td.getId()));
        // Нэмэх
        newDepIds.stream()
                .filter(id -> !existingIds.contains(id))
                .forEach(id -> {
                    BriefingTaskDepartment td = new BriefingTaskDepartment();
                    td.setTaskId(taskId);
                    td.setDepartmentId(id);
                    taskDepRepo.save(td);
                });

        // Идэвхтэй (хамгийн сүүлийн) cycle-ийн биелэлтүүдийг тааруулна
        BriefingCycle latest = cycleRepo.findTopByTaskIdOrderByCycleNoDesc(taskId);
        if (latest != null) {
            List<BriefingFulfillment> ful = fulRepo.findByCycleId(latest.getId());
            Set<Integer> fulDeps = ful.stream()
                    .map(BriefingFulfillment::getDepartmentId).collect(Collectors.toSet());
            // Хасагдсан албаны биелэлт + нотлох баримтыг устгана
            ful.stream().filter(f -> !newSet.contains(f.getDepartmentId())).forEach(f -> {
                evidenceRepo.deleteAll(evidenceRepo.findByFolderId(f.getFolderId()));
                fulRepo.deleteById(f.getId());
            });
            // Шинэ албад хоосон биелэлт
            newDepIds.stream().filter(id -> !fulDeps.contains(id))
                    .forEach(id -> createFulfillmentRow(latest.getId(), id));
        }
    }

    private BriefingCycle createCycle(Integer taskId, int cycleNo, LocalDate meetingDate, List<Integer> depIds) {
        BriefingMeeting meeting = getOrCreateMeeting(meetingDate);
        BriefingCycle c = new BriefingCycle();
        c.setTaskId(taskId);
        c.setMeetingId(meeting.getId());
        c.setCycleNo(cycleNo);
        c.setMeetingDate(meetingDate);
        c.setSubmitDeadline(submitDeadlineOf(meetingDate));
        c.setScoreDeadline(scoreDeadlineOf(meetingDate));
        c.setStatus(0);
        c.setCreatedDate(now());
        BriefingCycle saved = cycleRepo.save(c);
        depIds.forEach(depId -> createFulfillmentRow(saved.getId(), depId));
        return saved;
    }

    // ── Шуурхай зөвлөгөөн (§3.1) ─────────────────────────────────────────────────

    /** Тухайн Мягмарын хурлыг олж эсвэл (байхгүй бол) автоматаар үүсгэнэ */
    private BriefingMeeting getOrCreateMeeting(LocalDate meetingDate) {
        return meetingRepo.findByMeetingDate(meetingDate).orElseGet(() -> {
            BriefingMeeting m = new BriefingMeeting();
            m.setMeetingDate(meetingDate);
            m.setMeetingNo(defaultMeetingNo(meetingDate));
            m.setActiveFlag(1);
            m.setCreatedBy(UserContext.getUserId());
            m.setCreatedDate(now());
            return meetingRepo.save(m);
        });
    }

    /** Анхдагч хуралдааны дугаар — ISO долоо хоногоор (ж: 2026-24). Нарийн бичиг засна. */
    private String defaultMeetingNo(LocalDate d) {
        int week = d.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = d.get(java.time.temporal.IsoFields.WEEK_BASED_YEAR);
        return year + "-" + week;
    }

    /** Хурлуудын жагсаалт (нарийн бичиг) */
    public List<BriefingMeeting> listMeetings() {
        return meetingRepo.findByActiveFlagOrderByMeetingDateDesc(1);
    }

    /** Тухайн долоо хоногийн хурал (байхгүй бол үүсгэлгүйгээр null) */
    public BriefingMeeting currentMeeting() {
        return meetingRepo.findByMeetingDate(currentMeetingTuesday()).orElse(null);
    }

    /** Хурал бүртгэх/засах (зөвхөн нарийн бичиг). meetingDate байхгүй бол энэ долоо хоногийн Мягмар. */
    @Transactional
    public BriefingMeeting saveMeeting(Integer id, LocalDate meetingDate, String meetingNo, String summary) {
        access.requireSecretary(UserContext.getUserId());
        BriefingMeeting m;
        if (id != null) {
            m = meetingRepo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Хурал олдсонгүй"));
        } else {
            LocalDate date = meetingDate != null ? meetingDate : currentMeetingTuesday();
            m = meetingRepo.findByMeetingDate(date).orElseGet(() -> {
                BriefingMeeting nm = new BriefingMeeting();
                nm.setMeetingDate(date);
                nm.setActiveFlag(1);
                nm.setCreatedBy(UserContext.getUserId());
                nm.setCreatedDate(now());
                return nm;
            });
        }
        if (meetingNo == null || meetingNo.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Хуралдааны дугаар заавал бичнэ");
        m.setMeetingNo(meetingNo);
        m.setSummary(summary);
        return meetingRepo.save(m);
    }

    private void createFulfillmentRow(Integer cycleId, Integer departmentId) {
        BriefingFulfillment f = new BriefingFulfillment();
        f.setCycleId(cycleId);
        f.setDepartmentId(departmentId);
        f.setFolderId(UUID.randomUUID().toString());
        f.setUpdatedAt(now());
        fulRepo.save(f);
    }

    // ── Биелэлт оруулах (алба бүр) ──────────────────────────────────────────────

    @Transactional
    public BriefingDto submitFulfillment(Integer cycleId, Integer departmentId, String workText) {
        BriefingCycle cycle = cycleRepo.findById(cycleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мөчлөг олдсонгүй"));
        // Хатуу хугацаа: Баасан 16:00
        if (now().isAfter(cycle.getSubmitDeadline()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Биелэлт оруулах хугацаа дууссан (Баасан 16:00)");
        access.requireNotViewer(UserContext.getUserId());
        requireSameDepartment(departmentId);

        // §3.2 — хоосон биелэлт хадгалахгүй
        if (workText == null || workText.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Хийгдсэн ажлын тайлбар хоосон байна");

        BriefingFulfillment f = fulRepo.findByCycleIdAndDepartmentId(cycleId, departmentId)
                .orElseGet(() -> {
                    BriefingFulfillment nf = new BriefingFulfillment();
                    nf.setCycleId(cycleId);
                    nf.setDepartmentId(departmentId);
                    nf.setFolderId(UUID.randomUUID().toString());
                    return nf;
                });

        // §3.2 — нотлох баримтгүй бол илгээх боломжгүй (файл/линкийг урьдчилж хавсаргасан байх ёстой)
        boolean hasEvidence = !evidenceRepo.findByFolderId(f.getFolderId()).isEmpty();
        if (!hasEvidence)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Нотлох баримт хавсаргаагүй тул илгээх боломжгүй");

        f.setWorkText(workText);
        if (f.getSubmittedAt() == null) f.setSubmittedAt(now());
        f.setSubmittedBy(UserContext.getUserId());
        f.setStatus(1);                 // илгээгдсэн (шалгаж байгаа)
        f.setReturnComment(null);       // дахин илгээвэл буцаалтын тэмдэглэгээ арилна
        f.setReturnedAt(null);
        f.setUpdatedAt(now());
        fulRepo.save(f);

        audit.log("SUBMIT_FULFILLMENT", "FULFILLMENT", f.getId(), null,
                "cycleId=" + cycleId + "; deptId=" + departmentId + "; workText=" + workText);

        // §3.6 — "Танд шалгах үүрэг даалгавар ирсэн байна" → үүрэг өгсөн удирдлага + delegate-уудад
        BriefingTask task = taskRepo.findById(cycle.getTaskId()).orElse(null);
        if (task != null) {
            Set<Integer> reviewers = scorerUserIds(task);
            notificationService.notifyBriefingUsers(reviewers,
                    "Шалгах биелэлт ирлээ", "Танд шалгах үүрэг даалгавар ирсэн байна: " + task.getDescription());
        }

        return getOne(cycle.getTaskId());
    }

    /** Тухайн үүргийг дүгнэх эрхтэй хэрэглэгчид — assigner + delegate-ууд */
    private Set<Integer> scorerUserIds(BriefingTask task) {
        Set<Integer> ids = new HashSet<>();
        if (task.getAssignerId() != null) ids.add(task.getAssignerId());
        delegateRepo.findByTaskId(task.getId()).forEach(d -> ids.add(d.getUserId()));
        return ids;
    }

    // ── Нотлох баримт (file service-н objectName) ───────────────────────────────

    @Transactional
    public BriefingDto.Evidence addEvidence(String folderId, String objectName, String linkUrl, String evidenceType,
                                            String fileName, String contentType, Long fileSize) {
        BriefingFulfillment f = fulRepo.findByFolderId(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder олдсонгүй"));
        BriefingCycle cycle = cycleRepo.findById(f.getCycleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мөчлөг олдсонгүй"));
        if (now().isAfter(cycle.getSubmitDeadline()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Нотлох баримт хавсаргах хугацаа дууссан (Баасан 16:00)");
        access.requireNotViewer(UserContext.getUserId());
        requireSameDepartment(f.getDepartmentId());

        String type = (evidenceType == null || evidenceType.isBlank()) ? "FILE" : evidenceType.toUpperCase();
        if ("LINK".equals(type)) {
            if (linkUrl == null || linkUrl.isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Холбоос (URL) хоосон байна");
        } else {
            if (objectName == null || objectName.isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файлын objectName хоосон байна");
            validateFileType(fileName, contentType);
        }

        BriefingEvidence e = new BriefingEvidence();
        e.setFolderId(folderId);
        e.setObjectName(objectName);
        e.setLinkUrl(linkUrl);
        e.setEvidenceType(type);
        e.setFileName(fileName);
        e.setContentType(contentType);
        e.setFileSize(fileSize);
        e.setUploadedBy(UserContext.getUserId());
        e.setUploadedAt(now());
        BriefingEvidence saved = evidenceRepo.save(e);
        return toEvidenceDto(saved);
    }

    /** Зөвшөөрөгдөх файлын төрөл (§3.2): PDF, Word, Excel, JPG/PNG */
    private static final Set<String> ALLOWED_EXT = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png");

    private void validateFileType(String fileName, String contentType) {
        String ext = "";
        if (fileName != null && fileName.contains(".")) {
            ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Зөвшөөрөгдөх файл: PDF, Word, Excel, JPG, PNG. Бусад файлыг линкээр хавсаргана уу.");
        }
    }

    @Transactional
    public void deleteEvidence(Integer id) {
        BriefingEvidence e = evidenceRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Баримт олдсонгүй"));
        fulRepo.findByFolderId(e.getFolderId()).ifPresent(f -> {
            BriefingCycle cycle = cycleRepo.findById(f.getCycleId()).orElse(null);
            if (cycle != null && now().isAfter(cycle.getSubmitDeadline()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Хугацаа дууссан тул баримт устгах боломжгүй");
            access.requireNotViewer(UserContext.getUserId());
            requireSameDepartment(f.getDepartmentId());
        });
        evidenceRepo.deleteById(id);
    }

    // ── Дүгнэх (assigner, нэг ерөнхий оноо) ──────────────────────────────────────

    @Transactional
    public BriefingDto score(Integer cycleId, Integer score, String scoreComment) {
        if (score == null || score < 0 || score > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дүн 0-100 хооронд байна");

        BriefingCycle cycle = cycleRepo.findById(cycleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мөчлөг олдсонгүй"));
        BriefingTask task = taskRepo.findById(cycle.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Үүрэг олдсонгүй"));

        // Үүрэг өгсөн удирдлага, эсвэл түүний өмнөөс эрх олгогдсон (хэлтсийн дарга/Ер.инженер), эсвэл админ дүгнэнэ
        if (!access.canScoreTask(UserContext.getUserId(), task))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Танд энэ үүргийг дүгнэх эрх алга");
        // Хатуу хугацаа: Даваа 14:00
        if (now().isAfter(cycle.getScoreDeadline()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Дүгнэх хугацаа дууссан (Даваа 14:00)");

        // Биелэлт оруулаагүй бол дүгнэх боломжгүй (хугацаа дуусахад автоматаар 0 болно)
        boolean anySubmitted = fulRepo.findByCycleId(cycleId).stream()
                .anyMatch(f -> f.getSubmittedAt() != null);
        if (!anySubmitted)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Биелэлт оруулаагүй тул дүгнэх боломжгүй (энэ 7 хоногт 0 болно)");

        Integer oldScore = cycle.getScore();
        cycle.setScore(score);
        cycle.setScoreComment(scoreComment);
        cycle.setScoredBy(UserContext.getUserId());
        cycle.setScoredAt(now());
        cycle.setStatus(1);
        cycleRepo.save(cycle);

        audit.log("SCORE", "CYCLE", cycleId,
                oldScore == null ? null : ("score=" + oldScore),
                "score=" + score + (scoreComment != null && !scoreComment.isBlank() ? "; comment=" + scoreComment : ""));

        if (score == 100) {
            task.setStatus(1); // бүрэн биелсэн
            task.setUpdatedDate(now());
            taskRepo.save(task);
        }
        return getOne(task.getId());
    }

    // ── Биелэлт буцаах (§3.3) ────────────────────────────────────────────────────

    /**
     * Удирдлага биелэлтийг нэгжид буцааж засуулна. Биелэлт "буцаагдсан" төлөвт орж,
     * submittedAt цэвэрлэгдэн (хугацаа дуусаагүй бол) нэгж дахин оруулах боломжтой болно.
     */
    @Transactional
    public BriefingDto returnFulfillment(Integer fulfillmentId, String comment) {
        BriefingFulfillment f = fulRepo.findById(fulfillmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Биелэлт олдсонгүй"));
        BriefingCycle cycle = cycleRepo.findById(f.getCycleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мөчлөг олдсонгүй"));
        BriefingTask task = taskRepo.findById(cycle.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Үүрэг олдсонгүй"));

        if (!access.canScoreTask(UserContext.getUserId(), task))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Танд биелэлт буцаах эрх алга");
        if (cycle.getScore() != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дүгнэгдсэн тул буцаах боломжгүй");
        if (f.getSubmittedAt() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Илгээгээгүй биелэлтийг буцаах боломжгүй");

        f.setStatus(2);                  // буцаагдсан
        f.setReturnComment(comment);
        f.setReturnedAt(now());
        f.setReturnedBy(UserContext.getUserId());
        f.setSubmittedAt(null);          // дахин оруулах боломжтой болгоно
        f.setSubmittedBy(null);
        f.setUpdatedAt(now());
        fulRepo.save(f);

        audit.log("RETURN", "FULFILLMENT", f.getId(), null,
                "deptId=" + f.getDepartmentId() + (comment != null ? "; comment=" + comment : ""));

        // §3.6 — "Таны биелэлтийг буцаасан байна" → тухайн нэгжид
        notificationService.notifyBriefingDepts(Set.of(f.getDepartmentId()),
                "Биелэлт буцаагдсан", "Таны биелэлтийг буцаасан байна: " + task.getDescription());

        return getOne(task.getId());
    }

    // ── Сунгах (шинэ cycle) ──────────────────────────────────────────────────────

    @Transactional
    public BriefingDto extend(Integer taskId) {
        access.requireSecretary(UserContext.getUserId());
        BriefingTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Үүрэг олдсонгүй"));
        if (task.getStatus() != null && task.getStatus() == 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Үүрэг бүрэн биелсэн тул сунгах шаардлагагүй");

        BriefingCycle latest = cycleRepo.findTopByTaskIdOrderByCycleNoDesc(taskId);
        if (latest == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Мөчлөг олдсонгүй");
        if (latest.getScore() != null && latest.getScore() == 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Үүрэг аль хэдийн бүрэн биелсэн");
        // Дүгнэгдсэн эсвэл дүгнэх хугацаа дууссан байж байж сунгана
        boolean closed = (latest.getStatus() != null && latest.getStatus() == 1)
                || now().isAfter(latest.getScoreDeadline());
        if (!closed)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Одоогийн мөчлөг дуусаагүй байна (дүгнэгдсэний дараа сунгана)");

        List<Integer> depIds = taskDepRepo.findByTaskId(taskId).stream()
                .map(BriefingTaskDepartment::getDepartmentId).collect(Collectors.toList());
        LocalDate nextMeeting = latest.getMeetingDate().plusDays(7);
        createCycle(taskId, latest.getCycleNo() + 1, nextMeeting, depIds);

        task.setUpdatedDate(now());
        taskRepo.save(task);
        notificationService.notifyBriefing("Сунгасан үүрэг: " + task.getDescription(), new HashSet<>(depIds));

        audit.log("EXTEND", "TASK", taskId, null, "cycleNo=" + (latest.getCycleNo() + 1));

        return getOne(taskId);
    }

    // ── Delegation (§2) ──────────────────────────────────────────────────────────

    @Transactional
    public BriefingDto addDelegate(Integer taskId, Integer userId) {
        access.requireSecretary(UserContext.getUserId());
        if (userId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Хэрэглэгч сонгоно уу");
        taskRepo.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Үүрэг олдсонгүй"));
        if (!delegateRepo.existsByTaskIdAndUserId(taskId, userId)) {
            BriefingTaskDelegate d = new BriefingTaskDelegate();
            d.setTaskId(taskId);
            d.setUserId(userId);
            delegateRepo.save(d);
        }
        return getOne(taskId);
    }

    @Transactional
    public BriefingDto removeDelegate(Integer taskId, Integer userId) {
        access.requireSecretary(UserContext.getUserId());
        delegateRepo.findByTaskId(taskId).stream()
                .filter(d -> Objects.equals(d.getUserId(), userId))
                .forEach(d -> delegateRepo.deleteById(d.getId()));
        return getOne(taskId);
    }

    // ── Soft delete ───────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Integer id) {
        access.requireSecretary(UserContext.getUserId());
        BriefingTask task = taskRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Үүрэг олдсонгүй"));
        task.setActiveFlag(0);
        task.setUpdatedDate(now());
        taskRepo.save(task);
        audit.log("DELETE_TASK", "TASK", id, null, "desc=" + task.getDescription());
    }

    // ── Mapping ──────────────────────────────────────────────────────────────────

    private List<BriefingDto> mapTasks(List<BriefingTask> tasks) {
        if (tasks.isEmpty()) return List.of();

        Map<Integer, String> depMap = departmentRepo.findAll().stream()
                .collect(Collectors.toMap(Department::getDepId, Department::getDepName, (a, b) -> a));
        Map<Integer, String> userMap = userRepo.findAll().stream()
                .collect(Collectors.toMap(User::getId, this::fullName, (a, b) -> a));
        Map<Integer, String> meetingNoMap = meetingRepo.findAll().stream()
                .filter(m -> m.getMeetingNo() != null)
                .collect(Collectors.toMap(BriefingMeeting::getId, BriefingMeeting::getMeetingNo, (a, b) -> a));

        List<Integer> taskIds = tasks.stream().map(BriefingTask::getId).collect(Collectors.toList());

        Map<Integer, List<BriefingTaskDepartment>> depsByTask = taskDepRepo.findByTaskIdIn(taskIds)
                .stream().collect(Collectors.groupingBy(BriefingTaskDepartment::getTaskId));

        Map<Integer, List<BriefingTaskDelegate>> delegatesByTask = delegateRepo.findByTaskIdIn(taskIds)
                .stream().collect(Collectors.groupingBy(BriefingTaskDelegate::getTaskId));

        List<BriefingCycle> cycles = cycleRepo.findByTaskIdInOrderByCycleNoAsc(taskIds);
        Map<Integer, List<BriefingCycle>> cyclesByTask = cycles.stream()
                .collect(Collectors.groupingBy(BriefingCycle::getTaskId));

        List<Integer> cycleIds = cycles.stream().map(BriefingCycle::getId).collect(Collectors.toList());
        List<BriefingFulfillment> fuls = cycleIds.isEmpty() ? List.of() : fulRepo.findByCycleIdIn(cycleIds);
        Map<Integer, List<BriefingFulfillment>> fulByCycle = fuls.stream()
                .collect(Collectors.groupingBy(BriefingFulfillment::getCycleId));

        List<String> folderIds = fuls.stream().map(BriefingFulfillment::getFolderId).collect(Collectors.toList());
        Map<String, List<BriefingEvidence>> evByFolder = folderIds.isEmpty() ? Map.of()
                : evidenceRepo.findByFolderIdIn(folderIds).stream()
                    .collect(Collectors.groupingBy(BriefingEvidence::getFolderId));

        return tasks.stream().map(t -> {
            BriefingDto d = new BriefingDto();
            d.setId(t.getId());
            d.setAssignerId(t.getAssignerId());
            d.setAssignerName(t.getAssignerId() != null ? userMap.get(t.getAssignerId()) : null);
            d.setDescription(t.getDescription());
            d.setStatus(t.getStatus());
            d.setCreatedBy(t.getCreatedBy());
            d.setCreatedByName(t.getCreatedBy() != null ? userMap.get(t.getCreatedBy()) : null);
            d.setCreatedDate(t.getCreatedDate());

            d.setDepartments(depsByTask.getOrDefault(t.getId(), List.of()).stream().map(td -> {
                BriefingDto.DepRef r = new BriefingDto.DepRef();
                r.setDepartmentId(td.getDepartmentId());
                r.setDepName(depMap.get(td.getDepartmentId()));
                return r;
            }).collect(Collectors.toList()));

            List<BriefingTaskDelegate> dels = delegatesByTask.getOrDefault(t.getId(), List.of());
            d.setDelegateIds(dels.stream().map(BriefingTaskDelegate::getUserId).collect(Collectors.toList()));
            // delegateIds-тай индекс/урт нь яг таарах ёстой (frontend нэрийг индексээр тулгадаг) —
            // нэр олдоогүй тохиолдолд хасахгүй "#id" fallback өгнө.
            d.setDelegateNames(dels.stream()
                    .map(x -> { String n = userMap.get(x.getUserId()); return n != null ? n : "#" + x.getUserId(); })
                    .collect(Collectors.toList()));

            d.setCycles(cyclesByTask.getOrDefault(t.getId(), List.of()).stream().map(c -> {
                BriefingDto.Cycle cd = new BriefingDto.Cycle();
                cd.setId(c.getId());
                cd.setCycleNo(c.getCycleNo());
                cd.setMeetingId(c.getMeetingId());
                cd.setMeetingNo(c.getMeetingId() != null ? meetingNoMap.get(c.getMeetingId()) : null);
                cd.setMeetingDate(c.getMeetingDate());
                cd.setSubmitDeadline(c.getSubmitDeadline());
                cd.setScoreDeadline(c.getScoreDeadline());
                cd.setScore(c.getScore());
                cd.setScoredBy(c.getScoredBy());
                cd.setScoredByName(c.getScoredBy() != null ? userMap.get(c.getScoredBy()) : null);
                cd.setScoredAt(c.getScoredAt());
                cd.setScoreComment(c.getScoreComment());
                cd.setStatus(c.getStatus());
                cd.setDerivedStatus(deriveCycleStatus(c, fulByCycle.getOrDefault(c.getId(), List.of())));
                cd.setFulfillments(fulByCycle.getOrDefault(c.getId(), List.of()).stream().map(f -> {
                    BriefingDto.Fulfillment fd = new BriefingDto.Fulfillment();
                    fd.setId(f.getId());
                    fd.setDepartmentId(f.getDepartmentId());
                    fd.setDepName(depMap.get(f.getDepartmentId()));
                    fd.setWorkText(f.getWorkText());
                    fd.setFolderId(f.getFolderId());
                    fd.setSubmittedBy(f.getSubmittedBy());
                    fd.setSubmittedByName(f.getSubmittedBy() != null ? userMap.get(f.getSubmittedBy()) : null);
                    fd.setSubmittedAt(f.getSubmittedAt());
                    fd.setStatus(f.getStatus());
                    fd.setReturnComment(f.getReturnComment());
                    fd.setReturnedAt(f.getReturnedAt());
                    fd.setEvidence(evByFolder.getOrDefault(f.getFolderId(), List.of()).stream()
                            .map(this::toEvidenceDto).collect(Collectors.toList()));
                    return fd;
                }).collect(Collectors.toList()));
                return cd;
            }).collect(Collectors.toList()));

            return d;
        }).collect(Collectors.toList());
    }

    /**
     * Мөчлөгийн дериватив төлөв (§3.1/§8). Frontend зөвхөн харуулж өнгөөр ялгана.
     *   DONE       — дүн ≥ 85 (ногоон)
     *   IN_PROGRESS— дүн 50–84 (шар)
     *   NOT_DONE   — дүн 0–49 (улаан)
     *   REVIEWING  — биелэлт илгээгдсэн, дүгнэлт хүлээж буй
     *   OVERDUE    — биелэлт оруулах хугацаа хэтэрсэн ч ямар ч алба оруулаагүй (улаан)
     *   NEW        — шинэ, биелэлт ороогүй, хугацаа хэтрээгүй
     */
    private String deriveCycleStatus(BriefingCycle c, List<BriefingFulfillment> fuls) {
        Integer score = c.getScore();
        if (score != null) {
            if (score >= 85) return "DONE";
            if (score >= 50) return "IN_PROGRESS";
            return "NOT_DONE";
        }
        boolean anySubmitted = fuls.stream().anyMatch(f -> f.getSubmittedAt() != null);
        if (anySubmitted) return "REVIEWING";
        if (now().isAfter(c.getSubmitDeadline())) return "OVERDUE";
        return "NEW";
    }

    private BriefingDto.Evidence toEvidenceDto(BriefingEvidence e) {
        BriefingDto.Evidence ed = new BriefingDto.Evidence();
        ed.setId(e.getId());
        ed.setObjectName(e.getObjectName());
        ed.setLinkUrl(e.getLinkUrl());
        ed.setEvidenceType(e.getEvidenceType());
        ed.setFileName(e.getFileName());
        ed.setContentType(e.getContentType());
        ed.setFileSize(e.getFileSize());
        ed.setUploadedAt(e.getUploadedAt());
        return ed;
    }

    private String fullName(User u) {
        String ln = u.getLastName() != null && !u.getLastName().isBlank()
                ? u.getLastName().charAt(0) + ". " : "";
        String fn = u.getFirstName() != null ? u.getFirstName() : "";
        return (ln + fn).trim();
    }

    /** Биелэлт/баримтыг зөвхөн тухайн албаны хэрэглэгч оруулна */
    private void requireSameDepartment(Integer departmentId) {
        Integer uid = UserContext.getUserId();
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Нэвтрэлт шаардлагатай");
        User u = userRepo.findById(uid).orElse(null);
        if (u == null || !Objects.equals(u.getDepartmentId(), departmentId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Зөвхөн өөрийн албаны биелэлтийг оруулна");
    }
}
