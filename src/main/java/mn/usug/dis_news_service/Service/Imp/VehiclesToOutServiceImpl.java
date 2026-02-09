package mn.usug.dis_news_service.Service.Imp;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.Entity.VehiclesToOut;
import mn.usug.dis_news_service.DAO.VehiclesToOutRepository;
import mn.usug.dis_news_service.Service.VehiclesToOutService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiclesToOutServiceImpl implements VehiclesToOutService {

    private final VehiclesToOutRepository repository;

    @Override
    public List<VehiclesToOut> findAll() {
        return repository.findAll();
    }

    @Override
    public List<VehiclesToOut> findByDate(LocalDate date) {
        return repository.findByCreatedDate(date);
    }

    @Override
    public VehiclesToOut findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found: " + id));
    }

    @Override
    public VehiclesToOut save(VehiclesToOut vehiclesToOut) {
        return repository.save(vehiclesToOut);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}
