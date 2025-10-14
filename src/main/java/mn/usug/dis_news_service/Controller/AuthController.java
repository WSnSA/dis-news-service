package mn.usug.dis_news_service.Controller;

import jakarta.persistence.Id;
import mn.usug.dis_news_service.Entity.User;
import mn.usug.dis_news_service.Service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    MainService mainService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("username") String username,@RequestParam("password") String password) {
        User user = mainService.getUserByUsername(username);

        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        else if(!user.getPassword().equals(password)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong password");
        }
        else return ResponseEntity.ok(user);
    }

    @PostMapping("/deactive")
    public ResponseEntity<?> deactive(@RequestParam("username") String username,@RequestParam("password") String password) {
        User user = mainService.getUserByUsername(username);
        user.setActiveFlag(false);
        mainService.saveUser(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam("username") String username,@RequestParam("password") String password) {
        User user = mainService.getUserByUsername(username);
        if (user != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
        else {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            mainService.saveUser(newUser);
            return ResponseEntity.ok(newUser);
        }
    }


}
