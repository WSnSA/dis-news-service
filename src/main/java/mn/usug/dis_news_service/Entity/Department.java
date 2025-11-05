package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dep_id")
    private Integer depId;

    @Column(name = "dep_name", length = 100)
    private String depName;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "chairman_id")
    private Integer chairmanId;

    @Column(name = "short_name")
    private String shortName;

}
