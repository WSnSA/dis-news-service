package mn.usug.dis_news_service.Service;

// WorkNewsService.java
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.DepartmentDAO;
import mn.usug.dis_news_service.DAO.UserDAO;
import mn.usug.dis_news_service.DAO.WorkNewsDayRepository;
import mn.usug.dis_news_service.DAO.WorkNewsItemRepository;
import mn.usug.dis_news_service.DTO.WorkNewsDayRes;
import mn.usug.dis_news_service.DTO.WorkNewsItemCreateReq;
import mn.usug.dis_news_service.DTO.WorkNewsItemRes;
import mn.usug.dis_news_service.Entity.Department;
import mn.usug.dis_news_service.Entity.User;
import mn.usug.dis_news_service.Entity.WorkNewsDay;
import mn.usug.dis_news_service.Entity.WorkNewsItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkNewsService {

    private final WorkNewsDayRepository dayRepo;
    private final WorkNewsItemRepository itemRepo;
    private final UserDAO userDAO;
    private final DepartmentDAO departmentDAO;

    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy/MM");

    private LocalDate resolveShiftDate(LocalDateTime dt) {
        return dt.toLocalTime().isBefore(LocalTime.of(8, 0))
                ? dt.toLocalDate().minusDays(1)
                : dt.toLocalDate();
    }

    private LocalDate currentShiftDate() {
        return resolveShiftDate(LocalDateTime.now());
    }

    private void validateShiftEditable(LocalDate targetDate) {
        if (!Objects.equals(targetDate, currentShiftDate())) {
            throw new IllegalStateException("Зөвхөн одоогийн ээлжийн мэдээг засах боломжтой.");
        }
    }

    @Transactional
    public Long createItem(WorkNewsItemCreateReq req) {
        Integer ctxId = UserContext.getUserId();
        Long userId = ctxId != null ? ctxId.longValue()
                : (req.userId() != null ? req.userId() : 0L);
        Long departmentId = req.departmentId() != null ? req.departmentId() : 0L;

        LocalDateTime now = LocalDateTime.now();
        LocalDate date = resolveShiftDate(now);
        String monthKey = date.format(MONTH_KEY);

        WorkNewsDay day = dayRepo.findByNewsDate(date).orElseGet(() -> {
            WorkNewsDay d = new WorkNewsDay();
            d.setNewsDate(date);
            d.setMonthKey(monthKey);
            d.setStatus(1);
            d.setCreatedAt(now);
            d.setCreatedBy(userId);
            return dayRepo.save(d);
        });

        WorkNewsItem item = new WorkNewsItem();
        item.setDay(day);
        item.setItemType((req.itemType() == null || req.itemType().isBlank()) ? "OTHER" : req.itemType());
        item.setTitle(req.title());
        item.setContent(req.content());
        item.setMetaJson(req.metaJson());
        item.setSortOrder(req.sortOrder() == null ? 0 : req.sortOrder());
        item.setDepartmentId(departmentId);
        item.setCreatedAt(now);
        item.setCreatedBy(userId);

        return itemRepo.save(item).getId();
    }

    @Transactional(readOnly = true)
    public WorkNewsDayRes getByDay(LocalDate date) {
        WorkNewsDay day = dayRepo.findByNewsDate(date)
                .orElseThrow(() -> new NoSuchElementException("Work news day not found: " + date));

        List<WorkNewsItemRes> items = itemRepo.findByDayIdOrderBySortOrderAsc(day.getId())
                .stream()
                .map(i -> new WorkNewsItemRes(
                        i.getId(),
                        i.getItemType(),
                        i.getTitle(),
                        i.getContent(),
                        i.getSortOrder(),
                        i.getCreatedAt() == null ? null : i.getCreatedAt().toString(),
                        i.getCreatedBy(),
                        resolveUserName(i.getCreatedBy()),
                        i.getDepartmentId(),
                        resolveDeptName(i.getDepartmentId())
                ))
                .toList();

        return new WorkNewsDayRes(day.getNewsDate().toString(), day.getMonthKey(), items);
    }

    @Transactional
    public void updateItem(Long id, WorkNewsItemCreateReq req) {
        WorkNewsItem item = itemRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("WorkNewsItem not found: " + id));

        validateShiftEditable(item.getDay().getNewsDate());

        Integer ctxId = UserContext.getUserId();

        if (req.itemType() != null && !req.itemType().isBlank()) item.setItemType(req.itemType());
        if (req.title() != null) item.setTitle(req.title());
        if (req.content() != null) item.setContent(req.content());
        if (req.metaJson() != null) item.setMetaJson(req.metaJson());
        if (req.sortOrder() != null) item.setSortOrder(req.sortOrder());

        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(ctxId != null ? ctxId.longValue() : null);

        itemRepo.save(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        WorkNewsItem item = itemRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("WorkNewsItem not found: " + id));

        validateShiftEditable(item.getDay().getNewsDate());
        itemRepo.delete(item);
    }

    private String resolveUserName(Long userId) {
        if (userId == null || userId == 0L) return null;
        User u = userDAO.findById(userId.intValue()).orElse(null);
        if (u == null) return null;
        String ln = u.getLastName() != null ? u.getLastName().substring(0, 1) + "." : "";
        String fn = u.getFirstName() != null ? u.getFirstName() : "";
        return (ln + fn).trim();
    }

    private String resolveDeptName(Long departmentId) {
        if (departmentId == null || departmentId == 0L) return null;
        Department d = departmentDAO.findById(departmentId.intValue()).orElse(null);
        return d != null ? d.getDepName() : null;
    }

    @Transactional(readOnly = true)
    public List<WorkNewsDayRes> getByMonth(String monthKey, boolean desc) {
        List<WorkNewsDay> days = desc
                ? dayRepo.findByMonthKeyOrderByNewsDateDesc(monthKey)
                : dayRepo.findByMonthKeyOrderByNewsDateAsc(monthKey);

        List<WorkNewsDayRes> res = new ArrayList<>();
        for (WorkNewsDay d : days) {
            List<WorkNewsItemRes> items = itemRepo.findByDayIdOrderBySortOrderAsc(d.getId())
                    .stream()
                    .map(i -> new WorkNewsItemRes(
                            i.getId(),
                            i.getItemType(),
                            i.getTitle(),
                            i.getContent(),
                            i.getSortOrder(),
                            i.getCreatedAt() == null ? null : i.getCreatedAt().toString(),
                            i.getCreatedBy(),
                            resolveUserName(i.getCreatedBy()),
                            i.getDepartmentId(),
                            resolveDeptName(i.getDepartmentId())
                    ))
                    .toList();
            res.add(new WorkNewsDayRes(d.getNewsDate().toString(), d.getMonthKey(), items));
        }
        return res;
    }
}