package mn.usug.dis_news_service.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Шуурхай хурлын үүрэг даалгаврын бүрэн мод (frontend нэг дуудлагаар авна).
 * task → departments + cycles → fulfillments → evidence
 */
@Data
public class BriefingDto {

    private Integer id;
    private Integer assignerId;
    private String  assignerName;
    private String  description;
    /** 0=идэвхтэй, 1=бүрэн биелсэн */
    private Integer status;
    private Integer createdBy;
    private String  createdByName;
    private LocalDateTime createdDate;

    private List<DepRef> departments;
    private List<Cycle>  cycles;

    /** Холбогдох алба */
    @Data
    public static class DepRef {
        private Integer departmentId;
        private String  depName;
    }

    /** 7 хоног тутмын мөчлөг */
    @Data
    public static class Cycle {
        private Integer id;
        private Integer cycleNo;
        private LocalDate meetingDate;
        private LocalDateTime submitDeadline;
        private LocalDateTime scoreDeadline;
        private Integer score;
        private Integer scoredBy;
        private String  scoredByName;
        private LocalDateTime scoredAt;
        /** 0=нээлттэй, 1=дүгнэгдсэн */
        private Integer status;
        private List<Fulfillment> fulfillments;
    }

    /** Алба тус бүрийн биелэлт */
    @Data
    public static class Fulfillment {
        private Integer id;
        private Integer departmentId;
        private String  depName;
        private String  workText;
        private String  folderId;
        private Integer submittedBy;
        private String  submittedByName;
        private LocalDateTime submittedAt;
        private List<Evidence> evidence;
    }

    /** Нотлох баримтын файл */
    @Data
    public static class Evidence {
        private Integer id;
        private String  objectName;
        private String  fileName;
        private String  contentType;
        private Long    fileSize;
        private LocalDateTime uploadedAt;
    }
}
