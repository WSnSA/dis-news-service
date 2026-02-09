package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.TaskRepository;
import mn.usug.dis_news_service.Entity.Task;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ref/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository taskRepository;

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
    @GetMapping("/getAllByFilter")
    public List<Task> getByFilter(
            @RequestParam Integer depId,
            @RequestParam Integer positionId
    ) {
        if (depId == 0 && positionId == 0) {
            return taskRepository.findAll();
        }
        return taskRepository.findByDepartmentIdAndPositionId(depId, positionId);
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
        if (payload.getId() == null) {
            task = new Task();

            task.setCreatedDate(LocalDateTime.now()); // Үүрэг өгсөн огноо
            task.setStatus(0);      // new
            task.setActiveFlag(1);  // идэвхтэй
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

        return taskRepository.save(task);
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
