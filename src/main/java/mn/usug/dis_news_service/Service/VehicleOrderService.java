package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.DepartmentDAO;
import mn.usug.dis_news_service.DAO.VehicleOrderItemRepository;
import mn.usug.dis_news_service.DAO.VehicleOrderRepository;
import mn.usug.dis_news_service.DAO.VehicleTypeRepository;
import mn.usug.dis_news_service.DTO.VehicleItemDto;
import mn.usug.dis_news_service.DTO.VehicleOrderDto;
import mn.usug.dis_news_service.Entity.Department;
import mn.usug.dis_news_service.Entity.VehicleOrder;
import mn.usug.dis_news_service.Entity.VehicleType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleOrderService {

    private final VehicleOrderRepository orderRepo;
    private final VehicleOrderItemRepository itemRepo;
    private final VehicleTypeRepository typeRepo;
    private final DepartmentDAO departmentRepo;

    public List<VehicleOrderDto> getByDate(LocalDate date) {
        return mapOrders(orderRepo.findByDate(date));
    }

    /** Баталгаажаагүй (status=0) — 502 ажилтан харна */
    public List<VehicleOrderDto> getPending(LocalDate date) {
        return mapOrders(orderRepo.findPendingByDate(date));
    }

    /** Баталгаажсан (status=1) — 507 ажилтан харна */
    public List<VehicleOrderDto> getConfirmed(LocalDate date) {
        return mapOrders(orderRepo.findConfirmedByDate(date));
    }

    private List<VehicleOrderDto> mapOrders(List<VehicleOrder> orders) {

        Map<Integer, String> depMap =
                departmentRepo.findAll().stream()
                        .collect(Collectors.toMap(
                                Department::getDepId,
                                Department::getDepName
                        ));

        return orders.stream().map(o -> {

            List<VehicleItemDto> vehicles =
                    itemRepo.findByVehicleOrderId(o.getId())
                            .stream()
                            .map(i -> {
                                VehicleType type = i.getVehicleType();
                                if (type == null) return null;

                                String displayName;
                                Integer qty = i.getQty();

                                if ("Бусад".equals(type.getName())) {
                                    displayName = i.getOtherText();
                                    qty = null;
                                } else {
                                    displayName = type.getName();
                                }

                                return new VehicleItemDto(type.getId(), displayName, qty);
                            })
                            .filter(Objects::nonNull)
                            .toList();

            VehicleOrderDto dto = new VehicleOrderDto();
            dto.setId(o.getId());
            dto.setOrderDate(o.getOrderDate());
            dto.setStartDate(o.getStartDate());
            dto.setEndDate(o.getEndDate());
            dto.setTaskDuration(o.getTaskDuration());
            dto.setWorkDescription(o.getWorkDescription());
            dto.setAssignedDepartmentId(o.getAssignedDepartmentId());
            dto.setAssignedDepartmentName(depMap.get(o.getAssignedDepartmentId()));
            dto.setStatus(o.getStatus());
            dto.setVehicles(vehicles);

            return dto;
        }).toList();
    }
}
