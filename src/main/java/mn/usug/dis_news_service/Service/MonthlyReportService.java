package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.DisSettingRepository;
import mn.usug.dis_news_service.DAO.MonthlyReportPlanRepository;
import mn.usug.dis_news_service.DAO.MonthlyReportRowRepository;
import mn.usug.dis_news_service.DTO.MonthlyReportDto;
import mn.usug.dis_news_service.DTO.MonthlyReportDto.Line;
import mn.usug.dis_news_service.DTO.MonthlyReportDto.Section;
import mn.usug.dis_news_service.DTO.MonthlyReportDto.Signer;
import mn.usug.dis_news_service.Entity.DisSetting;
import mn.usug.dis_news_service.Entity.MonthlyReportPlan;
import mn.usug.dis_news_service.Entity.MonthlyReportRow;
import mn.usug.dis_news_service.Entity.StFacilityDaily;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * "Сарын мэдээ" — олборлосон болон татан зайлуулж цэвэрлэсэн усны сарын мэдээ.
 *
 * Мөрүүд monthly_report_row-оос, төлөвлөгөө monthly_report_plan-аас.
 * Бүх тооцоо 07:00 ээлжийн өдрөөр (07:00 – маргаашийн 06:59):
 *  - WS_FM  : FM-1 + FM-7 + FM-8 тоолуурын өсөлт. Өмнөх ээлжийн сүүлийн заалтыг baseline болгоно,
 *             буурсан (тоолуур шинэчлэгдсэн) заалтыг тооцохгүй — MainService.calculateShiftFlowDiff-тэй ижил.
 *  - ST_*   : sewage_treatment-ийн цагийн утгуудын нийлбэр. ST_RECEIVED_WASTE дээр шинэ байгууламжийн
 *             (st_facility_daily) ээлж бүрийн сүүлийн бүртгэлийн receivedWaste нэмэгдэнэ.
 * "Хэмнэлт" = гүйцэтгэл − төлөвлөгөө.
 */
@Service
@RequiredArgsConstructor
public class MonthlyReportService {

    private static final int SHIFT_HOUR = 7;
    private static final String SIGN_PREFIX = "monthly_report_sign_";
    private static final int SIGN_COUNT = 6;

    private final MonthlyReportRowRepository rowRepo;
    private final MonthlyReportPlanRepository planRepo;
    private final DisSettingRepository settingRepo;
    private final SewageTreatmentService sewageService;

    /* ══════════════════════ Тайлан ══════════════════════ */

    public MonthlyReportDto build(int year, int month) {
        validate(year, month);
        List<MonthlyReportRow> rows = rowRepo.findByActiveFlagOrderBySortOrderAscIdAsc(1);

        // Мөр бүрийн сар бүрийн (1..12) гүйцэтгэл
        Map<Integer, Monthly> actual = new HashMap<>();
        computeWater(rows, year, month, actual);
        computeSewage(rows, year, month, actual);

        // Төлөвлөгөө: rowId → [1..12]
        Map<Integer, Long[]> plans = new HashMap<>();
        for (MonthlyReportPlan p : planRepo.findByPlanYear(year)) {
            if (p.getPlanMonth() == null || p.getPlanMonth() < 1 || p.getPlanMonth() > 12) continue;
            plans.computeIfAbsent(p.getRowId(), k -> new Long[13])[p.getPlanMonth()] = p.getPlanValue();
        }

        String mm = String.format("%02d", month);
        List<Section> sections = List.of(
                section(MonthlyReportRow.SECTION_WATER, null, true,
                        "Шахсан ус /м3/", "Хэмнэлт +/-", "Шахсан ус /м3/", "Хэмнэлт +/-",
                        rows, actual, plans, month),
                section(MonthlyReportRow.SECTION_SEWAGE, "ТАТАН ЗАЙЛУУЛСАН УС", false,
                        "Татан зайлуулсан ус /м3/", "Хэмнэлт +/-", "Татан зайлуулсан ус /м3/", "Зөрүү",
                        rows, actual, plans, month)
        );

        LocalDate last = LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth());
        return new MonthlyReportDto(
                year, month,
                year + " ОНЫ " + mm + " САРЫН ОЛБОРЛОСОН БОЛОН ТАТАН ЗАЙЛУУЛЖ ЦЭВЭРЛЭСЭН УСНЫ МЭДЭЭ",
                year + " оны " + mm + " сарын " + String.format("%02d", last.getDayOfMonth()),
                "Өссөн дүнгээр /01-" + mm + " сар/",
                sections,
                getSigners()
        );
    }

    private Section section(String code, String title, boolean showUnit,
                            String mActLbl, String mDiffLbl, String yActLbl, String yDiffLbl,
                            List<MonthlyReportRow> allRows, Map<Integer, Monthly> actual,
                            Map<Integer, Long[]> plans, int month) {
        List<MonthlyReportRow> rows = allRows.stream().filter(r -> code.equals(r.getSection())).toList();
        List<Line> lines = new ArrayList<>();
        Acc total = new Acc();
        String unit = rows.stream().map(MonthlyReportRow::getUnit).filter(Objects::nonNull).findFirst().orElse(null);

        // group_no-оор дараалуулна (sort_order-ийн дараалал хадгалагдана)
        Map<Integer, List<MonthlyReportRow>> groups = new TreeMap<>();
        for (MonthlyReportRow r : rows) groups.computeIfAbsent(r.getGroupNo() == null ? 1 : r.getGroupNo(), k -> new ArrayList<>()).add(r);

        int no = 1;
        for (List<MonthlyReportRow> g : groups.values()) {
            Acc sub = new Acc();
            for (MonthlyReportRow r : g) {
                Monthly a = actual.get(r.getId());
                Long[] p = plans.get(r.getId());
                Long mAct = a == null ? null : a.get(month);
                Long yAct = a == null ? null : a.sum(1, month);
                Long mPlan = p == null ? null : p[month];
                Long yPlan = p == null ? null : sum(p, 1, month);
                Line line = line("ROW", no++, r.getId(), r.getName(), r.getUnit(), mAct, mPlan, yAct, yPlan);
                lines.add(line);
                sub.add(line);
                if (r.getIncludeInTotal() == null || r.getIncludeInTotal() == 1) total.add(line);
            }
            lines.add(sub.toLine("SUBTOTAL", "ДҮН", unit));
        }
        lines.add(total.toLine("TOTAL", "НИЙТ", unit));
        return new Section(code, title, showUnit, mActLbl, mDiffLbl, yActLbl, yDiffLbl, lines);
    }

    private static Line line(String kind, Integer no, Integer rowId, String name, String unit,
                             Long mAct, Long mPlan, Long yAct, Long yPlan) {
        return new Line(kind, no, rowId, name, unit,
                mAct, mPlan, diff(mAct, mPlan),
                yAct, yPlan, diff(yAct, yPlan));
    }

    private static Long diff(Long act, Long plan) {
        return act == null || plan == null ? null : act - plan;
    }

    private static Long sum(Long[] arr, int from, int to) {
        Long s = null;
        for (int i = from; i <= to; i++) if (arr[i] != null) s = (s == null ? 0 : s) + arr[i];
        return s;
    }

    /** ДҮН / НИЙТ мөрийн нийлбэр — хоосон нүдийг алгасна */
    private static final class Acc {
        Long mAct, mPlan, yAct, yPlan;

        void add(Line l) {
            mAct = plus(mAct, l.monthActual());
            mPlan = plus(mPlan, l.monthPlan());
            yAct = plus(yAct, l.ytdActual());
            yPlan = plus(yPlan, l.ytdPlan());
        }

        Line toLine(String kind, String name, String unit) {
            return line(kind, null, null, name, unit, mAct, mPlan, yAct, yPlan);
        }

        /** null-ийг алгасна. (Тернар оператор Long/long холивол null-ийг unbox хийж NPE өгдөг тул if) */
        private static Long plus(Long a, Long b) {
            if (b == null) return a;
            if (a == null) return b;
            return Long.valueOf(a.longValue() + b.longValue());
        }
    }

    /** Нэг мөрийн сар бүрийн дүн (index 1..12). Өгөгдөлгүй сар = null */
    private static final class Monthly {
        final Long[] v = new Long[13];

        void add(int m, long x) {
            v[m] = (v[m] == null ? 0 : v[m]) + x;
        }

        Long get(int m) {
            return v[m];
        }

        Long sum(int from, int to) {
            return MonthlyReportService.sum(v, from, to);
        }
    }

    /* ── Ус: FM тоолуурын өсөлт ── */

    private void computeWater(List<MonthlyReportRow> rows, int year, int month, Map<Integer, Monthly> out) {
        List<MonthlyReportRow> ws = rows.stream()
                .filter(r -> MonthlyReportRow.METRIC_WS_FM.equals(r.getMetric()))
                .toList();
        Set<Integer> menuIds = new HashSet<>();
        ws.forEach(r -> menuIds.addAll(parseIds(r.getMenuIds())));
        if (menuIds.isEmpty()) return;

        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate monthEnd = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);

        // Baseline-д өмнөх хэдэн өдрийг (ээлжийн сүүлийн заалт) хамт уншина
        List<Object[]> raw = rowRepo.findFmReadings(menuIds,
                yearStart.minusDays(7).toString(), monthEnd.plusDays(1).toString());

        // menuId → сар бүрийн шахсан ус
        Map<Integer, Monthly> byMenu = new HashMap<>();
        Integer curMenu = null;
        Integer[] prev = new Integer[3];
        for (Object[] r : raw) {
            Integer menuId = toInt(r[0]);
            if (!Objects.equals(menuId, curMenu)) {
                curMenu = menuId;
                prev = new Integer[3];
            }
            LocalDate d = LocalDate.parse(String.valueOf(r[1]));
            Integer hour = toInt(r[2]);
            LocalDate shift = hour != null && hour < SHIFT_HOUR ? d.minusDays(1) : d;
            boolean inRange = !shift.isBefore(yearStart) && !shift.isAfter(monthEnd);
            Monthly m = inRange ? byMenu.computeIfAbsent(menuId, k -> new Monthly()) : null;
            if (m != null) m.add(shift.getMonthValue(), 0); // заалттай сар — 0 ч гэсэн харагдана

            for (int i = 0; i < 3; i++) {
                Integer cur = toInt(r[3 + i]);
                if (cur == null || cur <= 0) continue;
                if (m != null && prev[i] != null && cur > prev[i]) m.add(shift.getMonthValue(), cur - prev[i]);
                prev[i] = cur;
            }
        }

        for (MonthlyReportRow row : ws) {
            Monthly agg = null;
            for (Integer id : parseIds(row.getMenuIds())) {
                Monthly m = byMenu.get(id);
                if (m == null) continue;
                if (agg == null) agg = new Monthly();
                for (int i = 1; i <= 12; i++) if (m.v[i] != null) agg.add(i, m.v[i]);
            }
            if (agg != null) out.put(row.getId(), agg);
        }
    }

    /* ── Бохир ус: цагийн утгуудын нийлбэр (+ шинэ байгууламж) ── */

    private void computeSewage(List<MonthlyReportRow> rows, int year, int month, Map<Integer, Monthly> out) {
        List<MonthlyReportRow> st = rows.stream()
                .filter(r -> r.getMetric() != null && r.getMetric().startsWith("ST_"))
                .toList();
        if (st.isEmpty()) return;

        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate nextMonth = LocalDate.of(year, month, 1).plusMonths(1);
        String fromTs = yearStart + " 0" + SHIFT_HOUR + ":00:00";
        String toTs = nextMonth + " 0" + SHIFT_HOUR + ":00:00";

        // metric → (stationId → Monthly)
        Map<String, Map<Integer, Monthly>> byMetric = new HashMap<>();
        Map<String, Set<Integer>> idsByMetric = new HashMap<>();
        for (MonthlyReportRow r : st) idsByMetric.computeIfAbsent(r.getMetric(), k -> new HashSet<>()).addAll(parseIds(r.getMenuIds()));

        for (Map.Entry<String, Set<Integer>> e : idsByMetric.entrySet()) {
            if (e.getValue().isEmpty()) continue;
            Map<Integer, Monthly> byStation = new HashMap<>();
            for (Object[] r : rowRepo.sumSewageByMonth(e.getValue(), e.getKey(), fromTs, toTs)) {
                Integer sid = toInt(r[0]);
                Integer mon = toInt(r[1]);
                if (sid == null || mon == null || mon < 1 || mon > 12) continue;
                long v = r[2] == null ? 0 : Math.round(((Number) r[2]).doubleValue());
                byStation.computeIfAbsent(sid, k -> new Monthly()).add(mon, v);
            }
            if (MonthlyReportRow.METRIC_ST_RECEIVED_WASTE.equals(e.getKey())) {
                addFacility(e.getValue(), yearStart, nextMonth.minusDays(1), byStation);
            }
            byMetric.put(e.getKey(), byStation);
        }

        for (MonthlyReportRow row : st) {
            Map<Integer, Monthly> byStation = byMetric.getOrDefault(row.getMetric(), Map.of());
            Monthly agg = null;
            for (Integer id : parseIds(row.getMenuIds())) {
                Monthly m = byStation.get(id);
                if (m == null) continue;
                if (agg == null) agg = new Monthly();
                for (int i = 1; i <= 12; i++) if (m.v[i] != null) agg.add(i, m.v[i]);
            }
            if (agg != null) out.put(row.getId(), agg);
        }
    }

    /**
     * st_facility_daily: ээлж бүрт тухайн станцын хамгийн сүүлийн бүртгэлийг авна
     * (SewageTreatmentService.mergeFacility-тэй ижил дүрэм).
     */
    private void addFacility(Set<Integer> stationIds, LocalDate from, LocalDate to, Map<Integer, Monthly> byStation) {
        List<StFacilityDaily> recs = rowRepo.findFacilityRange(stationIds, from, to.plusDays(1));
        // stationId|shiftDate → хамгийн сүүлийн бүртгэл
        Map<String, StFacilityDaily> latest = new HashMap<>();
        Map<String, LocalDate> shiftOf = new HashMap<>();
        for (StFacilityDaily rec : recs) {
            if (rec.getStationId() == null || rec.getRecordDate() == null) continue;
            Integer h = rec.getRecordHour();
            LocalDate shift = h != null && h < SHIFT_HOUR ? rec.getRecordDate().minusDays(1) : rec.getRecordDate();
            if (shift.isBefore(from) || shift.isAfter(to)) continue;
            String key = rec.getStationId() + "|" + shift;
            StFacilityDaily cur = latest.get(key);
            if (cur == null || shiftPos(rec, shift) >= shiftPos(cur, shift)) {
                latest.put(key, rec);
                shiftOf.put(key, shift);
            }
        }
        for (Map.Entry<String, StFacilityDaily> e : latest.entrySet()) {
            double w = sewageService.facilityReceivedWaste(e.getValue().getDataJson());
            byStation.computeIfAbsent(e.getValue().getStationId(), k -> new Monthly())
                    .add(shiftOf.get(e.getKey()).getMonthValue(), Math.round(w));
        }
    }

    private static int shiftPos(StFacilityDaily rec, LocalDate shiftDate) {
        int h = rec.getRecordHour() == null ? SHIFT_HOUR : rec.getRecordHour();
        return rec.getRecordDate().isAfter(shiftDate) ? h + 24 : h;
    }

    /* ══════════════════════ Төлөвлөгөө ══════════════════════ */

    public record PlanRow(Integer rowId, String section, Integer groupNo, String name, Long[] months) {}

    public record PlanItem(Integer rowId, Integer month, Long value) {}

    /** Тухайн оны бүх мөрийн 12 сарын төлөвлөгөө (months[0..11]) */
    public List<PlanRow> getPlans(int year) {
        validate(year, 1);
        Map<Integer, Long[]> byRow = new HashMap<>();
        for (MonthlyReportPlan p : planRepo.findByPlanYear(year)) {
            if (p.getPlanMonth() == null || p.getPlanMonth() < 1 || p.getPlanMonth() > 12) continue;
            byRow.computeIfAbsent(p.getRowId(), k -> new Long[12])[p.getPlanMonth() - 1] = p.getPlanValue();
        }
        return rowRepo.findByActiveFlagOrderBySortOrderAscIdAsc(1).stream()
                .map(r -> new PlanRow(r.getId(), r.getSection(), r.getGroupNo(), r.getName(),
                        byRow.getOrDefault(r.getId(), new Long[12])))
                .toList();
    }

    @Transactional
    public int savePlans(int year, List<PlanItem> items, Integer userId) {
        validate(year, 1);
        if (items == null) return 0;
        Set<Integer> validRows = new HashSet<>();
        rowRepo.findAll().forEach(r -> validRows.add(r.getId()));
        int n = 0;
        for (PlanItem it : items) {
            if (it == null || it.rowId() == null || !validRows.contains(it.rowId())) continue;
            if (it.month() == null || it.month() < 1 || it.month() > 12) continue;
            if (it.value() != null && it.value() < 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Төлөвлөгөө сөрөг байж болохгүй");
            MonthlyReportPlan p = planRepo.findByRowIdAndPlanYearAndPlanMonth(it.rowId(), year, it.month())
                    .orElseGet(() -> {
                        MonthlyReportPlan x = new MonthlyReportPlan();
                        x.setRowId(it.rowId());
                        x.setPlanYear(year);
                        x.setPlanMonth(it.month());
                        return x;
                    });
            if (Objects.equals(p.getPlanValue(), it.value()) && p.getId() != null) continue;
            p.setPlanValue(it.value());
            p.setUpdatedBy(userId);
            p.setUpdatedDate(LocalDateTime.now());
            planRepo.save(p);
            n++;
        }
        return n;
    }

    /* ══════════════════════ Гарын үсэг ══════════════════════ */

    /** dis_setting: monthly_report_sign_1..6 = "Албан тушаал|Нэр". Хоосон албан тушаалтай мөр гарахгүй. */
    public List<Signer> getSigners() {
        List<Signer> out = new ArrayList<>();
        for (int i = 1; i <= SIGN_COUNT; i++) {
            String v = settingRepo.findBySettingKey(SIGN_PREFIX + i).map(DisSetting::getSettingValue).orElse(null);
            if (v == null || v.isBlank()) continue;
            String[] parts = v.split("\\|", 2);
            String title = parts[0].trim();
            if (title.isEmpty()) continue;
            out.add(new Signer(title, parts.length > 1 ? parts[1].trim() : ""));
        }
        return out;
    }

    @Transactional
    public List<Signer> saveSigners(List<Signer> signers, Integer userId) {
        List<Signer> list = signers == null ? List.of() : signers;
        if (list.size() > SIGN_COUNT)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Хамгийн ихдээ " + SIGN_COUNT + " гарын үсэг");
        for (int i = 1; i <= SIGN_COUNT; i++) {
            Signer s = i <= list.size() ? list.get(i - 1) : null;
            String title = s == null || s.title() == null ? "" : s.title().replace("|", "/").trim();
            String name = s == null || s.name() == null ? "" : s.name().replace("|", "/").trim();
            String value = title.isEmpty() ? "" : title + "|" + name;
            if (value.length() > 255)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Гарын үсгийн мөр хэт урт байна");
            String key = SIGN_PREFIX + i;
            int order = 100 + i;
            DisSetting row = settingRepo.findBySettingKey(key).orElseGet(() -> {
                DisSetting x = new DisSetting();
                x.setSettingKey(key);
                x.setLabel("Сарын мэдээ — гарын үсэг " + key.substring(SIGN_PREFIX.length()));
                x.setGroupName("Сарын мэдээ");
                x.setSortOrder(order);
                return x;
            });
            row.setSettingValue(value);
            row.setUpdatedBy(userId);
            row.setUpdatedDate(LocalDateTime.now());
            settingRepo.save(row);
        }
        return getSigners();
    }

    /* ══════════════════════ helpers ══════════════════════ */

    private static void validate(int year, int month) {
        if (year < 2000 || year > 2100 || month < 1 || month > 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Он/сар буруу байна");
    }

    static List<Integer> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        List<Integer> out = new ArrayList<>();
        for (String s : csv.split(",")) {
            try {
                out.add(Integer.parseInt(s.trim()));
            } catch (NumberFormatException ignored) {
                // буруу утгыг алгасна
            }
        }
        return out;
    }

    private static Integer toInt(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}
