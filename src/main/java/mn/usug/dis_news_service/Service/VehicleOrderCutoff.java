package mn.usug.dis_news_service.Service;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Shared passenger-car and mechanism booking deadline, in Ulaanbaatar time. */
@Service
public class VehicleOrderCutoff {
    private final SettingService settings;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public VehicleOrderCutoff(SettingService settings) {
        this(settings, Clock.system(ZoneId.of("Asia/Ulaanbaatar")));
    }

    VehicleOrderCutoff(SettingService settings, Clock clock) {
        this.settings = settings;
        this.clock = clock;
    }

    public int getHour() {
        int hour = settings.getInt("order_cutoff_hour", 16);
        return hour >= 0 && hour <= 23 ? hour : 16;
    }

    public void requireOpen() {
        int hour = getHour();
        if (LocalTime.now(clock).getHour() >= hour) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Захиалгын хугацаа дууссан — %02d:00 цагаас хойш захиалга өгөх/засах/устгах боломжгүй", hour));
        }
    }
}
