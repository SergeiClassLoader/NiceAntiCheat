package pro.cyrent.anticheat.util.auth;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.util.json.JSONObject;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.zip.CRC32;

public class ConnectionServiceUtil {

    public static boolean isValid() {
        return true;
    }

    public static CompletableFuture<Boolean> validateLicenseAsync(Plugin plugin) {
        return CompletableFuture.supplyAsync(() -> {

            HashMap<String, String> headers = new HashMap<>();
            headers.put(StringUtils.decode(
                            "AdwPYTQq1Z4=", "AMoNY4vojovxt2N9kfNhgtbRwNykczv6OVfR78NcBP6im4iM9UaHV2JgjduxGsgiC"),
                    "Core5J2ITm2ry7UigjpuZ9SjmkCd7MQo46XET1Ot8yHkyEFOTam6"
            );

            headers.put(StringUtils.decode("IfVDZX24dFk=",
                            "3EwsLD4S2PuYxVNIcRzBLDiwkwAa4tZauU2vJiQjwMQzXJQI99NT4qscaFBEJiycgXyXuSA5s"),
                    Anticheat.INSTANCE.getLicense()
            );

            String result = HTTPUtil.getResponse("http://localhost:40245/service/verify", headers);

            if (result == null || result.length() < 10) {
                return false;
            }

            JSONObject jsonObject = new JSONObject(result);

            if (!jsonObject.has("data") || !jsonObject.has("segment") || !jsonObject.has("part")) {
                return false;
            }

            String data = jsonObject.getString("data");
            String segement = jsonObject.getString("segment");
            long part = jsonObject.getLong("part");
            EncryptionUtil encryptionUtil = new EncryptionUtil();
            String finalResult = encryptionUtil.decrypt(encryptionUtil.decrypt(data, segement), Anticheat.INSTANCE.getLicense());

            CRC32 crc32 = new CRC32();

            for (int i = 0; i < 6; i++) {
                crc32.update(Anticheat.INSTANCE.getLicense().getBytes(StandardCharsets.UTF_8));
            }

            long localValue = crc32.getValue();
            crc32.reset();

            headers.clear();
            return localValue == part && finalResult != null && finalResult.length() > 3;
        });
    }

    public static CompletableFuture<String> getIPAddressAsync() {
        return null;
    }
}
