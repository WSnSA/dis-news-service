package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.Model.WaterHourlyRowDto;
import mn.usug.dis_news_service.Service.WaterHourlyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ws")
@RequiredArgsConstructor
public class WaterHourlyController {

    private final WaterHourlyService service;

    // жишээ: /api/ws/water-hourly?date=2026-02-10&hour=9
    @GetMapping("/water-hourly")
    public List<WaterHourlyRowDto> getWaterHourly(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int hour
    ) {
        return service.getWaterHourly(date, hour);
    }

    // Ээлжийн мэдээний бүрдэл (станц × цаг). жишээ: /api/ws/water-hourly/coverage?date=2026-11-09
    @GetMapping("/water-hourly/coverage")
    public List<Map<String, Object>> getCoverage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return service.getCoverage(date);
    }
}
