package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.VehicleTypeRepository;
import mn.usug.dis_news_service.Entity.VehicleType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ref/vehicle-type")
@RequiredArgsConstructor
public class VehicleTypeController {

    private static final Integer ACTIVE = 1;
    private static final Integer INACTIVE = 0;

    private final VehicleTypeRepository repository;

    /** Бүгд идэвхтэй (parent + child). Идэвхгүй болгосон төрлүүд харагдахгүй — мөр нь хуучин түүхэд хэвээр үлдэнэ. */
    @GetMapping("/getAll")
    public List<VehicleType> getAll() {
        return repository.findByActiveFlag(ACTIVE);
    }

    /** Зөвхөн идэвхтэй үндсэн (parent_id IS NULL) — жагсаалтад ашиглана */
    @GetMapping("/getRoots")
    public List<VehicleType> getRoots() {
        return repository.findByParentIdIsNullAndActiveFlag(ACTIVE);
    }

    /** Идэвхтэй дэд төрлүүд parentId-аар */
    @GetMapping("/getByParent")
    public List<VehicleType> getByParent(@RequestParam Integer parentId) {
        return repository.findByParentIdAndActiveFlag(parentId, ACTIVE);
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

    /**
     * Soft delete — мөрийг устгахгүй, зөвхөн идэвхгүй болгоно.
     * Учир нь машин/захиалга зэрэг хуучин бичлэгүүд vehicle_type_id-аар холбогддог;
     * мөрийг устгавал foreign key зөрчигдөн алдаа гарна, нэр нь түүхэнд алдагдана.
     * Үндсэн төрлийг идэвхгүй болгоход дэд төрлүүд нь хамт идэвхгүй болно.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        repository.findById(id).ifPresent(type -> {
            type.setActiveFlag(INACTIVE);
            repository.save(type);
            // Дэд төрлүүдийг хамт идэвхгүй болгоно
            for (VehicleType child : repository.findByParentId(id)) {
                child.setActiveFlag(INACTIVE);
                repository.save(child);
            }
        });
        return ResponseEntity.noContent().build();
    }
}
