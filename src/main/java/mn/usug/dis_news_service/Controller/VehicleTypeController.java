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

    /** Бүгд (parent + child) */
    @GetMapping("/getAll")
    public List<VehicleType> getAll() {
        return repository.findAll();
    }

    /** Зөвхөн үндсэн (parent_id IS NULL) — жагсаалтад ашиглана */
    @GetMapping("/getRoots")
    public List<VehicleType> getRoots() {
        return repository.findByParentIdIsNull();
    }

    /** Дэд төрлүүд parentId-аар */
    @GetMapping("/getByParent")
    public List<VehicleType> getByParent(@RequestParam Integer parentId) {
        return repository.findByParentId(parentId);
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
        existing.setParentId(vehicleType.getParentId());
        return repository.save(existing);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        repository.deleteById(id);
    }
}
