package pro.cyrent.anticheat.util.auth;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class StringUtils {

    public static String decode(String text, String key) {
        byte[] salt = new byte[]{1, 2, 3, 5, 7, 11, 13, 17};
        byte[] keyArray = new byte[24];


        try {


            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] temporaryKey = messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));
            if (temporaryKey.length < 24) {
                int index = 0;
                for (int i = temporaryKey.length; i < 24; i++) {
                    keyArray[i] = temporaryKey[index];
                }
            }
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(2, new SecretKeySpec(keyArray, "DESede"), new IvParameterSpec(salt));
            byte[] decrypted = cipher.doFinal(Base64.getMimeDecoder().decode(text));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException |
                 BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException generalSecurityException) {
            return null;
        }
    }


    public static String decodeHTML(String text, String key) {
        byte[] keyArray = new byte[24];
        byte[] salt = {1, 2, 3, 5, 7, 11, 13, 17};

        try {
            byte[] textData = text.getBytes(StandardCharsets.UTF_8);

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] temporaryKey = messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));

            if (temporaryKey.length < 24) {
                int index = 0;
                for (int i = temporaryKey.length; i < 24; ++i) {
                    keyArray[i] = temporaryKey[index];
                }
            }

            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyArray, "DESede"), new IvParameterSpec(salt));

            byte[] encryptedData = cipher.doFinal(textData);

            return Base64.getMimeEncoder().encodeToString(encryptedData);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException ex) {
            return null;
        }
    }



    public static String getAlphaNumericString(int var0, long var1) {
        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(var0);

        for (int i = 0; i < var0; i++) {
            int index = (int) (s.length() * Math.random());
            sb.append(s.charAt(index));
        }

        return sb.toString();
    }

}
