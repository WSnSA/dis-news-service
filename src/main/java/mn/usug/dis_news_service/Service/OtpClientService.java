package mn.usug.dis_news_service.Service;
// package mn.usug.dis_news_service.Service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OtpClientService {

    private final RestTemplate rt = new RestTemplate();
    private final String base = "http://172.16.0.55:8085"; // танай зураг дээрх mail-service

    public boolean sendOtp(String email) {
        ResponseEntity<String> res = rt.postForEntity(
                base + "/api/otp/send",
                Map.of("email", email),
                String.class
        );
        return res.getStatusCode().is2xxSuccessful();
    }

    public boolean verifyOtp(String email, String code) {
        ResponseEntity<String> res = rt.postForEntity(
                base + "/api/otp/verify",
                Map.of("email", email, "code", code),
                String.class
        );
        if (!res.getStatusCode().is2xxSuccessful()) return false;

        String body = (res.getBody() == null) ? "" : res.getBody().toLowerCase();
        return body.contains("verified") || body.contains("ok");
    }
}