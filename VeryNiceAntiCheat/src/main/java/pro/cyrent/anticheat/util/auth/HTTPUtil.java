package pro.cyrent.anticheat.util.auth;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.bukkit.Bukkit;
import pro.cyrent.anticheat.Anticheat;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HTTPUtil {

    public static String getResponse(String URL) {
        try {
            URLConnection connection = new URL(URL).openConnection();
            connection.setConnectTimeout(20000);
            connection.connect();

            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException ex) {
            return null;
        }
    }

    public static String getResponse(String URL, Map<String, String> header) {

        try {
            URLConnection connection = new URL(URL).openConnection();
            connection.setConnectTimeout(20000);
            header.forEach(connection::setRequestProperty);
            connection.connect();


            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException ex) {
            return null;
        }
    }
    public static void addTrustedUser(String username) {
        if(Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(Anticheat.INSTANCE.getPlugin(), () -> addTrustedUser(username));
            return;
        }

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://backend.antiskid.club/service/known");
        httpPost.setHeader("User-Agent", "Lumos Client/1.0");
        httpPost.setHeader("lumoskey", Anticheat.INSTANCE.getLicense());
        
        try {
            httpPost.setEntity(new StringEntity(username));
            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}