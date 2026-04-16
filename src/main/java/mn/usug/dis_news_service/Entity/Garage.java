package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "garage")
@Data
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
