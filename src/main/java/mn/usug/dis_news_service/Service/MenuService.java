package mn.usug.dis_news_service.Service;

import mn.usug.dis_news_service.DAO.MenuDAO;
import mn.usug.dis_news_service.Entity.Menu;
import mn.usug.dis_news_service.Model.MenuModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MenuService {
    @Autowired
    MenuDAO menuDAO;

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

}
