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

    /** Газрын даргын өмнөөс дүгнэх эрхтэй хэрэглэгчид (§2 delegation) */
    private List<Integer> delegateIds;
    private List<String>  delegateNames;

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
        private Integer meetingId;
        private String  meetingNo;
        private LocalDate meetingDate;
        private LocalDateTime submitDeadline;
        private LocalDateTime scoreDeadline;
        private Integer score;
        private Integer scoredBy;
        private String  scoredByName;
        private LocalDateTime scoredAt;
        private String  scoreComment;
        /** 0=нээлттэй, 1=дүгнэгдсэн */
        private Integer status;
        /**
         * Дериватив төлөв (§3.1/§8) — backend now()+score-оор тооцоолно, frontend зөвхөн харуулна:
         * NEW / REVIEWING / IN_PROGRESS / DONE / NOT_DONE / OVERDUE
         */
        private String derivedStatus;
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
        /** 0=ороогүй, 1=илгээгдсэн, 2=буцаагдсан (§3.3) */
        private Integer status;
        private String  returnComment;
        private LocalDateTime returnedAt;
        private List<Evidence> evidence;
    }

    /** Нотлох баримтын файл эсвэл линк */
    @Data
    public static class Evidence {
        private Integer id;
        private String  objectName;
        private String  linkUrl;
        private String  evidenceType;   // FILE / LINK
        private String  fileName;
        private String  contentType;
        private Long    fileSize;
        private LocalDateTime uploadedAt;
    }
}
