package mn.usug.dis_news_service.Service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.RepairPartRepository;
import mn.usug.dis_news_service.DAO.RepairPartTypeRepository;
import mn.usug.dis_news_service.Entity.RepairPart;
import mn.usug.dis_news_service.Entity.RepairPartType;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Нягтлан бодохоос гардаг "Бараа материалын дэлгэрэнгүй" тайланг уншиж
 * сэлбэгийн лавлахыг шинэчилнэ.
 *
 * Файлын бүтэц (АВТО БААЗ-ын 1430-xx-9000 дансны тайлан):
 *   мөр 0–4  толгойн текст
 *   мөр 5–6  хоёр давхар баганын нэр
 *   бүлгийн мөр  "1430-06-9000 - БМ-ТЭХМ–Тос-АБ / ... - Орос сэлбэг"
 *                (зөвхөн 0-р багана дүүрэн) — дараагийн мөрүүдийн дэд ангилал
 *   өгөгдлийн мөр  Код | Нэр | ХН | Үнэ | Эхний(Тоо,Өртөг) | Орлого | Зарлага |
 *                  Эцсийн үлдэгдэл(Тоо, Үнэ)
 *   төгсгөл  "Нийт:", "Бүгд дүн:"
 *
 * Үлдэгдлийг ЭЦСИЙН ҮЛДЭГДЭЛ-ийн тооноос авна, үнийг "Үнэ" баганаас
 * (эцсийн үнэ нь үлдэгдэл 0 бол 0 болчихдог).
 */
@Service
@RequiredArgsConstructor
public class RepairPartImportService {

    private static final Integer ACTIVE = 1;

    /* Баганын индекс — тайлангийн бүтэц тогтмол */
    private static final int C_CODE  = 0;
    private static final int C_NAME  = 1;
    private static final int C_UNIT  = 2;
    private static final int C_PRICE = 3;
    private static final int C_CLOSING_QTY = 10;

    /** Бүлгийн толгойн дансны код — "1430-06-9000 - БМ-ТЭХМ–Тос-АБ / ..." */
    private static final java.util.regex.Pattern ACCOUNT_CODE =
            java.util.regex.Pattern.compile("^\\d{4}-\\d{2}-\\d{4}");

    private final RepairPartRepository partRepository;
    private final RepairPartTypeRepository typeRepository;

    @Data
    @Builder
    public static class Result {
        private Long partTypeId;
        private String partTypeName;
        /** Файлаас таньсан төрөл ("Тос" / "Сэлбэг") — хэрэглэгч өөрчилж болно */
        private String detectedType;
        private int created;
        private int updated;
        private int unchanged;
        /** Код эсвэл нэргүй мөрүүд */
        private int skipped;
        private List<String> groups;
        private List<Row> rows;
        private boolean dryRun;
    }

    @Data
    @Builder
    public static class Row {
        private String code;
        private String name;
        private String unit;
        private BigDecimal price;
        private BigDecimal stock;
        private String group;
        /** NEW / UPDATED / UNCHANGED */
        private String action;
    }

    public Result importFile(MultipartFile file, Long partTypeId, boolean dryRun) throws Exception {
        List<Row> rows = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        String detected = null;
        String currentGroup = null;

        try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
                if (row == null) continue;

                String code = fmt.formatCellValue(row.getCell(C_CODE)).trim();
                String name = fmt.formatCellValue(row.getCell(C_NAME)).trim();
                if (code.isEmpty() && name.isEmpty()) continue;

                // Төгсгөлийн нийлбэр мөрүүд
                if (code.startsWith("Нийт") || code.startsWith("Бүгд")) continue;

                // Бүлгийн толгой — зөвхөн эхний багана дүүрэн
                boolean restEmpty = fmt.formatCellValue(row.getCell(C_UNIT)).isBlank()
                        && fmt.formatCellValue(row.getCell(C_PRICE)).isBlank()
                        && fmt.formatCellValue(row.getCell(C_CLOSING_QTY)).isBlank();
                // Бүлгийн толгой нь дансны код — "1430-06-9000 - ...". Файлын
                // эхний мөрүүд ("УСУГ АВТО БААЗ - 1") зураастай ч бүлэг биш.
                if (restEmpty && name.isEmpty() && ACCOUNT_CODE.matcher(code).find()) {
                    currentGroup = tailAfterDash(code);
                    groups.add(code);
                    if (detected == null) {
                        if (code.contains("Тос")) detected = "Тос тосолгоо";
                        else if (code.contains("Сэлбэг")) detected = "Сэлбэг";
                        else if (code.contains("Дугуй")) detected = "Дугуй";
                    }
                    continue;
                }

                // Өгөгдлийн мөр — код нь тоон дараалал байх ёстой
                if (!code.matches("\\d{6,}")) continue;
                if (name.isEmpty()) continue;

                rows.add(Row.builder()
                        .code(code)
                        .name(name)
                        .unit(blankToNull(fmt.formatCellValue(row.getCell(C_UNIT)).trim()))
                        .price(number(row.getCell(C_PRICE)))
                        .stock(number(row.getCell(C_CLOSING_QTY)))
                        .group(currentGroup)
                        .build());
            }
        }

        // Нэг код олон нярав дээр (олон дэд данс) тархсан байдаг —
        // жишээ нь хөдөлгүүрийн тос 3449.5 л нэг хүн дээр, 40 л нөгөө дээр.
        // Кодоор нь нэгтгэж үлдэгдлийг НЭМНЭ, эс тэгвээс сүүлийн мөр өмнөхийг
        // дарж бичиж, үлдэгдэл алдагдана.
        rows = mergeByCode(rows);

        RepairPartType type = resolveType(partTypeId, detected);
        if (type == null) {
            throw new IllegalArgumentException(
                    "Сэлбэгийн төрлийг тодорхойлж чадсангүй. Төрлөө сонгоно уу.");
        }

        // Тухайн төрлийн одоогийн лавлах — кодоор нь тааруулна
        Map<String, RepairPart> existing = new HashMap<>();
        for (RepairPart p : partRepository.findAll()) {
            if (!type.getId().equals(p.getPartTypeId())) continue;
            if (p.getCode() != null && !p.getCode().isBlank()) {
                existing.put(p.getCode().trim(), p);
            }
        }

        int created = 0, updated = 0, unchanged = 0;
        for (Row row : rows) {
            RepairPart part = existing.get(row.getCode());
            if (part == null) {
                row.setAction("NEW");
                created++;
                if (!dryRun) {
                    RepairPart fresh = new RepairPart();
                    fresh.setPartTypeId(type.getId());
                    fresh.setCode(row.getCode());
                    fresh.setName(row.getName());
                    fresh.setUnit(row.getUnit());
                    fresh.setUnitPrice(row.getPrice());
                    fresh.setStockQty(row.getStock());
                    fresh.setNote(row.getGroup());
                    fresh.setActiveFlag(ACTIVE);
                    partRepository.save(fresh);
                }
                continue;
            }

            boolean same = eq(part.getName(), row.getName())
                    && eq(part.getUnit(), row.getUnit())
                    && eqNum(part.getUnitPrice(), row.getPrice())
                    && eqNum(part.getStockQty(), row.getStock());
            if (same) {
                row.setAction("UNCHANGED");
                unchanged++;
                continue;
            }

            row.setAction("UPDATED");
            updated++;
            if (!dryRun) {
                part.setName(row.getName());
                part.setUnit(row.getUnit());
                part.setUnitPrice(row.getPrice());
                part.setStockQty(row.getStock());
                if (row.getGroup() != null) part.setNote(row.getGroup());
                // Устгасан байсан мөр тайланд дахин гарвал сэргээнэ
                part.setActiveFlag(ACTIVE);
                partRepository.save(part);
            }
        }

        return Result.builder()
                .partTypeId(type.getId())
                .partTypeName(type.getName())
                .detectedType(detected)
                .created(created)
                .updated(updated)
                .unchanged(unchanged)
                .skipped(0)
                .groups(groups)
                .rows(rows)
                .dryRun(dryRun)
                .build();
    }

    /* ══════════ helpers ══════════ */

    /**
     * Давхардсан кодыг нэг мөр болгоно: үлдэгдлийг нэмж, бүлгүүдийг нь
     * тэмдэглэлд жагсаана. Нэр, нэгж, үнийг эхний мөрөөс авна.
     */
    private static List<Row> mergeByCode(List<Row> rows) {
        Map<String, Row> byCode = new LinkedHashMap<>();
        Map<String, List<String>> groups = new LinkedHashMap<>();

        for (Row r : rows) {
            Row seen = byCode.get(r.getCode());
            if (seen == null) {
                byCode.put(r.getCode(), r);
            } else {
                seen.setStock(sum(seen.getStock(), r.getStock()));
                // Нэр, нэгж, үнэ хоосон байсан бол дараагийн мөрөөс нөхнө
                if (blankToNull(seen.getUnit()) == null)  seen.setUnit(r.getUnit());
                if (seen.getPrice() == null)              seen.setPrice(r.getPrice());
            }
            String g = blankToNull(r.getGroup());
            if (g != null) {
                groups.computeIfAbsent(r.getCode(), k -> new ArrayList<>());
                if (!groups.get(r.getCode()).contains(g)) groups.get(r.getCode()).add(g);
            }
        }

        for (Map.Entry<String, List<String>> e : groups.entrySet()) {
            Row r = byCode.get(e.getKey());
            if (r != null) r.setGroup(String.join(", ", e.getValue()));
        }
        return new ArrayList<>(byCode.values());
    }

    private static BigDecimal sum(BigDecimal a, BigDecimal b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.add(b);
    }

    /** Хэрэглэгчийн сонголт давуу; байхгүй бол файлаас таньсан төрөл */
    private RepairPartType resolveType(Long partTypeId, String detected) {
        List<RepairPartType> all = typeRepository.findAll();
        if (partTypeId != null) {
            return all.stream().filter(t -> partTypeId.equals(t.getId())).findFirst().orElse(null);
        }
        if (detected == null) return null;
        return all.stream()
                .filter(t -> detected.equalsIgnoreCase(t.getName()))
                .findFirst()
                .orElse(null);
    }

    /** "... / АВ7602200901 - Д.Сарангэрэл-Орос сэлбэг" → "Орос сэлбэг" */
    private static String tailAfterDash(String raw) {
        int slash = raw.lastIndexOf('/');
        String tail = slash >= 0 ? raw.substring(slash + 1) : raw;
        int dash = tail.lastIndexOf('-');
        return dash >= 0 ? tail.substring(dash + 1).trim() : tail.trim();
    }

    private static BigDecimal number(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            String raw = cell.getStringCellValue().trim().replace(",", "");
            if (raw.isEmpty()) return null;
            return new BigDecimal(raw);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v;
    }

    private static boolean eq(String a, String b) {
        return Objects.equals(blankToNull(a), blankToNull(b));
    }

    private static boolean eqNum(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return a == null && b == null;
        return a.compareTo(b) == 0;
    }
}
