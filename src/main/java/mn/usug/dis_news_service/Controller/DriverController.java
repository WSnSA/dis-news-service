package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.DriverRepository;
import mn.usug.dis_news_service.DAO.VehiclesToOutCancelRepository;
import mn.usug.dis_news_service.DAO.VehiclesToOutRepository;
import mn.usug.dis_news_service.Entity.Driver;
import mn.usug.dis_news_service.Entity.VehiclesToOutCancel;
import mn.usug.dis_news_service.Model.DriverHistoryDto;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ref/driver")
@RequiredArgsConstructor
public class DriverController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    private final DriverRepository repository;
    private final VehiclesToOutRepository dispatchRepo;
    private final VehiclesToOutCancelRepository cancelRepo;

    /**
     * @param includeInactive true бол устгасан жолоочийг ч буцаана —
     *        "Ажилтан" хуудсанд саарлаар харуулахад хэрэгтэй.
     */
    @GetMapping("/getAll")
    public List<Driver> getAll(
            @RequestParam(value = "includeInactive", required = false, defaultValue = "false")
            boolean includeInactive
    ) {
        return includeInactive
                ? repository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                : repository.findByActiveFlagOrderByNameAsc(ACTIVE);
    }

    /** Хуваарилалтын хайлт — устгасан жолооч сонголтод гарахгүй */
    @GetMapping("/search")
    public List<Driver> search(@RequestParam String q) {
        return repository.findByActiveFlagAndNameContainingIgnoreCase(ACTIVE, q);
    }

    @PostMapping("/save")
    public Driver save(@RequestBody Driver driver) {
        driver.setId(null);
        driver.setActiveFlag(ACTIVE);
        return repository.save(driver);
    }

    @PutMapping("/update/{id}")
    public Driver update(@PathVariable Long id, @RequestBody Driver driver) {
        Driver existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + id));
        existing.setName(driver.getName());
        existing.setPhone(driver.getPhone());
        existing.setLicenseCategories(driver.getLicenseCategories());
        return repository.save(existing);
    }

    /**
     * Жолоочийн ажлын түүх — хэзээ, ямар машинаар, аль албанд гарсан.
     *
     * from/to өгөөгүй бол сүүлийн 1 жилийг хамруулна (бүх түүх хэт том).
     */
    @GetMapping("/history")
    public List<DriverHistoryDto> history(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        LocalDate fromDate = parseDate(from, LocalDate.now().minusYears(1));
        LocalDate toDate   = parseDate(to,   LocalDate.now().plusYears(1));

        List<Object[]> rows = dispatchRepo.findDriverHistory(fromDate, toDate);
        if (rows.isEmpty()) return List.of();

        // Цуцлагдсан өдрүүдийг нэг дуудлагаар авна
        List<Integer> vtoIds = rows.stream()
                .map(r -> r[0] != null ? ((Number) r[0]).intValue() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, List<LocalDate>> cancelled = new HashMap<>();
        for (VehiclesToOutCancel c : cancelRepo.findByVehiclesToOutIdIn(vtoIds)) {
            cancelled.computeIfAbsent(c.getVehiclesToOutId(), k -> new ArrayList<>()).add(c.getCancelDate());
        }

        Map<Long, Driver> driverMap = repository.findAll().stream()
                .collect(Collectors.toMap(Driver::getId, Function.identity(), (a, b) -> a));

        Map<Long, List<DriverHistoryDto.Trip>> tripsByDriver = new LinkedHashMap<>();
        Map<Long, String> nameById  = new HashMap<>();
        Map<Long, String> phoneById = new HashMap<>();
        // Өдөр давхардуулахгүйн тулд жолооч тус бүрийн ажилласан өдрүүдийг цуглуулна
        Map<Long, Set<LocalDate>> daysByDriver = new HashMap<>();

        for (Object[] r : rows) {
            Integer vtoId   = r[0] != null ? ((Number) r[0]).intValue() : null;
            Long driverId   = r[1] != null ? ((Number) r[1]).longValue() : null;
            if (driverId == null) continue;

            LocalDate start = toLocalDate(r[8]);
            LocalDate end   = toLocalDate(r[9]);
            if (start == null) continue;
            if (end == null || end.isBefore(start)) end = start;

            List<LocalDate> skip = cancelled.getOrDefault(vtoId, List.of());
            Set<LocalDate> worked = new LinkedHashSet<>();
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                if (!skip.contains(d)) worked.add(d);
            }
            daysByDriver.computeIfAbsent(driverId, k -> new HashSet<>()).addAll(worked);

            nameById.putIfAbsent(driverId, str(r[2]));
            phoneById.putIfAbsent(driverId, str(r[3]));

            tripsByDriver.computeIfAbsent(driverId, k -> new ArrayList<>())
                    .add(DriverHistoryDto.Trip.builder()
                            .id(vtoId)
                            .plateNumber(str(r[4]))
                            .vehicleText(str(r[5]))
                            .department(str(r[6]))
                            .workDescription(str(r[7]))
                            .startDate(start)
                            .endDate(end)
                            .cancelledDates(skip.stream().sorted().toList())
                            .days(worked.size())
                            .build());
        }

        List<DriverHistoryDto> out = new ArrayList<>();
        for (Map.Entry<Long, List<DriverHistoryDto.Trip>> e : tripsByDriver.entrySet()) {
            Driver d = driverMap.get(e.getKey());
            List<DriverHistoryDto.Trip> trips = e.getValue();
            out.add(DriverHistoryDto.builder()
                    .driverId(e.getKey())
                    // Лавлах дээрх одоогийн нэр гол; мөрөнд бичсэн нэр нь нөөц
                    .driverName(d != null ? d.getName() : nameById.get(e.getKey()))
                    .phone(d != null && d.getPhone() != null ? d.getPhone() : phoneById.get(e.getKey()))
                    .activeFlag(d != null ? d.getActiveFlag() : 1)
                    .tripCount(trips.size())
                    .workedDays(daysByDriver.getOrDefault(e.getKey(), Set.of()).size())
                    .vehicleCount((int) trips.stream()
                            .map(DriverHistoryDto.Trip::getPlateNumber)
                            .filter(Objects::nonNull)
                            .distinct().count())
                    .trips(trips)
                    .build());
        }

        out.sort((a, b) -> Integer.compare(b.getWorkedDays(), a.getWorkedDays()));
        return out;
    }

    private LocalDate parseDate(String s, LocalDate fallback) {
        if (s == null || s.isBlank()) return fallback;
        try {
            return LocalDate.parse(s.trim().substring(0, 10));
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof LocalDate d) return d;
        try {
            return LocalDate.parse(o.toString().substring(0, 10));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    /** Soft delete — хуваарилалтын түүхэд жолоочийн нэр үлдэх ёстой */
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(d -> {
            d.setActiveFlag(INACTIVE);
            repository.save(d);
        });
    }
}
