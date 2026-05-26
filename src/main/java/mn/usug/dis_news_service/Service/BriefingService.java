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
    private final DepartmentDAO departmentRepo;
    private final UserDAO userRepo;
    private final NotificationService notificationService;

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
        return userRepo.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getCanAssignTask()))
                .filter(u -> Boolean.TRUE.equals(u.getActiveFlag()))
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
        BriefingCycle c = new BriefingCycle();
        c.setTaskId(taskId);
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
        requireSameDepartment(departmentId);

        BriefingFulfillment f = fulRepo.findByCycleIdAndDepartmentId(cycleId, departmentId)
                .orElseGet(() -> {
                    BriefingFulfillment nf = new BriefingFulfillment();
                    nf.setCycleId(cycleId);
                    nf.setDepartmentId(departmentId);
                    nf.setFolderId(UUID.randomUUID().toString());
                    return nf;
                });
        f.setWorkText(workText);
        if (f.getSubmittedAt() == null) f.setSubmittedAt(now());
        f.setSubmittedBy(UserContext.getUserId());
        f.setUpdatedAt(now());
        fulRepo.save(f);

        return getOne(cycle.getTaskId());
    }

    // ── Нотлох баримт (file service-н objectName) ───────────────────────────────

    @Transactional
    public BriefingDto.Evidence addEvidence(String folderId, String objectName, String fileName,
                                            String contentType, Long fileSize) {
        BriefingFulfillment f = fulRepo.findByFolderId(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder олдсонгүй"));
        BriefingCycle cycle = cycleRepo.findById(f.getCycleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мөчлөг олдсонгүй"));
        if (now().isAfter(cycle.getSubmitDeadline()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Нотлох баримт хавсаргах хугацаа дууссан (Баасан 16:00)");
        requireSameDepartment(f.getDepartmentId());

        BriefingEvidence e = new BriefingEvidence();
        e.setFolderId(folderId);
        e.setObjectName(objectName);
        e.setFileName(fileName);
        e.setContentType(contentType);
        e.setFileSize(fileSize);
        e.setUploadedBy(UserContext.getUserId());
        e.setUploadedAt(now());
        BriefingEvidence saved = evidenceRepo.save(e);
        return toEvidenceDto(saved);
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
            requireSameDepartment(f.getDepartmentId());
        });
        evidenceRepo.deleteById(id);
    }

    // ── Дүгнэх (assigner, нэг ерөнхий оноо) ──────────────────────────────────────

    @Transactional
    public BriefingDto score(Integer cycleId, Integer score) {
        if (score == null || score < 0 || score > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дүн 0-100 хооронд байна");

        BriefingCycle cycle = cycleRepo.findById(cycleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мөчлөг олдсонгүй"));
        BriefingTask task = taskRepo.findById(cycle.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Үүрэг олдсонгүй"));

        // Зөвхөн үүрэг өгсөн (хянаж баталгаажуулагч) албан тушаалтан дүгнэнэ
        if (!Objects.equals(UserContext.getUserId(), task.getAssignerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Зөвхөн үүрэг өгсөн албан тушаалтан дүгнэнэ");
        // Хатуу хугацаа: Даваа 14:00
        if (now().isAfter(cycle.getScoreDeadline()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Дүгнэх хугацаа дууссан (Даваа 14:00)");

        // Биелэлт оруулаагүй бол дүгнэх боломжгүй (хугацаа дуусахад автоматаар 0 болно)
        boolean anySubmitted = fulRepo.findByCycleId(cycleId).stream()
                .anyMatch(f -> f.getSubmittedAt() != null);
        if (!anySubmitted)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Биелэлт оруулаагүй тул дүгнэх боломжгүй (энэ 7 хоногт 0 болно)");

        cycle.setScore(score);
        cycle.setScoredBy(UserContext.getUserId());
        cycle.setScoredAt(now());
        cycle.setStatus(1);
        cycleRepo.save(cycle);

        if (score == 100) {
            task.setStatus(1); // бүрэн биелсэн
            task.setUpdatedDate(now());
            taskRepo.save(task);
        }
        return getOne(task.getId());
    }

    // ── Сунгах (шинэ cycle) ──────────────────────────────────────────────────────

    @Transactional
    public BriefingDto extend(Integer taskId) {
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

        return getOne(taskId);
    }

    // ── Soft delete ───────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Integer id) {
        BriefingTask task = taskRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Үүрэг олдсонгүй"));
        task.setActiveFlag(0);
        task.setUpdatedDate(now());
        taskRepo.save(task);
    }

    // ── Mapping ──────────────────────────────────────────────────────────────────

    private List<BriefingDto> mapTasks(List<BriefingTask> tasks) {
        if (tasks.isEmpty()) return List.of();

        Map<Integer, String> depMap = departmentRepo.findAll().stream()
                .collect(Collectors.toMap(Department::getDepId, Department::getDepName, (a, b) -> a));
        Map<Integer, String> userMap = userRepo.findAll().stream()
                .collect(Collectors.toMap(User::getId, this::fullName, (a, b) -> a));

        List<Integer> taskIds = tasks.stream().map(BriefingTask::getId).collect(Collectors.toList());

        Map<Integer, List<BriefingTaskDepartment>> depsByTask = taskDepRepo.findByTaskIdIn(taskIds)
                .stream().collect(Collectors.groupingBy(BriefingTaskDepartment::getTaskId));

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

            d.setCycles(cyclesByTask.getOrDefault(t.getId(), List.of()).stream().map(c -> {
                BriefingDto.Cycle cd = new BriefingDto.Cycle();
                cd.setId(c.getId());
                cd.setCycleNo(c.getCycleNo());
                cd.setMeetingDate(c.getMeetingDate());
                cd.setSubmitDeadline(c.getSubmitDeadline());
                cd.setScoreDeadline(c.getScoreDeadline());
                cd.setScore(c.getScore());
                cd.setScoredBy(c.getScoredBy());
                cd.setScoredByName(c.getScoredBy() != null ? userMap.get(c.getScoredBy()) : null);
                cd.setScoredAt(c.getScoredAt());
                cd.setStatus(c.getStatus());
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
                    fd.setEvidence(evByFolder.getOrDefault(f.getFolderId(), List.of()).stream()
                            .map(this::toEvidenceDto).collect(Collectors.toList()));
                    return fd;
                }).collect(Collectors.toList()));
                return cd;
            }).collect(Collectors.toList()));

            return d;
        }).collect(Collectors.toList());
    }

    private BriefingDto.Evidence toEvidenceDto(BriefingEvidence e) {
        BriefingDto.Evidence ed = new BriefingDto.Evidence();
        ed.setId(e.getId());
        ed.setObjectName(e.getObjectName());
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
