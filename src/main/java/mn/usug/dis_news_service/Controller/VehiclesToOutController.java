package mn.usug.dis_news_service.Controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.VehicleOrderRepository;
import mn.usug.dis_news_service.DTO.VehiclesToOutSaveDto;
import mn.usug.dis_news_service.Entity.VehicleOrder;
import mn.usug.dis_news_service.Entity.VehiclesToOut;
import mn.usug.dis_news_service.Model.VehiclesToOutRowDto;
import mn.usug.dis_news_service.Service.Imp.VehiclesToOutServiceImpl;
import mn.usug.dis_news_service.Service.NotificationService;
import mn.usug.dis_news_service.Model.BusyVehicleDto;
import mn.usug.dis_news_service.Service.TimeSlot;
import mn.usug.dis_news_service.Service.UserContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** Нэг машины (улсын дугаараар) захиалгаар ажилд гарсан түүх */
    @GetMapping("/by-vehicle")
    public List<VehiclesToOutRowDto> getByVehicle(@RequestParam String plate) {
        return service.findRowsByPlate(plate);
    }

    /** Машин хуваарилалтын статистик — сар/улирал/жил + алба бүрээр төрлөөр */
    @GetMapping("/stats")
    public mn.usug.dis_news_service.Model.DispatchStatsDto stats(@RequestParam int year) {
        return service.getStats(year);
    }

    /** Тайлангийн тоо бүрийн ард байгаа дэлгэрэнгүй мөрүүд (нэг жилээр) — frontend талд шүүж экспортолно */
    @GetMapping("/stats-detail")
    public List<mn.usug.dis_news_service.Model.DispatchDetailDto> statsDetail(@RequestParam int year) {
        return service.getStatsDetail(year);
    }

    /**
     * Сонгосон захиалгуудын хугацаа+ээлжид аль хэдийн оногдсон машинуудыг буцаана.
     * Хуваарилах форм эдгээрийг dropdown-оос хасна.
     * Body: захиалгын id-ууд  [12, 13]  →  { "12": [BusyVehicleDto…], "13": [] }
     */
    @PostMapping("/busy-vehicles")
    public Map<Long, List<BusyVehicleDto>> busyVehicles(@RequestBody List<Long> orderIds) {
        return service.findBusyVehicles(orderIds);
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
        assertNoDoubleBooking(List.of(dto));
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

        assertNoDoubleBooking(dtos);

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
        VehiclesToOut existing = service.findById(id);
        existing.setVehicleMechanism(vehiclesToOut.getVehicleMechanism());
        existing.setVehicleRegistrationNumber(vehiclesToOut.getVehicleRegistrationNumber());
        existing.setDriverPhoneNumber(vehiclesToOut.getDriverPhoneNumber());
        existing.setDriverName(vehiclesToOut.getDriverName());
        existing.setUpdatedDate(LocalDateTime.now());
        existing.setUpdatedBy(UserContext.getUserId());
        VehiclesToOut saved = service.save(existing);

        if (existing.getVehicleOrderId() != null) {
            orderRepo.findById(existing.getVehicleOrderId().longValue()).ifPresent(order ->
                notificationService.notifyVehicleOrder("Машины хуваарилалт засварлагдлаа", order.getAssignedDepartmentId())
            );
        }
        return saved;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.deleteById(id);
    }

    /* ===== давхар хуваарилалтын шалгалт ===== */

    /**
     * Нэг машин ижил хугацаанд хоёр захиалгад орохоос сэргийлнэ.
     * Хоёр эх сурвалжийг шалгана:
     *   1. Өгөгдлийн санд аль хэдийн хадгалагдсан хуваарилалт
     *   2. Яг энэ хүсэлтийн дотор давхардсан улсын дугаар
     * Зөрчил илэрвэл юу ч хадгалахгүйгээр 409 буцаана.
     */
    private void assertNoDoubleBooking(List<VehiclesToOutSaveDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        // Захиалга бүрийн завгүй машиныг нэг л удаа уншина
        Map<Long, List<BusyVehicleDto>> busyByOrder = new HashMap<>();
        // Энэ хүсэлтийн дотор улсын дугаар бүрийг аль захиалгуудад өгснийг хөтөлнө.
        // Жагсаалтаар хадгалах учир нь: ижил машин 3 мөрөнд орвол (Даваа, Мягмар, Даваа)
        // зөвхөн сүүлийнхтэй харьцуулбал эхний мөргөлдөөн алдагдана.
        Map<String, List<VehiclesToOutSaveDto>> takenInBatch = new HashMap<>();
        List<String> conflicts = new ArrayList<>();

        for (VehiclesToOutSaveDto dto : dtos) {
            String key = service.plateKey(dto.getVehicleRegistration());
            if (key.isEmpty() || dto.getVehicleOrderId() == null) continue;

            Long orderId = dto.getVehicleOrderId().longValue();

            // 1. Хадгалагдсан хуваарилалттай мөргөлдөж байна уу
            List<BusyVehicleDto> busy = busyByOrder.computeIfAbsent(orderId, service::findBusyVehicles);
            BusyVehicleDto who = busy.stream()
                    .filter(b -> service.plateKey(b.getPlate()).equals(key))
                    .findFirst().orElse(null);

            if (who != null) {
                conflicts.add(describeConflict(dto.getVehicleRegistration(), who));
                continue;
            }

            // 2. Энэ хүсэлтийн дотор давхардсан уу (огноо + ээлж огтлолцсон бол)
            List<VehiclesToOutSaveDto> earlier = takenInBatch.computeIfAbsent(key, k -> new ArrayList<>());
            if (earlier.stream().anyMatch(prev -> overlapsWithinBatch(prev, dto))) {
                conflicts.add("%s — энэ жагсаалтад хоёр удаа сонгогдсон байна".formatted(dto.getVehicleRegistration()));
                continue;
            }
            earlier.add(dto);
        }

        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Давхар хуваарилалт: " + String.join("; ", conflicts));
        }
    }

    /** Нэг хүсэлтийн дотор хоёр мөр ижил машиныг зэрэг эзэлж байна уу */
    private boolean overlapsWithinBatch(VehiclesToOutSaveDto a, VehiclesToOutSaveDto b) {
        // Нэг захиалгын дотор нэг машиныг хоёр удаа өгөх нь үргэлж алдаа
        if (java.util.Objects.equals(a.getVehicleOrderId(), b.getVehicleOrderId())) return true;

        VehicleOrder oa = loadOrder(a.getVehicleOrderId());
        VehicleOrder ob = loadOrder(b.getVehicleOrderId());
        if (oa == null || ob == null) return true;   // тодорхойгүй бол болгоомжилно

        LocalDate aStart = firstDate(oa), aEnd = lastDate(oa);
        LocalDate bStart = firstDate(ob), bEnd = lastDate(ob);
        if (aStart == null || bStart == null) return true;

        boolean dateOverlap = !aStart.isAfter(bEnd) && !bStart.isAfter(aEnd);
        return dateOverlap && TimeSlot.overlaps(oa.getTimeSlot(), ob.getTimeSlot());
    }

    private String describeConflict(String plate, BusyVehicleDto who) {
        if (who == null) return "%s — өөр захиалгад оногдсон байна".formatted(plate);
        return "%s — %s (%s, %s%s)".formatted(
                plate,
                who.getDepartment() == null || who.getDepartment().isBlank() ? "өөр алба" : who.getDepartment(),
                who.getTimeSlotLabel(),
                who.getStartDate(),
                who.getEndDate() != null && !who.getEndDate().equals(who.getStartDate())
                        ? " → " + who.getEndDate() : "");
    }

    private VehicleOrder loadOrder(Integer id) {
        return id == null ? null : orderRepo.findById(id.longValue()).orElse(null);
    }

    private LocalDate firstDate(VehicleOrder o) {
        return o.getStartDate() != null ? o.getStartDate() : o.getOrderDate();
    }

    private LocalDate lastDate(VehicleOrder o) {
        LocalDate start = firstDate(o);
        LocalDate end = o.getEndDate() != null ? o.getEndDate() : start;
        return (end == null || (start != null && end.isBefore(start))) ? start : end;
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

        // order-аас orderType хуулж тавина — frontend дээр car/truck шүүхэд
        if (dto.getVehicleOrderId() != null) {
            orderRepo.findById(dto.getVehicleOrderId().longValue())
                    .ifPresent(o -> e.setOrderType(o.getOrderType()));
        }
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
            // Боломжгүй (status=3) байсан захиалга дахин хуваарилагдвал шалтгааныг цэвэрлэнэ
            order.setDeclineReason(null);
            orderRepo.save(order);
        });
    }
}
