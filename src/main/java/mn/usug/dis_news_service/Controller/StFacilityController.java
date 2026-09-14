package mn.usug.dis_news_service.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.StFacilityDailyRepository;
import mn.usug.dis_news_service.Entity.StFacilityDaily;
import mn.usug.dis_news_service.Service.UserContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Цэвэрлэх байгууламжийн (олон хэсэгтэй) өдөр тутмын бүртгэл.
 * Бүх өгөгдөл data_json дотор JSON-оор хадгалагдана; stationId = menu.id (path '/st/%').
 */
@RestController
@RequestMapping("/st-facility")
@RequiredArgsConstructor
public class StFacilityController {

    private final StFacilityDailyRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    /** Тухайн станцын тухайн өдрийн бүртгэл (засах/дүүргэхэд) */
    @GetMapping("/get")
    public Map<String, Object> get(
            @RequestParam Integer stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("stationId", stationId);
        res.put("date", date.toString());
        StFacilityDaily rec = repo
                .findFirstByStationIdAndRecordDateAndActiveFlag(stationId, date, 1)
                .orElse(null);
        res.put("exists", rec != null);
        res.put("data", parse(rec != null ? rec.getDataJson() : null));
        return res;
    }

    /** Түүх — тухайн станцын огнооны завсрын бүртгэлүүд */
    @GetMapping("/history")
    public List<Map<String, Object>> history(
            @RequestParam Integer stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<Map<String, Object>> out = new ArrayList<>();
        for (StFacilityDaily rec : repo
                .findByStationIdAndRecordDateBetweenAndActiveFlagOrderByRecordDate(stationId, from, to, 1)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", rec.getRecordDate() != null ? rec.getRecordDate().toString() : null);
            row.put("data", parse(rec.getDataJson()));
            out.add(row);
        }
        return out;
    }

    /** Хадгалах (upsert: station + өдөр). Body: { stationId, date, data:{...} } */
    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody Map<String, Object> body) {
        Integer stationId = ((Number) body.get("stationId")).intValue();
        LocalDate date = LocalDate.parse((String) body.get("date"));
        String dataJson = write(body.get("data"));

        StFacilityDaily rec = repo
                .findFirstByStationIdAndRecordDateAndActiveFlag(stationId, date, 1)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        if (rec == null) {
            rec = new StFacilityDaily();
            rec.setStationId(stationId);
            rec.setRecordDate(date);
            rec.setActiveFlag(1);
            rec.setCreatedBy(UserContext.getUserId());
            rec.setCreatedDate(now);
        } else {
            rec.setUpdatedBy(UserContext.getUserId());
            rec.setUpdatedDate(now);
        }
        rec.setDataJson(dataJson);
        StFacilityDaily saved = repo.save(rec);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id", saved.getId());
        res.put("stationId", saved.getStationId());
        res.put("date", saved.getRecordDate().toString());
        return res;
    }

    /* ── helpers ── */
    private JsonNode parse(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    private String write(Object data) {
        if (data == null) return "{}";
        try {
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }
}
