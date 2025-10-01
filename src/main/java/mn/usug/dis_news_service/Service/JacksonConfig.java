package mn.usug.dis_news_service.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // 👈 Enable LocalDateTime support
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 👈 ISO-8601 format
        return mapper;
    }
}
