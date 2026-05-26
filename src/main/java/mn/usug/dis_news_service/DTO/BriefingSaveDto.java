package mn.usug.dis_news_service.DTO;

import lombok.Data;

import java.util.List;

/** Үүрэг даалгавар бүртгэх / засах оролт (шуурхай хурал дээр бичигч хэрэглэгч). */
@Data
public class BriefingSaveDto {

    /** null=шинэ, утгатай=засах */
    private Integer id;

    /** Үүрэг өгсөн ба дүгнэх албан тушаалтан (can_assign_task=1) */
    private Integer assignerId;

    private String description;

    /** Холбогдох албадын departmentId жагсаалт (1-с доошгүй) */
    private List<Integer> departmentIds;
}
