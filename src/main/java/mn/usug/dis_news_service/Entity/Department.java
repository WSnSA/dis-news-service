package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Department")
public class Department {
    @Id
    @Column(name = "dep_id")
    private Integer depId;

    @Column(name = "dep_name", length = 100)
    private String depName;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "chairman_id")
    private Integer chairmanId;

}
