package mn.usug.dis_news_service.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Машин хуваарилалтын тайлангийн тоо бүрийн ард байгаа дэлгэрэнгүй мөр.
 * Алба/төрөл/сар нь статистикийн нэгтгэлтэй яг ижил дүрмээр тооцогдоно.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchDetailDto {
    private String createdDate;         // "yyyy-MM-dd HH:mm"
    private int month;                  // 1..12
    private String department;          // захиалагч алба (эсвэл "Тодорхойгүй")
    private String typeName;            // машины төрөл
    private String workDescription;
    private String vehicleMechanism;
    private String vehicleRegistration;
    private String driverName;
    private String phone;
}
