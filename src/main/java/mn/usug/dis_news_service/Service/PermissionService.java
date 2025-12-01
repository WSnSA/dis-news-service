package mn.usug.dis_news_service.Service;

import jakarta.transaction.Transactional;
import mn.usug.dis_news_service.DAO.PermissionDAO;
import mn.usug.dis_news_service.Entity.Permission;
import mn.usug.dis_news_service.Model.PermissionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {
    @Autowired
    PermissionDAO permissionDAO;

    public List<Permission> getAllPermissions() {
        return permissionDAO.findAll();
    }

    public Permission getPermissionById(Integer id) {
        return permissionDAO.findById(id).orElse(null);
    }

    public List<Permission> getPermissionByUserId(Integer id) {
        return permissionDAO.findByUserId(id);
    }

    @Transactional
    public ResponseEntity addPermission(List<PermissionModel> models) {
        for (PermissionModel model : models) {
            Permission permission = new Permission();
            permission.setUserId(model.getUserId());
            permission.setMenuId(model.getMenuId());
            permission.setCanEdit(model.getEditRights());
            permission.setCanView(model.getViewRights());
            permissionDAO.save(permission);
        }
        return ResponseEntity.ok("");
    }

    @Transactional
    public ResponseEntity editPermission(List<PermissionModel> models) {
        for (PermissionModel model : models) {
            Permission permission = permissionDAO.findByUserIdAndMenuId(model.getUserId(), model.getMenuId());
            permission.setUserId(model.getUserId());
            permission.setMenuId(model.getMenuId());
            permission.setCanEdit(model.getEditRights());
            permission.setCanView(model.getViewRights());
            permissionDAO.save(permission);
        }
        return ResponseEntity.ok("");
    }
}
