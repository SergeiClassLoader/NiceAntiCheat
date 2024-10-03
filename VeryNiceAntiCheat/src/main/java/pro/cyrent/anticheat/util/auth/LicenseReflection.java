// Decompiled with: CFR 0.152
// Class Version: 8
package pro.cyrent.anticheat.util.auth;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class LicenseReflection {

    public String getKeyFromClass() {


        try {
            Class<?> clazz = Class.forName("pro.cyrent.pre.util.MathUtil");
            Field field = clazz.getDeclaredField("Xm392xo0PLkMLK");
            field.setAccessible(true);

            String fieldString = (String) field.get(clazz.newInstance());
            String[] split = new String(Base64.getDecoder().decode(fieldString.getBytes(StandardCharsets.UTF_8))).split(":");

            return split.length > 0 ? StringUtils.decode(split[0], split[1]) : null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }




}
