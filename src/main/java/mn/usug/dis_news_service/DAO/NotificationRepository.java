package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Хэрэглэгчийн сүүлийн 50 мэдэгдэл — шинэнх нь эхэнд
    List<Notification> findTop50ByUserIdOrderByCreatedAtDesc(Integer userId);

    // Бүх уншаагүй мэдэгдлийг уншсан болгох
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = 1 WHERE n.userId = :userId AND n.isRead = 0")
    void markAllReadByUserId(Integer userId);

    // Нэг мэдэгдэл уншсан болгох
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = 1 WHERE n.id = :id")
    void markReadById(Long id);
}
