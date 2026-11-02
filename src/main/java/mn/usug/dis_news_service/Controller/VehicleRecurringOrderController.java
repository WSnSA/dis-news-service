package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DTO.VehicleRecurringOrderDto;
import mn.usug.dis_news_service.DTO.VehicleRecurringOrderSaveDto;
import mn.usug.dis_news_service.Service.VehicleRecurringOrderService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * Давтамжит (долоо хоног тутмын) машин/механизм захиалгын загвар.
 * Эрхийг "Давтамжит захиалга" цэсний permission (menuId × canView/canEdit)-ээр олгоно.
 */
@RestController
@RequestMapping("/ref/vehicle-recurring")
@RequiredArgsConstructor
public class VehicleRecurringOrderController {

    private final VehicleRecurringOrderService service;

    @GetMapping("/getAll")
    public List<VehicleRecurringOrderDto> getAll() {
        return service.getAll();
    }

    /** Одоогийн хэрэглэгч давтамжит захиалга үүсгэх эрхтэй эсэх (frontend "Давтах" товч gate) */
    @GetMapping("/my-access")
    public Map<String, Object> myAccess() {
        return Map.of("canUse", service.canUse(mn.usug.dis_news_service.Service.UserContext.getUserId()));
    }

    /** Байгаа захиалгыг 7 хоног тутам давтагддаг загвар болгоно ("Давтах"). */
    @PostMapping("/from-order/{orderId}")
    public Map<String, Object> fromOrder(@PathVariable Long orderId,
                                         @RequestBody(required = false) Map<String, Object> body) {
        List<Integer> weekdays = null;
        if (body != null && body.get("weekdays") instanceof List<?> raw) {
            weekdays = raw.stream()
                    .map(x -> x == null ? null : Integer.valueOf(x.toString()))
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());
        }
        Long id = service.createFromOrder(orderId, weekdays);
        return Map.of("id", id);
    }

    /** Загвар үүсгэх/засах (id=null бол шинэ) */
    @PostMapping("/save")
    public void save(@RequestBody VehicleRecurringOrderSaveDto dto) {
        service.save(dto);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /** Ирэх 7 хоногийн захиалгыг гараар (товлол хүлээлгүй) үүсгэнэ. Үүссэн тоог буцаана. */
    @PostMapping("/generate")
    public Map<String, Object> generate() {
        LocalDate monday = service.nextWeekMonday(LocalDate.now(ZoneId.of("Asia/Ulaanbaatar")));
        int created = service.generateForWeek(monday);
        return Map.of("weekMonday", monday.toString(), "created", created);
    }
}
