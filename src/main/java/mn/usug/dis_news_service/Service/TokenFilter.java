package mn.usug.dis_news_service.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class TokenFilter extends OncePerRequestFilter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                String userJson = AESUtil.decryptToken(token);
                if (userJson != null) {
                    JsonNode node = mapper.readTree(userJson);
                    Integer id   = node.has("id")       ? node.get("id").asInt()         : null;
                    String  name = node.has("username") ? node.get("username").asText()  : null;
                    UserContext.set(id, name);
                }
            }
        } catch (Exception ignored) {}

        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
