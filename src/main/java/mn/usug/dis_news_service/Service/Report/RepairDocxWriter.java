package mn.usug.dis_news_service.Service.Report;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Засварын мэдээг Word (.docx) болгож бичнэ — Ц.Нямдоржийн ашигладаг
 * загварын бүтэц, гарчиг, баганын дарааллыг хадгална.
 */
@Service
@RequiredArgsConstructor
public class RepairDocxWriter {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HH:mm");
    private static final String FONT = "Arial";

    private final RepairReportService data;

    /* ══════════════ Өдөр тутмын мэдээ ══════════════ */

    public byte[] daily(LocalDate date) throws IOException {
        List<RepairReportService.Row> rows = data.dailyRows(date);
        RepairReportService.Attendance att = data.attendance(date, date);

        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            landscape(doc);

            // "АВТО БААЗ ДЭЭР-:7 Л.Зэв, … нар ажиллаж байна. Нийт 7 ажилтан."
            String workedNames = String.join(", ", att.worked());
            para(doc, "АВТО БААЗ ДЭЭР-:%d %s нар ажиллаж байна. Нийт %d ажилтан."
                    .formatted(att.worked().size(), workedNames, att.worked().size()), true, 11);

            // "Өвчтэй-1: …     Нөхөн амралт-2: …"
            StringBuilder absent = new StringBuilder();
            if (!att.sick().isEmpty())
                absent.append("Өвчтэй-%d: %s".formatted(att.sick().size(), String.join(", ", att.sick())));
            if (!att.leave().isEmpty()) {
                if (absent.length() > 0) absent.append("        ");
                absent.append("Нөхөн амралт-%d: %s".formatted(att.leave().size(), String.join(", ", att.leave())));
            }
            para(doc, absent.toString(), false, 11);

            XWPFTable table = table(doc, 9);

            // Толгой — загварын баганын дараалал
            header(table.getRow(0), List.of(
                    "Тухайн өдөр засварт орсон машины тоо", "№", "Төрөл", "Дугаар",
                    "Засварт орсон хугацаа", "Шалтгаан", "Төрөл",
                    "Засварлагдсан эсэх", "Бэлэн болох хугацаа"));

            int i = 1;
            for (RepairReportService.Row r : rows) {
                XWPFTableRow tr = table.createRow();
                // 1-р багана зөвхөн эхний мөрөнд — доош нь нэгтгэнэ
                cell(tr, 0, i == 1 ? "%d авто машин / %d".formatted(data.fleetTotal(), rows.size()) : "");
                if (i > 1) vMerge(tr.getCell(0));
                cell(tr, 1, String.valueOf(i));
                cell(tr, 2, r.typeText());
                cell(tr, 3, nz(r.plate()));
                cell(tr, 4, r.startTime() != null ? r.startTime().format(T) : "");
                cell(tr, 5, nz(r.fault()));
                cell(tr, 6, r.section());
                cell(tr, 7, r.done() ? "Бэлэн" : "Засвартай");
                cell(tr, 8, r.expectedReady() != null ? r.expectedReady().format(D) : "");
                i++;
            }
            if (rows.isEmpty()) {
                XWPFTableRow tr = table.createRow();
                cell(tr, 0, "%d авто машин / 0".formatted(data.fleetTotal()));
                for (int c = 1; c < 9; c++) cell(tr, c, "");
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    /* ══════════════ 7 хоногийн тайлан ══════════════ */

    public byte[] weekly(LocalDate from, LocalDate to) throws IOException {
        List<RepairReportService.Row> rows = data.rows(from, to);
        RepairReportService.Attendance att = data.attendance(from, to);
        Map<String, Long> bySection = data.countBySection(rows);
        long ready = rows.stream().filter(RepairReportService.Row::done).count();

        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            para(doc, "АВТО БААЗЫН ЗАСВАРЫН ХЭСГИЙН %d ОНЫ %d-Р САРЫН %d-НЫ ӨДРӨӨС %d ОНЫ %d-Р САРЫН %d НИЙ ӨДРИЙН 7 ХОНОГИЙН АЖЛЫН ТАЙЛАН"
                    .formatted(from.getYear(), from.getMonthValue(), from.getDayOfMonth(),
                               to.getYear(), to.getMonthValue(), to.getDayOfMonth()), true, 12);
            para(doc, "%s                       ЗАСВАРЫН ХЭСЭГ ӨНГӨРӨГЧ 7 ХОНОГТ".formatted(to.format(D)), false, 11);

            para(doc, "Нэг. Ажилласан бүрэлдэхүүн:", true, 11);
            String composition = att.bySpecialty().isEmpty()
                    ? "Ирц бүртгэгдээгүй."
                    : att.bySpecialty().entrySet().stream()
                        .map(e -> "%d-%s".formatted(e.getValue(), e.getKey()))
                        .reduce((a, b) -> a + " " + b).orElse("") + " ажилласан.";
            para(doc, composition, false, 11);

            para(doc, "Хоёр. Шаардлагатай заавар, зөвлөмж өгсөн эсэх:", true, 11);
            para(doc, "Өдөр тутам засварчдадаа ажилд гарахаас өмнө ХАБЭА зааварчилгаа, "
                    + "Ажиллах зөвшөөрөл өгч ажилд хуваарилан ажиллаж байна.", false, 11);

            para(doc, "Гурав. Засвар, үйлчилгээнд орсон техник, хэрэгслийн тоо хэсэг тус бүрээр:", true, 11);
            para(doc, "Нийт давхардсан тоогоор %d тээврийн хэрэгсэл, машин механизм засвар үйлчилгээнд орж   %d тээврийн хэрэгсэл ажилд бэлэн болсон:"
                    .formatted(rows.size(), ready), false, 11);
            para(doc, "Цэвэр усны хэсгийн –  %d т/х".formatted(bySection.getOrDefault("Цэвэр", 0L)), false, 11);
            para(doc, "Бохир усны хэсгийн – %d т/х".formatted(bySection.getOrDefault("Бохир", 0L)), false, 11);
            para(doc, "Үйлчилгээний хэсгийн – %d т/х".formatted(bySection.getOrDefault("Үйлчилгээ", 0L)), false, 11);

            section(doc, rows, "Цэвэр",     "ЦЭВЭР УСНЫ ТЭЭВРИЙН ХЭРЭГСЭЛ");
            section(doc, rows, "Бохир",     "БОХИР УСНЫ ХЭСГИЙН ТЭЭВРИЙН ХЭРЭГСЭЛ");
            section(doc, rows, "Үйлчилгээ", "ҮЙЛЧИЛГЭЭНИЙ ХЭСГИЙН ТЭЭВРИЙН ХЭРЭГСЭЛ");

            para(doc, "Дөрөв. Бусад хийж гүйцэтгэсэн ажил", true, 11);
            para(doc, "", false, 11);
            para(doc, "Тав. Бүрдсэн баримт", true, 11);
            documentSection(doc, from, to);
            para(doc, "", false, 11);
            para(doc, "Зургаа. Асуулт", true, 11);

            doc.write(out);
            return out.toByteArray();
        }
    }

    /** Нэг хэсгийн задаргаа — сэлбэггүй зогссон, засварлагдсан, засварт байгаа */
    private void section(XWPFDocument doc, List<RepairReportService.Row> all, String key, String title) {
        List<RepairReportService.Row> rows = all.stream().filter(r -> r.section().equals(key)).toList();
        if (rows.isEmpty()) return;

        para(doc, title, true, 11);

        List<RepairReportService.Row> waiting = rows.stream()
                .filter(RepairReportService.Row::waitingParts).toList();
        para(doc, "Сэлбэггүй зогсож байгаа – %d".formatted(waiting.size()), false, 11);
        waiting.forEach(r -> para(doc, "%s %s - %s".formatted(nz(r.plate()), r.typeText(), nz(r.fault())), false, 11));

        List<RepairReportService.Row> done = rows.stream().filter(RepairReportService.Row::done).toList();
        para(doc, "Засварлагдсан – %d".formatted(done.size()), false, 11);
        done.forEach(r -> para(doc, "%s %s - %s".formatted(nz(r.plate()), r.typeText(), nz(r.fault())), false, 11));

        List<RepairReportService.Row> inRepair = rows.stream()
                .filter(r -> !r.done() && !r.waitingParts()).toList();
        para(doc, "Одоогоор засварын гаражд байгаа %d".formatted(inRepair.size()), false, 11);
        inRepair.forEach(r -> para(doc, "%s %s - %s".formatted(nz(r.plate()), r.typeText(), nz(r.fault())), false, 11));
    }

    /* ══════════════ Хагас жил / жилийн нэгтгэл ══════════════ */

    public byte[] period(LocalDate from, LocalDate to, String title) throws IOException {
        List<RepairReportService.Row> rows = data.rows(from, to);
        Map<String, Long> bySection = data.countBySection(rows);
        long ready = rows.stream().filter(RepairReportService.Row::done).count();

        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            para(doc, title, true, 13);
            para(doc, "%s — %s".formatted(from.format(D), to.format(D)), false, 11);
            para(doc, "", false, 11);

            para(doc, "Нэг. Засвар үйлчилгээний нэгдсэн үзүүлэлт", true, 11);
            para(doc, "Нийт паркийн тоо – %d".formatted(data.fleetTotal()), false, 11);
            para(doc, "Давхардсан тоогоор засварт орсон – %d".formatted(rows.size()), false, 11);
            para(doc, "Ажилд бэлэн болсон – %d".formatted(ready), false, 11);
            para(doc, "Одоогоор засварт байгаа – %d".formatted(rows.size() - ready), false, 11);
            para(doc, "", false, 11);

            para(doc, "Хоёр. Хэсэг тус бүрээр", true, 11);
            XWPFTable t = table(doc, 4);
            header(t.getRow(0), List.of("Хэсэг", "Засварт орсон", "Бэлэн болсон", "Гүйцэтгэл %"));
            for (String key : List.of("Цэвэр", "Бохир", "Үйлчилгээ")) {
                long total = bySection.getOrDefault(key, 0L);
                long ok = rows.stream().filter(r -> r.section().equals(key) && r.done()).count();
                XWPFTableRow tr = t.createRow();
                cell(tr, 0, key);
                cell(tr, 1, String.valueOf(total));
                cell(tr, 2, String.valueOf(ok));
                cell(tr, 3, total == 0 ? "0%" : "%d%%".formatted(Math.round(ok * 100.0 / total)));
            }

            para(doc, "", false, 11);
            para(doc, "Гурав. Ангиллаар", true, 11);
            Map<String, Long> byCategory = new java.util.LinkedHashMap<>();
            rows.forEach(r -> byCategory.merge(
                    r.categoryName() == null || r.categoryName().isBlank() ? "Тодорхойгүй" : r.categoryName(),
                    1L, Long::sum));
            byCategory.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> para(doc, "%s – %d".formatted(e.getKey(), e.getValue()), false, 11));

            para(doc, "", false, 11);
            para(doc, "Дөрөв. Бүрдсэн баримт", true, 11);
            documentSection(doc, from, to);

            doc.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Тухайн хугацаанд бүрдсэн акт / шаардах. Тайлан уншиж байгаа хүн
     * "аль засварт баримт бичигдсэн бэ" гэдгийг эндээс харна.
     */
    private void documentSection(XWPFDocument doc, LocalDate from, LocalDate to) {
        var docs = data.documents(from, to);
        long acts = docs.stream().filter(x -> "ACT".equals(x.getDocType())).count();
        long reqs = docs.size() - acts;

        para(doc, "", false, 11);
        para(doc, "Бүрдсэн баримт — акт %d, шаардах %d".formatted(acts, reqs), true, 11);
        if (docs.isEmpty()) {
            para(doc, "Энэ хугацаанд баримт бүртгэгдээгүй.", false, 11);
            return;
        }

        XWPFTable t = table(doc, 5);
        header(t.getRow(0), List.of("Огноо", "Төрөл", "Дугаар", "Машин", "Дүн (₮)"));
        for (var x : docs) {
            XWPFTableRow tr = t.createRow();
            cell(tr, 0, x.getDocDate() == null ? "" : x.getDocDate().format(D));
            cell(tr, 1, "ACT".equals(x.getDocType()) ? "Акт" : "Шаардах");
            cell(tr, 2, nz(x.getDocNo()));
            cell(tr, 3, nz(x.getPlateNumber()));
            cell(tr, 4, x.getAmount() == null ? "" : x.getAmount().stripTrailingZeros().toPlainString());
        }
    }

    /* ══════════════ helpers ══════════════ */

    private void landscape(XWPFDocument doc) {
        var sectPr = doc.getDocument().getBody().addNewSectPr();
        var pgSz = sectPr.addNewPgSz();
        pgSz.setOrient(org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation.LANDSCAPE);
        pgSz.setW(java.math.BigInteger.valueOf(16838));
        pgSz.setH(java.math.BigInteger.valueOf(11906));
    }

    private void para(XWPFDocument doc, String text, boolean bold, int size) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text == null ? "" : text);
        r.setBold(bold);
        r.setFontSize(size);
        r.setFontFamily(FONT);
    }

    /**
     * Хүснэгт үүсгэнэ.
     *
     * POI нь createTable() дээр <w:tblGrid> элементийг ҮҮСГЭДЭГГҮЙ бөгөөд
     * түүнгүй .docx файл эвдэрсэн тооцогдож Word "засварлах уу?" гэж асуудаг.
     * Тиймээс багана бүрт gridCol-ийг гараар нэмж өгнө.
     */
    private XWPFTable table(XWPFDocument doc, int cols) {
        XWPFTable t = doc.createTable(1, cols);
        var grid = t.getCTTbl().addNewTblGrid();
        int width = 14400 / cols;
        for (int i = 0; i < cols; i++) {
            grid.addNewGridCol().setW(java.math.BigInteger.valueOf(width));
        }
        t.setWidth("100%");
        return t;
    }

    private void header(XWPFTableRow row, List<String> titles) {
        for (int i = 0; i < titles.size(); i++) {
            XWPFTableCell c = row.getCell(i);
            if (c == null) continue;
            c.removeParagraph(0);
            XWPFParagraph p = c.addParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun r = p.createRun();
            r.setText(titles.get(i));
            r.setBold(true);
            r.setFontSize(10);
            r.setFontFamily(FONT);
            c.setColor("EFEFEF");
        }
    }

    private void cell(XWPFTableRow row, int index, String text) {
        XWPFTableCell c = row.getCell(index);
        if (c == null) return;
        if (!c.getParagraphs().isEmpty()) c.removeParagraph(0);
        XWPFParagraph p = c.addParagraph();
        XWPFRun r = p.createRun();
        r.setText(text == null ? "" : text);
        r.setFontSize(10);
        r.setFontFamily(FONT);
    }

    /** Баганыг дээд мөртэй нь босоогоор нэгтгэнэ */
    private void vMerge(XWPFTableCell cell) {
        var tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        var merge = tcPr.isSetVMerge() ? tcPr.getVMerge() : tcPr.addNewVMerge();
        merge.setVal(STMerge.CONTINUE);
    }

    private static String nz(String v) { return v == null ? "" : v; }
}
