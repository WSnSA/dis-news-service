package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.TaskRepository;
import mn.usug.dis_news_service.Entity.Task;
import mn.usug.dis_news_service.Service.NotificationService;
import mn.usug.dis_news_service.Service.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ref/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    /* =====================================
       1. БҮХ ҮҮРЭГ
       ===================================== */
    @GetMapping("/getAll")
    public List<Task> getAll() {
        return taskRepository.findActive();
    }

    /* =====================================
       2. ӨДӨР ТУТМЫН ХУРЛЫН ҮҮРЭГ
       ===================================== */
    @GetMapping("/getByDate")
    public List<Task> getByDate(@RequestParam String date) {
        return taskRepository.findByCreatedDate(LocalDate.parse(date));
    }

    /* =====================================
       3. АЛБА + АЛБАН ТУШААЛ
       ===================================== */
    @GetMapping("/countPending")
    public long countPending() {
        return taskRepository.countPending();
    }

    @PutMapping("/reassign/{id}")
    public ResponseEntity<?> reassign(@PathVariable Integer id, @RequestBody java.util.Map<String, Object> body) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) return ResponseEntity.status(404).body("Үүрэг олдсонгүй");
        if (body.containsKey("returnNote"))   task.setReturnNote((String) body.get("returnNote"));
        if (body.containsKey("departmentId")) task.setDepartmentId((Integer) body.get("departmentId"));
        if (body.containsKey("positionId"))   task.setPositionId((Integer) body.get("positionId"));
        task.setStatus(0);
        task.setFulfillment(null);
        taskRepository.save(task);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/getAllByFilter")
    public List<Task> getByFilter(
            @RequestParam Integer depId,
            @RequestParam Integer positionId
    ) {
        if (depId == 0 && positionId == 0) {
            return taskRepository.findActive();
        }
        if (positionId == 0) {
            return taskRepository.findActiveByDept(depId);
        }
        return taskRepository.findActiveByDeptAndPosition(depId, positionId);
    }

    /* =====================================
       4. ҮҮРЭГ БҮРТГЭХ (ХУРЛААР)
       ===================================== */
    @PostMapping("/save")
    public Task save(@RequestBody Task payload) {

        Task task;

        // =========================
        // CREATE
        // =========================
        boolean isNew = payload.getId() == null;
        if (isNew) {
            task = new Task();

            task.setCreatedDate(LocalDateTime.now());
            task.setCreatedBy(UserContext.getUserId());
            task.setStatus(0);
            task.setActiveFlag(1);
        }
        // =========================
        // UPDATE
        // =========================
        else {
            task = taskRepository.findById(payload.getId())
                    .orElseThrow(() -> new RuntimeException("Task not found"));
        }

        // =========================
        // COMMON FIELDS (create + update)
        // =========================
        task.setAssignedPositionName(payload.getAssignedPositionName());
        task.setWorkDescription(payload.getWorkDescription());
        task.setDepartmentId(payload.getDepartmentId());
        task.setPositionId(payload.getPositionId());
        task.setDeadlineDate(payload.getDeadlineDate());

        Task saved = taskRepository.save(task);
        if (isNew) {
            notificationService.notifyTask(
                saved.getWorkDescription(),
                saved.getDepartmentId(),
                saved.getPositionId()
            );
        }
        return saved;
    }


    /* =====================================
       5. БИЕЛЭЛТ ОРУУЛАХ
       ===================================== */
    @PutMapping("/updateFulfillment/{id}")
    public Task updateFulfillment(
            @PathVariable Integer id,
            @RequestParam String fulfillment,
            @RequestParam Integer status
    ) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setFulfillment(fulfillment);
        task.setStatus(status);

        return taskRepository.save(task);
    }

    /* =====================================
       6. SOFT DELETE (АРХИВ)
       ===================================== */
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setActiveFlag(0);
        taskRepository.save(task);
    }
}
