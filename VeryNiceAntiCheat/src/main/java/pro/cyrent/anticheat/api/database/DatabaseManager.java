package pro.cyrent.anticheat.api.database;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.database.api.ClientData;
import pro.cyrent.anticheat.api.database.api.InputData;
import pro.cyrent.anticheat.api.database.impl.MongoManager;
import pro.cyrent.anticheat.util.json.JSONArray;
import pro.cyrent.anticheat.util.json.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class DatabaseManager {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
    private final List<InputData> logQueue = new CopyOnWriteArrayList<>();
    private final List<ClientData> clientQueue = new CopyOnWriteArrayList<>();
    private final List<InputData> customLogQueue = new CopyOnWriteArrayList<>();

    private final MongoManager mongoManager = new MongoManager();

    public void setup() {
        executorService.execute(() -> {
            if (Anticheat.INSTANCE.getConfigValues().isOwnMongo()) {
                mongoManager.initManager();
            }
        });

        executorService.scheduleAtFixedRate(this::sendBackEndLogs, 10L, 10L, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(this::sendMongoLogs, 10L, 10L, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(this::sendLogsAndClientData, 10L, 10L, TimeUnit.SECONDS);
    }

    private void sendBackEndLogs() {
        if (!logQueue.isEmpty()) {
            List<InputData> toProcess = new ArrayList<>(logQueue);
            logQueue.clear();

            JSONArray jsonArray = new JSONArray();
            toProcess.forEach(inputData -> {
                JSONObject jsonObject = convertInputDataToJson(inputData);
                jsonArray.put(jsonObject);
            });

            sendToMongo("https://backend.antiskid.club/", jsonArray);
        }
    }

    private void sendMongoLogs() {
        if (!customLogQueue.isEmpty() && Anticheat.INSTANCE.getConfigValues().isOwnMongo()) {
            List<InputData> toProcess = new ArrayList<>(customLogQueue);
            customLogQueue.clear();
            toProcess.forEach(mongoManager::addViolation);
        }
    }

    private void sendLogsAndClientData() {
        if (!clientQueue.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            clientQueue.forEach(inputData -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("", inputData.getLicense());
                jsonObject.put("", inputData.getCurrentDate());
                jsonObject.put("", inputData.getValid());
                jsonObject.put("", inputData.getServerIP());
                jsonArray.put(jsonObject);
            });
            clientQueue.clear();


            //sendToMongo(httpClient, httpPost, jsonArray);
        }
    }

    private void sendToMongo(String url, JSONArray jsonArray) {

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Lumos Client/1.0");
        httpPost.setHeader("lumoskey", Anticheat.INSTANCE.getLicense());
        try {
            httpPost.setEntity(new StringEntity(jsonArray.toString()));
            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToMongo(CloseableHttpClient httpClient, HttpPost httpPost, JSONArray jsonArray) {

    }

    private JSONObject convertInputDataToJson(InputData inputData) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("playerName", inputData.getPlayerName());
        jsonObject.put("checkType", inputData.getCheckType());
        jsonObject.put("checkName", inputData.getCheckName());
        jsonObject.put("checkData", inputData.getCheckData());
        jsonObject.put("violation", inputData.getViolation());
        jsonObject.put("currentDate", inputData.getCurrentDate());
        jsonObject.put("loginTime", inputData.getLoginTime());
        jsonObject.put("uuid", inputData.getUUID());
        jsonObject.put("ping", inputData.getPing());
        jsonObject.put("loginDate", inputData.getLoginDate());
        jsonObject.put("experimental", inputData.isExperimental());
        jsonObject.put("banwave", inputData.getBanWave());
        jsonObject.put("license", inputData.getLicense());
        return jsonObject;
    }

    public void reset() {
        mongoManager.dropReset();
    }

    public void shutdown() {
        mongoManager.shutdown();
        executorService.shutdown();
    }
}