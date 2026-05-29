package mn.usug.dis_news_service.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class UserModel {
    private String firstName;
    private String lastName;
    private String pin;
    private String username;
    private String password;
    private Integer departmentId;
    private Integer positionId;
    private String phoneNumber;
    private String mailAddress;
    private Boolean activeFlag;
    private String status;

    /** Шуурхай хурлаар үүрэг даалгавар өгөх / дүгнэх эрхтэй эсэх */
    private Boolean canAssignTask;
}
