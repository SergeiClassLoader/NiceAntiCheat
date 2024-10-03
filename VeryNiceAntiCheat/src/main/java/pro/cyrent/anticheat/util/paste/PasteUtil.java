package pro.cyrent.anticheat.util.paste;

import pro.cyrent.anticheat.Anticheat;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created on 21/12/2020 Package integral.studios.hydro.util.http
 */
public class PasteUtil {

    private static String getDomain() {
        return "panel.antiskid.club";
    }

    public static String createCustom(String data) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://panel.antiskid.club/endpoint/upload");

        httpPost.setHeader("User-Agent", "Lumos Client/1.0");
        httpPost.setHeader("pass", "bc29bcf6-b837-4d39-a45a-d5ba1d4f9197");

        try {
            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("data", Base64.getEncoder()
                    .encodeToString(data.getBytes(StandardCharsets.UTF_8))));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            return "https://panel.antiskid.club/paste?id=" + EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            // e.printStackTrace();
        }

        return null;
    }
    public static String createPaste(String data) {

        try {
            HttpClient httpclient = HttpClients.createDefault();

            HttpPost httppost = new HttpPost("https://panel.antiskid.club/service/paste");

            httppost.setHeader("User-Agent",
                    "Hydro Client/1.0");

            httppost.setHeader("LumosKey", Anticheat.INSTANCE.getLicense());

            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("Data",
                    Base64.getEncoder().encodeToString(data.getBytes())));

            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse httpResponse = httpclient.execute(httppost);
            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                try (InputStream inputStream = entity.getContent()) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                            StandardCharsets.UTF_8));

                    StringBuilder stringBuilder = new StringBuilder();

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    if (stringBuilder.toString().contains("-")) {
                        return String.format("https://%s/paste?id=", getDomain()) +
                                stringBuilder.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
