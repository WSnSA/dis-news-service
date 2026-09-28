package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.DisSettingRepository;
import mn.usug.dis_news_service.Entity.DisSetting;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DIS-ийн тохиргоог (цагийн хязгаарууд) уншиж/шинэчилнэ.
 * Утга байхгүй/буруу бол код доторх анхдагчийг ашигладаг тул систем эвдрэхгүй.
 */
@Service
@RequiredArgsConstructor
public class SettingService {

    private final DisSettingRepository repo;

    public List<DisSetting> getAll() {
        return repo.findAllByOrderBySortOrderAscIdAsc();
    }

    /** Тохиргоог бүхэл тоо (цаг г.м)-оор авна; байхгүй/буруу бол def. */
    public int getInt(String key, int def) {
        return repo.findBySettingKey(key)
                .map(s -> {
                    try { return Integer.parseInt(s.getSettingValue().trim()); }
                    catch (Exception e) { return def; }
                })
                .orElse(def);
    }

    public DisSetting update(String key, String value, Integer userId) {
        if (key == null || key.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "key заавал");
        DisSetting s = repo.findBySettingKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тохиргоо олдсонгүй: " + key));
        // Цагийн тохиргоо тул 0–23 хооронд бүхэл тоо байх ёстой
        int h;
        try { h = Integer.parseInt(value == null ? "" : value.trim()); }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "0–23 хооронд бүхэл тоо оруулна");
        }
        if (h < 0 || h > 23)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Цаг 0–23 хооронд байх ёстой");
        s.setSettingValue(String.valueOf(h));
        s.setUpdatedBy(userId);
        s.setUpdatedDate(LocalDateTime.now());
        return repo.save(s);
    }
}
