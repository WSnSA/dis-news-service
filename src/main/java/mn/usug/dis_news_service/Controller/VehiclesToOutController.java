package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.Entity.VehiclesToOut;
import mn.usug.dis_news_service.Model.VehiclesToOutRowDto;
import mn.usug.dis_news_service.Service.Imp.VehiclesToOutServiceImpl;
import mn.usug.dis_news_service.Service.NotificationService;
import mn.usug.dis_news_service.Service.UserContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/vehicles-to-out")
@RequiredArgsConstructor
public class VehiclesToOutController {

    private final VehiclesToOutServiceImpl service;
    private final NotificationService notificationService;

    // ✅ Front чинь яг ингэж дуудаж байгаа: /api/vehicles-to-out?date=2026-02-12
    @GetMapping
    public List<VehiclesToOutRowDto> getByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return service.findRowsByDate(date);
    }

    @GetMapping("/{id}")
    public VehiclesToOut getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public VehiclesToOut create(@RequestBody VehiclesToOut vehiclesToOut) {
        vehiclesToOut.setCreatedDate(LocalDateTime.now());
        vehiclesToOut.setCreatedBy(UserContext.getUserId());
        VehiclesToOut saved = service.save(vehiclesToOut);
        String info = saved.getVehicleRegistrationNumber() != null
                ? saved.getVehicleRegistrationNumber() + (saved.getDriverName() != null ? " — " + saved.getDriverName() : "")
                : saved.getDriverName();
        notificationService.notifyVehicleOut(info);
        return saved;
    }

    @PutMapping("/{id}")
    public VehiclesToOut update(@PathVariable Integer id, @RequestBody VehiclesToOut vehiclesToOut) {
        vehiclesToOut.setId(id);
        vehiclesToOut.setUpdatedDate(LocalDateTime.now());
        vehiclesToOut.setUpdatedBy(UserContext.getUserId());
        return service.save(vehiclesToOut);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.deleteById(id);
    }
}
