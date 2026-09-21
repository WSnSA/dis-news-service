package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.RepairSpecialtyRepository;
import mn.usug.dis_news_service.DAO.RepairWorkerRepository;
import mn.usug.dis_news_service.Entity.RepairSpecialty;
import mn.usug.dis_news_service.Entity.RepairWorker;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Засварын бүртгэл — "Ажилтан" таб (лавлах жагсаалт).
 *
 * Мэргэжлүүд (Автын засварчин / Цахилгаанчин / Моторчин / Гагнуурчин) нь
 * migration-аар суудаг ба UI дээр шүүлтүүр болно.
 */
@RestController
@RequestMapping("/repair/worker")
@RequiredArgsConstructor
public class RepairWorkerController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    private final RepairWorkerRepository repository;
    private final RepairSpecialtyRepository specialtyRepository;

    /** Шүүлтүүрийн мэргэжлүүд */
    @GetMapping("/specialties")
    public List<RepairSpecialty> specialties() {
        return specialtyRepository.findByActiveFlagOrderBySortOrderAscIdAsc(ACTIVE);
    }

    /**
     * @param includeInactive true бол устгагдсан мөрийг ч буцаана.
     *        Устгасан бичлэг сэргээх боломжтой байхын тулд.
     */
    @GetMapping("/getAll")
    public List<RepairWorker> getAll(
            @RequestParam(value = "includeInactive", required = false, defaultValue = "false")
            boolean includeInactive
    ) {
        return includeInactive
                ? repository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                : repository.findByActiveFlagOrderByNameAsc(ACTIVE);
    }

    @PostMapping("/save")
    public RepairWorker save(@RequestBody RepairWorker worker) {
        worker.setId(null);
        worker.setActiveFlag(ACTIVE);
        return repository.save(worker);
    }

    @PutMapping("/update/{id}")
    public RepairWorker update(@PathVariable Long id, @RequestBody RepairWorker worker) {
        RepairWorker existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RepairWorker not found: " + id));
        existing.setSpecialtyId(worker.getSpecialtyId());
        existing.setName(worker.getName());
        existing.setPhone(worker.getPhone());
        existing.setGrade(worker.getGrade());
        existing.setNote(worker.getNote());
        return repository.save(existing);
    }

    /** Устгасан ажилтан-г буцааж идэвхжүүлнэ */
    @PutMapping("/restore/{id}")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        repository.findById(id).ifPresent(row -> {
            row.setActiveFlag(ACTIVE);
            repository.save(row);
        });
        return ResponseEntity.noContent().build();
    }

    /** Soft delete — хийсэн ажлын түүхэд ажилтны нэр үлдэх ёстой */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(worker -> {
            worker.setActiveFlag(INACTIVE);
            repository.save(worker);
        });
        return ResponseEntity.noContent().build();
    }
}
