package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.RepairCategoryRepository;
import mn.usug.dis_news_service.Entity.RepairCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * Засварын бүртгэл — "Ангилал" таб.
 * Их засвар / Урсгал засвар / Техникийн үйлчилгээ / Сервис үйлчилгээ гэсэн үндсэн 4 мөрийг
 * db_migration_repair_category.sql суулгана; хэрэглэгч нэмэлт ангилал үүсгэж болно.
 */
@RestController
@RequestMapping("/repair/category")
@RequiredArgsConstructor
public class RepairCategoryController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    private final RepairCategoryRepository repository;

    @GetMapping("/getAll")
    public List<RepairCategory> getAll() {
        return repository.findByActiveFlagOrderBySortOrderAscIdAsc(ACTIVE);
    }

    /**
     * Шинэ ангилал. `code` нь системийн түлхүүр тул UI-аас ирсэн утгыг үл тоомсорлоно —
     * зөвхөн migration-аар суусан үндсэн 4 ангилал кодтой байна.
     */
    @PostMapping("/save")
    public RepairCategory save(@RequestBody RepairCategory category) {
        category.setId(null);
        category.setCode(null);
        category.setActiveFlag(ACTIVE);
        if (category.getSortOrder() == null) {
            category.setSortOrder(nextSortOrder());
        }
        return repository.save(category);
    }

    /** Нэр / тайлбар / дараалал зөвхөн. `code` болон `activeFlag` серверийн мэдэлд үлдэнэ. */
    @PutMapping("/update/{id}")
    public RepairCategory update(@PathVariable Long id, @RequestBody RepairCategory category) {
        RepairCategory existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RepairCategory not found: " + id));
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        if (category.getSortOrder() != null) {
            existing.setSortOrder(category.getSortOrder());
        }
        return repository.save(existing);
    }

    /**
     * Soft delete — мөрийг устгахгүй, зөвхөн идэвхгүй болгоно.
     * Засварын бичлэгүүд хожим repair_category_id-аар холбогдох тул мөрийг устгавал
     * хуучин бүртгэлийн ангиллын нэр түүхэнд алдагдана.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(category -> {
            category.setActiveFlag(INACTIVE);
            repository.save(category);
        });
        return ResponseEntity.noContent().build();
    }

    private Integer nextSortOrder() {
        return repository.findAll().stream()
                .map(RepairCategory::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }
}
