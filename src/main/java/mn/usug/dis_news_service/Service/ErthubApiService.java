package mn.usug.dis_news_service.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ErthubApiService {

    private static final String URL = "https://erthub.mn/api/vehicle";

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public Map<String, Object> call(String operation, String plate, String type) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("operation", operation);
            body.put("plate_number", plate);
            body.put("type", type != null ? type : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(URL, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("EртHub API [{}] error for {}: {}", operation, plate, e.getMessage());
            return null;
        }
    }
}
