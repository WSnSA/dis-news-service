package mn.usug.dis_news_service.Service;

import mn.usug.dis_news_service.Entity.VehiclesToOut;
import mn.usug.dis_news_service.Model.VehiclesToOutRowDto;

import java.time.LocalDate;
import java.util.List;

public interface VehiclesToOutService {

    List<VehiclesToOut> findAll();

    VehiclesToOut findById(Integer id);

    VehiclesToOut save(VehiclesToOut vehiclesToOut);

    void deleteById(Integer id);

    List<VehiclesToOut> findByDate(LocalDate date);

    /** vehicleOrderId-аар хуваарилалтын дэлгэрэнгүй буцаана (захиалга өгсөн алба харахад) */
    VehiclesToOutRowDto findRowByOrderId(Integer vehicleOrderId);

    /** vehicleOrderId-аар бүх машины жагсаалт буцаана */
    List<VehiclesToOutRowDto> findRowsByOrderId(Integer vehicleOrderId);

    /** Тухайн захиалгын хугацаа+ээлжид аль хэдийн оногдсон машинууд */
    List<mn.usug.dis_news_service.Model.BusyVehicleDto> findBusyVehicles(Long vehicleOrderId);

    /** Олон захиалгын завгүй машиныг нэг дуудлагаар — { orderId: [BusyVehicleDto…] } */
    java.util.Map<Long, List<mn.usug.dis_news_service.Model.BusyVehicleDto>> findBusyVehicles(List<Long> vehicleOrderIds);

    /** Улсын дугаарыг харьцуулах хэлбэрт оруулна (том үсэг, зайгүй) */
    String plateKey(String plate);
}
