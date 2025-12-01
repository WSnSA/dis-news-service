package mn.usug.dis_news_service.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class PermissionModel {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("userId")
    private Integer userId;

    @JsonProperty("menuId")
    private Integer menuId;

    @JsonProperty("view")
    private Integer viewRights;

    @JsonProperty("edit")
    private Integer editRights;
}
