package mn.usug.dis_news_service.Controller;

import jakarta.persistence.Id;
import mn.usug.dis_news_service.Entity.User;
import mn.usug.dis_news_service.Model.UserModel;
import mn.usug.dis_news_service.Service.AESUtil;
import mn.usug.dis_news_service.Service.MainService;
import mn.usug.dis_news_service.Service.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    ReferenceService refService;

    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam("username") String username,@RequestParam("password") String password) {
        User user = refService.getUserByUsername(username);

        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        else if(!user.getPassword().equals(password)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong password");
        }
        else return ResponseEntity.ok(AESUtil.encryptObject(user));
    }

    @PostMapping("/deactive")
    public ResponseEntity<?> deactive(@RequestParam("username") String username,@RequestParam("password") String password) {
        User user = refService.getUserByUsername(username);
        user.setActiveFlag(false);
        refService.saveUser(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserModel model) {
        User user = refService.getUserByUsername(model.getUsername());
        if (user != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
        else {
            return ResponseEntity.ok(refService.createUser(model));
        }
    }

}
