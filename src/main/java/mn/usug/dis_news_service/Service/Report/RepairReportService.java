package mn.usug.dis_news_service.Service.Report;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.*;
import mn.usug.dis_news_service.Entity.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Засварын мэдээний өгөгдөл цуглуулагч.
 *
 * Ц.Нямдоржийн ашигладаг гурван загварыг нэг эх сурвалжаас бүрдүүлнэ:
 *   - өдөр тутмын мэдээ (zasvariin medee)
 *   - 7 хоногийн тайлан
 *   - хагас жил / жилийн нэгтгэл
 */
@Service
@RequiredArgsConstructor
public class RepairReportService {

    private static final Integer ACTIVE = 1;

    /** vehicle.service_type → тайланд гардаг хэсгийн нэр */
    public static final Map<Integer, String> SECTIONS = Map.of(
            1, "Үйлчилгээ",
            2, "Цэвэр",
            3, "Бохир"
    );

    private final VehicleRepairRepository repairRepository;
    private final VehicleRepository vehicleRepository;
    private final RepairCategoryRepository categoryRepository;
    private final RepairWorkerRepository workerRepository;
    private final RepairSpecialtyRepository specialtyRepository;
    private final RepairAttendanceRepository attendanceRepository;

    /** Нэг засварын мөр — тайланд шаардагдах бүх талбартай */
    public record Row(
            String plate, String brand, String model,
            String section,          // Цэвэр / Бохир / Үйлчилгээ
            String categoryName,
            String fault,
            LocalDate startDate, java.time.LocalTime startTime,
            LocalDate endDate, LocalDate expectedReady,
            boolean done, boolean waitingParts
    ) {
        /** Тайлангийн "Төрөл" багана — марк, загвар */
        public String typeText() {
            String t = (brand == null ? "" : brand) + " " + (model == null ? "" : model);
            return t.trim().isEmpty() ? "—" : t.trim();
        }
    }

    /** Ирцийн нэгтгэл */
    public record Attendance(List<String> worked, List<String> sick, List<String> leave,
                             Map<String, Long> bySpecialty) {}

    /** Нийт паркийн тоо — "154 авто машин" гэсэн мөрөнд ордог */
    public long fleetTotal() {
        return vehicleRepository.count();
    }

    /** Огнооны мужид хамрагдах бүх засвар */
    public List<Row> rows(LocalDate from, LocalDate to) {
        Map<Long, Vehicle> vehicles = vehicleRepository.findAll().stream()
                .collect(Collectors.toMap(Vehicle::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(RepairCategory::getId, RepairCategory::getName, (a, b) -> a));

        List<Row> out = new ArrayList<>();
        for (VehicleRepair r : repairRepository.findAll()) {
            if (!ACTIVE.equals(r.getActiveFlag())) continue;
            LocalDate start = r.getStartDate();
            if (start == null) continue;

            // Тухайн мужид ЭХЭЛСЭН эсвэл мужийн туршид ҮРГЭЛЖИЛЖ байсан засвар
            LocalDate end = r.getEndDate();
            boolean overlaps = !start.isAfter(to) && (end == null || !end.isBefore(from));
            if (!overlaps) continue;

            Vehicle v = vehicles.get(r.getVehicleId());
            Integer st = v != null ? v.getServiceType() : null;

            out.add(new Row(
                    r.getPlateNumber(),
                    v != null ? v.getBrand() : null,
                    v != null ? v.getModel() : null,
                    SECTIONS.getOrDefault(st == null ? -1 : st, "Бусад"),
                    categories.getOrDefault(r.getRepairCategoryId(), ""),
                    firstNonBlank(r.getFaultDescription(), r.getNote()),
                    start, r.getStartTime(), end, r.getExpectedReady(),
                    Integer.valueOf(VehicleRepair.STATUS_DONE).equals(r.getStatus()),
                    r.getWaitingParts() != null && r.getWaitingParts() == 1
            ));
        }
        out.sort(Comparator
                .comparing(Row::section)
                .thenComparing(row -> row.startDate() == null ? LocalDate.MIN : row.startDate()));
        return out;
    }

    /** Тухайн өдөр засварт байсан машинууд */
    public List<Row> dailyRows(LocalDate date) {
        return rows(date, date);
    }

    /** Ирц — өдөр тутмын мэдээний толгойд болон 7 хоногийн бүрэлдэхүүнд */
    public Attendance attendance(LocalDate from, LocalDate to) {
        Map<Long, RepairWorker> workers = workerRepository.findAll().stream()
                .collect(Collectors.toMap(RepairWorker::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> specialties = specialtyRepository.findAll().stream()
                .collect(Collectors.toMap(RepairSpecialty::getId, RepairSpecialty::getName, (a, b) -> a));

        List<String> worked = new ArrayList<>(), sick = new ArrayList<>(), leave = new ArrayList<>();
        Map<String, Long> bySpecialty = new LinkedHashMap<>();
        Set<Long> countedForSpecialty = new HashSet<>();

        for (RepairAttendance a : attendanceRepository.findByWorkDateBetweenAndActiveFlag(from, to, ACTIVE)) {
            RepairWorker w = workers.get(a.getRepairWorkerId());
            if (w == null) continue;
            String name = w.getName();
            switch (a.getStatus() == null ? RepairAttendance.WORKED : a.getStatus()) {
                case RepairAttendance.SICK  -> { if (!sick.contains(name))  sick.add(name); }
                case RepairAttendance.LEAVE -> { if (!leave.contains(name)) leave.add(name); }
                default -> {
                    if (!worked.contains(name)) worked.add(name);
                    // 7 хоногийн "8-Засварчин 1-Цахилгаанчин" мөрд нэг хүнийг нэг л удаа тоолно
                    if (countedForSpecialty.add(w.getId())) {
                        String sp = specialties.getOrDefault(w.getSpecialtyId(), "Бусад");
                        bySpecialty.merge(sp, 1L, Long::sum);
                    }
                }
            }
        }
        return new Attendance(worked, sick, leave, bySpecialty);
    }

    /** Хэсэг тус бүрийн тоо — "Цэвэр усны хэсгийн – 22 т/х" */
    public Map<String, Long> countBySection(List<Row> rows) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String s : List.of("Цэвэр", "Бохир", "Үйлчилгээ")) counts.put(s, 0L);
        for (Row r : rows) counts.merge(r.section(), 1L, Long::sum);
        return counts;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v.trim();
        return "";
    }
}
