package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Зохион байгуулалтын нэгж / хэлтэс / удирдлага (§3.4 танилцуулгын дараалал).
 * unitType: UNIT (ЗБН) / DEPARTMENT (хэлтэс) / MANAGEMENT (удирдлага).
 */
@Entity
@Table(name = "briefing_unit")
@Data
public class BriefingUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "unit_type", length = 20)
    private String unitType;

    /** ref/department-тэй холбоос (сонголтоор) */
    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "active_flag")
    private Integer activeFlag;
}
