package mn.usug.dis_news_service.Service;

import org.springframework.transaction.annotation.Transactional;
import mn.usug.dis_news_service.DAO.*;
import mn.usug.dis_news_service.Entity.*;
import mn.usug.dis_news_service.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Service
public class MainService {
    @Autowired
    HourlyWsStationDAO hourlyWsStationDAO;

    @Autowired
    HourlyWsSecondDAO hourlyWsSecondDAO;

    @Transactional
    public ResponseEntity regHourly(HourlyReport report) {

        // 1) Save Station
        HourlyWsStation station = new HourlyWsStation();

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

        // 2) Save Seconds → from secondList
        if (report.getSecondList() != null) {
            report.getSecondList().forEach(item -> {
                regHourlySecond(report.getMenuId(), report.getDate(), report.getHour(), item);
            });
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


    public ResponseEntity<DailyReportListResponse> getDailyReportList(Integer menuId, Date date) {

        DailyReportListResponse response = new DailyReportListResponse();
        response.setMenuId(menuId);
        response.setDate(date);

        // 1) Уг өдрийн бүх станцын цагийн мэдээлэл
        List<HourlyWsStation> stations = hourlyWsStationDAO.findByMenuIdAndDate(menuId, date);

        // 2) Уг өдрийн бүх second мэдээлэл
        List<HourlyWsSecond> seconds = hourlyWsSecondDAO.findAllByMenuIdAndDate(menuId, date);

        // 3) seconds-ийг hour-аар бүлэглэх
        Map<Integer, List<HourlyWsSecond>> secondsByHour = seconds.stream()
                .collect(Collectors.groupingBy(HourlyWsSecond::getHour));

        // 4) Эцсийн үр дүн
        List<HourReport> hourReports = new ArrayList<>();

        for (HourlyWsStation station : stations) {
            HourReport h = new HourReport();
            h.setHour(station.getHour());
            h.setStation(station);

            List<HourlyWsSecond> sec = secondsByHour.getOrDefault(station.getHour(), new ArrayList<>());
            h.setSeconds(sec);

            hourReports.add(h);
        }

        response.setHours(hourReports);

        return ResponseEntity.ok(response);
    }



    public HourlyReport getDailyReport(Integer menuId, Date date) {
        HourlyReport report = new HourlyReport();
        report.setMenuId(menuId);
        report.setDate(date);

        List<HourlyWsStation> stations = hourlyWsStationDAO.findByMenuIdAndDate(menuId, date);

        report.setFirstPool(stations.stream().mapToInt(s -> n(s.getFirstPool())).sum());
        report.setSecondPool(stations.stream().mapToInt(s -> n(s.getSecondPool())).sum());
        report.setThirdPool(stations.stream().mapToInt(s -> n(s.getThirdPool())).sum());
        report.setFourthPool(stations.stream().mapToInt(s -> n(s.getFourthPool())).sum());

        report.setPipeFm1(stations.stream().mapToInt(s -> n(s.getPipeFm1())).sum());
        report.setPipeFm7(stations.stream().mapToInt(s -> n(s.getPipeFm7())).sum());
        report.setPipeFm8(stations.stream().mapToInt(s -> n(s.getPipeFm8())).sum());

        // Худгийн тоо — хамгийн сүүлийн цагийн утгыг авна
        stations.stream()
                .reduce((a, b) -> b)
                .ifPresent(last -> {
                    report.setFirstWorkingCount(last.getFirstWorkingCount());
                    report.setFirstPendingCount(last.getFirstPendingCount());
                    report.setFirstRepairingCount(last.getFirstRepairingCount());
                });

        List<HourlyWsSecond> seconds = hourlyWsSecondDAO.findAllByMenuIdAndDate(menuId, date);

        Map<Integer, List<HourlyWsSecond>> grouped =
                seconds.stream().collect(Collectors.groupingBy(HourlyWsSecond::getGeneratorNo));

        // Насосны тоо — хамгийн сүүлийн цагийн бүртгэлээр
        int latestHour = seconds.stream()
                .mapToInt(s -> s.getHour() != null ? s.getHour() : 0)
                .max().orElse(-1);
        List<HourlyWsSecond> latestSeconds = latestHour >= 0
                ? seconds.stream().filter(s -> latestHour == (s.getHour() != null ? s.getHour() : 0)).collect(Collectors.toList())
                : new ArrayList<>();

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
