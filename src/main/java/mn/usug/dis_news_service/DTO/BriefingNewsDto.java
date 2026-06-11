package mn.usug.dis_news_service.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Шуурхайн мэдээ — нэгж бүрээр (танилцуулгын дараалалд эрэмбэлсэн).
 * id == null бол тухайн нэгж мэдээгээ оруулаагүй (placeholder).
 */
@Data
public class BriefingNewsDto {

    private Integer id;
    private Integer meetingId;
    private Integer unitId;
    private String  unitCode;
    private String  unitName;
    private String  unitType;
    private Integer sortOrder;

    private String  summaryText;
    private String  result;
    private String  extraProposal;
    private String  folderId;
    /** 0=draft, 1=submitted */
    private Integer status;
    private LocalDateTime submittedAt;
    private String  createdByName;

    private List<Evidence> evidence;

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
