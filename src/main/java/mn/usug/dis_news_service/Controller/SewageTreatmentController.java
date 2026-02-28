package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DTO.SewageTreatmentSummaryDto;
import mn.usug.dis_news_service.Service.SewageTreatmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sewage-treatment")
@RequiredArgsConstructor
public class SewageTreatmentController {

    private final SewageTreatmentService service;

    @GetMapping("/summary")
    public List<SewageTreatmentSummaryDto> summary(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "hour", defaultValue = "0") int hour
    ) {
        return service.getSummary(date, hour);
    }
}


