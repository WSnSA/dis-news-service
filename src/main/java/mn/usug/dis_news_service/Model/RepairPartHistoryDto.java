package mn.usug.dis_news_service.Model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Нэг сэлбэгийн хэрэглээний түүх — аль машинд, хэзээ, хэдэн ширхэг зарцуулсан бэ.
 */
@Data
@Builder
public class RepairPartHistoryDto {

    private Long partId;
    private String partName;
    private String partTypeName;
    private String unit;
    private Integer activeFlag;

    /** Хэдэн удаа зарцуулагдсан (мөрийн тоо) */
    private int useCount;
    /** Нийт зарцуулсан тоо хэмжээ */
    private BigDecimal totalQty;
    /** Нийт дүн */
    private BigDecimal totalAmount;

    private List<Use> uses;

    @Data
    @Builder
    public static class Use {
        private Long repairId;
        private String plateNumber;
        /** Марк, загвар */
        private String vehicleText;
        private String categoryName;
        private BigDecimal qty;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private String note;
        /** Зарцуулалт бүртгэсэн огноо */
        private LocalDate usedDate;
    }
}
