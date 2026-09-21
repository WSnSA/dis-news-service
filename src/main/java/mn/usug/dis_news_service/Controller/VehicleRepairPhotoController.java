package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.VehicleRepairPhotoRepository;
import mn.usug.dis_news_service.Entity.VehicleRepairPhoto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Засварын зураг. Файл өөрөө гадаад file service дээр хадгалагдана —
 * frontend тийш нь шууд upload хийгээд буцаж ирсэн objectName-г энд бүртгүүлнэ.
 */
@RestController
@RequestMapping("/repair/photo")
@RequiredArgsConstructor
public class VehicleRepairPhotoController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    private final VehicleRepairPhotoRepository repository;

    @GetMapping("/{repairId}")
    public List<VehicleRepairPhoto> getByRepair(@PathVariable Long repairId) {
        return repository.findByVehicleRepairIdAndActiveFlagOrderByIdAsc(repairId, ACTIVE);
    }

    @PostMapping
    public VehicleRepairPhoto save(@RequestBody VehicleRepairPhoto photo) {
        if (photo.getVehicleRepairId() == null || photo.getObjectName() == null) {
            throw new IllegalArgumentException("Засвар болон файлын мэдээлэл дутуу байна");
        }
        photo.setId(null);
        photo.setActiveFlag(ACTIVE);
        return repository.save(photo);
    }

    /** Soft delete — file service дээрх файлыг хөндөхгүй */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(p -> {
            p.setActiveFlag(INACTIVE);
            repository.save(p);
        });
        return ResponseEntity.noContent().build();
    }
}
