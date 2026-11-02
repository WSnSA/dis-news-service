package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mn.usug.dis_news_service.DAO.*;
import mn.usug.dis_news_service.DTO.VehicleOrderItemSaveDto;
import mn.usug.dis_news_service.DTO.VehicleRecurringOrderDto;
import mn.usug.dis_news_service.DTO.VehicleRecurringOrderSaveDto;
import mn.usug.dis_news_service.Entity.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Давтамжит (долоо хоног тутмын) машин/механизм захиалгын загварын логик +
 * ирэх 7 хоногийн (Даваа–Баасан) захиалгыг автоматаар үүсгэх.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleRecurringOrderService {

    private final VehicleRecurringOrderRepository recRepo;
    private final VehicleRecurringOrderItemRepository recItemRepo;
    private final VehicleOrderRepository orderRepo;
    private final VehicleOrderItemRepository orderItemRepo;
    private final VehicleTypeRepository vehicleTypeRepo;
    private final DepartmentDAO departmentRepo;
    private final UserDAO userRepo;
    private final VehicleOrderApprovalPolicy approvalPolicy;
    private final MenuDAO menuDAO;
    private final PermissionDAO permissionDAO;

    /** Давтамжит захиалгын цэсний path (эрх шалгахад ашиглана) */
    private static final String MENU_PATH = "vehicle-recurring";

    private static final ZoneId UB = ZoneId.of("Asia/Ulaanbaatar");

    private LocalDateTime now() { return LocalDateTime.now(UB); }

    // ── Унших ────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VehicleRecurringOrderDto> getAll() {
        List<VehicleRecurringOrder> list = recRepo.findByActiveFlagOrderByIdDesc(1);
        if (list.isEmpty()) return List.of();

        Map<Integer, String> depMap = departmentRepo.findAll().stream()
                .collect(Collectors.toMap(Department::getDepId, Department::getDepName, (a, b) -> a));
        Map<Integer, String> userMap = userRepo.findAll().stream()
                .collect(Collectors.toMap(User::getId, this::fullName, (a, b) -> a));
        Map<Integer, String> typeMap = vehicleTypeRepo.findAll().stream()
                .collect(Collectors.toMap(VehicleType::getId, VehicleType::getName, (a, b) -> a));

        return list.stream().map(r -> {
            VehicleRecurringOrderDto d = new VehicleRecurringOrderDto();
            d.setId(r.getId());
            d.setWorkDescription(r.getWorkDescription());
            d.setAssignedDepartmentId(r.getAssignedDepartmentId());
            d.setAssignedDepartmentName(r.getAssignedDepartmentId() != null ? depMap.get(r.getAssignedDepartmentId()) : null);
            d.setAssignedEmployeeId(r.getAssignedEmployeeId());
            d.setAssignedEmployeeName(r.getAssignedEmployeeId() != null ? userMap.get(r.getAssignedEmployeeId()) : null);
            d.setOrderType(r.getOrderType());
            d.setPickupLocation(r.getPickupLocation());
            d.setDropoffLocation(r.getDropoffLocation());
            d.setPassengerCount(r.getPassengerCount());
            d.setRequestedTime(r.getRequestedTime());
            d.setWeekdays(parseWeekdays(r.getWeekdays()));
            d.setActiveFlag(r.getActiveFlag());
            d.setCreatedBy(r.getCreatedBy());
            d.setCreatedByName(r.getCreatedBy() != null ? userMap.get(r.getCreatedBy()) : null);
            d.setCreatedDate(r.getCreatedDate());
            d.setItems(recItemRepo.findByRecurringId(r.getId()).stream().map(it -> {
                VehicleRecurringOrderDto.Item i = new VehicleRecurringOrderDto.Item();
                i.setVehicleTypeId(it.getVehicleTypeId());
                i.setVehicleTypeName(it.getVehicleTypeId() != null ? typeMap.get(it.getVehicleTypeId()) : null);
                i.setQty(it.getQty());
                i.setOtherText(it.getOtherText());
                return i;
            }).collect(Collectors.toList()));
            return d;
        }).collect(Collectors.toList());
    }

    // ── Хадгалах / засах ──────────────────────────────────────────────────────────

    @Transactional
    public void save(VehicleRecurringOrderSaveDto dto) {
        Integer actorId = UserContext.getUserId();
        if (dto.getWeekdays() == null || dto.getWeekdays().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Давтагдах гаригийг 1-с доошгүй сонгоно");
        for (Integer d : dto.getWeekdays())
            if (d == null || d < 1 || d > 5)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Гариг зөвхөн Даваа–Баасан (1–5) байна");

        boolean isNew = dto.getId() == null;
        VehicleRecurringOrder r = isNew ? new VehicleRecurringOrder()
                : recRepo.findById(dto.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Загвар олдсонгүй"));

        r.setWorkDescription(dto.getWorkDescription());
        r.setAssignedDepartmentId(dto.getAssignedDepartmentId());
        r.setAssignedEmployeeId(dto.getAssignedEmployeeId());
        Integer orderType = dto.getOrderType() != null ? dto.getOrderType() : 0;
        r.setOrderType(orderType);
        r.setPickupLocation(dto.getPickupLocation());
        r.setDropoffLocation(dto.getDropoffLocation());
        r.setPassengerCount(dto.getPassengerCount());
        r.setRequestedTime(dto.getRequestedTime());
        r.setWeekdays(dto.getWeekdays().stream().distinct().sorted()
                .map(String::valueOf).collect(Collectors.joining(",")));
        r.setActiveFlag(dto.getActiveFlag() != null ? dto.getActiveFlag() : 1);
        r.setUpdatedBy(actorId);
        r.setUpdatedDate(now());
        if (isNew) {
            r.setCreatedBy(actorId);
            r.setCreatedDate(now());
        }
        VehicleRecurringOrder saved = recRepo.save(r);

        // Загварын тээврийн төрлүүдийг бүрэн солино
        recItemRepo.deleteByRecurringId(saved.getId());
        if (dto.getVehicles() != null) {
            for (VehicleOrderItemSaveDto itemDto : dto.getVehicles()) {
                if (itemDto.getVehicleTypeId() == null) continue;
                VehicleType type = vehicleTypeRepo.findById(itemDto.getVehicleTypeId()).orElse(null);
                if (type == null) continue;
                VehicleRecurringOrderItem it = new VehicleRecurringOrderItem();
                it.setRecurringId(saved.getId());
                it.setVehicleTypeId(type.getId());
                if ("Бусад".equals(type.getName())) {
                    it.setOtherText(itemDto.getOtherText());
                    it.setQty(null);
                } else {
                    it.setQty(itemDto.getQty());
                    it.setOtherText(null);
                }
                recItemRepo.save(it);
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        VehicleRecurringOrder r = recRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Загвар олдсонгүй"));
        r.setActiveFlag(0);
        r.setUpdatedBy(UserContext.getUserId());
        r.setUpdatedDate(now());
        recRepo.save(r);
    }

    // ── Эрх (permission matrix: "Давтамжит захиалга" цэсний canEdit) ──────────────

    /** Тухайн хэрэглэгч давтамжит захиалга үүсгэх/удирдах эрхтэй эсэх */
    @Transactional(readOnly = true)
    public boolean canUse(Integer userId) {
        if (userId == null) return false;
        for (Menu m : menuDAO.findByPath(MENU_PATH)) {
            Permission p = permissionDAO.findByUserIdAndMenuId(userId, m.getId());
            if (p != null && Integer.valueOf(1).equals(p.getCanEdit())) return true;
        }
        return false;
    }

    private void requireCanUse() {
        if (!canUse(UserContext.getUserId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Танд давтамжит захиалга үүсгэх эрх алга");
    }

    // ── Байгаа захиалгаас давтамжит загвар үүсгэх ("Давтах") ──────────────────────

    /**
     * Байгаа нэг захиалгыг (vehicle_order) 7 хоног тутам давтагддаг загвар болгоно.
     * weekdays хоосон бол захиалгын өдрийн гаригаар (Даваа–Баасан) давтана. Тээврийн
     * төрлүүдийг захиалгаас хуулна. Үүсгэсэн загварын id-г буцаана.
     */
    @Transactional
    public Long createFromOrder(Long orderId, List<Integer> weekdays) {
        requireCanUse();
        Integer actorId = UserContext.getUserId();
        VehicleOrder o = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Захиалга олдсонгүй"));

        List<Integer> days = new ArrayList<>();
        if (weekdays != null) {
            for (Integer d : weekdays) if (d != null && d >= 1 && d <= 5) days.add(d);
        }
        if (days.isEmpty()) days.add(deriveWeekday(o)); // захиалгын өдрийн гариг (1–5)
        days = days.stream().distinct().sorted().collect(Collectors.toList());

        VehicleRecurringOrder r = new VehicleRecurringOrder();
        r.setWorkDescription(o.getWorkDescription());
        r.setAssignedDepartmentId(o.getAssignedDepartmentId());
        r.setAssignedEmployeeId(o.getAssignedEmployeeId());
        r.setOrderType(o.getOrderType() != null ? o.getOrderType() : 0);
        r.setPickupLocation(o.getPickupLocation());
        r.setDropoffLocation(o.getDropoffLocation());
        r.setPassengerCount(o.getPassengerCount());
        r.setRequestedTime(o.getRequestedTime());
        r.setWeekdays(days.stream().map(String::valueOf).collect(Collectors.joining(",")));
        r.setActiveFlag(1);
        r.setCreatedBy(actorId);
        r.setCreatedDate(now());
        r.setUpdatedBy(actorId);
        r.setUpdatedDate(now());
        VehicleRecurringOrder saved = recRepo.save(r);

        for (VehicleOrderItem it : orderItemRepo.findByVehicleOrderId(orderId)) {
            VehicleRecurringOrderItem ri = new VehicleRecurringOrderItem();
            ri.setRecurringId(saved.getId());
            ri.setVehicleTypeId(it.getVehicleType() != null ? it.getVehicleType().getId() : null);
            ri.setQty(it.getQty());
            ri.setOtherText(it.getOtherText());
            recItemRepo.save(ri);
        }
        return saved.getId();
    }

    /** Захиалгын өдрийн ISO гариг (1–5). Амралтын өдөр (6/7) бол Даваа (1) руу буулгана. */
    private int deriveWeekday(VehicleOrder o) {
        LocalDate d = o.getStartDate() != null ? o.getStartDate() : o.getOrderDate();
        if (d == null) return 1;
        int dow = d.getDayOfWeek().getValue();   // Даваа=1 ... Ням=7
        return dow <= 5 ? dow : 1;
    }

    // ── Захиалга автоматаар үүсгэх ────────────────────────────────────────────────

    /** Өнөөдрийг багтаасан долоо хоногийн дараагийн 7 хоногийн Даваа. */
    public LocalDate nextWeekMonday(LocalDate today) {
        return today.with(DayOfWeek.MONDAY).plusWeeks(1);
    }

    /**
     * Ирэх 7 хоногийн (weekMonday-с эхлэх) сонгосон гаригуудад идэвхтэй загвар бүрээс
     * захиалга үүсгэнэ. Давхардлыг (recurringId × өдөр) шалгаж алгасдаг тул давтан
     * дуудахад аюулгүй. Үүсгэсэн захиалгын тоог буцаана.
     */
    @Transactional
    public int generateForWeek(LocalDate weekMonday) {
        List<VehicleRecurringOrder> templates = recRepo.findByActiveFlagOrderByIdDesc(1);
        int created = 0;
        for (VehicleRecurringOrder t : templates) {
            List<Integer> dows = parseWeekdays(t.getWeekdays());
            for (Integer dow : dows) {
                if (dow < 1 || dow > 7) continue;
                LocalDate date = weekMonday.plusDays(dow - 1);
                if (orderRepo.existsByRecurringIdAndOrderDateAndActiveFlag(t.getId(), date, 1)) continue;
                createOrderFromTemplate(t, date);
                created++;
            }
        }
        if (created > 0)
            log.info("Давтамжит захиалга: {} захиалга үүслээ (долоо хоног {})", created, weekMonday);
        return created;
    }

    private void createOrderFromTemplate(VehicleRecurringOrder t, LocalDate date) {
        VehicleOrder order = new VehicleOrder();
        order.setWorkDescription(t.getWorkDescription());
        order.setAssignedDepartmentId(t.getAssignedDepartmentId());
        order.setAssignedEmployeeId(t.getAssignedEmployeeId());
        order.setOrderDate(date);
        order.setStartDate(date);
        order.setEndDate(date);
        Integer orderType = t.getOrderType() != null ? t.getOrderType() : 0;
        order.setOrderType(orderType);
        order.setPickupLocation(t.getPickupLocation());
        order.setDropoffLocation(t.getDropoffLocation());
        order.setPassengerCount(t.getPassengerCount());
        order.setRequestedTime(t.getRequestedTime());
        order.setStatus(0);
        // Загвар үүсгэсэн хэрэглэгчийн эрхээр албаны баталгаажуулалтыг шийднэ
        // (save()-тэй ижил логик): механизм → шууд, суудлын машин → шаардлагатай (эс алгасвал)
        boolean skipDeptApproval = orderType == 1 && approvalPolicy.skipsDeptApproval(t.getCreatedBy());
        order.setDeptApproved(orderType == 1 && !skipDeptApproval ? Boolean.FALSE : Boolean.TRUE);
        order.setActiveFlag(1);
        order.setRecurringId(t.getId());
        order.setCreatedBy(t.getCreatedBy());
        order.setCreatedDate(now());
        VehicleOrder saved = orderRepo.save(order);

        for (VehicleRecurringOrderItem recItem : recItemRepo.findByRecurringId(t.getId())) {
            VehicleType type = recItem.getVehicleTypeId() != null
                    ? vehicleTypeRepo.findById(recItem.getVehicleTypeId()).orElse(null) : null;
            if (type == null) continue;
            VehicleOrderItem item = new VehicleOrderItem();
            item.setVehicleOrder(saved);
            item.setVehicleType(type);
            item.setQty(recItem.getQty());
            item.setOtherText(recItem.getOtherText());
            item.setCreatedDate(now());
            orderItemRepo.save(item);
        }
    }

    // ── Туслах ────────────────────────────────────────────────────────────────────

    private List<Integer> parseWeekdays(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        List<Integer> out = new ArrayList<>();
        for (String p : csv.split(",")) {
            try { out.add(Integer.parseInt(p.trim())); } catch (NumberFormatException ignored) { }
        }
        return out;
    }

    private String fullName(User u) {
        String last = u.getLastName() != null ? u.getLastName().trim() : "";
        String ln = !last.isEmpty() ? last.charAt(0) + ". " : "";
        String fn = u.getFirstName() != null ? u.getFirstName().trim() : "";
        return (ln + fn).trim();
    }
}
