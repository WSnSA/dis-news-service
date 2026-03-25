package mn.usug.dis_news_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class DisNewsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DisNewsServiceApplication.class, args);
	}

}
