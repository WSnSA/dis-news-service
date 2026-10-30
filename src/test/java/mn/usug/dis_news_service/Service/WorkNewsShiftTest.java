package mn.usug.dis_news_service.Service;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class WorkNewsShiftTest {
    @Test void rejectsStaleShiftInsteadOfSilentlyChangingDate() {
        var error = assertThrows(ResponseStatusException.class, () ->
                WorkNewsService.validateRequestedShift("2026-12-31", LocalDate.of(2027, 1, 1)));
        assertEquals(409, error.getStatusCode().value());
    }
    @Test void acceptsCurrentShiftAndLegacyCallers() {
        var shift = LocalDate.of(2028, 2, 29);
        assertDoesNotThrow(() -> WorkNewsService.validateRequestedShift("2028-02-29", shift));
        assertDoesNotThrow(() -> WorkNewsService.validateRequestedShift(null, shift));
    }
    @Test void rejectsInvalidDate() {
        var error = assertThrows(ResponseStatusException.class, () ->
                WorkNewsService.validateRequestedShift("not-a-date", LocalDate.of(2026, 10, 30)));
        assertEquals(400, error.getStatusCode().value());
    }
}
