package mn.usug.dis_news_service.Model;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
