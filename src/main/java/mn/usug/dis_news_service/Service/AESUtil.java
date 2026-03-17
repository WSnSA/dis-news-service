package mn.usug.dis_news_service.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {
    private static final String SECRET_KEY = "1234567890123456"; // 16 chars
    private static final String INIT_VECTOR = "6543210987654321"; // 16 chars

    /**
     * Token = base64( JSON.stringify( AES_encrypted_user ) )
     * Frontend stores: btoa(unescape(encodeURIComponent(JSON.stringify(encryptedData))))
     * Returns the decrypted User JSON string, or null on failure.
     */
    public static String decryptToken(String token) {
        try {
            byte[] decoded = Base64.getDecoder().decode(token);
            String jsonStr = new String(decoded, "UTF-8");
            // JSON.stringify of a string adds outer quotes — use ObjectMapper to unwrap
            ObjectMapper mapper = new ObjectMapper();
            String encryptedBase64 = mapper.readValue(jsonStr, String.class);
            return decrypt(encryptedBase64);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String decrypt(String encryptedBase64) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
            return new String(original, "UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }

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
