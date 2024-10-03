package pro.cyrent.anticheat.util.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ChecksSettingsFile {

    private ChecksSettingsFile() {}

    static ChecksSettingsFile instance = new ChecksSettingsFile();

    public static ChecksSettingsFile getInstance() {
        return instance;
    }

    private FileConfiguration config;
    private FileConfiguration data;
    private File dfile;

    public void setup(Plugin p) {
        config = p.getConfig();
        dfile = new File("plugins/LumosAC/check-settings.yml");

        if (!dfile.exists()) {
            try {
                dfile.createNewFile();
            } catch (IOException ignored) {
            }
        }

        data = YamlConfiguration.loadConfiguration(dfile);

    }

    public FileConfiguration getData() {
        return data;
    }


    public void writeDefaults() {
        if (!data.contains("Check.AutoClickerA.Max-Cps")) data.set("Check.AutoClickerA.Max-Cps",
                19);
        if (!data.contains("Check.AutoClickerA.CancelHits")) data.set("Check.AutoClickerA.CancelHits",
                false);

        if (!data.contains("Check.AutoClickerA.Warning")) data.set("Check.AutoClickerA.Warning", false);
        if (!data.contains("Check.AutoClickerA.Warning-Message")) data.set("Check.AutoClickerA.Warning-Message",
                "&c[Warning] &6You're clicking &c%CPS%&6, the MAX CPS allowed is &c%MAXCPS%&6.");

        if (!data.contains("Check.AutoClickerA.Send-Alert")) data.set("Check.AutoClickerA.Send-Alert", true);

        saveData();
    }

    public void saveData() {
        try {
            data.save(dfile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadData() {
        this.data = YamlConfiguration.loadConfiguration(dfile);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}