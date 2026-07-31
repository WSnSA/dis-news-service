package mn.usug.dis_news_service.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.MenuDAO;
import mn.usug.dis_news_service.DAO.SewageTreatmentRepository;
import mn.usug.dis_news_service.DAO.StFacilityDailyRepository;
import mn.usug.dis_news_service.DTO.SewageTreatmentSaveReq;
import mn.usug.dis_news_service.DTO.SewageTreatmentSummaryDto;
import mn.usug.dis_news_service.Entity.Menu;
import mn.usug.dis_news_service.Entity.SewageTreatment;
import mn.usug.dis_news_service.Entity.StFacilityDaily;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SewageTreatmentService {

    /** Өдрийн бүртгэлтэй байгууламжийг цагийн дэлгэцэд харуулах цаг (ээлжийн эхлэл) */
    private static final int FACILITY_SHIFT_HOUR = 7;

    private final SewageTreatmentRepository repo;
    private final MenuDAO menuDAO;
    private final StFacilityDailyRepository facilityRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                        r.get("treatedWater")  == null ? 0d : ((Number) r.get("treatedWater")).doubleValue(),
                        r.get("solidWaste")    == null ? 0d : ((Number) r.get("solidWaste")).doubleValue()
                ))
                .toList();
    }

    public List<SewageTreatmentSummaryDto> getDailySummary(LocalDate date) {
        // Ээлжийн өдөр: D өдрийн 8:00 – D+1 өдрийн 7:00
        List<SewageTreatmentSummaryDto> rows = repo.findDailySummaryShiftRaw(date, date.plusDays(1)).stream()
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
                        r.get("treatedWater")    == null ? 0d : ((Number) r.get("treatedWater")).doubleValue(),
                        r.get("solidWaste")      == null ? 0d : ((Number) r.get("solidWaste")).doubleValue()
                ))
                .toList();

        // Шинэ цэвэрлэх байгууламжийн (st_facility_daily) өдрийн бүртгэлийг мөрүүд дээр нэмнэ
        return mergeFacility(rows, date);
    }

    /**
     * Цагийн нэгтгэл + шинэ байгууламжийн өдрийн бүртгэл.
     * Өдрийн бүртгэл цаггүй тул зөвхөн ээлжийн эхний цагт (07) харуулна — давхар тооцохоос сэргийлнэ.
     * (report/daily нь getSummary-г шууд дууддаг тул тэнд өөрчлөлт орохгүй.)
     */
    public List<SewageTreatmentSummaryDto> getSummaryWithFacility(LocalDate date, int hour) {
        List<SewageTreatmentSummaryDto> rows = getSummary(date, hour);
        if (hour != FACILITY_SHIFT_HOUR) return rows;
        return mergeFacility(rows, date);
    }

    /* ══════════ st_facility_daily → нэгтгэлийн мөр ══════════ */

    /**
     * Тухайн өдрийн st_facility_daily бүртгэлийг DTO мөрүүд дээр давхарлана.
     * Мөр байхгүй станцыг цэсний нэрээр нь шинээр нэмнэ.
     */
    private List<SewageTreatmentSummaryDto> mergeFacility(List<SewageTreatmentSummaryDto> rows, LocalDate date) {
        List<StFacilityDaily> recs = facilityRepo.findByRecordDateAndActiveFlag(date, 1);
        if (recs.isEmpty()) return rows;

        Map<Integer, FacilityAgg> byStation = new LinkedHashMap<>();
        for (StFacilityDaily rec : recs) {
            if (rec.getStationId() == null) continue;
            byStation.put(rec.getStationId(), aggregate(rec.getDataJson()));
        }

        List<SewageTreatmentSummaryDto> out = new ArrayList<>();
        for (SewageTreatmentSummaryDto r : rows) {
            FacilityAgg a = byStation.remove(r.stationId());
            out.add(a == null ? r : overlay(r, a));
        }

        // Нэгтгэлд огт байхгүй станцуудыг цэснээс нэр аваад нэмнэ (цагийн дэлгэц)
        if (!byStation.isEmpty()) {
            Map<Integer, Menu> menus = menuDAO.findAll().stream()
                    .filter(m -> m.getId() != null)
                    .collect(Collectors.toMap(Menu::getId, m -> m, (x, y) -> x));
            for (Map.Entry<Integer, FacilityAgg> e : byStation.entrySet()) {
                Menu m = menus.get(e.getKey());
                if (m == null) continue;
                Menu parent = m.getParentId() != null ? menus.get(m.getParentId()) : null;
                out.add(overlay(new SewageTreatmentSummaryDto(
                        e.getKey(),
                        parent != null ? parent.getName() : null,
                        m.getName(),
                        "0", "0", "0",
                        0d, 0d, 0d, 0d, 0d, 0d
                ), e.getValue()));
            }
        }
        return out;
    }

    private SewageTreatmentSummaryDto overlay(SewageTreatmentSummaryDto r, FacilityAgg a) {
        return new SewageTreatmentSummaryDto(
                r.stationId(), r.groupName(), r.stationName(),
                a.working.isEmpty() ? r.workingCount() : String.join(",", a.working),
                a.pending.isEmpty() ? r.pendingCount() : String.join(",", a.pending),
                a.repair.isEmpty() ? r.repairingCount() : String.join(",", a.repair),
                nz(r.receivedWaste()) + a.receivedWaste,
                nz(r.receivedWool()),
                nz(r.receivedWater()),
                nz(r.substanceSpent()),
                nz(r.treatedWater()),
                nz(r.solidWaste()) + a.solidWaste,
                nz(r.compressedAir()) + a.compressedAir,
                nz(r.biogas()) + a.biogas,
                nz(r.dewateredSludge()) + a.dewateredSludge
        );
    }

    /** data_json-г задлаад нэг станцын дүнг гаргана */
    private FacilityAgg aggregate(String json) {
        FacilityAgg a = new FacilityAgg();
        if (json == null || json.isBlank()) return a;
        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (Exception e) {
            return a;
        }
        int offset = 0;   // блок хооронд дугаарлалт үргэлжилнэ (Насос 1..5, дараа нь 6..9)
        var it = root.fields();
        while (it.hasNext()) {
            JsonNode block = it.next().getValue();
            if (block == null || !block.isObject()) continue;

            JsonNode units = block.get("units");
            if (units != null && units.isArray()) {
                for (int i = 0; i < units.size(); i++) {
                    String st = units.get(i).isNull() ? null : units.get(i).asText(null);
                    String no = String.valueOf(offset + i + 1);
                    if ("working".equals(st)) a.working.add(no);
                    else if ("pending".equals(st)) a.pending.add(no);
                    else if ("repair".equals(st)) a.repair.add(no);
                }
                offset += units.size();
            }

            JsonNode values = block.get("values");
            if (values != null && values.isObject()) {
                a.receivedWaste += dbl(values.get("receivedWaste"));
                a.solidWaste += dbl(values.get("solidWaste"));
                a.compressedAir += dbl(values.get("compressedAir"));
                a.biogas += dbl(values.get("biogas"));
                a.dewateredSludge += dbl(values.get("dewateredSludge"));
            }
        }
        return a;
    }

    private static double dbl(JsonNode n) {
        return n == null || n.isNull() ? 0d : n.asDouble(0d);
    }

    private static double nz(Double d) {
        return d == null ? 0d : d;
    }

    /** Нэг станцын өдрийн дүн */
    private static final class FacilityAgg {
        final List<String> working = new ArrayList<>();
        final List<String> pending = new ArrayList<>();
        final List<String> repair = new ArrayList<>();
        double receivedWaste, solidWaste, compressedAir, biogas, dewateredSludge;
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
            e.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ulaanbaatar")));
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
        e.setSolidWaste(req.solidWaste()         != null ? req.solidWaste()     : 0.0);

        return repo.save(e);
    }

    /** Тухайн станцын өдрийн бүх цагийн бүртгэл */
    public List<Map<String, Object>> getHistory(int stationId, LocalDate date) {
        List<Map<String, Object>> raw = repo.findShiftHistory(stationId, date, date.plusDays(1));

        Map<Integer, Map<String, Object>> byHour = raw.stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r.get("hour")).intValue(),
                        r -> r,
                        (a, b) -> b
                ));

        List<Map<String, Object>> result = new ArrayList<>();

        for (int h = 7; h <= 23; h++) {
            result.add(byHour.getOrDefault(h, emptyHourRow(h)));
        }
        for (int h = 0; h <= 6; h++) {
            result.add(byHour.getOrDefault(h, emptyHourRow(h)));
        }

        return result;
    }

    private Map<String, Object> emptyHourRow(int hour) {
        Map<String, Object> row = new HashMap<>();
        row.put("hour", hour);
        row.put("workingCount", "0");
        row.put("pendingCount", "0");
        row.put("repairingCount", "0");
        row.put("receivedWaste", null);
        row.put("receivedWool", null);
        row.put("receivedWater", null);
        row.put("substanceSpent", null);
        row.put("treatedWater", null);
        row.put("solidWaste", null);
        return row;
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
                        e.getTreatedWater()  != null ? e.getTreatedWater()  : 0d,
                        e.getSolidWaste()    != null ? e.getSolidWaste()    : 0d
                ))
                .orElse(null);
    }
}
