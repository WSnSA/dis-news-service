package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.NotificationRepository;
import mn.usug.dis_news_service.Entity.Notification;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repo;

    /** Хэрэглэгчийн сүүлийн 50 мэдэгдэл */
    @GetMapping
    public List<Notification> getByUser(@RequestParam Integer userId) {
        return repo.findTop50ByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Бүгдийг уншсан болгох */
    @PutMapping("/read-all")
    public void markAllRead(@RequestParam Integer userId) {
        repo.markAllReadByUserId(userId);
    }

    /** Нэг мэдэгдэл уншсан болгох */
    @PutMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        repo.markReadById(id);
    }
}
