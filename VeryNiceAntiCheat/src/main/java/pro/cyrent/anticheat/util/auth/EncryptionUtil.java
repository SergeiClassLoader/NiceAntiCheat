package pro.cyrent.anticheat.util.auth;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class EncryptionUtil {
    private final byte[] salt;

    public EncryptionUtil() {
        this.salt = new byte[]{-87, -101, -56, 50, 86, 53, -29, 3};
    }

    public String decrypt(String data, String key) {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, 256);
            PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, 256);
            SecretKey secretKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
            cipher.init(2, secretKey, parameterSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static String getHWID() {
        return get();
    }

    private static String get() {
        String hwid = null;
        try {
            hwid = SHA1(System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("COMPUTERNAME") + System.getProperty("user.name"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            assert hwid != null;
            return SHA1(hwid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String SHA1(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash;
        md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte datum : data) {
            int b = datum >>> 4 & 0xF;
            int h = 0;
            do {
                if (b <= 9) {
                    buf.append((char) (48 + b));
                } else {
                    buf.append((char) (97 + (b - 10)));
                }
                b = datum & 0xF;
            } while (h++ < 1);
        }
        return buf.toString();
    }
}
 