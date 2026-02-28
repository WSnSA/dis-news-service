package mn.usug.dis_news_service.Service;
// package mn.usug.dis_news_service.Service;

import mn.usug.dis_news_service.DAO.UserDAO;
import mn.usug.dis_news_service.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ForgotPasswordService {

    @Autowired UserDAO userDAO;
    @Autowired OtpClientService otpClient;

    // 1) OTP илгээх
    public ResponseEntity<?> requestOtp(String email) {
        User user = userDAO.findUserByMailAddress(email);

        // ✅ security: account байгаа/байхгүйг ил гаргахгүйгээр OK буцааж болно
        if (user == null) return ResponseEntity.ok("OTP sent if account exists");

        otpClient.sendOtp(email);
        return ResponseEntity.ok("OTP sent");
    }

    // 2) OTP баталгаажуулж нууц үг солих (hash хийхгүй)
    public ResponseEntity<?> resetPassword(String email, String code, String newPassword) {
        User user = userDAO.findUserByMailAddress(email);
        if (user == null) return ResponseEntity.status(404).body("User not found");

        boolean ok = otpClient.verifyOtp(email, code);
        if (!ok) return ResponseEntity.status(400).body("OTP invalid");

        // ✅ hash хийхгүйгээр шууд хадгална (таны хүссэнээр)
        user.setPassword(newPassword);
        userDAO.save(user);

        return ResponseEntity.ok("Password updated");
    }
}