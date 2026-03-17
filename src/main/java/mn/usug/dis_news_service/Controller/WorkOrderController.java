package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.WorkOrderRepository;
import mn.usug.dis_news_service.DTO.WorkNewsItemCreateReq;
import mn.usug.dis_news_service.Entity.WorkOrder;
import mn.usug.dis_news_service.Service.NotificationService;
import mn.usug.dis_news_service.Service.UserContext;
import mn.usug.dis_news_service.Service.WorkNewsService;
import org.springframework.web.bind.annotation.*;

import mn.usug.dis_news_service.DTO.WorkNewsItemCreateReq;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ref/work-order")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderRepository repository;
    private final NotificationService notificationService;
    private final WorkNewsService workNewsService;

    /* =====================
       GET ALL (ACTIVE)
       ===================== */
    @GetMapping("/getAll")
    public List<WorkOrder> getAll() {
        return repository.findActive();
    }

    /* =====================
       GET BY DATE
       ===================== */
    @GetMapping("/getByDate")
    public List<WorkOrder> getByDate(@RequestParam LocalDate date) {
        return repository.findByDate(date);
    }

    /* =====================
       GET BY DEPT (Үүсгэсэн / Хүлээн авсан)
       ===================== */
    @GetMapping("/getByAssignedDept")
    public List<WorkOrder> getByAssignedDept(@RequestParam Integer deptId) {
        return repository.findByAssignedDept(deptId);
    }

    @GetMapping("/getByExecutorDept")
    public List<WorkOrder> getByExecutorDept(@RequestParam Integer deptId) {
        return repository.findByExecutorDept(deptId);
    }

    /* =====================
       SAVE (INSERT / UPDATE)
       ===================== */
    @PostMapping("/save")
    public WorkOrder save(@RequestBody WorkOrder req) {

        // 🟢 INSERT
        if (req.getId() == null) {
            req.setCreatedDate(LocalDateTime.now());
            req.setCreatedBy(UserContext.getUserId());
            req.setStatus(0);
            req.setActiveFlag(1);
            WorkOrder saved = repository.save(req);
            notificationService.notifyWorkOrderNew(
                    saved.getWorkLocation() != null ? saved.getWorkLocation() : "",
                    saved.getDepartmentId()
            );
            return saved;
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
        db.setLat(req.getLat());
        db.setLng(req.getLng());

        db.setUpdatedDate(LocalDateTime.now());
        db.setUpdatedBy(UserContext.getUserId());

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

    /* =====================
       UPDATE STATUS (Хүлээн авсан tab-аас төлөв өөрчлөх)
       status=2 → ажлын мэдээнд автоматаар бүртгэж, үүсгэсэн албанд мэдэгдэл илгээнэ
       ===================== */
    @PutMapping("/updateStatus/{id}")
    public WorkOrder updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {

        WorkOrder w = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkOrder not found: " + id));

        if (body.containsKey("status"))
            w.setStatus(((Number) body.get("status")).intValue());
        if (body.containsKey("fulfillment"))
            w.setFulfillment((String) body.get("fulfillment"));

        w.setUpdatedDate(LocalDateTime.now());
        w.setUpdatedBy(UserContext.getUserId());
        WorkOrder saved = repository.save(w);

        // Дууссан үед ажлын мэдээнд автоматаар бүртгэнэ
        if (saved.getStatus() != null && saved.getStatus() == 2) {
            String title = (saved.getWorkLocation() != null ? saved.getWorkLocation() : "")
                    + (saved.getWorkDescription() != null ? " — " + saved.getWorkDescription() : "");
            String fulfillment = saved.getFulfillment() != null ? saved.getFulfillment() : "";
            WorkNewsItemCreateReq req = new WorkNewsItemCreateReq(
                    LocalDate.now().toString(),
                    "WORK_ORDER",
                    title,
                    fulfillment,
                    0,
                    null,
                    saved.getAssignedDepartmentId() != null ? saved.getAssignedDepartmentId().longValue() : 0L,
                    null
            );
            workNewsService.createItem(req);
            notificationService.notifyWorkOrderDone(
                    saved.getWorkLocation() != null ? saved.getWorkLocation() : "",
                    saved.getAssignedDepartmentId()
            );
        }

        return saved;
    }
}
