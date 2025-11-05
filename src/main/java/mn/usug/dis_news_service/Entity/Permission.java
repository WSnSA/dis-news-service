package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "menu_id")
    private Integer menuId;

    @Column(name = "can_view")
    private Integer canView;

    @Column(name = "can_edit")
    private Integer canEdit;
}
