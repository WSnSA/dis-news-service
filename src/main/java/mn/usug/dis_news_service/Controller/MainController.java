package mn.usug.dis_news_service.Controller;

import mn.usug.dis_news_service.Model.HourlyReport;
import mn.usug.dis_news_service.Service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/main")
public class MainController {
    @Autowired
    MainService mainService;

    @PostMapping("/hourly")
    public ResponseEntity regHourly(@RequestBody HourlyReport report) {
        return mainService.regHourly(report);
    }

    @GetMapping("/getHourlyHistory")
    public ResponseEntity getHourlyHistory(@RequestParam("menuId") Integer menuId,
                                           @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                           @RequestParam("hour") Integer hour) {
        return mainService.getHourlyHistory(menuId, date, hour);
    }

    @GetMapping("/getDailyReportList")
    public ResponseEntity getDailyReportList(@RequestParam("menuId") Integer menuId,
                                         @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return mainService.getDailyReportList(menuId, date);
    }

    @GetMapping("/getDailyReport")
    public ResponseEntity getDailyReport(@RequestParam("menuId") Integer menuId,
                                         @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return mainService.getDailyReport(menuId, date);
    }

    @GetMapping("/getMonthlyReport")
    public ResponseEntity getMonthlyReport(
            @RequestParam("menuId") Integer menuId,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        return mainService.getMonthlyReport(menuId, year, month);
    }

}
