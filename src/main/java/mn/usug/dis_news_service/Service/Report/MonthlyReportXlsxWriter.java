package mn.usug.dis_news_service.Service.Report;

import mn.usug.dis_news_service.DTO.MonthlyReportDto;
import mn.usug.dis_news_service.DTO.MonthlyReportDto.Line;
import mn.usug.dis_news_service.DTO.MonthlyReportDto.Section;
import mn.usug.dis_news_service.DTO.MonthlyReportDto.Signer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * "Сарын мэдээ"-г диспетчерийн жишээ файлын (water_ttlnews) загвараар Excel (.xlsx) болгоно.
 *
 * Багана: A Д/д | B Эх үүсвэр | C Нэгж | D–F Тухайн сар | G–I Өссөн дүнгээр
 */
@Component
public class MonthlyReportXlsxWriter {

    private static final int LAST_COL = 8; // I
    private static final byte[] HEADER_RGB = {(byte) 0x99, (byte) 0x99, (byte) 0xFF};

    public byte[] write(MonthlyReportDto r) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles st = new Styles(wb);
            Sheet sh = wb.createSheet("water_ttlnews");

            int[] widths = {5, 26, 6, 13, 13, 13, 14, 14, 14};
            for (int i = 0; i < widths.length; i++) sh.setColumnWidth(i, widths[i] * 256);

            int row = 0;
            // Гарчиг
            Row t = sh.createRow(row);
            t.setHeightInPoints(30);
            cell(t, 0, r.title(), st.title);
            merge(sh, row, row, 0, LAST_COL, null);
            row += 2;

            // Огноо (баруун тийш)
            Row d = sh.createRow(row);
            cell(d, 3, r.dateLabel(), st.date);
            merge(sh, row, row, 3, LAST_COL, null);
            row++;

            boolean first = true;
            for (Section s : r.sections()) {
                if (!first) {
                    row++;
                    Row sr = sh.createRow(row);
                    sr.setHeightInPoints(20);
                    cell(sr, 0, s.title(), st.sectionTitle);
                    merge(sh, row, row, 0, LAST_COL, null);
                    row += 2;
                }
                first = false;
                row = header(sh, st, row, s, r.ytdLabel());
                for (Line l : s.lines()) row = line(sh, st, row, s, l);
            }

            // Гарын үсэг
            row++;
            for (Signer g : r.signers()) {
                Row sr = sh.createRow(row);
                sr.setHeightInPoints(18);
                cell(sr, 1, g.title() + ":", st.signTitle);
                merge(sh, row, row, 1, 4, null);
                String name = g.name() == null || g.name().isBlank() ? " " : g.name();
                cell(sr, 5, "/          " + name + "          /", st.signName);
                merge(sh, row, row, 5, LAST_COL, null);
                row++;
            }

            // Хэвлэх тохиргоо — A4 босоо, нэг хуудасны өргөнд
            PrintSetup ps = sh.getPrintSetup();
            ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
            ps.setLandscape(false);
            ps.setFitWidth((short) 1);
            ps.setFitHeight((short) 0);
            sh.setFitToPage(true);
            sh.setMargin(Sheet.LeftMargin, 0.4);
            sh.setMargin(Sheet.RightMargin, 0.3);
            sh.setMargin(Sheet.TopMargin, 0.5);
            sh.setMargin(Sheet.BottomMargin, 0.5);
            sh.setHorizontallyCenter(true);

            wb.write(out);
            return out.toByteArray();
        }
    }

    private int header(Sheet sh, Styles st, int row, Section s, String ytdLabel) {
        Row h1 = sh.createRow(row);
        Row h2 = sh.createRow(row + 1);
        h1.setHeightInPoints(18);
        h2.setHeightInPoints(36);
        for (int c = 0; c <= LAST_COL; c++) {
            h1.createCell(c).setCellStyle(st.header);
            h2.createCell(c).setCellStyle(st.header);
        }
        h1.getCell(0).setCellValue("Д/д");
        h1.getCell(1).setCellValue("Эх үүсвэрүүд");
        h1.getCell(2).setCellValue(s.showUnit() ? "Нэгж" : "");
        h1.getCell(3).setCellValue("Тухайн сар");
        h1.getCell(6).setCellValue(ytdLabel);
        h2.getCell(3).setCellValue(s.monthActualLabel());
        h2.getCell(4).setCellValue("Төлөвлөгөө /м3/");
        h2.getCell(5).setCellValue(s.monthDiffLabel());
        h2.getCell(6).setCellValue(s.ytdActualLabel());
        h2.getCell(7).setCellValue("Төлөвлөгөө /м3/");
        h2.getCell(8).setCellValue(s.ytdDiffLabel());
        merge(sh, row, row + 1, 0, 0, st);
        merge(sh, row, row + 1, 1, 1, st);
        merge(sh, row, row + 1, 2, 2, st);
        merge(sh, row, row, 3, 5, st);
        merge(sh, row, row, 6, 8, st);
        return row + 2;
    }

    private int line(Sheet sh, Styles st, int row, Section s, Line l) {
        boolean bold = !"ROW".equals(l.kind());
        Row x = sh.createRow(row);
        x.setHeightInPoints(15);
        CellStyle num = bold ? st.numBold : st.num;

        if ("ROW".equals(l.kind())) {
            cell(x, 0, l.no(), st.no);
            cell(x, 1, l.name(), st.text);
        } else {
            // ДҮН / НИЙТ — А:В нэгтгэж баруун тийш
            cell(x, 0, l.name(), st.totalLabel);
            x.createCell(1).setCellStyle(st.totalLabel);
            merge(sh, row, row, 0, 1, null);
        }
        cell(x, 2, s.showUnit() ? l.unit() : null, bold ? st.centerBold : st.center);
        cell(x, 3, l.monthActual(), num);
        cell(x, 4, l.monthPlan(), num);
        cell(x, 5, l.monthDiff(), num);
        cell(x, 6, l.ytdActual(), num);
        cell(x, 7, l.ytdPlan(), num);
        cell(x, 8, l.ytdDiff(), num);
        return row + 1;
    }

    /* ── helpers ── */

    private static void cell(Row r, int c, Object v, CellStyle style) {
        Cell cell = r.getCell(c) != null ? r.getCell(c) : r.createCell(c);
        if (v instanceof Number n) cell.setCellValue(n.doubleValue());
        else if (v != null) cell.setCellValue(String.valueOf(v));
        cell.setCellStyle(style);
    }

    /** Нэгтгэсэн мужид хүрээ (border) өгнө — style==null бол хүрээгүй */
    private static void merge(Sheet sh, int r1, int r2, int c1, int c2, CellStyle bordered) {
        if (r1 == r2 && c1 == c2) return;
        CellRangeAddress a = new CellRangeAddress(r1, r2, c1, c2);
        sh.addMergedRegion(a);
        if (bordered != null) {
            RegionUtil.setBorderTop(BorderStyle.THIN, a, sh);
            RegionUtil.setBorderBottom(BorderStyle.THIN, a, sh);
            RegionUtil.setBorderLeft(BorderStyle.THIN, a, sh);
            RegionUtil.setBorderRight(BorderStyle.THIN, a, sh);
        }
    }

    private static final class Styles {
        final CellStyle title, date, sectionTitle, header, no, text, textBold, center, centerBold,
                num, numBold, totalLabel, signTitle, signName;

        Styles(XSSFWorkbook wb) {
            Font base = font(wb, 9, false);
            Font bold = font(wb, 9, true);

            title = wb.createCellStyle();
            title.setFont(font(wb, 11, true));
            title.setAlignment(HorizontalAlignment.CENTER);
            title.setVerticalAlignment(VerticalAlignment.CENTER);
            title.setWrapText(true);

            date = wb.createCellStyle();
            date.setFont(base);
            date.setAlignment(HorizontalAlignment.RIGHT);

            sectionTitle = wb.createCellStyle();
            sectionTitle.setFont(font(wb, 11, true));
            sectionTitle.setAlignment(HorizontalAlignment.CENTER);

            XSSFCellStyle h = wb.createCellStyle();
            h.setFont(bold);
            h.setAlignment(HorizontalAlignment.CENTER);
            h.setVerticalAlignment(VerticalAlignment.CENTER);
            h.setWrapText(true);
            h.setFillForegroundColor(new XSSFColor(HEADER_RGB, null));
            h.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            border(h);
            header = h;

            no = bordered(wb, base, HorizontalAlignment.CENTER);
            text = bordered(wb, base, HorizontalAlignment.LEFT);
            textBold = bordered(wb, bold, HorizontalAlignment.LEFT);
            center = bordered(wb, base, HorizontalAlignment.CENTER);
            centerBold = bordered(wb, bold, HorizontalAlignment.CENTER);
            totalLabel = bordered(wb, bold, HorizontalAlignment.RIGHT);

            DataFormat fmt = wb.createDataFormat();
            num = bordered(wb, base, HorizontalAlignment.RIGHT);
            num.setDataFormat(fmt.getFormat("0"));
            numBold = bordered(wb, bold, HorizontalAlignment.RIGHT);
            numBold.setDataFormat(fmt.getFormat("0"));

            signTitle = wb.createCellStyle();
            signTitle.setFont(base);
            signTitle.setAlignment(HorizontalAlignment.LEFT);
            signName = wb.createCellStyle();
            signName.setFont(base);
            signName.setAlignment(HorizontalAlignment.CENTER);
        }

        private static Font font(XSSFWorkbook wb, int size, boolean bold) {
            XSSFFont f = wb.createFont();
            f.setFontName("Arial");
            f.setFontHeightInPoints((short) size);
            f.setBold(bold);
            return f;
        }

        private static CellStyle bordered(XSSFWorkbook wb, Font f, HorizontalAlignment align) {
            CellStyle s = wb.createCellStyle();
            s.setFont(f);
            s.setAlignment(align);
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            border(s);
            return s;
        }

        private static void border(CellStyle s) {
            s.setBorderTop(BorderStyle.THIN);
            s.setBorderBottom(BorderStyle.THIN);
            s.setBorderLeft(BorderStyle.THIN);
            s.setBorderRight(BorderStyle.THIN);
        }
    }
}
