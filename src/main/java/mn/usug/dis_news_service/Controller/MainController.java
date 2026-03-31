package mn.usug.dis_news_service.Controller;

import mn.usug.dis_news_service.DAO.MenuDAO;
import mn.usug.dis_news_service.Model.HourlyReport;
import mn.usug.dis_news_service.Model.HourlySecondReport;
import mn.usug.dis_news_service.Service.MainService;
import mn.usug.dis_news_service.Service.MenuService;
import mn.usug.dis_news_service.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/main")
public class MainController {
    @Autowired
    MainService mainService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private MenuDAO menuDAO;

    @PostMapping("/hourly")
    public ResponseEntity regHourly(@RequestBody HourlyReport report) {
        ResponseEntity result = mainService.regHourly(report);
        if (result.getStatusCode().is2xxSuccessful()) {
            String stationName = menuDAO.findById(report.getMenuId())
                    .map(m -> m.getName())
                    .orElse("Станц");
            notificationService.notifyWsHourly(stationName, report.getHour());
        }
        return result;
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
        return ResponseEntity.ok().body(mainService.getDailyReport(menuId, date));
    }

    @GetMapping("/getMonthlyReport")
    public ResponseEntity getMonthlyReport(
            @RequestParam("menuId") Integer menuId,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        return mainService.getMonthlyReport(menuId, year, month);
    }

    @GetMapping("/getMarkers")
    public ResponseEntity getMarkers(){
        return menuService.getMarkers();
    }

    @GetMapping("/getDailySummary")
    public ResponseEntity getDailySummary(
            @RequestParam("type") Integer type,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date
    ){
        return menuService.getDailySummary(type, date);
    }

    @GetMapping("/dailyFmByHour")
    public ResponseEntity getDailyFmByHour(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return mainService.getDailyFmByHour(date);
    }

}
