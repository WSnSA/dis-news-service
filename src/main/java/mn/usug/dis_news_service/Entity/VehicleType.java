package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vehicle_type")
@Data
public class VehicleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "parent_id")
    private Integer parentId;

    /** 1=идэвхтэй, 0=идэвхгүй (soft delete). Хуучин түүхэнд нэр хадгалагдахын тулд мөрийг устгахгүй. */
    @Column(name = "active_flag")
    private Integer activeFlag = 1;
}
