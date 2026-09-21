package mn.usug.dis_news_service.Service.Report;

import mn.usug.dis_news_service.Entity.RepairDocument;
import mn.usug.dis_news_service.Entity.RepairDocumentItem;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    /* ══════════════ Акт ══════════════ */

    public void act(RepairDocument doc, OutputStream out) throws IOException {
        try (XWPFDocument d = new XWPFDocument()) {
            XWPFParagraph title = d.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            run(title, "АКТ №" + nz(doc.getDocNo()), true, 14);

            LocalDate date = doc.getDocDate() != null ? doc.getDocDate() : LocalDate.now();
            XWPFParagraph dp = d.createParagraph();
            dp.setAlignment(ParagraphAlignment.RIGHT);
            run(dp, "%d оны %d сарын %d өдөр".formatted(date.getYear(), date.getMonthValue(), date.getDayOfMonth()),
                    false, 10);

            field(d, "Машин", nz(doc.getPlateNumber()), null);
            field(d, "Хэнээс", nz(doc.getFromPerson()), "(Овог нэр, албан тушаал)");
            field(d, "Зориулалт", nz(doc.getPurpose()), null);

            blank(d);

            List<RepairDocumentItem> items = doc.getItems() != null ? doc.getItems() : List.of();
            if (!items.isEmpty()) {
                XWPFTable t = table(d, 6);
                header(t.getRow(0), List.of("№", "Нэр", "Код", "Хэмжих нэгж", "Тоо", "Дүн (₮)"));

                BigDecimal total = BigDecimal.ZERO;
                int i = 1;
                for (RepairDocumentItem it : items) {
                    BigDecimal qty   = it.getQtyIssued() != null ? it.getQtyIssued() : it.getQtyRequested();
                    BigDecimal price = it.getUnitPrice() != null ? it.getUnitPrice() : BigDecimal.ZERO;
                    BigDecimal sum   = (qty != null ? qty : BigDecimal.ZERO).multiply(price);
                    total = total.add(sum);

                    XWPFTableRow row = t.createRow();
                    cell(row, 0, String.valueOf(i++));
                    cell(row, 1, nz(it.getItemName()));
                    cell(row, 2, nz(it.getItemCode()));
                    cell(row, 3, nz(it.getUnit()));
                    cell(row, 4, num(qty));
                    cell(row, 5, num(sum));
                }

                XWPFTableRow sumRow = t.createRow();
                cell(sumRow, 0, "");
                cell(sumRow, 1, "Нийт");
                cell(sumRow, 2, "");
                cell(sumRow, 3, "");
                cell(sumRow, 4, "");
                cell(sumRow, 5, num(doc.getAmount() != null ? doc.getAmount() : total));
                blank(d);
            } else if (doc.getAmount() != null) {
                para(d, "Нийт дүн: " + num(doc.getAmount()) + "₮", true, 11);
                blank(d);
            }

            if (!nz(doc.getContent()).isBlank()) {
                para(d, nz(doc.getContent()), false, 10);
                blank(d);
            }

            para(d, "Зөвшөөрсөн:", true, 10);
            signLine(d, "Дарга");
            signLine(d, "Нягтлан бодогч");
            blank(d);
            signLine(d, "Актыг бичсэн", nz(doc.getIssuerName()));
            signLine(d, "Хүлээн авсан", nz(doc.getReceiverName()));

            d.write(out);
        }
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
