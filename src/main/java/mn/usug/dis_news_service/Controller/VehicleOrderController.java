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

    @PostMapping("/save")
    @Transactional
    public void save(@RequestBody VehicleOrderSaveDto dto) {

        /* ===== 1️⃣ Parent ===== */
        VehicleOrder order = new VehicleOrder();
        order.setWorkDescription(dto.getWorkDescription());
        order.setAssignedDepartmentId(dto.getAssignedDepartmentId());
        order.setAssignedEmployeeId(dto.getAssignedEmployeeId());
        order.setOrderDate(dto.getOrderDate());
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
