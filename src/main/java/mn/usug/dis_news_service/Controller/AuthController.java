package mn.usug.dis_news_service.Controller;

import mn.usug.dis_news_service.Entity.User;
import mn.usug.dis_news_service.Model.LoginRequest;
import mn.usug.dis_news_service.Model.UserModel;
import mn.usug.dis_news_service.Service.AESUtil;
import mn.usug.dis_news_service.Service.ForgotPasswordService;
import mn.usug.dis_news_service.Service.ReferenceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    ReferenceService refService;
    @Autowired
    ForgotPasswordService forgotPasswordService;
    @Autowired
    NamedParameterJdbcTemplate jdbc;
    @Value("${erp.app-url:https://erp.usug.mn}")
    String erpAppUrl;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    /**
     * ERP-ээс нэвтэрсэн чигээр шилжих (topbar "Мэдээний программ"). ERP (erp-service DisNewsSsoController)
     * sso_ticket-д нэг удаагийн тасалбарын SHA-256 хэш, хэрэглэгчийн id, хугацаа (60 сек) бичнэ.
     * Энд тасалбарыг устгаж (зөвхөн нэг удаа) /login-тэй ижил хариу буцаана.
     */
    @PostMapping("/sso")
    public ResponseEntity<?> sso(@RequestBody Map<String, String> body) {
        String ticket = body == null ? null : body.get("ticket");
        if (ticket == null || ticket.length() < 20 || ticket.length() > 100) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ticket");
        }
        MapSqlParameterSource p = new MapSqlParameterSource("h", sha256(ticket));
        List<Integer> ids;
        try {
            ids = jdbc.queryForList("SELECT user_id FROM sso_ticket WHERE token_hash = :h AND source = 'ERP' AND expires_at > NOW()", p, Integer.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ticket");
        }
        // Нэг удаа: устгасан нэг л хүсэлт нэвтэрнэ
        if (ids.isEmpty() || jdbc.update("DELETE FROM sso_ticket WHERE token_hash = :h AND source = 'ERP'", p) != 1) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired ticket");
        }
        User user = refService.getUserById(ids.get(0));
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        if (!Boolean.TRUE.equals(user.getActiveFlag())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User inactive");
        return ResponseEntity.ok(AESUtil.encryptObject(user));
    }

    /**
     * Мэдээний программаас ERP руу нэвтэрсэн чигээр (topbar "ERP" товч). Нэвтэрсэн хэрэглэгчийн токеныг шалгаж
     * (токен дахь нууц үг DB-тэй таарах ёстой — тогтмол түлхүүрээр хуурамч токен үүсгэхээс хамгаална), sso_ticket-д
     * source='DISNEWS' нэг удаагийн 60 сек тасалбар бичээд ERP-ийн холбоосыг буцаана. ERP (erp-service
     * POST /auth/disnews-sso) тасалбарыг устгаж тухайн хүний ERP session-ыг өгнө.
     */
    @PostMapping("/erp-sso")
    public ResponseEntity<?> erpSso(@RequestHeader(value = "Authorization", required = false) String auth) {
        User user = verifiedUser(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Дахин нэвтэрнэ үү."));
        try {
            ensureTicketTable();
            byte[] b = new byte[32];
            RANDOM.nextBytes(b);
            String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            jdbc.update("INSERT INTO sso_ticket (token_hash, user_id, source, expires_at, created_at) "
                            + "VALUES (:h, :u, 'DISNEWS', NOW() + INTERVAL 60 SECOND, NOW())",
                    new MapSqlParameterSource().addValue("h", sha256(ticket)).addValue("u", user.getId()));
            jdbc.update("DELETE FROM sso_ticket WHERE expires_at < NOW() - INTERVAL 1 HOUR", Map.of());
            return ResponseEntity.ok(Map.of("url", erpAppUrl.replaceAll("/+$", "") + "/auth/sso#t=" + ticket, "expiresIn", 60));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", "ERP руу шилжүүлж чадсангүй. Түр хүлээгээд дахин оролдоно уу."));
        }
    }

    /** Bearer токеныг задлаад, токен дахь id + нууц үг DB-ийн идэвхтэй хэрэглэгчтэй таарвал тэр хэрэглэгч. */
    private User verifiedUser(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        try {
            String json = AESUtil.decryptToken(auth.substring(7));
            if (json == null) return null;
            JsonNode n = MAPPER.readTree(json);
            if (!n.hasNonNull("id") || !n.hasNonNull("password")) return null;
            User u = refService.getUserById(n.get("id").asInt());
            if (u == null || !Boolean.TRUE.equals(u.getActiveFlag()) || u.getPassword() == null) return null;
            return MessageDigest.isEqual(u.getPassword().getBytes(StandardCharsets.UTF_8), n.get("password").asText().getBytes(StandardCharsets.UTF_8)) ? u : null;
        } catch (Exception e) {
            return null;
        }
    }

    private volatile boolean ticketTableReady;

    /** ddl-auto=none — ERP-тэй хуваалцах хүснэгтийг анх хэрэглэхэд үүсгэнэ (erp-service DisNewsSsoController-той ижил). */
    private void ensureTicketTable() {
        if (ticketTableReady) return;
        jdbc.getJdbcTemplate().execute("CREATE TABLE IF NOT EXISTS sso_ticket ("
                + " token_hash CHAR(64) NOT NULL PRIMARY KEY, user_id INT NOT NULL, source VARCHAR(20) NULL,"
                + " expires_at DATETIME NOT NULL, created_at DATETIME NOT NULL, KEY idx_sso_ticket_exp (expires_at)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        ticketTableReady = true;
    }

    private static String sha256(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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
        if (model.getCanAssignTask() != null) user.setCanAssignTask(model.getCanAssignTask());
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
