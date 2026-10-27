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
