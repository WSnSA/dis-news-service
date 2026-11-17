package mn.usug.dis_news_service.DTO;

import java.util.List;

/**
 * "Сарын мэдээ" — олборлосон болон татан зайлуулж цэвэрлэсэн ус.
 * Дэлгэцийн preview ба Excel хоёулаа энэ бүтцээс зурагдана (тоо таарна).
 */
public record MonthlyReportDto(
        int year,
        int month,
        /** "2026 ОНЫ 11 САРЫН ОЛБОРЛОСОН БОЛОН ..." */
        String title,
        /** "2026 оны 11 сарын 30" */
        String dateLabel,
        /** "Өссөн дүнгээр /01-11 сар/" */
        String ytdLabel,
        List<Section> sections,
        List<Signer> signers
) {

    /**
     * @param code         WATER | SEWAGE
     * @param title        хэсгийн гарчиг (WATER-д null — үндсэн гарчиг дор шууд гарна)
     * @param showUnit     "Нэгж" багана харуулах эсэх
     */
    public record Section(
            String code,
            String title,
            boolean showUnit,
            String monthActualLabel,
            String monthDiffLabel,
            String ytdActualLabel,
            String ytdDiffLabel,
            List<Line> lines
    ) {}

    /**
     * @param kind ROW | SUBTOTAL (ДҮН) | TOTAL (НИЙТ)
     * Тоо null = өгөгдөл/төлөвлөгөө байхгүй (хоосон нүд)
     */
    public record Line(
            String kind,
            Integer no,
            Integer rowId,
            String name,
            String unit,
            Long monthActual,
            Long monthPlan,
            Long monthDiff,
            Long ytdActual,
            Long ytdPlan,
            Long ytdDiff
    ) {}

    public record Signer(String title, String name) {}
}
