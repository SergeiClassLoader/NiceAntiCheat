package pro.cyrent.anticheat.util.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


public class StatsUtil {

    public static int kickAmount = 0;
    public static int banAmount = 0;
    public static int flagAmount = 0;
    public static int overallPlayersJoined = 0;

    public static List<String> checkFriendlyNameBans = new CopyOnWriteArrayList<>();
    public static List<String> checkFriendlyNameKicks = new CopyOnWriteArrayList<>();

    public static String getMostFrequentStringName(List<String> names) {
        if (names == null || names.isEmpty()) {
            return "";
        }

        Map<String, Integer> frequencyMap = new HashMap<>();

        // Count the frequency of each name
        for (String name : names) {
            frequencyMap.put(name, frequencyMap.getOrDefault(name, 0) + 1);
        }

        // Find the name with the highest frequency
        String mostFrequentName = null;
        int maxFrequency = 0;

        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxFrequency) {
                maxFrequency = entry.getValue();
                mostFrequentName = entry.getKey();
            }
        }

        return mostFrequentName;
    }
}
