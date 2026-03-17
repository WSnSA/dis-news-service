package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "station")
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "department_id", nullable = false)
    private Integer departmentId;

    @Column(name = "chef_id", nullable = false)
    private Integer chefId;

    @Column(name = "isWaterSupply", nullable = false)
    private Integer isWaterSupply;

    @Column(name = "first_generator_count")
    private Integer firstGeneratorCount;

    @Column(name = "second_generator_count")
    private Integer secondGeneratorCount;

    @Column(name = "pool_count")
    private Integer poolCount;

    @Column(name = "pump_count")
    private Integer pumpCount;

    @Column(name = "wells_number")
    private Integer wellsNumber;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password", length = 100)
    private String password;

}