package mn.usug.dis_news_service.Controller;

import mn.usug.dis_news_service.Entity.Permission;
import mn.usug.dis_news_service.Model.PermissionModel;
import mn.usug.dis_news_service.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permission")
public class PermissionController {
    @Autowired
    PermissionService permissionService;

    @GetMapping("/getAll")
    public Iterable<Permission> getAllPermissions() {
        return permissionService.getAllPermissions();
    }
    @PostMapping("/add")
    public ResponseEntity addPermission(@RequestBody List<PermissionModel> body) {
        return permissionService.addPermission(body);
    }
    @PostMapping("/edit")
    public ResponseEntity editPermission(@RequestBody List<PermissionModel> body) {
        return permissionService.editPermission(body);
    }
    @GetMapping("/get")
    public List<Permission> getPermissionById(@RequestParam("id") Integer id) {
        return permissionService.getPermissionByUserId(id);
    }

}
