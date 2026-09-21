package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.*;
import mn.usug.dis_news_service.Entity.*;
import mn.usug.dis_news_service.Service.Report.RepairFormDocxWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Засварын бүртгэл — "Баримт бөглөх" таб.
 *
 * Хоёр төрөл: ACT = Акт, REQUEST = Шаардах (БМ-6 маягт).
 * Хоёр хэлбэр:
 *   TEXT  — зориулалтаа бичгээр тайлбарлана
 *   ITEMS — сэлбэгийн лавлахаас сонгосон мөрүүдтэй
 */
@RestController
@RequestMapping("/repair/document")
@RequiredArgsConstructor
public class RepairDocumentController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    private static final List<String> DOC_TYPES = List.of("ACT", "REQUEST");
    private static final List<String> DOC_MODES = List.of("TEXT", "ITEMS");

    private final RepairDocumentRepository repository;
    private final RepairDocumentItemRepository itemRepository;
    private final RepairPartRepository partRepository;
    private final VehicleRepairPartRepository usagePartRepository;
    private final RepairFormDocxWriter writer;

    /** Бүх баримт — мөрүүдтэй нь хамт */
    @GetMapping("/getAll")
    public List<RepairDocument> getAll() {
        List<RepairDocument> docs = repository.findByActiveFlagOrderByDocDateDescIdDesc(ACTIVE);
        attachItems(docs);
        return docs;
    }

    /** Нэг засварт хамаарах баримтууд — машины түүх, тайланд */
    @GetMapping("/by-repair/{repairId}")
    public List<RepairDocument> byRepair(@PathVariable Long repairId) {
        List<RepairDocument> docs = repository.findByVehicleRepairIdAndActiveFlagOrderByDocDateDescIdDesc(repairId, ACTIVE);
        attachItems(docs);
        return docs;
    }

    /** Нэг машины бүх баримт — улсын дугаараар */
    @GetMapping("/by-plate")
    public List<RepairDocument> byPlate(@RequestParam String plate) {
        if (plate == null || plate.isBlank()) return List.of();
        List<RepairDocument> docs = repository.findByPlate(plate.trim());
        attachItems(docs);
        return docs;
    }

    @PostMapping("/save")
    @Transactional
    public RepairDocument save(@RequestBody RepairDocument document) {
        List<RepairDocumentItem> items = document.getItems();
        document.setId(null);
        document.setActiveFlag(ACTIVE);
        document.setDocType(normalizeType(document.getDocType()));
        document.setDocMode(normalizeMode(document.getDocMode()));
        RepairDocument saved = repository.save(document);
        replaceItems(saved, items);
        return withItems(saved);
    }

    @PutMapping("/update/{id}")
    @Transactional
    public RepairDocument update(@PathVariable Long id, @RequestBody RepairDocument document) {
        RepairDocument existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RepairDocument not found: " + id));
        existing.setDocType(normalizeType(document.getDocType()));
        existing.setDocMode(normalizeMode(document.getDocMode()));
        existing.setDocNo(document.getDocNo());
        existing.setDocDate(document.getDocDate());
        existing.setVehicleRepairId(document.getVehicleRepairId());
        existing.setVehicleId(document.getVehicleId());
        existing.setPlateNumber(document.getPlateNumber());
        existing.setAmount(document.getAmount());
        existing.setContent(document.getContent());
        existing.setFromPerson(document.getFromPerson());
        existing.setToPlace(document.getToPlace());
        existing.setPurpose(document.getPurpose());
        existing.setReceiverName(document.getReceiverName());
        existing.setIssuerName(document.getIssuerName());
        existing.setFormNo(document.getFormNo());
        existing.setApproverTitle(document.getApproverTitle());
        existing.setApproverName(document.getApproverName());
        existing.setCity(document.getCity());
        existing.setCommission(document.getCommission());
        existing.setCommissionDriver(document.getCommissionDriver());
        existing.setNormKm(document.getNormKm());
        existing.setActualKm(document.getActualKm());
        existing.setFinding(document.getFinding());
        existing.setLiability(document.getLiability());
        existing.setVehicleBrand(document.getVehicleBrand());
        RepairDocument saved = repository.save(existing);
        replaceItems(saved, document.getItems());
        return withItems(saved);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(document -> {
            document.setActiveFlag(INACTIVE);
            repository.save(document);
        });
        return ResponseEntity.noContent().build();
    }

    /**
     * Засварын зарцуулалтаас шаардах хуудасны мөрүүдийг бэлдэнэ.
     * Хадгалахгүй — UI дээр харуулаад хэрэглэгч засах боломжтой.
     */
    @GetMapping("/items-from-repair/{repairId}")
    public List<RepairDocumentItem> itemsFromRepair(@PathVariable Long repairId) {
        Map<Long, RepairPart> parts = partRepository.findAll().stream()
                .collect(Collectors.toMap(RepairPart::getId, Function.identity(), (a, b) -> a));

        List<RepairDocumentItem> out = new ArrayList<>();
        int i = 0;
        for (VehicleRepairPart line : usagePartRepository
                .findByVehicleRepairIdAndActiveFlagOrderByIdAsc(repairId, ACTIVE)) {
            RepairPart ref = parts.get(line.getRepairPartId());
            RepairDocumentItem item = new RepairDocumentItem();
            item.setRepairPartId(line.getRepairPartId());
            item.setItemName(firstNonBlank(ref != null ? ref.getName() : null, line.getPartName(), "—"));
            item.setItemCode(ref != null ? ref.getCode() : null);
            item.setUnit(firstNonBlank(ref != null ? ref.getUnit() : null, line.getPartUnit(), null));
            item.setQtyRequested(line.getQty() != null ? line.getQty() : BigDecimal.ONE);
            item.setQtyApproved(line.getQty());
            item.setQtyIssued(line.getQty());
            item.setUnitPrice(line.getUnitPrice());
            item.setSortOrder(i++);
            out.add(item);
        }
        return out;
    }

    /** Word файлаар татна — Шаардах бол БМ-6 маягт, Акт бол актын хуудас */
    @GetMapping("/{id}/docx")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws Exception {
        RepairDocument doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RepairDocument not found: " + id));
        withItems(doc);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if ("REQUEST".equals(doc.getDocType())) {
            writer.request(doc, out);
        } else {
            writer.act(doc, out);
        }

        String base = ("REQUEST".equals(doc.getDocType()) ? "Шаардах хуудас" : "Акт")
                + (doc.getDocNo() != null && !doc.getDocNo().isBlank() ? " " + doc.getDocNo() : "")
                + (doc.getPlateNumber() != null && !doc.getPlateNumber().isBlank() ? " " + doc.getPlateNumber() : "");
        String fileName = base.replaceAll("[\\\\/:*?\"<>|]", "_") + ".docx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        // RFC 5987 — Кирилл нэр зөв дамжина
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"document.docx\"; filename*=UTF-8''"
                        + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
        return ResponseEntity.ok().headers(headers).body(out.toByteArray());
    }

    /* ══════════ helpers ══════════ */

    private void replaceItems(RepairDocument doc, List<RepairDocumentItem> items) {
        itemRepository.deleteByRepairDocumentId(doc.getId());
        if (items == null || items.isEmpty()) {
            doc.setItems(List.of());
            return;
        }
        int i = 0;
        List<RepairDocumentItem> saved = new ArrayList<>();
        for (RepairDocumentItem item : items) {
            if (item.getItemName() == null || item.getItemName().isBlank()) continue;
            item.setId(null);
            item.setRepairDocumentId(doc.getId());
            item.setActiveFlag(ACTIVE);
            item.setSortOrder(i++);
            if (item.getQtyRequested() == null) item.setQtyRequested(BigDecimal.ONE);
            saved.add(itemRepository.save(item));
        }
        doc.setItems(saved);
    }

    private RepairDocument withItems(RepairDocument doc) {
        doc.setItems(itemRepository
                .findByRepairDocumentIdAndActiveFlagOrderBySortOrderAscIdAsc(doc.getId(), ACTIVE));
        return doc;
    }

    /** Олон баримтын мөрийг нэг дуудлагаар нөхнө */
    private void attachItems(List<RepairDocument> docs) {
        if (docs.isEmpty()) return;
        List<Long> ids = docs.stream().map(RepairDocument::getId).toList();
        Map<Long, List<RepairDocumentItem>> byDoc = itemRepository
                .findByRepairDocumentIdInAndActiveFlag(ids, ACTIVE)
                .stream()
                .sorted(Comparator.comparing(RepairDocumentItem::getSortOrder)
                        .thenComparing(RepairDocumentItem::getId))
                .collect(Collectors.groupingBy(RepairDocumentItem::getRepairDocumentId));
        docs.forEach(d -> d.setItems(byDoc.getOrDefault(d.getId(), List.of())));
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    /** Танихгүй төрөл ирвэл акт гэж үзнэ */
    private String normalizeType(String raw) {
        String type = raw == null ? "" : raw.trim().toUpperCase();
        return DOC_TYPES.contains(type) ? type : "ACT";
    }

    private String normalizeMode(String raw) {
        String mode = raw == null ? "" : raw.trim().toUpperCase();
        return DOC_MODES.contains(mode) ? mode : "TEXT";
    }
}
