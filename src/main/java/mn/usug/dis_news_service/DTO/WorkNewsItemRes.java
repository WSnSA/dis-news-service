package mn.usug.dis_news_service.DTO;
// dto/WorkNewsItemRes.java
public record WorkNewsItemRes(
        Long id,
        String itemType,
        String title,
        String content,
        Integer sortOrder,
        String createdAt,
        Long createdBy,
        String createdByName,
        Long departmentId,
        String departmentName
) {}
