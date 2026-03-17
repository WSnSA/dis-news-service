package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DTO.WorkNewsDayRes;
import mn.usug.dis_news_service.DTO.WorkNewsItemCreateReq;
import mn.usug.dis_news_service.Service.NotificationService;
import mn.usug.dis_news_service.Service.UserContext;
import mn.usug.dis_news_service.Service.WorkNewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/work-news")
@RequiredArgsConstructor
public class WorkNewsController {

    private final WorkNewsService service;
    private final NotificationService notificationService;

    @PostMapping("/items")
    public Long createItem(@RequestBody WorkNewsItemCreateReq req) {
        Long id = service.createItem(req);
        notificationService.notifyWorkNews(req.title());
        return id;
    }

    // /api/work-news/day/2026-02-13
    @GetMapping("/day/{date}")
    public ResponseEntity<WorkNewsDayRes> getByDay(@PathVariable String date) {
        try {
            return ResponseEntity.ok(service.getByDay(LocalDate.parse(date)));
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    // /api/work-news/month/2026/02?desc=true
    @GetMapping("/month/{year}/{month}")
    public List<WorkNewsDayRes> getByMonth(
            @PathVariable String year,
            @PathVariable String month,
            @RequestParam(defaultValue = "true") boolean desc
    ) {
        return service.getByMonth(year + "/" + month, desc);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody WorkNewsItemCreateReq req) {
        service.updateItem(id, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        service.deleteItem(id);
        return ResponseEntity.ok().build();
    }
}