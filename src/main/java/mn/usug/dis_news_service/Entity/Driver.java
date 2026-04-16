package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "driver")
@Data
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    /** "A,B,C,D" хэлбэрээр хадгална */
    @Column(name = "license_categories", length = 30)
    private String licenseCategories;
}
