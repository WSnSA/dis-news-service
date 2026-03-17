package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.Model.WaterHourlyRowDto;
import mn.usug.dis_news_service.Service.WaterHourlyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
}
