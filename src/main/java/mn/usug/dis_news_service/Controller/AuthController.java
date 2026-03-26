package mn.usug.dis_news_service.Controller;

import mn.usug.dis_news_service.Entity.User;
import mn.usug.dis_news_service.Model.LoginRequest;
import mn.usug.dis_news_service.Model.UserModel;
import mn.usug.dis_news_service.Service.AESUtil;
import mn.usug.dis_news_service.Service.ForgotPasswordService;
import mn.usug.dis_news_service.Service.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam("username") String username,
            @RequestParam(value = "reason", defaultValue = "") String reason
    ) {
        User user = refService.getUserByUsername(username);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Хэрэглэгч олдсонгүй");
        user.setPassword("123456");
        user.setFirstLogin(true);
        refService.saveUser(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deactive")
    public ResponseEntity<?> deactive(@RequestParam("username") String username) {
        User user = refService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
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

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam("username") String username,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword
    ) {
        User user = refService.getUserByUsername(username);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Хэрэглэгч олдсонгүй");
        if (!user.getPassword().equals(oldPassword)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Одоогийн нууц үг буруу байна");
        user.setPassword(newPassword);
        refService.saveUser(user);
        return ResponseEntity.ok().build();
    }




    @PutMapping("/first-password-change")
    public ResponseEntity<?> firstPasswordChange(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String newPassword = body.get("newPassword");
        User user = refService.getUserByUsername(username);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Хэрэглэгч олдсонгүй");
        if (!Boolean.TRUE.equals(user.getFirstLogin())) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Анхны нэвтрэлт биш байна");
        user.setPassword(newPassword);
        user.setFirstLogin(false);
        refService.saveUser(user);
        Map<String, Object> result = new HashMap<>();
        result.put("firstLogin", false);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserModel model) {
        User user = refService.getUserByUsername(model.getUsername());
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Хэрэглэгч олдсонгүй");
        if (model.getFirstName() != null) user.setFirstName(model.getFirstName());
        if (model.getLastName() != null) user.setLastName(model.getLastName());
        if (model.getPin() != null) user.setPin(model.getPin());
        if (model.getPhoneNumber() != null) user.setPhoneNumber(model.getPhoneNumber());
        if (model.getMailAddress() != null) user.setMailAddress(model.getMailAddress());
        if (model.getDepartmentId() != null) user.setDepartmentId(model.getDepartmentId());
        if (model.getPositionId() != null) user.setPositionId(model.getPositionId());
        refService.saveUser(user);
        return ResponseEntity.ok().build();
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
