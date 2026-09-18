package mn.usug.dis_news_service.Model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Нэг засварын зарцуулалт — сэлбэгийн мөрүүд, ажилласан хүмүүс, нийт дүн.
 * Акт / Шаардах баримтын дүн энэ нийлбэрээс бүрдэнэ.
 */
@Data
@Builder
public class RepairUsageDto {

    private Long vehicleRepairId;
    private List<PartLine> parts;
    private List<WorkerLine> workers;

    /** Сэлбэгийн нийт дүн (₮) — мөр бүрийн тоо × бүртгэсэн үеийн нэгж үнэ */
    private BigDecimal partsTotal;

    /** Ажилласан нийт цаг */
    private BigDecimal totalHours;

    @Data
    @Builder
    public static class PartLine {
        private Long id;
        private Long repairPartId;
        private String partName;
        private String partTypeName;
        private String unit;
        private BigDecimal qty;
        private BigDecimal unitPrice;
        /** qty × unitPrice */
        private BigDecimal amount;
        private String note;
    }

    @Data
    @Builder
    public static class WorkerLine {
        private Long id;
        private Long repairWorkerId;
        private String workerName;
        private String specialtyName;
        private BigDecimal hours;
        private String note;
    }
}
