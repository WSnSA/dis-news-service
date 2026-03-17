package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DTO.SewageTreatmentSaveReq;
import mn.usug.dis_news_service.DTO.SewageTreatmentSummaryDto;
import mn.usug.dis_news_service.Entity.Menu;
import mn.usug.dis_news_service.Entity.SewageTreatment;
import mn.usug.dis_news_service.Service.SewageTreatmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sewage-treatment")
@RequiredArgsConstructor
public class SewageTreatmentController {

    private final SewageTreatmentService service;

    /** Нэгтгэл (summary) — огноо, цагаар */
    @GetMapping("/summary")
    public List<SewageTreatmentSummaryDto> summary(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "hour", defaultValue = "0") int hour) {
        return service.getSummary(date, hour);
    }

    /** Цэвэрлэх станцуудын жагсаалт (dropdown-д ашиглана) */
    @GetMapping("/stations")
    public List<Menu> getStations() {
        return service.getStations();
    }

    /** Тухайн станц, огноо, цагийн бүртгэлийг буцаана (засах үед) */
    @GetMapping("/get")
    public SewageTreatmentSummaryDto getOne(
            @RequestParam("stationId") int stationId,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "hour", defaultValue = "0") int hour) {
        return service.getByStationAndHour(stationId, date, hour);
    }

    /** Тухайн станцын өдрийн бүх цагийн бүртгэл (хяналтын grid-д) */
    @GetMapping("/history")
    public List<Map<String, Object>> getHistory(
            @RequestParam("stationId") int stationId,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return service.getHistory(stationId, date);
    }

    /** Хадгалах (insert / update) */
    @PostMapping("/save")
    public SewageTreatment save(@RequestBody SewageTreatmentSaveReq req) {
        return service.save(req);
    }
}
