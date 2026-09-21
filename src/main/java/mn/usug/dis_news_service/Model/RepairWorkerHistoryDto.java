package mn.usug.dis_news_service.Model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Нэг ажилтны ажлын түүх — аль машинд, ямар засварт, хэдэн цаг ажилласан.
 *
 * ЧУХАЛ: сэлбэг нь ЗАСВАРТ бүртгэгддэг, ажилтанд биш. Тиймээс энд гарч буй
 * сэлбэгийн дүн нь "тухайн ажилтны оролцсон засварт зарцуулсан сэлбэг" гэсэн
 * утгатай. Нэг засварт хоёр хүн ажилласан бол хоёуланд нь ижил дүн харагдана —
 * хүн тус бүрээр хуваарилах өгөгдөл системд байхгүй.
 */
@Data
@Builder
public class RepairWorkerHistoryDto {

    private Long workerId;
    private String workerName;
    private String specialtyName;
    private Integer activeFlag;

    /** Оролцсон засварын тоо */
    private int repairCount;

    /** Хэдэн өөр машин дээр ажилласан */
    private int vehicleCount;

    /** Бүртгэсэн нийт цаг */
    private BigDecimal totalHours;

    /** Оролцсон засваруудад зарцуулсан сэлбэгийн дүн (засварын дүн, хүний биш) */
    private BigDecimal partsAmount;

    private List<Job> jobs;

    @Data
    @Builder
    public static class Job {
        private Long repairId;
        private String plateNumber;
        /** Марк, загвар */
        private String vehicleText;
        private String categoryName;
        private String location;
        private String faultDescription;
        private LocalDate startDate;
        private LocalDate endDate;
        /** 0=засварт байна, 1=дууссан */
        private Integer status;
        /** Тухайн ажилтны бүртгүүлсэн цаг */
        private BigDecimal hours;

        /**
         * Энэ ажлыг бүртгэх үед ажилтан ямар нэрээр бичигдсэн бэ.
         * Лавлах дээрх одоогийн нэрээс зөрвөл UI-д тэмдэглэгдэнэ.
         */
        private String recordedName;
        /** Засварын бүх сэлбэг */
        private List<PartUse> parts;
    }

    @Data
    @Builder
    public static class PartUse {
        private String partName;
        private String partTypeName;
        private BigDecimal qty;
        private String unit;
        private BigDecimal amount;
    }
}
