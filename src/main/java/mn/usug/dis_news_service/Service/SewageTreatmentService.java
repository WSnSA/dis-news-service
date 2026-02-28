package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.SewageTreatmentRepository;
import mn.usug.dis_news_service.DTO.SewageTreatmentSummaryDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SewageTreatmentService {

    private final SewageTreatmentRepository repo;

    public List<SewageTreatmentSummaryDto> getSummary(LocalDate date, int hour) {
        if (hour < 0 || hour > 23) hour = 0;

        return repo.findSummaryRaw(date, hour).stream()
                .map(r -> new SewageTreatmentSummaryDto(
                        ((Number) r.get("stationId")).intValue(),
                        (String) r.get("groupName"),
                        (String) r.get("stationName"),
                        String.valueOf(r.get("workingCount")),
                        String.valueOf(r.get("pendingCount")),
                        String.valueOf(r.get("repairingCount")),
                        r.get("receivedWaste") == null ? 0d : ((Number) r.get("receivedWaste")).doubleValue(),
                        r.get("receivedWool") == null ? 0d : ((Number) r.get("receivedWool")).doubleValue(),
                        r.get("receivedWater") == null ? 0d : ((Number) r.get("receivedWater")).doubleValue(),
                        r.get("substanceSpent") == null ? 0d : ((Number) r.get("substanceSpent")).doubleValue(),
                        r.get("treatedWater") == null ? 0d : ((Number) r.get("treatedWater")).doubleValue()
                ))
                .toList();
    }
}
