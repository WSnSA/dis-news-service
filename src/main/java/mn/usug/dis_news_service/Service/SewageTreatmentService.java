package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.MenuDAO;
import mn.usug.dis_news_service.DAO.SewageTreatmentRepository;
import mn.usug.dis_news_service.DTO.SewageTreatmentSaveReq;
import mn.usug.dis_news_service.DTO.SewageTreatmentSummaryDto;
import mn.usug.dis_news_service.Entity.Menu;
import mn.usug.dis_news_service.Entity.SewageTreatment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SewageTreatmentService {

    private final SewageTreatmentRepository repo;
    private final MenuDAO menuDAO;

    public List<SewageTreatmentSummaryDto> getSummary(LocalDate date, int hour) {
        if (hour < 0 || hour > 23) hour = 0;
        return repo.findSummaryRaw(date, hour).stream()
                .filter(r -> r.get("stationId") != null)
                .map(r -> new SewageTreatmentSummaryDto(
                        ((Number) r.get("stationId")).intValue(),
                        (String) r.get("groupName"),
                        (String) r.get("stationName"),
                        String.valueOf(r.get("workingCount")),
                        String.valueOf(r.get("pendingCount")),
                        String.valueOf(r.get("repairingCount")),
                        r.get("receivedWaste") == null ? 0d : ((Number) r.get("receivedWaste")).doubleValue(),
                        r.get("receivedWool")  == null ? 0d : ((Number) r.get("receivedWool")).doubleValue(),
                        r.get("receivedWater") == null ? 0d : ((Number) r.get("receivedWater")).doubleValue(),
                        r.get("substanceSpent") == null ? 0d : ((Number) r.get("substanceSpent")).doubleValue(),
                        r.get("treatedWater")  == null ? 0d : ((Number) r.get("treatedWater")).doubleValue()
                ))
                .toList();
    }

    public List<SewageTreatmentSummaryDto> getDailySummary(LocalDate date) {
        return repo.findDailySummaryRaw(date).stream()
                .filter(r -> r.get("stationId") != null)
                .map(r -> new SewageTreatmentSummaryDto(
                        ((Number) r.get("stationId")).intValue(),
                        (String) r.get("groupName"),
                        (String) r.get("stationName"),
                        r.get("workingCount")   != null ? String.valueOf(r.get("workingCount"))   : "0",
                        r.get("pendingCount")   != null ? String.valueOf(r.get("pendingCount"))   : "0",
                        r.get("repairingCount") != null ? String.valueOf(r.get("repairingCount")) : "0",
                        r.get("receivedWaste")   == null ? 0d : ((Number) r.get("receivedWaste")).doubleValue(),
                        r.get("receivedWool")    == null ? 0d : ((Number) r.get("receivedWool")).doubleValue(),
                        r.get("receivedWater")   == null ? 0d : ((Number) r.get("receivedWater")).doubleValue(),
                        r.get("substanceSpent")  == null ? 0d : ((Number) r.get("substanceSpent")).doubleValue(),
                        r.get("treatedWater")    == null ? 0d : ((Number) r.get("treatedWater")).doubleValue()
                ))
                .toList();
    }

    public List<Menu> getStations() {
        return menuDAO.findSewageStations();
    }

    @Transactional
    public SewageTreatment save(SewageTreatmentSaveReq req) {
        LocalDate date = LocalDate.parse(req.date());
        int hour = req.hour() != null ? req.hour() : 0;

        SewageTreatment e = repo.findLatestByStationAndHour(req.stationId(), date, hour)
                .orElse(new SewageTreatment());

        boolean isNew = e.getId() == null;
        if (isNew) {
            e.setStationId(req.stationId());
            e.setCreatedDate(LocalDateTime.of(date, LocalTime.of(hour, 0)));
            Integer uid = UserContext.getUserId();
            e.setCreatedBy(uid != null ? uid : 0);
            e.setActiveFlag(1);
            e.setStatus(1);
        } else {
            e.setUpdatedDate(LocalDateTime.now());
            e.setUpdatedBy(UserContext.getUserId());
        }

        e.setWorkingCount(req.workingCount()   != null ? req.workingCount()    : "0");
        e.setPendingCount(req.pendingCount()   != null ? req.pendingCount()    : "0");
        e.setRepairingCount(req.repairingCount() != null ? req.repairingCount(): "0");
        e.setReceivedWaste(req.receivedWaste()   != null ? req.receivedWaste()  : 0.0);
        e.setReceivedWool(req.receivedWool()     != null ? req.receivedWool()   : 0.0);
        e.setReceivedWater(req.receivedWater()   != null ? req.receivedWater()  : 0.0);
        e.setSubstanceSpent(req.substanceSpent() != null ? req.substanceSpent(): 0.0);
        e.setTreatedWater(req.treatedWater()     != null ? req.treatedWater()   : 0.0);

        return repo.save(e);
    }

    /** Тухайн станцын өдрийн бүх цагийн бүртгэл */
    public List<Map<String, Object>> getHistory(int stationId, LocalDate date) {
        return repo.findDailyHistory(stationId, date);
    }

    /** Тухайн станц, огноо, цагийн бүртгэлийг буцаана (засах үед) */
    public SewageTreatmentSummaryDto getByStationAndHour(int stationId, LocalDate date, int hour) {
        return repo.findLatestByStationAndHour(stationId, date, hour)
                .map(e -> new SewageTreatmentSummaryDto(
                        e.getStationId(), null, null,
                        e.getWorkingCount(), e.getPendingCount(), e.getRepairingCount(),
                        e.getReceivedWaste() != null ? e.getReceivedWaste() : 0d,
                        e.getReceivedWool()  != null ? e.getReceivedWool()  : 0d,
                        e.getReceivedWater() != null ? e.getReceivedWater() : 0d,
                        e.getSubstanceSpent() != null ? e.getSubstanceSpent() : 0d,
                        e.getTreatedWater()  != null ? e.getTreatedWater()  : 0d
                ))
                .orElse(null);
    }
}
