package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.RepairDocumentRepository;
import mn.usug.dis_news_service.Entity.RepairDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Засварын бүртгэл — "Баримт бөглөх" таб.
 *
 * Хоёр төрөл: ACT = Акт, REQUEST = Шаардах.
 * Сэлбэг/ажилтны мөрүүдээс дүнг автоматаар бүрдүүлэх хэсэг 2-р шатанд нэмэгдэнэ —
 * одоогоор дүн, агуулгыг гараар бөглөнө.
 */
@RestController
@RequestMapping("/repair/document")
@RequiredArgsConstructor
public class RepairDocumentController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    /** Зөвшөөрөгдөх баримтын төрлүүд */
    private static final List<String> DOC_TYPES = List.of("ACT", "REQUEST");

    private final RepairDocumentRepository repository;

    @GetMapping("/getAll")
    public List<RepairDocument> getAll() {
        return repository.findByActiveFlagOrderByDocDateDescIdDesc(ACTIVE);
    }

    @PostMapping("/save")
    public RepairDocument save(@RequestBody RepairDocument document) {
        document.setId(null);
        document.setActiveFlag(ACTIVE);
        document.setDocType(normalizeType(document.getDocType()));
        return repository.save(document);
    }

    @PutMapping("/update/{id}")
    public RepairDocument update(@PathVariable Long id, @RequestBody RepairDocument document) {
        RepairDocument existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RepairDocument not found: " + id));
        existing.setDocType(normalizeType(document.getDocType()));
        existing.setDocNo(document.getDocNo());
        existing.setDocDate(document.getDocDate());
        existing.setVehicleRepairId(document.getVehicleRepairId());
        existing.setPlateNumber(document.getPlateNumber());
        existing.setAmount(document.getAmount());
        existing.setContent(document.getContent());
        return repository.save(existing);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(document -> {
            document.setActiveFlag(INACTIVE);
            repository.save(document);
        });
        return ResponseEntity.noContent().build();
    }

    /** Танихгүй төрөл ирвэл акт гэж үзнэ */
    private String normalizeType(String raw) {
        String type = raw == null ? "" : raw.trim().toUpperCase();
        return DOC_TYPES.contains(type) ? type : "ACT";
    }
}
