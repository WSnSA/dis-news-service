package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.VehicleTypeRepository;
import mn.usug.dis_news_service.Entity.VehicleType;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/save")
    public VehicleType save(@RequestBody VehicleType vehicleType) {
        vehicleType.setId(null);
        return repository.save(vehicleType);
    }

    @PutMapping("/update/{id}")
    public VehicleType update(@PathVariable Integer id, @RequestBody VehicleType vehicleType) {
        VehicleType existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("VehicleType not found: " + id));
        existing.setName(vehicleType.getName());
        return repository.save(existing);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        repository.deleteById(id);
    }
}
