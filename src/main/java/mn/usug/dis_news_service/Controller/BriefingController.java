package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DTO.BriefingDto;
import mn.usug.dis_news_service.DTO.BriefingSaveDto;
import mn.usug.dis_news_service.Service.BriefingService;
import org.springframework.web.bind.annotation.*;

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
        String folderId    = str(body.get("folderId"));
        String objectName  = str(body.get("objectName"));
        String fileName    = str(body.get("fileName"));
        String contentType = str(body.get("contentType"));
        Long   fileSize    = body.get("fileSize") != null
                ? Long.valueOf(body.get("fileSize").toString()) : null;
        return service.addEvidence(folderId, objectName, fileName, contentType, fileSize);
    }

    @DeleteMapping("/evidence/{id}")
    public void deleteEvidence(@PathVariable Integer id) {
        service.deleteEvidence(id);
    }

    /** Дүгнэх (assigner, нэг ерөнхий оноо 0-100) */
    @PostMapping("/cycle/{cycleId}/score")
    public BriefingDto score(@PathVariable Integer cycleId, @RequestBody Map<String, Object> body) {
        Integer score = body.get("score") != null
                ? Integer.valueOf(body.get("score").toString()) : null;
        return service.score(cycleId, score);
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
}
