package mn.usug.dis_news_service.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Суудлын машины захиалгын өдөр тутмын хувиарлалтын статистик.
 * status: 0=хүлээгдэж байна, 1=баталгаажсан, 2=хуваарилагдсан, 3=боломжгүй.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarDispatchStatDto {
    private String date;        // yyyy-MM-dd
    private long total;         // тухайн өдрийн нийт захиалга
    private long dispatched;    // хуваарилагдсан (status=2)
    private long pending;       // хүлээгдэж буй (status=0)
    private long confirmed;     // баталгаажсан (status=1)
    private long declined;      // боломжгүй (status=3)
}
