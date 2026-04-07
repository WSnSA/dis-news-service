package mn.usug.dis_news_service.Service;

import org.springframework.transaction.annotation.Transactional;
import mn.usug.dis_news_service.DAO.*;
import mn.usug.dis_news_service.Entity.*;
import mn.usug.dis_news_service.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import mn.usug.dis_news_service.DAO.UserDAO;

@Service
public class MainService {
    @Autowired
    HourlyWsStationDAO hourlyWsStationDAO;

    @Autowired
    HourlyWsSecondDAO hourlyWsSecondDAO;

    @Autowired
    UserDAO userDAO;

    @Autowired
    mn.usug.dis_news_service.DAO.StationDAO stationDAO;

    @Transactional
    public ResponseEntity regHourly(HourlyReport report) {

        // 1) Upsert HourlyWsStation — байгаа бол шинэчлэх, байхгүй бол үүсгэх
        HourlyWsStation station = hourlyWsStationDAO
                .findByMenuIdAndDateAndHour(report.getMenuId(), report.getDate(), report.getHour());
        boolean isNew = station == null;
        if (isNew) {
            station = new HourlyWsStation();
            station.setCreatedBy(UserContext.getUserId());
            station.setCreatedDate(LocalDateTime.now(ZoneId.of("Asia/Ulaanbaatar")));
        } else {
            station.setUpdatedBy(UserContext.getUserId());
            station.setUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Ulaanbaatar")));
        }

        station.setMenuId(report.getMenuId());
        station.setDate(report.getDate());
        station.setHour(report.getHour());

        station.setFirstWorkingCount(report.getFirstWorkingCount());
        station.setFirstPendingCount(report.getFirstPendingCount());
        station.setFirstRepairingCount(report.getFirstRepairingCount());

        station.setFirstPool(report.getFirstPool());
        station.setSecondPool(report.getSecondPool());
        station.setThirdPool(report.getThirdPool());
        station.setFourthPool(report.getFourthPool());

        station.setPipeFm1(report.getPipeFm1());
        station.setPipeFm7(report.getPipeFm7());
        station.setPipeFm8(report.getPipeFm8());

        hourlyWsStationDAO.save(station);

        // 2) Хуучин second бүртгэлийг устгаж, шинийг хадгална
        hourlyWsSecondDAO.deleteByMenuIdAndDateAndHour(
                report.getMenuId(), report.getDate(), report.getHour());

        if (report.getSecondList() != null) {
            report.getSecondList().forEach(item ->
                regHourlySecond(report.getMenuId(), report.getDate(), report.getHour(), item));
        }

        return ResponseEntity.ok("OK");
    }


    @Transactional
    public void regHourlySecond(Integer menuId, Date date, Integer hour, HourlySecondReport report) {
        HourlyWsSecond station = new HourlyWsSecond();

        station.setMenuId(menuId);
        station.setDate(date);
        station.setHour(hour);
        station.setStatus(report.getStatus());

        station.setGeneratorNo(report.getGeneratorNo());
        station.setFrequency(report.getFrequency());
        station.setECurrent(report.getECurrent());
        station.setPressure(report.getPressure());
        station.setPressure2(report.getPressure2());
        station.setPressure3(report.getPressure3());
        station.setPressure4(report.getPressure4());
        station.setTemperature(report.getTemperature());
        station.setTemperature2(report.getTemperature2());
        station.setTemperature3(report.getTemperature3());
        station.setTemperature4(report.getTemperature4());
        station.setGauge(report.getGauge());
        station.setCreation(report.getCreation());
        station.setPumpedWater(report.getPumpedWater());
        station.setPool(report.getPool());
        station.setChlorine(report.getChlorine());

        hourlyWsSecondDAO.save(station);
    }

    public ResponseEntity<ReportModelHourly> getHourlyHistory(Integer menuId, Date date, Integer hour) {
        ReportModelHourly model = new ReportModelHourly();
        HourlyWsStation station = hourlyWsStationDAO.findByMenuIdAndDateAndHour(menuId, date, hour);
        model.setStation(station);
        List<HourlyWsSecond> list = hourlyWsSecondDAO.findAllByMenuIdAndDateAndHour(menuId, date, hour);
        model.setSecondList(list);

        return ResponseEntity.ok(model);
    }


    public ResponseEntity<DailyReportListResponse> getDailyReportList(Integer menuId, LocalDate date) {

        DailyReportListResponse response = new DailyReportListResponse();
        response.setMenuId(menuId);
        response.setDate(java.sql.Date.valueOf(date));

        String dateStr = date.toString();
        String nextDateStr = date.plusDays(1).toString();

        List<HourlyWsStation> stations =
                hourlyWsStationDAO.findByMenuIdAndShiftDay(menuId, dateStr, nextDateStr);

        // createdBy/updatedBy ID-уудаар хэрэглэгчийн нэрийг batch-аар ачааллана
        Set<Integer> userIds = new HashSet<>();
        stations.forEach(s -> {
            if (s.getCreatedBy() != null) userIds.add(s.getCreatedBy());
            if (s.getUpdatedBy() != null) userIds.add(s.getUpdatedBy());
        });
        Map<Integer, String> nameById = new HashMap<>();
        if (!userIds.isEmpty()) {
            userDAO.findAllById(userIds).forEach(u ->
                nameById.put(u.getId(),
                    ((u.getLastName() != null ? u.getLastName() : "") + " " +
                     (u.getFirstName() != null ? u.getFirstName() : "")).trim())
            );
        }
        stations.forEach(s -> {
            if (s.getCreatedBy() != null) s.createdByName = nameById.getOrDefault(s.getCreatedBy(), "");
            if (s.getUpdatedBy() != null) s.updatedByName = nameById.getOrDefault(s.getUpdatedBy(), "");
        });

        List<HourlyWsSecond> seconds =
                hourlyWsSecondDAO.findAllByMenuIdAndShiftDay(menuId, dateStr, nextDateStr);

        Map<Integer, HourlyWsStation> stationByHour = stations.stream()
                .collect(Collectors.toMap(
                        HourlyWsStation::getHour,
                        s -> s,
                        (a, b) -> a
                ));

        Map<Integer, List<HourlyWsSecond>> secondsByHour = seconds.stream()
                .collect(Collectors.groupingBy(HourlyWsSecond::getHour));

        List<Integer> shiftHours = new ArrayList<>();
        for (int h = 8; h <= 23; h++) shiftHours.add(h);
        for (int h = 0; h <= 7; h++) shiftHours.add(h);

        List<HourReport> hourReports = new ArrayList<>();
        for (Integer h : shiftHours) {
            HourReport hr = new HourReport();
            hr.setHour(h);
            hr.setStation(stationByHour.get(h));
            hr.setSeconds(secondsByHour.getOrDefault(h, new ArrayList<>()));
            hourReports.add(hr);
        }

        response.setHours(hourReports);
        return ResponseEntity.ok(response);
    }

    /**
     * FM-n заалт (тоолуурын нийлбэр утга) дээр тулгуурлан ээлжийн шахсан усыг тооцоолно.
     * Цаг тутмын заалтуудын зөрүүний нийлбэрийг буцаана.
     * Өмнөх цаг бүртгэлгүй бол хамгийн сүүлд бичсэн заалтыг ашиглана (stations date+hour ASC эрэмбэтэй).
     * Сөрөг зөрүүг (тоолуур шинэчлэгдсэн гэх мэт) тооцохгүй.
     */
    private int calculateShiftFlowDiff(
            List<HourlyWsStation> stations,
            Function<HourlyWsStation, Integer> getter
    ) {
        if (stations == null || stations.isEmpty()) return 0;
        int total = 0;
        Integer prev = null;
        for (HourlyWsStation s : stations) {
            Integer cur = getter.apply(s);
            if (cur == null || cur <= 0) continue;
            if (prev != null && cur > prev) {
                total += cur - prev;
            }
            prev = cur;
        }
        return total;
    }

    public HourlyReport getDailyReport(Integer menuId, LocalDate date) {
        HourlyReport report = new HourlyReport();
        report.setMenuId(menuId);
        report.setDate(java.sql.Date.valueOf(date));

        String dateStr     = date.toString();
        String nextDateStr = date.plusDays(1).toString();

        List<HourlyWsStation> stations =
                hourlyWsStationDAO.findByMenuIdAndShiftDay(menuId, dateStr, nextDateStr);

        report.setHasData(!stations.isEmpty());

        report.setPipeFm1(calculateShiftFlowDiff(stations, HourlyWsStation::getPipeFm1));
        report.setPipeFm7(calculateShiftFlowDiff(stations, HourlyWsStation::getPipeFm7));
        report.setPipeFm8(calculateShiftFlowDiff(stations, HourlyWsStation::getPipeFm8));

        // Усан сан — өнөөдрийн ээлжийн хамгийн сүүлийн бүртгэлээс
        stations.stream()
                .reduce((a, b) -> b)
                .ifPresent(last -> {
                    report.setFirstPool(last.getFirstPool());
                    report.setSecondPool(last.getSecondPool());
                    report.setThirdPool(last.getThirdPool());
                    report.setFourthPool(last.getFourthPool());
                });

        // 1-р өргөгчийн худгийн тоо — станцын хамгийн сүүлийн бүртгэлээс (өдрөөс үл хамааран)
        hourlyWsStationDAO.findLatestByMenuId(menuId).ifPresent(latest -> {
            report.setFirstWorkingCount(latest.getFirstWorkingCount());
            report.setFirstPendingCount(latest.getFirstPendingCount());
            report.setFirstRepairingCount(latest.getFirstRepairingCount());
        });

        // Station config-оос усан сангийн capacity мэдээлэл
        stationDAO.findByMenuId(menuId).ifPresent(st -> {
            Integer fwt = st.getFirstWellTotal() != null ? st.getFirstWellTotal() : st.getWellsNumber();
            report.setFirstWellTotal(fwt);
            report.setPoolDetails(st.getPoolDetails());
        });

        List<HourlyWsSecond> seconds =
                hourlyWsSecondDAO.findAllByMenuIdAndShiftDay(menuId, dateStr, nextDateStr);

        // Генератор тус бүрийн ээлжийн aggregate
        Map<Integer, List<HourlyWsSecond>> grouped =
                seconds.stream().collect(Collectors.groupingBy(HourlyWsSecond::getGeneratorNo));

        // Ээлжийн хамгийн сүүлийн second timestamp-ийг date + hour-аар олно
        Comparator<HourlyWsSecond> secondComparator = Comparator
                .comparing(HourlyWsSecond::getDate)
                .thenComparing(s -> s.getHour() != null ? s.getHour() : -1)
                .thenComparing(HourlyWsSecond::getId);

        Optional<HourlyWsSecond> latestSecondOpt = seconds.stream().max(secondComparator);

        List<HourlyWsSecond> latestSeconds = latestSecondOpt
                .map(latest -> seconds.stream()
                        .filter(s ->
                                Objects.equals(s.getDate(), latest.getDate()) &&
                                        Objects.equals(s.getHour(), latest.getHour()))
                        .toList())
                .orElseGet(ArrayList::new);

        Map<Integer, List<HourlyWsSecond>> stated =
                latestSeconds.stream().collect(Collectors.groupingBy(HourlyWsSecond::getStatus));

        report.setSecondWorkingCount(stated.getOrDefault(1, new ArrayList<>()).size());
        report.setSecondPendingCount(stated.getOrDefault(2, new ArrayList<>()).size());
        report.setSecondRepairingCount(stated.getOrDefault(3, new ArrayList<>()).size());

        List<HourlySecondReport> generatorReports = new ArrayList<>();

        for (Map.Entry<Integer, List<HourlyWsSecond>> e : grouped.entrySet()) {
            Integer genNo = e.getKey();
            List<HourlyWsSecond> list = e.getValue();

            HourlySecondReport r = new HourlySecondReport();
            r.setGeneratorNo(genNo);

            r.setStatusList(list.stream().map(HourlyWsSecond::getStatus).toList());

            r.setFrequency(avg(list, HourlyWsSecond::getFrequency));
            r.setPressure(avg(list, HourlyWsSecond::getPressure));
            r.setPressure2(avg(list, HourlyWsSecond::getPressure2));
            r.setPressure3(avg(list, HourlyWsSecond::getPressure3));
            r.setPressure4(avg(list, HourlyWsSecond::getPressure4));

            r.setTemperature(avg(list, HourlyWsSecond::getTemperature));
            r.setTemperature2(avg(list, HourlyWsSecond::getTemperature2));
            r.setTemperature3(avg(list, HourlyWsSecond::getTemperature3));
            r.setTemperature4(avg(list, HourlyWsSecond::getTemperature4));

            r.setChlorine(avg(list, HourlyWsSecond::getChlorine));
            r.setECurrent(avg(list, HourlyWsSecond::getECurrent));

            r.setGauge(sum(list, HourlyWsSecond::getGauge));
            r.setCreation(sum(list, HourlyWsSecond::getCreation));
            r.setPumpedWater(sum(list, HourlyWsSecond::getPumpedWater));
            r.setPool(sum(list, HourlyWsSecond::getPool));

            generatorReports.add(r);
        }

        report.setHourlyWsSecondList(generatorReports);

        return report;
    }

    public ResponseEntity<HourlyReport> getMonthlyReport(Integer menuId, int year, int month) {

        LocalDate localStart = LocalDate.of(year, month, 1);
        LocalDate localEnd = localStart.withDayOfMonth(localStart.lengthOfMonth());

        Date start = java.sql.Date.valueOf(localStart);
        Date end = java.sql.Date.valueOf(localEnd);

        List<HourlyWsStation> stations =
                hourlyWsStationDAO.findByMenuIdAndDateBetween(menuId, start, end);

        List<HourlyWsSecond> seconds =
                hourlyWsSecondDAO.findByMenuIdAndDateBetween(menuId, start, end);

        HourlyReport report = new HourlyReport();
        report.setMenuId(menuId);
        report.setDate(start);



        report.setFirstPool(stations.stream().mapToInt(s -> n(s.getFirstPool())).sum());
        report.setSecondPool(stations.stream().mapToInt(s -> n(s.getSecondPool())).sum());
        report.setThirdPool(stations.stream().mapToInt(s -> n(s.getThirdPool())).sum());
        report.setFourthPool(stations.stream().mapToInt(s -> n(s.getFourthPool())).sum());

        report.setPipeFm1(stations.stream().mapToInt(s -> n(s.getPipeFm1())).sum());
        report.setPipeFm7(stations.stream().mapToInt(s -> n(s.getPipeFm7())).sum());
        report.setPipeFm8(stations.stream().mapToInt(s -> n(s.getPipeFm8())).sum());

        Map<Integer, List<HourlyWsSecond>> grouped =
                seconds.stream().collect(Collectors.groupingBy(HourlyWsSecond::getGeneratorNo));

        List<HourlySecondReport> genReports = new ArrayList<>();

        for (Map.Entry<Integer, List<HourlyWsSecond>> e : grouped.entrySet()) {
            Integer genNo = e.getKey();
            List<HourlyWsSecond> list = e.getValue();

            HourlySecondReport r = new HourlySecondReport();
            r.setGeneratorNo(genNo);

            // LIST
            r.setStatusList(list.stream().map(HourlyWsSecond::getStatus).toList());

            // AVG
            r.setFrequency(avg(list, HourlyWsSecond::getFrequency));
            r.setPressure(avg(list, HourlyWsSecond::getPressure));
            r.setPressure2(avg(list, HourlyWsSecond::getPressure2));
            r.setPressure3(avg(list, HourlyWsSecond::getPressure3));
            r.setPressure4(avg(list, HourlyWsSecond::getPressure4));

            r.setTemperature(avg(list, HourlyWsSecond::getTemperature));
            r.setTemperature2(avg(list, HourlyWsSecond::getTemperature2));
            r.setTemperature3(avg(list, HourlyWsSecond::getTemperature3));
            r.setTemperature4(avg(list, HourlyWsSecond::getTemperature4));

            r.setChlorine(avg(list, HourlyWsSecond::getChlorine));
            r.setECurrent(avg(list, HourlyWsSecond::getECurrent));

            // SUM
            r.setGauge(sum(list, HourlyWsSecond::getGauge));
            r.setCreation(sum(list, HourlyWsSecond::getCreation));
            r.setPumpedWater(sum(list, HourlyWsSecond::getPumpedWater));
            r.setPool(sum(list, HourlyWsSecond::getPool));

            genReports.add(r);
        }

        report.setHourlyWsSecondList(genReports);

        return ResponseEntity.ok(report);
    }

    public ResponseEntity getDailyFmByHour(Date date) {
        String ds     = toDateStr(date);
        String nextDs = shiftNextDayStr(date);
        List<Object[]> rows = hourlyWsStationDAO.getDailyFmByHourShift(ds, nextDs);
        List<Map<String, Object>> result = rows.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("hour", ((Number) row[0]).intValue());
            m.put("fm1",  row[1] != null ? ((Number) row[1]).intValue() : 0);
            m.put("fm7",  row[2] != null ? ((Number) row[2]).intValue() : 0);
            m.put("fm8",  row[3] != null ? ((Number) row[3]).intValue() : 0);
            m.put("total", row[4] != null ? ((Number) row[4]).intValue() : 0);
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    /** "yyyy-MM-dd" форматаар Date-г string болгоно (timezone-аас хамааралгүй) */
    private String toDateStr(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /** Ээлжийн дараагийн өдрийн string буцаана (D+1) */
    private String shiftNextDayStr(Date date) {
        LocalDate next = LocalDate.parse(toDateStr(date)).plusDays(1);
        return next.toString();
    }

    private int n(Integer v) {
        return v == null ? 0 : v;
    }
    private double avg(List<HourlyWsSecond> list, Function<HourlyWsSecond, Double> getter) {
        return list.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
    }

    private double sum(List<HourlyWsSecond> list, Function<HourlyWsSecond, Double> getter) {
        return list.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
