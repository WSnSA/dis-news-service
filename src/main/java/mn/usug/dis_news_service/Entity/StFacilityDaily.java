package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Цэвэрлэх байгууламжийн (шинэ, олон хэсэгтэй) өдөр тутмын бүртгэл.
 * Байгууламж бүрийн бүтэц өөр тул бүх өгөгдлийг data_json дотор JSON хэлбэрээр хадгална.
 * stationId = menu.id (path нь '/st/%').
 */
@Entity
@Table(name = "st_facility_daily")
@Data
public class StFacilityDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "station_id")
    private Integer stationId;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "data_json", columnDefinition = "json")
    private String dataJson;

    @Column(name = "active_flag")
    private Integer activeFlag;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
