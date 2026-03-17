package mn.usug.dis_news_service.Model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;

@Data
public class MenuModel {
    private Integer id;
    private Integer parentId;
    private String name;
    private String icon;
    private String path;
    private String component;
    private Integer activeFlag;
    private Date createdDate;
    private Date updatedDate;

    private Double latitude;
    private Double longitude;
}
