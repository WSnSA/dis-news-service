package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.TaskDepartmentRepository;
import mn.usug.dis_news_service.DAO.TaskRepository;
import mn.usug.dis_news_service.Entity.Task;
import mn.usug.dis_news_service.Entity.TaskDepartment;
import mn.usug.dis_news_service.Service.NotificationService;
import mn.usug.dis_news_service.Service.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ref/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository taskRepository;
    private final TaskDepartmentRepository taskDeptRepository;
    private final NotificationService notificationService;

    /* ── хариу task-уудад departments-г дүүргэнэ ── */
    private List<Task> withDepts(List<Task> tasks) {
        if (tasks.isEmpty()) return tasks;
        List<Integer> ids = tasks.stream().map(Task::getId).collect(Collectors.toList());
        Map<Integer, List<TaskDepartment>> map = taskDeptRepository.findByTaskIdIn(ids)
                .stream().collect(Collectors.groupingBy(TaskDepartment::getTaskId));
        tasks.forEach(t -> t.setDepartments(map.getOrDefault(t.getId(), List.of())));
        return tasks;
    }

    private Task withDepts(Task task) {
        task.setDepartments(taskDeptRepository.findByTaskId(task.getId()));
        return task;
    }

    /* ── dept хариуцагч нэгтгэсэн статус ── */
    private int calcStatus(List<TaskDepartment> depts) {
        if (depts == null || depts.isEmpty()) return 0;
        if (depts.stream().allMatch(d -> d.getStatus() == 2)) return 2;
        if (depts.stream().anyMatch(d -> d.getStatus() >= 1))  return 1;
        return 0;
    }

    /* =====================================
       1. БҮХ ҮҮРЭГ
       ===================================== */
    @GetMapping("/getAll")
    public List<Task> getAll() {
        return withDepts(taskRepository.findActive());
    }

    /* =====================================
       2. ӨДӨР ТУТМЫН ХУРЛЫН ҮҮРЭГ
       ===================================== */
    @GetMapping("/getByDate")
    public List<Task> getByDate(@RequestParam String date) {
        return withDepts(taskRepository.findByCreatedDate(LocalDate.parse(date)));
    }

    /* =====================================
       3. БИЕЛЭЛТ ХҮЛЭЭГДЭЖ БУЙ ТОО
       ===================================== */
    @GetMapping("/countPending")
    public long countPending() {
        return taskRepository.countPending();
    }

    /* =====================================
       4. АЛБА + ТУШААЛААР ШҮҮХ
       ===================================== */
    @GetMapping("/getAllByFilter")
    public List<Task> getByFilter(
            @RequestParam Integer depId,
            @RequestParam Integer positionId
    ) {
        List<Task> tasks;
        if (depId == 0) {
            tasks = taskRepository.findActive();
        } else {
            // task_departments-с хайх (шинэ)
            List<Integer> newIds = taskDeptRepository.findTaskIdsByDept(depId);
            // хуучин departmentId-р хайх
            List<Task> oldTasks = taskRepository.findActiveByDept(depId);
            Set<Integer> seen = new HashSet<>(newIds);
            List<Integer> combined = new ArrayList<>(newIds);
            oldTasks.forEach(t -> { if (seen.add(t.getId())) combined.add(t.getId()); });
            tasks = combined.isEmpty() ? List.of() : taskRepository.findByIds(combined);
        }
        return withDepts(tasks);
    }

    /* =====================================
       5. ҮҮРЭГ БҮРТГЭХ / ЗАСАХ
       Body: Task + departments хэсэгт [{departmentId}] жагсаалт
       ===================================== */
    @PostMapping("/save")
    public Task save(@RequestBody Task payload) {

        boolean isNew = payload.getId() == null;
        Task task;

        if (isNew) {
            task = new Task();
            task.setCreatedDate(LocalDateTime.now());
            task.setCreatedBy(UserContext.getUserId());
            task.setActiveFlag(1);
        } else {
            task = taskRepository.findById(payload.getId())
                    .orElseThrow(() -> new RuntimeException("Task not found"));
        }

        task.setAssignedPositionName(payload.getAssignedPositionName());
        task.setWorkDescription(payload.getWorkDescription());
        task.setDeadlineDate(payload.getDeadlineDate());
        // Нийт статус — task_departments-с тооцно; task-н өөрийн статус legacy-д хэрэглэнэ
        task.setDepartmentId(null);  // шинэ системд null
        task.setPositionId(null);

        Task saved = taskRepository.save(task);

        /* ── task_departments sync ── */
        List<TaskDepartment> incoming = payload.getDepartments();
        if (incoming != null && !incoming.isEmpty()) {
            List<Integer> newDeptIds = incoming.stream()
                    .map(TaskDepartment::getDepartmentId).collect(Collectors.toList());

            if (isNew) {
                // Шинэ: бүгдийг үүсгэнэ
                incoming.forEach(d -> {
                    TaskDepartment td = new TaskDepartment();
                    td.setTaskId(saved.getId());
                    td.setDepartmentId(d.getDepartmentId());
                    td.setStatus(0);
                    taskDeptRepository.save(td);
                });
                // Мэдэгдэл
                incoming.forEach(d -> notificationService.notifyTask(
                        saved.getWorkDescription(), d.getDepartmentId(), null));
            } else {
                // Засах: хасагдсан устгаж, нэмэгдсэн нэмнэ
                List<TaskDepartment> existing = taskDeptRepository.findByTaskId(saved.getId());
                Set<Integer> existingIds = existing.stream()
                        .map(TaskDepartment::getDepartmentId).collect(Collectors.toSet());

                // Устгах
                existing.stream()
                        .filter(td -> !newDeptIds.contains(td.getDepartmentId()))
                        .forEach(td -> taskDeptRepository.deleteById(td.getId()));

                // Нэмэх
                newDeptIds.stream()
                        .filter(dId -> !existingIds.contains(dId))
                        .forEach(dId -> {
                            TaskDepartment td = new TaskDepartment();
                            td.setTaskId(saved.getId());
                            td.setDepartmentId(dId);
                            td.setStatus(0);
                            taskDeptRepository.save(td);
                        });
            }
        }

        // Нийт статус шинэчлэх
        List<TaskDepartment> finalDepts = taskDeptRepository.findByTaskId(saved.getId());
        saved.setStatus(calcStatus(finalDepts));
        taskRepository.save(saved);

        return withDepts(saved);
    }

    /* =====================================
       6. БИЕЛЭЛТ ОРУУЛАХ (dept тус бүр)
       ===================================== */
    @PostMapping("/{taskId}/dept/{deptId}/fulfillment")
    @PutMapping("/{taskId}/dept/{deptId}/fulfillment")
    public Task updateDeptFulfillment(
            @PathVariable Integer taskId,
            @PathVariable Integer deptId,
            @RequestBody Map<String, Object> body
    ) {
        String fulfillment = body.getOrDefault("fulfillment", "").toString();
        Integer status = Integer.valueOf(body.getOrDefault("status", 0).toString());
        TaskDepartment td = taskDeptRepository.findByTaskIdAndDepartmentId(taskId, deptId)
                .orElseGet(() -> {
                    TaskDepartment newTd = new TaskDepartment();
                    newTd.setTaskId(taskId);
                    newTd.setDepartmentId(deptId);
                    newTd.setStatus(0);
                    return newTd;
                });

        td.setFulfillment(fulfillment);
        td.setStatus(status);
        td.setUpdatedDate(LocalDateTime.now());
        taskDeptRepository.save(td);

        // Нийт статус шинэчлэх
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        List<TaskDepartment> allDepts = taskDeptRepository.findByTaskId(taskId);
        task.setStatus(calcStatus(allDepts));
        taskRepository.save(task);

        return withDepts(task);
    }

    /* =====================================
       7. БУЦААХ / ШИЛЖҮҮЛЭХ
       ===================================== */
    @PutMapping("/reassign/{id}")
    public ResponseEntity<?> reassign(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) return ResponseEntity.status(404).body("Үүрэг олдсонгүй");
        if (body.containsKey("returnNote")) task.setReturnNote((String) body.get("returnNote"));

        // dept өөрчлөх бол task_departments-г шинэчилнэ
        if (body.containsKey("departmentId")) {
            Integer newDeptId = (Integer) body.get("departmentId");
            taskDeptRepository.deleteByTaskId(id);
            TaskDepartment td = new TaskDepartment();
            td.setTaskId(id);
            td.setDepartmentId(newDeptId);
            td.setStatus(0);
            taskDeptRepository.save(td);
            task.setStatus(0);
        }

        task.setFulfillment(null);
        taskRepository.save(task);
        return ResponseEntity.ok(withDepts(task));
    }

    /* =====================================
       8. SOFT DELETE
       ===================================== */
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setActiveFlag(0);
        taskRepository.save(task);
        taskDeptRepository.deleteByTaskId(id);
    }
}
