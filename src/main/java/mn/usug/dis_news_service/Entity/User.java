package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "pin", length = 10)
    private String pin;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password", length = 100)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "position_id")
    private Integer positionId;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "mail_address", length = 100)
    private String mailAddress;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "active_flag")
    private Boolean activeFlag;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "station_id")
    private Integer stationId;

}