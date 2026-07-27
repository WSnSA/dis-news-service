package mn.usug.dis_news_service.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Машин хуваарилалтын статистик — сар/улирал/жил + алба бүрээр (машины төрлөөр задалсан).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchStatsDto {

    private int year;
    private long yearTotal;                 // тухайн жилд нийт хэдэн машин хуваарилсан
    private List<PeriodCount> byMonth;      // 1..12 сар
    private List<PeriodCount> byQuarter;    // 1..4 улирал
    private List<DeptDispatch> byDepartment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodCount {
        private int period;   // сар (1..12) эсвэл улирал (1..4)
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeptDispatch {
        private String department;      // захиалга өгсөн алба (нэр)
        private long total;             // тухайн албанд нийт хэдэн машин
        private List<TypeCount> types;  // ямар2 машин (төрлөөр)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeCount {
        private String typeName;
        private long count;
    }
}
