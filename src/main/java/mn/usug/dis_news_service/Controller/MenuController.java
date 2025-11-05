package mn.usug.dis_news_service.Controller;

import mn.usug.dis_news_service.Entity.Menu;
import mn.usug.dis_news_service.Model.MenuModel;
import mn.usug.dis_news_service.Service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/menu")
public class MenuController {
    @Autowired
    MenuService menuService;

    @GetMapping("/getAll")
    public Iterable<Menu> getAllMenus() {
        return menuService.getAllMenus();
    }

    @PostMapping("/add")
    public Menu addMenu(@RequestBody MenuModel model) {
        return menuService.addMenu(model);
    }
    @PostMapping("/edit")
    public Menu editMenu(@RequestBody MenuModel model) {
        return menuService.editMenu(model);
    }
    @DeleteMapping("/delete")
    public Menu deleteMenu(@RequestParam("id") Integer id) {
        return menuService.deleteMenu(id);
    }

}
