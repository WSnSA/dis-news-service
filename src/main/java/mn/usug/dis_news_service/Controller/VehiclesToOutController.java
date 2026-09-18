package mn.usug.dis_news_service.Controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.VehicleOrderRepository;
import mn.usug.dis_news_service.DAO.VehiclesToOutCancelRepository;
import mn.usug.dis_news_service.DAO.VehiclesToOutRepository;
import mn.usug.dis_news_service.DTO.VehiclesToOutSaveDto;
import mn.usug.dis_news_service.Entity.VehiclesToOut;
import mn.usug.dis_news_service.Entity.VehiclesToOutCancel;
import mn.usug.dis_news_service.Model.VehiclesToOutRowDto;
import mn.usug.dis_news_service.Service.Imp.VehiclesToOutServiceImpl;
import mn.usug.dis_news_service.Service.NotificationService;
import mn.usug.dis_news_service.Service.UserContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vehicles-to-out")
@RequiredArgsConstructor
public class VehiclesToOutController {

    private final VehiclesToOutServiceImpl service;
    private final NotificationService notificationService;
    private final VehicleOrderRepository orderRepo;
    private final VehiclesToOutCancelRepository cancelRepo;
    private final VehiclesToOutRepository vehiclesToOutRepo;

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

    /**
     * Машины хуваарь — өгөгдсөн өдрөөс хойшхи бүх захиалга, машинаар нь.
     * GET /api/vehicles-to-out/upcoming?from=2026-09-18
     *
     * "Ажилд гарах" таб нэг өдрөөр шүүдэг тул 15-19-нд захиалсан машиныг олохын
     * тулд өдөр бүрийг гүйлгэх шаардлагатай байсныг орлоно.
     */
    @GetMapping("/upcoming")
    public List<mn.usug.dis_news_service.Model.VehicleScheduleDto> upcoming(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from
    ) {
        return service.findUpcoming(from != null ? from : LocalDate.now());
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
    @Transactional
    public void delete(@PathVariable Integer id) {
        VehiclesToOut row = service.findById(id);
        Integer orderId = row != null ? row.getVehicleOrderId() : null;

        // Хуваарилалт устахад түүний өдрийн цуцлалтууд утгагүй болно
        cancelRepo.deleteByVehiclesToOutId(id);
        service.deleteById(id);

        releaseOrderIfUnassigned(orderId);
    }

    /**
     * Захиалгын СҮҮЛЧИЙН хуваарилалт устсан бол захиалгыг "баталгаажсан" (status=1)
     * төлөвт буцаана.
     *
     * Ингэхгүй бол захиалга status=2 хэвээр үлдэж, "Баталгаажсан" табад
     * харагдахгүй болохоор дахин машин хуваарилах боломжгүй болдог байсан.
     * Захиалгад өөр машин үлдсэн бол хэсэгчлэн хуваарилагдсан хэвээр тул хөндөхгүй.
     */
    private void releaseOrderIfUnassigned(Integer vehicleOrderId) {
        if (vehicleOrderId == null) return;

        // deleteById-ийн дараа шууд асуух тул устгалыг эхлээд бичүүлнэ
        vehiclesToOutRepo.flush();
        if (!vehiclesToOutRepo.findAllByVehicleOrderIdOrderByIdAsc(vehicleOrderId).isEmpty()) return;

        orderRepo.findById(vehicleOrderId.longValue()).ifPresent(order -> {
            if (order.getStatus() == null || order.getStatus() != 2) return;
            order.setStatus(1);
            order.setUpdatedBy(UserContext.getUserId());
            order.setUpdatedDate(LocalDateTime.now());
            orderRepo.save(order);
            notificationService.notifyVehicleOrder(
                    "Машины хуваарилалт цуцлагдлаа — дахин хуваарилах шаардлагатай",
                    order.getAssignedDepartmentId());
        });
    }

    /* ===== нэг өдрийн цуцлалт ===== */

    /**
     * Хуваарилалтыг ЗӨВХӨН нэг өдөр цуцална: POST /api/vehicles-to-out/{id}/cancel-day
     * Body: { "date": "2026-09-02", "reason": "Машин засвартай" }
     *
     * Бусад өдрийн хуваарилалт хэвээр үлдэнэ. Тухайн өдөр машин захиалгын
     * жагсаалтад дахин сул болж харагдана.
     */
    @PostMapping("/{id}/cancel-day")
    @Transactional
    public void cancelDay(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        VehiclesToOut row = service.findById(id);
        if (row == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Хуваарилалт олдсонгүй");
        }

        String reason = body.getOrDefault("reason", "").trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("Цуцлах шалтгаан заавал бичнэ үү");
        }

        LocalDate date = parseDate(body.get("date"));
        assertWithinOrderRange(row, date);

        // Давхар цуцлахад шалтгааныг нь шинэчилнэ (uq_vto_cancel түлхүүр зөрчихгүй)
        VehiclesToOutCancel cancel = cancelRepo
                .findByVehiclesToOutIdAndCancelDate(id, date)
                .orElseGet(VehiclesToOutCancel::new);
        cancel.setVehiclesToOutId(id);
        cancel.setCancelDate(date);
        cancel.setReason(reason);
        cancel.setCreatedBy(UserContext.getUserId());
        cancel.setCreatedDate(LocalDateTime.now());
        cancelRepo.save(cancel);

        if (row.getVehicleOrderId() != null) {
            orderRepo.findById(row.getVehicleOrderId().longValue()).ifPresent(order ->
                notificationService.notifyVehicleOrder(
                        "%s — %s өдрийн машин цуцлагдлаа".formatted(
                                row.getVehicleRegistrationNumber() != null ? row.getVehicleRegistrationNumber() : "Машин",
                                date),
                        order.getAssignedDepartmentId())
            );
        }
    }

    /** Цуцлалтыг буцаана: DELETE /api/vehicles-to-out/{id}/cancel-day?date=2026-09-02 */
    @DeleteMapping("/{id}/cancel-day")
    @Transactional
    public void restoreDay(
            @PathVariable Integer id,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        cancelRepo.findByVehiclesToOutIdAndCancelDate(id, date).ifPresent(cancelRepo::delete);
    }

    /** Цуцлах өдөр захиалгын хугацаанд багтаж байгаа эсэх */
    private void assertWithinOrderRange(VehiclesToOut row, LocalDate date) {
        if (row.getVehicleOrderId() == null) return;   // legacy мөр — шалгах захиалга алга
        orderRepo.findById(row.getVehicleOrderId().longValue()).ifPresent(order -> {
            LocalDate start = order.getStartDate() != null ? order.getStartDate() : order.getOrderDate();
            if (start == null) return;
            LocalDate end = order.getEndDate() != null ? order.getEndDate() : start;
            if (end.isBefore(start)) end = start;
            if (date.isBefore(start) || date.isAfter(end)) {
                throw new IllegalArgumentException(
                        "%s нь захиалгын хугацаанаас (%s — %s) гадуур байна".formatted(date, start, end));
            }
        });
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Цуцлах өдрийг заана уу");
        }
        try {
            return LocalDate.parse(raw.trim().substring(0, 10));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Огноо буруу байна: " + raw);
        }
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
