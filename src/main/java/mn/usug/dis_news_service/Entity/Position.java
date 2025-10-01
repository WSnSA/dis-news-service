package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;
import lombok.Data;

@Data
@Entity
@Table(name = "position")
public class Position {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "dep_id", nullable = false)
    private Integer depId;

    @Column(name = "description", length = 5000)
    private String description;

    @ColumnDefault("0")
    @Column(name = "employee_count")
    private Integer employeeCount;

}