package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.VehicleTypeRepository;
import mn.usug.dis_news_service.Entity.VehicleType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ref/vehicle-type")
@RequiredArgsConstructor
public class VehicleTypeController {

    private final VehicleTypeRepository repository;

    @GetMapping("/getAll")
    public List<VehicleType> getAll() {
        return repository.findAll();
    }
}

