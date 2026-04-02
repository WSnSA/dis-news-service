package mn.usug.dis_news_service.Controller;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.VehicleOrderItemRepository;
import mn.usug.dis_news_service.DAO.VehicleOrderRepository;
import mn.usug.dis_news_service.DAO.VehicleTypeRepository;
import mn.usug.dis_news_service.DTO.VehicleOrderDto;
import mn.usug.dis_news_service.DTO.VehicleOrderItemSaveDto;
import mn.usug.dis_news_service.DTO.VehicleOrderSaveDto;
import mn.usug.dis_news_service.Entity.VehicleOrder;
import mn.usug.dis_news_service.Entity.VehicleOrderItem;
import mn.usug.dis_news_service.Entity.VehicleType;
import mn.usug.dis_news_service.Service.NotificationService;
import mn.usug.dis_news_service.Service.VehicleOrderService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ref/vehicle-order")
@RequiredArgsConstructor
public class VehicleOrderController {

    private final VehicleOrderService service;
    private final NotificationService notificationService;
    private final VehicleOrderRepository orderRepo;
    private final VehicleOrderItemRepository itemRepo;
    private final VehicleTypeRepository vehicleTypeRepo;

    @GetMapping("/getByDate")
    public List<VehicleOrderDto> getByDate(@RequestParam LocalDate date) {
        return service.getByDate(date);
    }

    /** Баталгаажаагүй (status=0) — 502 ажилтан харна */
    @GetMapping("/getPending")
    public List<VehicleOrderDto> getPending(@RequestParam LocalDate date) {
        return service.getPending(date);
    }

    /** Баталгаажсан (status=1) — 507 ажилтан харна */
    @GetMapping("/getConfirmed")
    public List<VehicleOrderDto> getConfirmed(@RequestParam LocalDate date) {
        return service.getConfirmed(date);
    }

    /**
     * 502 ажилтан захиалгуудыг баталгаажуулна: status 0 → 1
     * Body: захиалгын ID-уудын жагсаалт  [1, 2, 3]
     */
    @PostMapping("/bulk-confirm")
    @Transactional
    public void bulkConfirm(@RequestBody List<Long> orderIds) {
        orderIds.forEach(id ->
            orderRepo.findById(id).ifPresent(o -> {
                o.setStatus(1);
                orderRepo.save(o);
            })
        );
        notificationService.notifyVehicleOrder("Захиалгууд баталгаажлаа", null);
    }

    /** Хүлээгдэж буй (status=0) захиалгыг засах */
    @PutMapping("/update/{id}")
    @Transactional
    public void update(@PathVariable Long id, @RequestBody VehicleOrderSaveDto dto) {
        VehicleOrder order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("VehicleOrder not found: " + id));
        if (order.getStatus() != 0) {
            throw new IllegalStateException("Зөвхөн хүлээгдэж буй захиалгыг засах боломжтой");
        }
        order.setWorkDescription(dto.getWorkDescription());
        order.setStartDate(dto.getStartDate());
        order.setEndDate(dto.getEndDate() != null ? dto.getEndDate() : dto.getStartDate());
        order.setTaskDuration(dto.getTaskDuration());
        order.setUpdatedDate(LocalDateTime.now());
        orderRepo.save(order);

        itemRepo.deleteAll(itemRepo.findByVehicleOrderId(id));

        if (dto.getVehicles() != null) {
            for (VehicleOrderItemSaveDto itemDto : dto.getVehicles()) {
                VehicleOrderItem item = new VehicleOrderItem();
                item.setVehicleOrder(order);
                item.setCreatedDate(LocalDateTime.now());
                VehicleType type = vehicleTypeRepo.findById(itemDto.getVehicleTypeId())
                        .orElseThrow(() -> new RuntimeException("VehicleType not found: " + itemDto.getVehicleTypeId()));
                item.setVehicleType(type);
                if ("Бусад".equals(type.getName())) {
                    item.setOtherText(itemDto.getOtherText());
                    item.setQty(null);
                } else {
                    item.setQty(itemDto.getQty());
                    item.setOtherText(null);
                }
                itemRepo.save(item);
            }
        }
    }

    /** Зөвхөн хүлээгдэж буй (status=0) захиалгыг устгана */
    @DeleteMapping("/delete/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        VehicleOrder order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("VehicleOrder not found: " + id));
        if (order.getStatus() != 0) {
            throw new IllegalStateException("Зөвхөн хүлээгдэж буй захиалгыг устгах боломжтой");
        }
        itemRepo.deleteAll(itemRepo.findByVehicleOrderId(id));
        orderRepo.deleteById(id);
    }

    @PostMapping("/save")
    @Transactional
    public void save(@RequestBody VehicleOrderSaveDto dto) {

        /* ===== 1️⃣ Parent ===== */
        VehicleOrder order = new VehicleOrder();
        order.setWorkDescription(dto.getWorkDescription());
        order.setAssignedDepartmentId(dto.getAssignedDepartmentId());
        order.setAssignedEmployeeId(dto.getAssignedEmployeeId());
        order.setOrderDate(dto.getOrderDate());
        order.setStartDate(dto.getStartDate());
        order.setEndDate(dto.getEndDate() != null ? dto.getEndDate() : dto.getStartDate());
        order.setTaskDuration(dto.getTaskDuration());
        order.setStatus(0);
        order.setActiveFlag(1);
        order.setCreatedDate(LocalDateTime.now());

        VehicleOrder savedOrder = orderRepo.save(order);

        notificationService.notifyVehicleOrder(dto.getWorkDescription(), dto.getAssignedDepartmentId());

        /* ===== 2️⃣ Child ===== */
        if (dto.getVehicles() == null || dto.getVehicles().isEmpty()) {
            return;
        }

        for (VehicleOrderItemSaveDto itemDto : dto.getVehicles()) {

            VehicleOrderItem item = new VehicleOrderItem();
            item.setVehicleOrder(savedOrder);
            item.setCreatedDate(LocalDateTime.now());

            VehicleType type = vehicleTypeRepo
                    .findById(itemDto.getVehicleTypeId())
                    .orElseThrow(() ->
                            new RuntimeException("VehicleType not found: " + itemDto.getVehicleTypeId())
                    );

            item.setVehicleType(type);

            if ("Бусад".equals(type.getName())) {
                item.setOtherText(itemDto.getOtherText());
                item.setQty(null);
            } else {
                item.setQty(itemDto.getQty());
                item.setOtherText(null);
            }

            itemRepo.save(item);
        }
    }
}
