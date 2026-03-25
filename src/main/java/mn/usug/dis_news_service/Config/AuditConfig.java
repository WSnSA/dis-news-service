package mn.usug.dis_news_service.Config;

import mn.usug.dis_news_service.Service.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<Integer> auditorAware() {
        return () -> Optional.ofNullable(UserContext.getUserId());
    }
}
