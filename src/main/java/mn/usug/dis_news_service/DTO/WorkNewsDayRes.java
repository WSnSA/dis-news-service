package mn.usug.dis_news_service.DTO;
// dto/WorkNewsDayRes.java
import java.util.List;

public record WorkNewsDayRes(
        String newsDate,     // yyyy-MM-dd
        String monthKey,     // yyyy/MM
        List<WorkNewsItemRes> items
) {}