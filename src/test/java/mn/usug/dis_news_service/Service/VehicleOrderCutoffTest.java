package mn.usug.dis_news_service.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import mn.usug.dis_news_service.Controller.VehicleOrderController;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleOrderCutoffTest {
    private final SettingService settings = mock(SettingService.class);

    private VehicleOrderCutoff at(String instant, int hour) {
        when(settings.getInt("order_cutoff_hour", 16)).thenReturn(hour);
        return new VehicleOrderCutoff(settings,
                Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Ulaanbaatar")));
    }

    @Test void closesExactlyAt1600Ulaanbaatar() {
        assertDoesNotThrow(() -> at("2026-10-29T07:59:59Z", 16).requireOpen());
        var error = assertThrows(ResponseStatusException.class,
                () -> at("2026-10-29T08:00:00Z", 16).requireOpen());
        assertEquals(400, error.getStatusCode().value());
        assertThrows(ResponseStatusException.class,
                () -> at("2026-10-29T15:59:59Z", 16).requireOpen());
        assertDoesNotThrow(() -> at("2026-10-29T16:00:00Z", 16).requireOpen());
    }

    @Test void readsChangesWithoutCachingAndValidatesHours() {
        var policy = at("2026-10-29T08:00:00Z", 18);
        assertDoesNotThrow(policy::requireOpen);
        when(settings.getInt("order_cutoff_hour", 16)).thenReturn(16);
        assertThrows(ResponseStatusException.class, policy::requireOpen);
        assertEquals(16, at("2026-10-29T08:00:00Z", -1).getHour());
        assertEquals(16, at("2026-10-29T08:00:00Z", 24).getHour());
    }

    @Test void blocksEveryBookingMutationBeforeWriting() {
        var policy = at("2026-10-29T08:00:00Z", 16);
        var controller = new VehicleOrderController(policy, null, null, null, null, null, null, null);
        assertThrows(ResponseStatusException.class, () -> controller.save(null));
        assertThrows(ResponseStatusException.class, () -> controller.update(1L, null));
        assertThrows(ResponseStatusException.class, () -> controller.delete(1L));
    }
}
