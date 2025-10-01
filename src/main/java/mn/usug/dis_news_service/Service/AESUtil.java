package mn.usug.dis_news_service.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {
    private static final String SECRET_KEY = "1234567890123456"; // 16 chars
    private static final String INIT_VECTOR = "6543210987654321"; // 16 chars

    public static String encryptObject(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(obj); // 🔁 Convert object to JSON

            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(json.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted); // 🔐 Base64 to send to frontend
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
