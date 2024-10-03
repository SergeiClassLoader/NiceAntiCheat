package pro.cyrent.anticheat.util.messages;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class MessagesFile {

    private MessagesFile() {}

    static MessagesFile instance = new MessagesFile();

    public static MessagesFile getInstance() {
        return instance;
    }

    private FileConfiguration config;
    private FileConfiguration data;
    private File dfile;

    public void setup(Plugin p) {
        config = p.getConfig();
        if (!p.getDataFolder().exists()) {
            //p.getDataFolder().mkdir();
        }
        dfile = new File("plugins/LumosAC/messages.yml");

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

        if (!data.contains("Command-Messages.Line")) data.set("Command-Messages.Line",
                "&7&m------------------------------------------");

        if (!data.contains("Command-Messages.Beginning")) data.set("Command-Messages.Beginning",
                "&eLumosAC &7 - &7%VERSION%");

        if (!data.contains("Command-Messages.SubCommandExist")) data.set("Command-Messages.SubCommandExist",
                "&cSub command doesn't exist!");

        if (!data.contains("Command-Messages.NoPermission")) data.set("Command-Messages.NoPermission",
                "&cYou don't have permission to execute this command.");


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