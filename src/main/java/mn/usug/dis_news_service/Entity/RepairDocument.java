package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Засварын баримт — Акт (ACT) ба Шаардах (REQUEST).
 * Сэлбэг/ажилтны мөрүүдээс автоматаар бүрдэх хэсэг 2-р шатанд нэмэгдэнэ.
 */
@Entity
@Table(name = "repair_document")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RepairDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ACT = Акт, REQUEST = Шаардах */
    @Column(name = "doc_type", nullable = false, length = 20)
    private String docType;

    @Column(name = "doc_no", length = 60)
    private String docNo;

    @Column(name = "doc_date")
    private LocalDate docDate;

    /** vehicle_repair.id — холбоотой засвар */
    @Column(name = "vehicle_repair_id")
    private Long vehicleRepairId;

    /** Улсын дугаарыг давхар хадгална — засвар устсан ч түүх үлдэнэ */
    @Column(name = "plate_number", length = 50)
    private String plateNumber;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * TEXT  — зориулалтаа бичгээр тайлбарлана (мөр бөглөхгүй)
     * ITEMS — сэлбэгийн лавлахаас сонгосон мөрүүдтэй (БМ-6 хүснэгт)
     */
    @Column(name = "doc_mode", nullable = false, length = 20)
    private String docMode = "TEXT";

    /** vehicle.id — дугаараар хайж сонгосон машин (заавал биш) */
    @Column(name = "vehicle_id")
    private Long vehicleId;

    /* ── БМ-6 маягтын толгой ── */

    /** Хэнээс — овог нэр, албан тушаал */
    @Column(name = "from_person", length = 200)
    private String fromPerson;

    /** Хаана — цех, тасаг, алба */
    @Column(name = "to_place", length = 200)
    private String toPlace;

    @Column(name = "purpose", length = 300)
    private String purpose;

    @Column(name = "receiver_name", length = 200)
    private String receiverName;

    @Column(name = "issuer_name", length = 200)
    private String issuerName;

    /* ── Техникийн комиссын акт (ДМYА маягт) ── */

    /** "УСУГ ДМYА № 12" — маягтын дугаар */
    @Column(name = "form_no", length = 60)
    private String formNo;

    /** БАТЛАВ — баталсан хүн */
    @Column(name = "approver_title", length = 200)
    private String approverTitle;

    @Column(name = "approver_name", length = 200)
    private String approverName;

    @Column(name = "city", length = 100)
    private String city;

    /** Комиссын гишүүд — JSON [{title,name}] */
    @Column(name = "commission", columnDefinition = "TEXT")
    private String commission;

    /** Зөвшөөрсөн жолооч */
    @Column(name = "commission_driver", length = 200)
    private String commissionDriver;

    /** Гүйлтийн норм (км) */
    @Column(name = "norm_km")
    private Integer normKm;

    /** Ашиглалтад орсноос хойш явсан (км) */
    @Column(name = "actual_km")
    private Integer actualKm;

    /** Комиссоос тогтоосон нь — 1 дүгээр зүйл */
    @Column(name = "finding", columnDefinition = "TEXT")
    private String finding;

    /** Эвдрэлийн шалтгаан, хариуцах эзэн, төлбөр — 2 дугаар зүйл */
    @Column(name = "liability", columnDefinition = "TEXT")
    private String liability;

    @Column(name = "vehicle_brand", length = 150)
    private String vehicleBrand;

    /** Хариуд мөрүүдийг хамт буцаана — DB-д хадгалагдахгүй */
    @Transient
    private java.util.List<RepairDocumentItem> items;

    /** 1=идэвхтэй, 0=идэвхгүй (soft delete) */
    @Column(name = "active_flag", nullable = false)
    private Integer activeFlag = 1;

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
}
