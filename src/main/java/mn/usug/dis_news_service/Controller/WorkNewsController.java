package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DTO.WorkNewsDayRes;
import mn.usug.dis_news_service.DTO.WorkNewsItemCreateReq;
import mn.usug.dis_news_service.Service.WorkNewsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/work-news")
@RequiredArgsConstructor
public class WorkNewsController {

    private final WorkNewsService service;

    // TODO: танай auth-аас userId авдаг бол түүгээр солино
    private Long mockUserId() { return 1L; }

    @PostMapping("/items")
    public Long createItem(@RequestBody WorkNewsItemCreateReq req) {
        return service.createItem(req, mockUserId());
    }

    // /api/work-news/day/2026-02-13
    @GetMapping("/day/{date}")
    public WorkNewsDayRes getByDay(@PathVariable String date) {
        return service.getByDay(LocalDate.parse(date));
    }

    // /api/work-news/month/2026/02?desc=true
    @GetMapping("/month/{monthKey}")
    public List<WorkNewsDayRes> getByMonth(
            @PathVariable String monthKey,
            @RequestParam(defaultValue = "true") boolean desc
    ) {
        return service.getByMonth(monthKey, desc);
    }
}