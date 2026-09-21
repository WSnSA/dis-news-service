package mn.usug.dis_news_service.Service.Report;

import mn.usug.dis_news_service.Entity.RepairDocument;
import mn.usug.dis_news_service.Entity.RepairDocumentItem;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Шаардах хуудас (НХМаягт БМ-6) ба Акт — Word (.docx).
 *
 * БМ-6 нь Сангийн сайдын 2017 оны 12 дугаар сарын 5-ны өдрийн 347 тоот
 * тушаалын хавсралт маягт. Гар аргаар бөглөдөг цаасны бүтцийг давтана:
 * толгойн мөрүүд, 14 мөртэй хүснэгт, доод талын гарын үсгийн мөрүүд.
 */
@Service
public class RepairFormDocxWriter {

    private static final String FONT = "Arial";
    /** Маягтын хүснэгт 14 мөртэй — цаасан дээрхтэй ижил */
    private static final int FORM_ROWS = 14;

    /* ══════════════ Шаардах хуудас (БМ-6) ══════════════ */

    public void request(RepairDocument doc, OutputStream out) throws IOException {
        try (XWPFDocument d = new XWPFDocument()) {
            // Баруун дээд булан — тушаалын хавсралтын тэмдэглэгээ
            right(d, "Сангийн сайдын 2017 оны 12 дугаар сарын", 8);
            right(d, "5-ны өдрийн 347 тоот тушаалын хавсралт", 8);

            XWPFParagraph top = d.createParagraph();
            run(top, "НХМаягт БМ-6", false, 9);

            XWPFParagraph title = d.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            run(title, "ШААРДАХ ХУУДАС №" + nz(doc.getDocNo()), true, 14);

            LocalDate date = doc.getDocDate() != null ? doc.getDocDate() : LocalDate.now();
            XWPFParagraph dp = d.createParagraph();
            dp.setAlignment(ParagraphAlignment.RIGHT);
            run(dp, "%d оны %d сарын %d өдөр".formatted(date.getYear(), date.getMonthValue(), date.getDayOfMonth()),
                    false, 10);

            field(d, "", nz(doc.getToPlace()).isBlank() ? "УСУГ" : "УСУГ", "(байгууллагын нэр)");
            field(d, "Хэнээс", nz(doc.getFromPerson()), "(Овог нэр, албан тушаал)");
            field(d, "Хаана", nz(doc.getToPlace()), "(Цех, тасаг, алба)");

            // Зориулалт — машины дугаар ба тайлбар нэг мөрөнд
            String purpose = nz(doc.getPurpose());
            if (purpose.isBlank()) purpose = nz(doc.getContent());
            String plate = nz(doc.getPlateNumber());
            if (!plate.isBlank()) {
                purpose = purpose.isBlank() ? plate + " — засварт" : plate + " — " + purpose;
            }
            field(d, "Зориулалт", purpose, null);

            blank(d);

            XWPFTable t = table(d, 7);
            header(t.getRow(0), List.of("№", "Материалын үнэт зүйлийн нэр", "Код",
                    "Хэмжих нэгж", "Хүссэн", "Зөвшөөрсөн", "Олгосон"));

            List<RepairDocumentItem> items = doc.getItems() != null ? doc.getItems() : List.of();
            for (int i = 0; i < Math.max(FORM_ROWS, items.size()); i++) {
                XWPFTableRow row = t.createRow();
                cell(row, 0, String.valueOf(i + 1));
                if (i < items.size()) {
                    RepairDocumentItem it = items.get(i);
                    cell(row, 1, nz(it.getItemName()));
                    cell(row, 2, nz(it.getItemCode()));
                    cell(row, 3, nz(it.getUnit()));
                    cell(row, 4, num(it.getQtyRequested()));
                    cell(row, 5, num(it.getQtyApproved()));
                    cell(row, 6, num(it.getQtyIssued()));
                } else {
                    for (int c = 1; c < 7; c++) cell(row, c, "");
                }
            }

            blank(d);

            // TEXT хэлбэрийн шаардахад тайлбарыг доор нь бичнэ
            if ("TEXT".equals(doc.getDocMode()) && !nz(doc.getContent()).isBlank()) {
                para(d, nz(doc.getContent()), false, 10);
                blank(d);
            }

            para(d, "Зөвшөөрсөн:", true, 10);
            signLine(d, "Дарга");
            signLine(d, "Нягтлан бодогч");
            signLine(d, "Шаардах хуудас бичсэн");
            blank(d);
            signLine(d, "Олгосон", nz(doc.getIssuerName()));
            signLine(d, "Хүлээн авсан", nz(doc.getReceiverName()));

            d.write(out);
        }
    }

    /* ══════════════ Техникийн комиссын акт ══════════════ */

    /**
     * ДМYА маягтын акт. Элэгдсэн эд ангийг комисс шалгаж, гүйлтийн норм
     * хэдэн км байснаас хэд явсныг тэмдэглэн, дутууг хэн хариуцахыг
     * тогтоодог. Цаасан хувилбарын бүтцийг дагана.
     */
    public void act(RepairDocument doc, OutputStream out) throws IOException {
        try (XWPFDocument d = new XWPFDocument()) {
            // Баруун дээд булан — "УСУГ  ДМYА № 12"
            right(d, "УСУГ   ДМYА № " + nz(doc.getFormNo()), 11);
            blank(d);

            // БАТЛАВ блок — баруун гар талд
            right(d, "БАТЛАВ:", 11);
            right(d, nz(doc.getApproverTitle()).isBlank()
                    ? "УС СУВГИЙН УДИРДАХ ГАЗРЫН АВТО БААЗЫН ДАРГА"
                    : doc.getApproverTitle().toUpperCase(), 11);
            right(d, nz(doc.getApproverName()), 11);
            blank(d);

            XWPFParagraph title = d.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            run(title, "ТЕХНИКИЙН КОМИССЫН АКТ № " + nz(doc.getDocNo()), true, 13);
            blank(d);

            // Огноо зүүн, хот баруун талд
            LocalDate date = doc.getDocDate() != null ? doc.getDocDate() : LocalDate.now();
            XWPFParagraph dp = d.createParagraph();
            dp.setAlignment(ParagraphAlignment.BOTH);
            run(dp, "%d оны %02d дүгээр сарын %d".formatted(
                    date.getYear(), date.getMonthValue(), date.getDayOfMonth()), false, 11);
            run(dp, "\t\t\t\t\t", false, 11);
            run(dp, nz(doc.getCity()).isBlank() ? "Улаанбаатар хот" : doc.getCity(), false, 11);
            blank(d);

            // Комиссын бүрэлдэхүүн + шалгасан машин — нэг догол мөр
            List<String[]> members = parseCommission(doc.getCommission());
            String people = members.stream()
                    .map(m -> (m[0].isBlank() ? "" : m[0] + " ") + m[1])
                    .filter(x -> !x.isBlank())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            StringBuilder intro = new StringBuilder();
            if (!people.isBlank()) intro.append(people).append(" нарын ");
            intro.append("техник, эдийн засгийн байнгын комиссоос ");
            if (!nz(doc.getCommissionDriver()).isBlank()) {
                intro.append("жолооч ").append(doc.getCommissionDriver()).append(" оролцуулан ");
            }
            intro.append(nz(doc.getPlateNumber())).append(" улсын дугаартай ");
            if (!nz(doc.getVehicleBrand()).isBlank()) {
                intro.append(doc.getVehicleBrand()).append(" маркийн ");
            }
            intro.append("автомашины техникийн байдлыг шалгаж үзлээ.");
            justify(d, intro.toString());
            blank(d);

            // 1. Эд ангийн гүйлтийн харьцуулалт
            justify(d, "1. " + wearText(doc));
            blank(d);

            XWPFParagraph verdict = d.createParagraph();
            verdict.setAlignment(ParagraphAlignment.CENTER);
            run(verdict, "КОМИССООС ТОГТООСОН НЬ :", false, 11);
            blank(d);

            if (!nz(doc.getFinding()).isBlank()) {
                justify(d, "1. " + doc.getFinding());
            }
            justify(d, "2. Эвдрэлийн шалтгаан, хариуцах эзэн, төлбөрийн хэмжээг бич:");
            if (!nz(doc.getLiability()).isBlank()) {
                justify(d, "   " + doc.getLiability());
            }
            blank(d);
            blank(d);

            // Гарын үсгийн блок
            if (members.isEmpty()) {
                signRow(d, "КОМИССЫН ДАРГА :", "");
            } else {
                String[] head = members.get(0);
                signRow(d, "КОМИССЫН ДАРГА :", "");
                signRow(d, "   " + head[0].toUpperCase(), head[1].toUpperCase());
                if (members.size() > 1) {
                    para(d, "ГИШҮҮД:", false, 11);
                    for (int i = 1; i < members.size(); i++) {
                        signRow(d, "   " + members.get(i)[0].toUpperCase(),
                                members.get(i)[1].toUpperCase());
                    }
                }
            }
            if (!nz(doc.getCommissionDriver()).isBlank()) {
                signRow(d, "ЗӨВШӨӨРСӨН ЖОЛООЧ", doc.getCommissionDriver().toUpperCase());
            }

            d.write(out);
        }
    }

    /**
     * "2 ширхэг резинэн дугуй 50000 км яваахаас нь, ашиглалтад орсноос хойш
     * 43478 км явсан байна. Дутуу 6522 км." гэсэн мөрийг бүрдүүлнэ.
     */
    private String wearText(RepairDocument doc) {
        StringBuilder sb = new StringBuilder("Уг тээврийн хэрэгслийн хувийн хэргээс үзэхэд ");

        List<RepairDocumentItem> items = doc.getItems() != null ? doc.getItems() : List.of();
        if (!items.isEmpty()) {
            RepairDocumentItem it = items.get(0);
            BigDecimal qty = it.getQtyIssued() != null ? it.getQtyIssued() : it.getQtyRequested();
            if (qty != null && qty.signum() > 0) {
                sb.append(num(qty)).append(" ").append(nz(it.getUnit()).isBlank() ? "ширхэг" : it.getUnit())
                  .append(" ");
            }
            sb.append(nz(it.getItemName())).append(" ");
        }

        if (doc.getNormKm() != null) {
            sb.append(doc.getNormKm()).append(" км яваахаас нь, ");
        }
        if (doc.getActualKm() != null) {
            sb.append("ашиглалтад орсноос хойш ").append(doc.getActualKm()).append(" км явсан байна. ");
        }
        if (doc.getNormKm() != null && doc.getActualKm() != null) {
            int short_ = doc.getNormKm() - doc.getActualKm();
            sb.append(short_ >= 0 ? "Дутуу " : "Илүү ").append(Math.abs(short_)).append(" км.");
        }
        return sb.toString().trim();
    }

    /** commission нь [{"title":"АХЛАХ ИНЖЕНЕР","name":"Д.ЧАГНААДОРЖ"}] JSON */
    private List<String[]> parseCommission(String json) {
        List<String[]> out = new ArrayList<>();
        if (json == null || json.isBlank()) return out;
        try {
            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            if (!node.isArray()) return out;
            for (var n : node) {
                String t = n.path("title").asText("").trim();
                String nm = n.path("name").asText("").trim();
                if (t.isBlank() && nm.isBlank()) continue;
                out.add(new String[]{ t, nm });
            }
        } catch (Exception ignored) {
            // Гэмтэлтэй JSON — гарын үсгийн блокгүйгээр үргэлжилнэ
        }
        return out;
    }

    /** Зүүн талд албан тушаал, баруун талд нэр */
    private void signRow(XWPFDocument d, String left, String rightText) {
        XWPFParagraph p = d.createParagraph();
        run(p, left, false, 11);
        if (!rightText.isBlank()) {
            run(p, "\t\t\t\t", false, 11);
            run(p, rightText, false, 11);
        }
    }

    private void justify(XWPFDocument d, String text) {
        XWPFParagraph p = d.createParagraph();
        p.setAlignment(ParagraphAlignment.BOTH);
        run(p, text, false, 11);
    }

    /* ══════════════ helpers ══════════════ */

    /** "Хэнээс ____ Утга ____" гэсэн зураасан мөр + доор нь жижиг тайлбар */
    private void field(XWPFDocument d, String label, String value, String hint) {
        XWPFParagraph p = d.createParagraph();
        if (!label.isBlank()) run(p, label + "  ", false, 10);
        run(p, value.isBlank() ? "_".repeat(60) : value + "  " + "_".repeat(Math.max(4, 50 - value.length())),
                false, 10);
        if (hint != null) {
            XWPFParagraph h = d.createParagraph();
            h.setAlignment(ParagraphAlignment.CENTER);
            run(h, hint, false, 7);
        }
    }

    private void signLine(XWPFDocument d, String label) {
        signLine(d, label, "");
    }

    private void signLine(XWPFDocument d, String label, String value) {
        XWPFParagraph p = d.createParagraph();
        run(p, "        " + label + ": " + (value.isBlank() ? "" : value + " ") + ".".repeat(40), false, 10);
    }

    private void para(XWPFDocument d, String text, boolean bold, int size) {
        run(d.createParagraph(), text, bold, size);
    }

    private void right(XWPFDocument d, String text, int size) {
        XWPFParagraph p = d.createParagraph();
        p.setAlignment(ParagraphAlignment.RIGHT);
        run(p, text, false, size);
    }

    private void blank(XWPFDocument d) {
        d.createParagraph();
    }

    private void run(XWPFParagraph p, String text, boolean bold, int size) {
        XWPFRun r = p.createRun();
        r.setText(text == null ? "" : text);
        r.setBold(bold);
        r.setFontSize(size);
        r.setFontFamily(FONT);
    }

    /**
     * POI нь createTable() дээр &lt;w:tblGrid&gt; элементийг ҮҮСГЭДЭГГҮЙ бөгөөд
     * түүнгүй .docx эвдэрсэн тооцогдоно. Багана бүрт gridCol гараар нэмнэ.
     */
    private XWPFTable table(XWPFDocument d, int cols) {
        XWPFTable t = d.createTable(1, cols);
        var grid = t.getCTTbl().addNewTblGrid();
        int width = 9360 / cols;
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
            run(p, titles.get(i), true, 9);
            c.setColor("EFEFEF");
        }
    }

    private void cell(XWPFTableRow row, int index, String text) {
        XWPFTableCell c = row.getCell(index);
        if (c == null) return;
        if (!c.getParagraphs().isEmpty()) c.removeParagraph(0);
        XWPFParagraph p = c.addParagraph();
        run(p, text, false, 9);
    }

    /** Бүхэл тоо бол аравтын оронг хасна — "2" гэж бичнэ, "2.00" биш */
    private static String num(BigDecimal v) {
        if (v == null || v.signum() == 0) return "";
        return v.stripTrailingZeros().toPlainString();
    }

    private static String nz(String v) {
        return v == null ? "" : v;
    }
}
