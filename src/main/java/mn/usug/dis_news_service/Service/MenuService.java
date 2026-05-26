package mn.usug.dis_news_service.Service;

import mn.usug.dis_news_service.DAO.MenuDAO;
import mn.usug.dis_news_service.Entity.Menu;
import mn.usug.dis_news_service.Model.HourlyReport;
import mn.usug.dis_news_service.Model.MenuModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class MenuService {
    @Autowired
    MenuDAO menuDAO;
    @Autowired
    private MainService mainService;

    public Iterable<Menu> getAllMenus() {
        return menuDAO.findAll();
    }

    public Menu addMenu(MenuModel model) {
        Menu menu = new Menu();
        menu.setName(model.getName());
        menu.setIcon(model.getIcon());
        menu.setParentId(model.getParentId());
        menu.setPath(model.getPath());
        menu.setComponent(model.getComponent());
        menu.setActiveFlag(1);
        // Шинэ цэс хамгийн төгсгөлд орно
        Integer maxOrder = menuDAO.findAll().stream()
                .map(m -> m.getSortOrder() != null ? m.getSortOrder() : m.getId())
                .max(Integer::compareTo)
                .orElse(0);
        menu.setSortOrder(maxOrder + 1);
        menu.setCreatedDate(new Date());
        menu.setUpdatedDate(new Date());
        return menuDAO.save(menu);
    }

    /**
     * Цэсний харагдах дарааллыг batch хэлбэрээр шинэчилнэ.
     * Зөвхөн id + sortOrder утгыг ашиглана.
     */
    public Iterable<Menu> reorderMenus(List<MenuModel> orders) {
        for (MenuModel o : orders) {
            if (o.getId() == null || o.getSortOrder() == null) continue;
            Menu menu = menuDAO.findById(o.getId()).orElse(null);
            if (menu == null) continue;
            menu.setSortOrder(o.getSortOrder());
            menu.setUpdatedDate(new Date());
            menuDAO.save(menu);
        }
        return menuDAO.findAll();
    }

    public Menu editMenu(MenuModel model) {
        Menu menu = menuDAO.findById(model.getId()).orElse(null);
        if (menu == null) {
            return null;
        }
        menu.setName(model.getName());
        menu.setIcon(model.getIcon());
        menu.setParentId(model.getParentId());
        menu.setComponent(model.getComponent());
        menu.setPath(model.getPath());
        menu.setUpdatedDate(new Date());

        return menuDAO.save(menu);
    }

    public Menu deleteMenu(Integer id) {
        Menu menu = menuDAO.findById(id).orElse(null);
        if (menu == null) {
            return null;
        }
        menu.setActiveFlag(0);
        menu.setUpdatedDate(new Date());
        return menuDAO.save(menu);
    }

    public ResponseEntity getMarkers() {
        return ResponseEntity.ok().body(menuDAO.getMarkers());
    }

    public ResponseEntity<?> getDailySummary(Integer type, LocalDate date) {
        String typeStr = switch (type) {
            case 1 -> "source";
            case 2 -> "transmission";
            case 3 -> "js";
            case 4 -> "pool";
            default -> "";
        };

        List<Menu> menus = menuDAO.findByType(typeStr);
        if (menus.isEmpty()) return ResponseEntity.notFound().build();

        List<Integer> menuIds = menus.stream().map(Menu::getId).toList();

        // Batch load: N*4 query → 4 query
        Map<Integer, HourlyReport> batchResult = mainService.getDailyReportBatch(menuIds, date);

        List<HourlyReport> reportList = menus.stream()
                .map(menu -> {
                    HourlyReport r = batchResult.getOrDefault(menu.getId(), new HourlyReport());
                    r.setMenuId(menu.getId());
                    r.setStationName(menu.getName());
                    return r;
                })
                .toList();

        return ResponseEntity.ok(reportList);
    }
}
