package mn.usug.dis_news_service.DTO;



public record WorkNewsItemCreateReq(
        String newsDate,      // "2026-02-13"
        String itemType,               // null бол OTHER
        String title,
        String content,
        Integer sortOrder,
        String metaJson,
        Long departmentId,             // хэрэглэгчийн алба
        Long userId                    // хэрэглэгчийн id
) {}
