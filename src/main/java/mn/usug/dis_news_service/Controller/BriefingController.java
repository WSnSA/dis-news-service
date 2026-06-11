package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DTO.BriefingDto;
import mn.usug.dis_news_service.DTO.BriefingSaveDto;
import mn.usug.dis_news_service.DTO.BriefingNewsDto;
import mn.usug.dis_news_service.Entity.BriefingMeeting;
import mn.usug.dis_news_service.Entity.BriefingNews;
import mn.usug.dis_news_service.Entity.BriefingUnit;
import mn.usug.dis_news_service.Entity.BriefingAuditLog;
import mn.usug.dis_news_service.Service.BriefingService;
import mn.usug.dis_news_service.Service.BriefingNewsService;
import mn.usug.dis_news_service.Service.BriefingRoleService;
import mn.usug.dis_news_service.Service.BriefingAuditService;
import mn.usug.dis_news_service.Service.BriefingAccessService;
import mn.usug.dis_news_service.Service.UserContext;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Шуурхай хурлын үүрэг даалгавар.
 * Хуучин /ref/task-тай огт хамаагүй, бүрэн тусдаа модуль.
 */
@RestController
@RequestMapping("/ref/briefing")
@RequiredArgsConstructor
public class BriefingController {

    private final BriefingService service;
    private final BriefingRoleService roleService;
    private final BriefingNewsService newsService;
    private final BriefingAuditService auditService;
    private final BriefingAccessService access;

    // ── Audit log (§7) ───────────────────────────────────────────────────────────

    /** Сүүлийн аудит бичлэгүүд (зөвхөн админ) */
    @GetMapping("/audit")
    public List<BriefingAuditLog> audit(@RequestParam(defaultValue = "100") int limit) {
        access.requireAdmin(UserContext.getUserId());
        return auditService.recent(limit);
    }

    /** Тодорхой үүргийн аудит түүх */
    @GetMapping("/audit/task/{taskId}")
    public List<BriefingAuditLog> auditTask(@PathVariable Integer taskId) {
        return auditService.forEntity("TASK", taskId);
    }

    // ── RBAC (§2) ──────────────────────────────────────────────────────────────

    /** Одоогийн хэрэглэгчийн эрхийн мэдээлэл (frontend таб/товч gate) */
    @GetMapping("/my-access")
    public Map<String, Object> myAccess() {
        return roleService.myAccess();
    }

    /** Боломжит дүрийн жагсаалт (админ UI) */
    @GetMapping("/roles/catalog")
    public List<Map<String, Object>> roleCatalog() {
        return roleService.roleCatalog();
    }

    /** Дүр оноогдсон бүх хэрэглэгч (зөвхөн админ) */
    @GetMapping("/roles")
    public List<Map<String, Object>> userRoles() {
        return roleService.listUserRoles();
    }

    /** Дүр оноох (зөвхөн админ) */
    @PostMapping("/roles")
    public void assignRole(@RequestBody Map<String, Object> body) {
        Integer userId = body.get("userId") != null ? Integer.valueOf(body.get("userId").toString()) : null;
        String roleKey = str(body.get("roleKey"));
        roleService.assign(userId, roleKey);
    }

    /** Дүр хасах (зөвхөн админ) */
    @DeleteMapping("/roles")
    public void revokeRole(@RequestParam Integer userId, @RequestParam String roleKey) {
        roleService.revoke(userId, roleKey);
    }

    /** Бүх идэвхтэй үүрэг (мод хэлбэрээр: cycle → биелэлт → нотлох баримт) */
    @GetMapping("/getAll")
    public List<BriefingDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/get")
    public BriefingDto get(@RequestParam Integer id) {
        return service.getOne(id);
    }

    /** Үүрэг өгөх эрхтэй (can_assign_task=1) албан тушаалтнууд */
    @GetMapping("/assigners")
    public List<Map<String, Object>> assigners() {
        return service.listAssigners();
    }

    // ── Шуурхай зөвлөгөөн (§3.1) ────────────────────────────────────────────────

    /** Хурлуудын жагсаалт */
    @GetMapping("/meetings")
    public List<BriefingMeeting> meetings() {
        return service.listMeetings();
    }

    /** Энэ долоо хоногийн хурал (байхгүй бол null) */
    @GetMapping("/meeting/current")
    public BriefingMeeting currentMeeting() {
        return service.currentMeeting();
    }

    /** Хурал бүртгэх/засах (зөвхөн нарийн бичиг) */
    @PostMapping("/meeting")
    public BriefingMeeting saveMeeting(@RequestBody Map<String, Object> body) {
        Integer id = body.get("id") != null ? Integer.valueOf(body.get("id").toString()) : null;
        LocalDate meetingDate = body.get("meetingDate") != null && !body.get("meetingDate").toString().isBlank()
                ? LocalDate.parse(body.get("meetingDate").toString()) : null;
        String meetingNo = str(body.get("meetingNo"));
        String summary   = str(body.get("summary"));
        return service.saveMeeting(id, meetingDate, meetingNo, summary);
    }

    // ── Шуурхайн мэдээ (§3.4) ────────────────────────────────────────────────────

    /** Зохион байгуулалтын нэгж/хэлтэс/удирдлага (танилцуулгын дараалал) */
    @GetMapping("/news/units")
    public List<BriefingUnit> newsUnits() {
        return newsService.listUnits();
    }

    /** Тухайн хурлын мэдээ — нэгж бүрээр дарааллаар */
    @GetMapping("/news")
    public List<BriefingNewsDto> news(@RequestParam Integer meetingId) {
        return newsService.getMeetingNews(meetingId);
    }

    /** Мэдээ хадгалах (draft) */
    @PostMapping("/news")
    public BriefingNews saveNews(@RequestBody Map<String, Object> body) {
        Integer meetingId = intOf(body.get("meetingId"));
        Integer unitId    = intOf(body.get("unitId"));
        return newsService.saveNews(meetingId, unitId,
                str(body.get("summaryText")), str(body.get("result")), str(body.get("extraProposal")));
    }

    /** Мэдээ илгээх (submitted) */
    @PostMapping("/news/submit")
    public BriefingNews submitNews(@RequestBody Map<String, Object> body) {
        return newsService.submitNews(intOf(body.get("meetingId")), intOf(body.get("unitId")));
    }

    /** Мэдээний нотлох баримт (файл эсвэл линк) */
    @PostMapping("/news/evidence")
    public BriefingNewsDto.Evidence addNewsEvidence(@RequestBody Map<String, Object> body) {
        Long fileSize = body.get("fileSize") != null ? Long.valueOf(body.get("fileSize").toString()) : null;
        return newsService.addEvidence(str(body.get("folderId")), str(body.get("objectName")),
                str(body.get("linkUrl")), str(body.get("evidenceType")),
                str(body.get("fileName")), str(body.get("contentType")), fileSize);
    }

    @DeleteMapping("/news/evidence/{id}")
    public void deleteNewsEvidence(@PathVariable Integer id) {
        newsService.deleteEvidence(id);
    }

    /** Бүртгэх / засах */
    @PostMapping("/save")
    public BriefingDto save(@RequestBody BriefingSaveDto dto) {
        return service.save(dto);
    }

    /** Биелэлт оруулах (алба бүр) */
    @PostMapping("/cycle/{cycleId}/dept/{deptId}/fulfillment")
    public BriefingDto submitFulfillment(@PathVariable Integer cycleId,
                                         @PathVariable Integer deptId,
                                         @RequestBody Map<String, Object> body) {
        String workText = body.getOrDefault("workText", "").toString();
        return service.submitFulfillment(cycleId, deptId, workText);
    }

    /** Нотлох баримт бүртгэх (file service-д upload хийсний дараа objectName-г энд хадгална) */
    @PostMapping("/evidence")
    public BriefingDto.Evidence addEvidence(@RequestBody Map<String, Object> body) {
        String folderId     = str(body.get("folderId"));
        String objectName   = str(body.get("objectName"));
        String linkUrl      = str(body.get("linkUrl"));
        String evidenceType = str(body.get("evidenceType"));
        String fileName     = str(body.get("fileName"));
        String contentType  = str(body.get("contentType"));
        Long   fileSize     = body.get("fileSize") != null
                ? Long.valueOf(body.get("fileSize").toString()) : null;
        return service.addEvidence(folderId, objectName, linkUrl, evidenceType, fileName, contentType, fileSize);
    }

    @DeleteMapping("/evidence/{id}")
    public void deleteEvidence(@PathVariable Integer id) {
        service.deleteEvidence(id);
    }

    /** Дүгнэх (assigner/delegate, нэг ерөнхий оноо 0-100 + тайлбар) */
    @PostMapping("/cycle/{cycleId}/score")
    public BriefingDto score(@PathVariable Integer cycleId, @RequestBody Map<String, Object> body) {
        Integer score = body.get("score") != null
                ? Integer.valueOf(body.get("score").toString()) : null;
        return service.score(cycleId, score, str(body.get("scoreComment")));
    }

    /** Биелэлт буцаах (§3.3) — нэгжид засуулахаар буцаана */
    @PostMapping("/fulfillment/{fulfillmentId}/return")
    public BriefingDto returnFulfillment(@PathVariable Integer fulfillmentId, @RequestBody Map<String, Object> body) {
        return service.returnFulfillment(fulfillmentId, str(body.get("comment")));
    }

    /** Үүрэгт төлөөлөн дүгнэх эрх олгох (§2 delegation) */
    @PostMapping("/task/{taskId}/delegate")
    public BriefingDto addDelegate(@PathVariable Integer taskId, @RequestBody Map<String, Object> body) {
        return service.addDelegate(taskId, intOf(body.get("userId")));
    }

    @DeleteMapping("/task/{taskId}/delegate")
    public BriefingDto removeDelegate(@PathVariable Integer taskId, @RequestParam Integer userId) {
        return service.removeDelegate(taskId, userId);
    }

    /** Сунгах (биелээгүй үүрэгт шинэ 7 хоногийн cycle нэмнэ) */
    @PostMapping("/task/{taskId}/extend")
    public BriefingDto extend(@PathVariable Integer taskId) {
        return service.extend(taskId);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static Integer intOf(Object o) {
        return o == null || o.toString().isBlank() ? null : Integer.valueOf(o.toString());
    }
}
