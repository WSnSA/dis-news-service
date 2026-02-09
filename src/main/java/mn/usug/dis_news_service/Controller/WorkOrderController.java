package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.WorkOrderRepository;
import mn.usug.dis_news_service.Entity.WorkOrder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ref/work-order")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderRepository repository;

    /* =====================
       GET ALL (ACTIVE)
       ===================== */
    @GetMapping("/getAll")
    public List<WorkOrder> getAll() {
        return repository.findActive();
    }

    /* =====================
       GET BY DATE (ХАДГАЛНА)
       ===================== */
    @GetMapping("/getByDate")
    public List<WorkOrder> getByDate(@RequestParam LocalDate date) {
        return repository.findByDate(date);
    }

    /* =====================
       SAVE (INSERT / UPDATE)
       ===================== */
    @PostMapping("/save")
    public WorkOrder save(@RequestBody WorkOrder req) {

        // 🟢 INSERT
        if (req.getId() == null) {
            req.setCreatedDate(LocalDateTime.now());
            req.setStatus(0);      // Шинэ
            req.setActiveFlag(1);  // Идэвхтэй
            return repository.save(req);
        }

        // 🟡 UPDATE – заавал DB-с уншиж байж засна
        WorkOrder db = repository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("WorkOrder not found"));

        // 🔹 засах боломжтой талбарууд
        db.setWorkDescription(req.getWorkDescription());
        db.setWorkLocation(req.getWorkLocation());
        db.setDepartmentId(req.getDepartmentId());
        db.setAssignedDepartmentId(req.getAssignedDepartmentId());
        db.setAssignedEmployeeId(req.getAssignedEmployeeId());
        db.setDeadlineDate(req.getDeadlineDate());
        db.setFulfillment(req.getFulfillment());
        db.setStatus(req.getStatus());

        db.setUpdatedDate(LocalDateTime.now());
        db.setUpdatedBy(req.getUpdatedBy());

        return repository.save(db);
    }

    /* =====================
       SOFT DELETE
       ===================== */
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        repository.findById(id).ifPresent(w -> {
            w.setActiveFlag(0);
            w.setUpdatedDate(LocalDateTime.now());
            repository.save(w);
        });
    }
}
