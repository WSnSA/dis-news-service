package mn.usug.dis_news_service.Service;

// WorkNewsService.java
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.WorkNewsDayRepository;
import mn.usug.dis_news_service.DAO.WorkNewsItemRepository;
import mn.usug.dis_news_service.DTO.WorkNewsDayRes;
import mn.usug.dis_news_service.DTO.WorkNewsItemCreateReq;
import mn.usug.dis_news_service.DTO.WorkNewsItemRes;
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

    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy/MM");

    @Transactional
    public Long createItem(WorkNewsItemCreateReq req, Long userId) {
        LocalDate date = LocalDate.parse(req.newsDate()); // yyyy-MM-dd
        String monthKey = date.format(MONTH_KEY);

        WorkNewsDay day = dayRepo.findByNewsDate(date).orElseGet(() -> {
            WorkNewsDay d = new WorkNewsDay();
            d.setNewsDate(date);
            d.setMonthKey(monthKey);
            d.setStatus(1);
            d.setCreatedAt(LocalDateTime.now());
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
        item.setCreatedAt(LocalDateTime.now());
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
                        i.getCreatedBy()
                ))
                .toList();

        return new WorkNewsDayRes(day.getNewsDate().toString(), day.getMonthKey(), items);
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
                            i.getCreatedBy()
                    ))
                    .toList();
            res.add(new WorkNewsDayRes(d.getNewsDate().toString(), d.getMonthKey(), items));
        }
        return res;
    }
}