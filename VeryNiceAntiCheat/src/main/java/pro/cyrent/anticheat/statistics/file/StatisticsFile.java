package pro.cyrent.anticheat.statistics.file;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class StatisticsFile {

    private StatisticsFile() {
    }

    @Getter
    static StatisticsFile instance = new StatisticsFile();

    @Getter
    private FileConfiguration data;
    private File dfile;

    public void setup(Plugin p) {
        if (!p.getDataFolder().exists()) {
            p.getDataFolder().mkdir();
        }

        dfile = new File("plugins/LumosAC/statistics.yml");

        if (!dfile.exists()) {
            try {
                dfile.createNewFile();
            } catch (IOException ignored) {
            }
        }

        data = YamlConfiguration.loadConfiguration(dfile);
    }

    public void writeDefaults() {
        if (!data.contains("UID") || (data.contains("UID") && (!data.getString("UID").contains("-")
                || data.getString("UID").length() < 15))) data.set("UID", UUID.randomUUID().toString());
        if (!data.contains("enabled")) data.set("enabled", true);
        saveData();
    }


    public void saveData() {
        try {
            data.save(dfile);
        } catch (IOException ignored) {
        }
    }

    public void reloadData() {
        data = YamlConfiguration.loadConfiguration(dfile);
    }
}