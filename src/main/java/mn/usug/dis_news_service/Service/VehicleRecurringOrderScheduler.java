package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Давтамжит захиалгыг автоматаар үүсгэх товлол.
 * Долоо хоног бүрийн Баасан гарагт (5 дахь өдөр) 15:00-д ажиллаж, идэвхтэй загвар
 * бүрээс ирэх 7 хоногийн (Даваа–Баасан) сонгосон гаригуудад захиалга үүсгэнэ.
 *
 * Cron-ийг application.properties-ээр тохируулж болно (vehicle-recurring.cron.generate).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleRecurringOrderScheduler {

    private static final String UB = "Asia/Ulaanbaatar";
    private static final ZoneId UB_ZONE = ZoneId.of(UB);

    private final VehicleRecurringOrderService service;

    @Scheduled(cron = "${vehicle-recurring.cron.generate:0 0 15 ? * FRI}", zone = UB)
    public void generateNextWeek() {
        LocalDate monday = service.nextWeekMonday(LocalDate.now(UB_ZONE));
        int created = service.generateForWeek(monday);
        log.info("Давтамжит захиалгын товлол ажиллав: {} захиалга үүслээ (долоо хоног {}-с эхэлнэ)", created, monday);
    }
}
