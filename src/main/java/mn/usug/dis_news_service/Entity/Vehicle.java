package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vehicle")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Үндсэн ──────────────────────────────────────────────
    @Column(name = "plate_number", nullable = false, length = 20)
    private String plateNumber;

    @Column(name = "vin_number", length = 50)
    private String vinNumber;

    @Column(name = "cabin_number", length = 50)
    private String cabinNumber;

    @Column(name = "engine_number", length = 50)
    private String engineNumber;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "year", length = 10)
    private String year;

    @Column(name = "owner_name", length = 200)
    private String ownerName;

    @Column(name = "vehicle_type_id")
    private Integer vehicleTypeId;

    // ── XYP нэмэлт мэдээлэл ─────────────────────────────────
    @Column(name = "country_name", length = 100)
    private String countryName;

    @Column(name = "vehicle_desc", length = 200)
    private String vehicleDesc;

    @Column(name = "owner_type", length = 50)
    private String ownerType;

    @Column(name = "class_name", length = 10)
    private String className;

    @Column(name = "import_date")
    private LocalDate importDate;

    @Column(name = "fuel_type", length = 50)
    private String fuelType;

    @Column(name = "man_count")
    private Integer manCount;

    @Column(name = "axle_count")
    private Integer axleCount;

    @Column(name = "capacity", precision = 10, scale = 2)
    private BigDecimal capacity;

    @Column(name = "mass", precision = 10, scale = 2)
    private BigDecimal mass;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "length", precision = 10, scale = 2)
    private BigDecimal length;

    @Column(name = "width", precision = 10, scale = 2)
    private BigDecimal width;

    @Column(name = "height", precision = 10, scale = 2)
    private BigDecimal height;

    @Column(name = "transmission", length = 100)
    private String transmission;

    @Column(name = "wheel_position", length = 20)
    private String wheelPosition;

    @Column(name = "rfid", length = 50)
    private String rfid;

    @Column(name = "location", length = 50)
    private String location;

    @Column(name = "garage_id")
    private Long garageId;

    @Column(name = "affiliation", length = 100)
    private String affiliation;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // ── Audit ────────────────────────────────────────────────
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Integer updatedBy;

    @Transient
    private String vehicleTypeName;

    @Transient
    private String garageName;

    @Transient
    private List<VehicleDriver> drivers;
}
