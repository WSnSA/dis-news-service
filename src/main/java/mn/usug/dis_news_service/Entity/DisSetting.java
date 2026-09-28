package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DIS системийн тохируулж болох утгууд (одоогоор цагийн хязгаарууд).
 * Key-value хэлбэр — шинэ тохиргоо нэмэхэд код өөрчлөхгүйгээр л мөр нэмнэ.
 */
@Entity
@Table(name = "dis_setting")
@Data
public class DisSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Системийн түлхүүр (жишээ: order_cutoff_hour) */
    @Column(name = "setting_key", unique = true, length = 80)
    private String settingKey;

    @Column(name = "setting_value", length = 255)
    private String settingValue;

    /** Хэрэглэгчид харагдах тайлбар */
    @Column(name = "label", length = 255)
    private String label;

    /** Бүлэглэл (UI-д хэсэглэн харуулах) */
    @Column(name = "group_name", length = 120)
    private String groupName;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
