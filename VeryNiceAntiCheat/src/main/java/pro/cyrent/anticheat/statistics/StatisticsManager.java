package pro.cyrent.anticheat.statistics;


import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.statistics.file.StatisticsFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

@Getter
public class StatisticsManager {
    @Getter
    private static StatisticsManager instance;
    private final Map<Type, Object> typeObjectMap = new HashMap<>();

    private final PlayerStatisticTracker playerStatisticTracker = new PlayerStatisticTracker();

    public static final long FIVE_SECONDS = TimeUnit.SECONDS.toMillis(5L);

    private String uid;
    private boolean enabled;

    private final String restURL = "https://backend.antiskid.club/stats";
    private final String SPLIT = "<L1>";
    private final String SPLIT_2 = "<L2>";

    public StatisticsManager() {
        instance = this;
    }

    public void setup() {
        StatisticsFile.getInstance().setup(Anticheat.INSTANCE.getPlugin());
        StatisticsFile.getInstance().writeDefaults();
        this.uid = StatisticsFile.getInstance().getData().getString("UID");
        this.enabled = StatisticsFile.getInstance().getData().getBoolean("enabled");
        this.startTimer();
    }

    public void startTimer() {
        if (this.enabled) {
            Anticheat.INSTANCE.getGangService().scheduleAtFixedRate(this::collectInformation,
                    1L, 5L, TimeUnit.MINUTES);

            Anticheat.INSTANCE.getGangService().scheduleAtFixedRate(this::upload,
                    1L, 5L, TimeUnit.MINUTES);
        }
    }

   public  void collectInformation() {
        long now = System.currentTimeMillis();

        this.typeObjectMap.put(Type.PLAYER_COUNT, Anticheat.INSTANCE.getUserManager().getUserMap().size());

        // collect everyone's client brand and client version

        for (Map.Entry<UUID, PlayerData> entry : Anticheat.INSTANCE.getUserManager().getUserMap().entrySet()) {
            PlayerData user = entry.getValue();
            if ((now - user.getTimestamp()) < FIVE_SECONDS) {
                continue;
            }

            this.playerStatisticTracker.addClientBrand(user.getCustomPayloadProcessor().getBrand());
            this.playerStatisticTracker.addClientVersion(user.getVersion());
        }

        // sort the per player information

        this.playerStatisticTracker.getClientVersionMap().forEach((versions, integer) ->
                this.typeObjectMap.put(Type.USER_VERSION,
                        this.typeObjectMap.getOrDefault(Type.USER_VERSION, "") + versions.name() + this.SPLIT + integer + this.SPLIT_2));

        this.playerStatisticTracker.getClientBrandMap().forEach((s, integer) ->
                this.typeObjectMap.put(Type.USER_BRAND,
                        this.typeObjectMap.getOrDefault(Type.USER_BRAND, "") +  s + this.SPLIT + integer + this.SPLIT_2));

        this.playerStatisticTracker.clear();
    }

    public void upload() {
        StringBuilder postData = new StringBuilder();
        this.typeObjectMap.forEach((type, o) -> postData.append(type.name()).append(":").append(o).append(";"));
        this.typeObjectMap.clear();

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(this.restURL);

        httpPost.setHeader("User-Agent",
                "Lumos Client/1.0");

        httpPost.setHeader("OnlinePlayers", String.valueOf(Anticheat.INSTANCE.getUserManager().getUserMap().size()));

        httpPost.setHeader("LumosKey", Anticheat.INSTANCE.getLicense());

        httpPost.setHeader("UID", this.uid);

        try {
            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("data", Arrays.toString(compress(postData.toString()))));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addStat(Type type, Object obj) {
        /*
        if (this.enabled) {
            this.typeObjectMap.put(type, obj);
        }
         */
    }

    private byte[] compress(String data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(data.getBytes());
        gzipOutputStream.close();

        byte[] compressed = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return compressed;
    }

    public enum Type {
        START_UP,
        SHUT_DOWN,
        PLAYER_COUNT,
        LOG,
        USER_VERSION,
        USER_BRAND
    }
}
