package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.RepairPartRepository;
import mn.usug.dis_news_service.DAO.RepairPartTypeRepository;
import mn.usug.dis_news_service.Entity.RepairPart;
import mn.usug.dis_news_service.Entity.RepairPartType;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Засварын бүртгэл — "Сэлбэг" таб (лавлах жагсаалт).
 *
 * Төрлүүд (Сэлбэг / Тос тосолгоо / Дугуй / Аккумулятор) нь migration-аар суудаг ба
 * UI дээр шүүлтүүр болж ажиллана — Ангилал табын загвартай яг ижил.
 */
@RestController
@RequestMapping("/repair/part")
@RequiredArgsConstructor
public class RepairPartController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    private final RepairPartRepository repository;
    private final RepairPartTypeRepository typeRepository;

    /** Шүүлтүүрийн төрлүүд */
    @GetMapping("/types")
    public List<RepairPartType> types() {
        return typeRepository.findByActiveFlagOrderBySortOrderAscIdAsc(ACTIVE);
    }

    /**
     * @param includeInactive true бол устгагдсан мөрийг ч буцаана.
     *        Устгасан бичлэг сэргээх боломжтой байхын тулд.
     */
    @GetMapping("/getAll")
    public List<RepairPart> getAll(
            @RequestParam(value = "includeInactive", required = false, defaultValue = "false")
            boolean includeInactive
    ) {
        return includeInactive
                ? repository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                : repository.findByActiveFlagOrderByNameAsc(ACTIVE);
    }

    @PostMapping("/save")
    public RepairPart save(@RequestBody RepairPart part) {
        part.setId(null);
        part.setActiveFlag(ACTIVE);
        return repository.save(part);
    }

    @PutMapping("/update/{id}")
    public RepairPart update(@PathVariable Long id, @RequestBody RepairPart part) {
        RepairPart existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RepairPart not found: " + id));
        existing.setPartTypeId(part.getPartTypeId());
        existing.setName(part.getName());
        existing.setCode(part.getCode());
        existing.setUnit(part.getUnit());
        existing.setUnitPrice(part.getUnitPrice());
        existing.setStockQty(part.getStockQty());
        existing.setNote(part.getNote());
        return repository.save(existing);
    }

    /**
     * Устгасан сэлбэг-г буцааж идэвхжүүлнэ.
     *
     * UI-д товч БАЙХГҮЙ — устгасан мөр саарлаар үлдэж, буцаах боломжгүй
     * гэж тохиролцсон. Энэ нь санамсаргүй устгалыг гараар засах гарц.
     */
    @PutMapping("/restore/{id}")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        repository.findById(id).ifPresent(row -> {
            row.setActiveFlag(ACTIVE);
            repository.save(row);
        });
        return ResponseEntity.noContent().build();
    }

    /** Soft delete — зарцуулалтын түүхэд сэлбэгийн нэр үлдэх ёстой тул мөрийг устгахгүй */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(part -> {
            part.setActiveFlag(INACTIVE);
            repository.save(part);
        });
        return ResponseEntity.noContent().build();
    }
}
