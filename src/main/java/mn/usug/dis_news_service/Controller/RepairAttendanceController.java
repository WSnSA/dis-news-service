package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.RepairAttendanceRepository;
import mn.usug.dis_news_service.DAO.RepairSpecialtyRepository;
import mn.usug.dis_news_service.DAO.RepairWorkerRepository;
import mn.usug.dis_news_service.Entity.RepairAttendance;
import mn.usug.dis_news_service.Entity.RepairSpecialty;
import mn.usug.dis_news_service.Entity.RepairWorker;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Засварын хэсгийн өдөр тутмын ирц.
 *
 * Өдөр тутмын мэдээний эхний хоёр мөр үүнээс бүрдэнэ:
 *   "АВТО БААЗ ДЭЭР-:7 … нар ажиллаж байна"
 *   "Өвчтэй-1: … Нөхөн амралт-2: …"
 */
@RestController
@RequestMapping("/repair/attendance")
@RequiredArgsConstructor
public class RepairAttendanceController {

    private static final Integer ACTIVE = 1;

    private final RepairAttendanceRepository repository;
    private final RepairWorkerRepository workerRepository;
    private final RepairSpecialtyRepository specialtyRepository;

    /** Тухайн өдрийн ирц — нэр, мэргэжлийн хамт */
    @GetMapping
    public List<RepairAttendance> getByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return enrich(repository.findByWorkDateAndActiveFlagOrderByIdAsc(date, ACTIVE));
    }

    /**
     * Нэг өдрийн ирцийг бүтнээр нь солино. Давхар бичлэг үүсгэхгүйн тулд
     * тухайн өдрийн хуучин мөрүүдийг устгаад шинээр бичнэ.
     */
    @PostMapping
    public List<RepairAttendance> save(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody List<RepairAttendance> rows
    ) {
        repository.deleteAll(repository.findByWorkDateAndActiveFlagOrderByIdAsc(date, ACTIVE));
        for (RepairAttendance row : rows) {
            row.setId(null);
            row.setWorkDate(date);
            row.setActiveFlag(ACTIVE);
            if (row.getStatus() == null) row.setStatus(RepairAttendance.WORKED);
        }
        return enrich(repository.saveAll(rows));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private List<RepairAttendance> enrich(List<RepairAttendance> rows) {
        if (rows.isEmpty()) return rows;

        Map<Long, RepairWorker> workers = workerRepository.findAll().stream()
                .collect(Collectors.toMap(RepairWorker::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> specialties = specialtyRepository.findAll().stream()
                .collect(Collectors.toMap(RepairSpecialty::getId, RepairSpecialty::getName, (a, b) -> a));

        rows.forEach(r -> {
            RepairWorker w = workers.get(r.getRepairWorkerId());
            if (w != null) {
                r.setWorkerName(w.getName());
                r.setSpecialtyName(specialties.get(w.getSpecialtyId()));
            }
        });
        return rows;
    }
}
