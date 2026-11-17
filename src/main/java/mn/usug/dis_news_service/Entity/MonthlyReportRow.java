package mn.usug.dis_news_service.Entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * "Сарын мэдээ"-ний нэг мөр (эх үүсвэр / цэвэрлэх байгууламж).
 * Тоог аль цэс(станц)-аас, ямар үзүүлэлтээр авахыг энд тодорхойлно —
 * мөр нэмэх/солиход код өөрчлөхгүй. Migration: db_migration_monthly_water_report.sql
 */
@Data
@Entity
@Table(name = "monthly_report_row")
public class MonthlyReportRow {

    public static final String SECTION_WATER = "WATER";
    public static final String SECTION_SEWAGE = "SEWAGE";

    public static final String METRIC_WS_FM = "WS_FM";
    public static final String METRIC_ST_RECEIVED_WASTE = "ST_RECEIVED_WASTE";
    public static final String METRIC_ST_RECEIVED_WATER = "ST_RECEIVED_WATER";
    public static final String METRIC_ST_TREATED_WATER = "ST_TREATED_WATER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** WATER | SEWAGE */
    @Column(name = "section", nullable = false, length = 20)
    private String section;

    /** Бүлэг бүрийн дараа ДҮН мөр гарна */
    @Column(name = "group_no", nullable = false)
    private Integer groupNo;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "unit", length = 20)
    private String unit;

    /** WS_FM | ST_RECEIVED_WASTE | ST_RECEIVED_WATER | ST_TREATED_WATER */
    @Column(name = "metric", nullable = false, length = 30)
    private String metric;

    /** menu.id-ууд таслалаар ("4,12") */
    @Column(name = "menu_ids", length = 255)
    private String menuIds;

    /** 0 бол НИЙТ мөрөнд орохгүй (ДҮН-д орно) */
    @Column(name = "include_in_total", nullable = false)
    private Integer includeInTotal;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "active_flag", nullable = false)
    private Integer activeFlag;
}
