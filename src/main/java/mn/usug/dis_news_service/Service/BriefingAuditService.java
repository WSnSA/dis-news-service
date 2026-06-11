package mn.usug.dis_news_service.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.BriefingAuditLogRepository;
import mn.usug.dis_news_service.Entity.BriefingAuditLog;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Шуурхай хурлын аудит бүртгэл (§7).
 * Аудит бичих нь үндсэн ажлыг хэзээ ч блоклохгүй — алдааг залгина.
 */
@Service
@RequiredArgsConstructor
public class BriefingAuditService {

    private static final ZoneId UB = ZoneId.of("Asia/Ulaanbaatar");

    private final BriefingAuditLogRepository repo;
    private final ObjectMapper objectMapper;

    public void log(String action, String entityType, Integer entityId, Object oldVal, Object newVal) {
        try {
            BriefingAuditLog a = new BriefingAuditLog();
            a.setUserId(UserContext.getUserId());
            a.setUserName(UserContext.getUsername());
            a.setAction(action);
            a.setEntityType(entityType);
            a.setEntityId(entityId);
            a.setOldValue(toJson(oldVal));
            a.setNewValue(toJson(newVal));
            a.setIp(currentIp());
            a.setCreatedAt(LocalDateTime.now(UB));
            repo.save(a);
        } catch (Exception ignored) {
            // аудит бичих алдаа үндсэн гүйлгээг таслахгүй
        }
    }

    public List<BriefingAuditLog> recent(int limit) {
        return repo.findAllByOrderByCreatedAtDesc(PageRequest.of(0, Math.min(limit, 500)));
    }

    public List<BriefingAuditLog> forEntity(String entityType, Integer entityId) {
        return repo.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    private String toJson(Object o) {
        if (o == null) return null;
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    private String currentIp() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr == null) return null;
            HttpServletRequest req = attr.getRequest();
            String xf = req.getHeader("X-Forwarded-For");
            if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
            return req.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
