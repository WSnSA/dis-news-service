package mn.usug.dis_news_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "vehicle_type")
@Data
public class VehicleType {

    @Id
    private Integer id;

    private String name;
}
