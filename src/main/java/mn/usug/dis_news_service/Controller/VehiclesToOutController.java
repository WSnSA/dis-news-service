package mn.usug.dis_news_service.Controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.VehicleOrderRepository;
import mn.usug.dis_news_service.DTO.VehiclesToOutSaveDto;
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
    private final VehicleOrderRepository orderRepo;

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

    /** Захиалга өгсөн алба тухайн захиалгын хуваарилалтын бүх машины жагсаалтыг харна */
    @GetMapping("/by-order/{orderId}")
    public List<VehiclesToOutRowDto> getByOrderId(@PathVariable Integer orderId) {
        return service.findRowsByOrderId(orderId);
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

    /** Нэг захиалгын бүртгэл: POST /api/vehicles-to-out/save */
    @PostMapping("/save")
    public VehiclesToOut save(@RequestBody VehiclesToOutSaveDto dto) {
        VehiclesToOut entity = buildEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setCreatedBy(UserContext.getUserId());

        VehiclesToOut saved = service.save(entity);
        markOrderDispatched(dto.getVehicleOrderId());

        String info = (saved.getVehicleRegistrationNumber() != null ? saved.getVehicleRegistrationNumber() : "")
                + (saved.getDriverName() != null ? " — " + saved.getDriverName() : "");
        notificationService.notifyVehicleOut(info.trim());

        return saved;
    }

    /**
     * Олон захиалгыг нэг дор баталгаажуулах: POST /api/vehicles-to-out/bulk-save
     * Автобааз-ийн ажилтан сонгосон захиалгуудаа нэг дор илгээнэ.
     */
    @PostMapping("/bulk-save")
    @Transactional
    public void bulkSave(@RequestBody List<VehiclesToOutSaveDto> dtos) {
        int userId = UserContext.getUserId();
        LocalDateTime now = LocalDateTime.now();

        for (VehiclesToOutSaveDto dto : dtos) {
            VehiclesToOut entity = buildEntity(dto);
            entity.setCreatedDate(now);
            entity.setCreatedBy(userId);
            service.save(entity);
            markOrderDispatched(dto.getVehicleOrderId());
        }

        notificationService.notifyVehicleOut(dtos.size() + " машин захиалга хуваарилагдлаа ");
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

    /* ===== helpers ===== */

    private VehiclesToOut buildEntity(VehiclesToOutSaveDto dto) {
        VehiclesToOut e = new VehiclesToOut();
        e.setVehicleOrderId(dto.getVehicleOrderId());
        e.setDepartment(dto.getDepartment());
        e.setWorkDescription(dto.getWorkDescription());
        e.setVehicleMechanism(dto.getVehicleMechanism());
        e.setVehicleRegistrationNumber(dto.getVehicleRegistration());
        e.setDriverPhoneNumber(dto.getPhone());
        e.setDriverName(dto.getDriverName());
        e.setActiveFlag(1);
        e.setStatus(1);
        return e;
    }

    /**
     * vehicle_order.status = 2 (ажилд гарсан / хуваарилагдсан)
     *   0 = хүлээгдэж байна
     *   1 = 502 баталгаажуулсан
     *   2 = 507 хуваарилсан → ажилд гарсан
     */
    private void markOrderDispatched(Integer vehicleOrderId) {
        if (vehicleOrderId == null) return;
        orderRepo.findById(vehicleOrderId.longValue()).ifPresent(order -> {
            order.setStatus(2);
            orderRepo.save(order);
        });
    }
}
