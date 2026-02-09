package mn.usug.dis_news_service.Service;

import mn.usug.dis_news_service.Entity.VehiclesToOut;

import java.time.LocalDate;
import java.util.List;

public interface VehiclesToOutService {

    List<VehiclesToOut> findAll();

    VehiclesToOut findById(Integer id);

    VehiclesToOut save(VehiclesToOut vehiclesToOut);

    void deleteById(Integer id);

    List<VehiclesToOut> findByDate(LocalDate date);
}
