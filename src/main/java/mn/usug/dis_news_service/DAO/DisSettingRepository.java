package mn.usug.dis_news_service.DAO;

import mn.usug.dis_news_service.Entity.DisSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisSettingRepository extends JpaRepository<DisSetting, Integer> {
    Optional<DisSetting> findBySettingKey(String settingKey);
    List<DisSetting> findAllByOrderBySortOrderAscIdAsc();
}
