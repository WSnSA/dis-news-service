package mn.usug.dis_news_service.Controller;

import jakarta.persistence.Id;
import mn.usug.dis_news_service.Entity.User;
import mn.usug.dis_news_service.Model.LoginRequest;
import mn.usug.dis_news_service.Model.UserModel;
import mn.usug.dis_news_service.Service.AESUtil;
import mn.usug.dis_news_service.Service.ForgotPasswordService;
import mn.usug.dis_news_service.Service.MainService;
import mn.usug.dis_news_service.Service.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    ReferenceService refService;
    @Autowired
    ForgotPasswordService forgotPasswordService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = refService.getUserByUsername(req.getUsername());

        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        else if(!user.getPassword().equals(req.getPassword())){
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




    // POST /auth/forgot/request  { "email": "a@b.com" }
    @PostMapping("/forgot/request")
    public ResponseEntity<?> request(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        return forgotPasswordService.requestOtp(email);
    }

    // POST /auth/forgot/reset  { "email":"a@b.com", "code":"123456", "newPassword":"xxx" }
    @PostMapping("/forgot/reset")
    public ResponseEntity<?> reset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String newPassword = body.get("newPassword");
        return forgotPasswordService.resetPassword(email, code, newPassword);
    }

}
