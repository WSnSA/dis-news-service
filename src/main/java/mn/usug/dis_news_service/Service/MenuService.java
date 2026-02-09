package mn.usug.dis_news_service.Service;

import mn.usug.dis_news_service.DAO.MenuDAO;
import mn.usug.dis_news_service.Entity.Menu;
import mn.usug.dis_news_service.Model.HourlyReport;
import mn.usug.dis_news_service.Model.MenuModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        menu.setParentId(model.getParentId());
        menu.setPath(model.getPath());
        menu.setComponent(model.getComponent());
        menu.setActiveFlag(1);
        menu.setCreatedDate(new Date());
        menu.setUpdatedDate(new Date());
        return menuDAO.save(menu);
    }

    public Menu editMenu(MenuModel model) {
        Menu menu = menuDAO.findById(model.getId()).orElse(null);
        if (menu == null) {
            return null;
        }
        menu.setName(model.getName());
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

    public ResponseEntity getDailySummary(Integer type, Date date) {
        List<HourlyReport> reportList = new ArrayList<>();
        String typeStr = "";
        if (type == 0){
            typeStr = "";
        }
        else if (type == 1){
            typeStr = "source";
        }
        else if (type == 2){
            typeStr = "transmission";
        }
        else if (type == 3){
            typeStr = "js";
        }
        else if (type == 4){
            typeStr = "pool";
        }

        List<Menu> list = menuDAO.findByType(typeStr);

        list.forEach(item -> {
            HourlyReport report = mainService.getDailyReport(item.getId(), date);
            report.setStationName(item.getName());
            reportList.add(report);
        });


        return reportList.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok().body(reportList);
    }
}
