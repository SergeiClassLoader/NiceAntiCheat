package pro.cyrent.anticheat.util.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigFile {

    private ConfigFile() {}

    static ConfigFile instance = new ConfigFile();

    public static ConfigFile getInstance() {
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
        dfile = new File("plugins/LumosAC/config.yml");

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
        data.options().header("%PLAYER% = the player cheating. %PREFIX% = the prefix of the anticheat you set below." +
                 "\nOp-Bypass: Should OP Bypass the anticheat" +
                " else if false they will need the permission \"lumos.bypass\" to bypass"
                + "\nAlert Command: is a command you can set to execute when you click the alert displayed in chat"
                + "\nDisable Alerts: Disables the alerts for all players, but players still flag (can be useful for api needs)"
                + "\nPunishment Command Global: is an option allowing to use one single punishment command if on, " +
                "or if its off have a different command for each check/detection."
                + "\nLagBacks: Will lag back the player to their last correct position when flagged"
                + "\nLagBacks SafeMode: Will automatically disable punishment for movement related checks"
                + "\nLagBacks NoMovementAlert: Will not display flag alerts for movement based checks if lagged back"
                + "\nBackTrack Mitigate: Will players the player is fighting lag around on their screen if detected using lag cheats (may also cancel hits depending on the detection/check)"
                + "\nReach Strict: Makes Reach A, and Reach B more strict, and can make reach detect quicker (risky, and can have potential false bans)"
                + "\nSpoof Health: Can crashes clients that display player health wrong & randomizes player health visually"
                + "\nSpoof Team: Makes clients believe their on the same team as the player they attacked (can break killaura's & aimassist) (highly experimental)"
                + "\nWARNING: Spoof Team can cause issues with team based plugins utilizing the Server Team Packet."
                + "\nMovement GhostBlock-Fall-Damage: Whenever a player uses NoFall, or Spoofs Ground while falling or in the air, once they land it will make them take the proper fall damage"
                + "\nMovement Less Brute Force (Highly Experimental): will make Speed A, and Velocity C detect more but can false (not finished)"
                + "\nKicks ViaMCP: Kicks anyone using the ViaMCP mod which allows them to change their game version to anything while staying on the same version they loaded onto (mostly cheaters use it to gain an advantage)"
                + "\nGhostBlock Support: Prevents lag backs for ghost blocks when placing blocks. (Warning: It could cause bypasses)");

        if (!data.contains("Command.Name")) data.set("Command.Name", "lumos");

        if (!data.contains("Alert.Disable-Alerts")) data.set("Alert.Disable-Alerts", false);
        if (!data.contains("Alert.Console-Alerts")) data.set("Alert.Console-Alerts", true);

        if (!data.contains("Alert.Prefix")) data.set("Alert.Prefix", "&l&8[&eLumosAC&8]&r");

        if (!data.contains("Alert.Alert-Message")) data.set("Alert.Alert-Message",
                "&b[&eAC&b] &e%PLAYER% &7flagged &e%CHECK%(%CHECKTYPE%) &b[&e%VL% &b- %STATE%&b]");

        if (!data.contains("Alert.Hover")) data.set("Alert.Hover", true);

        if (!data.contains("Alert.Hover-Color-Primary")) data.set("Alert.Hover-Color-Primary", "&e");
        if (!data.contains("Alert.Hover-Color-Secondary")) data.set("Alert.Hover-Color-Secondary", "&f");
        if (!data.contains("Alert.Hover-Color-TeleportText")) data.set("Alert.Hover-Color-TeleportText", "&6");

        if (!data.contains("Alert.Alert-CMD-Enabled")) data.set("Alert.Alert-CMD-Enabled", true);

        if (!data.contains("Alert.Alert-Command")) data.set("Alert.Alert-Command",
                "/tp %PLAYER%");

        if (!data.contains("Alert.On-Join-Owner-Message")) data.set("Alert.On-Join-Owner-Message", false);

        if (!data.contains("Alert.Discord")) data.set("Alert.Discord", false);
//        if (!data.contains("Alert.Discord-WebhookURL")) data.set("Alert.Discord-WebhookURL",
//                "https://discord.com/api/webhooks/----");
//        if (!data.contains("Alert.Discord-Ban-Message")) data.set("Alert.Discord-Ban-Message",
//                "[Demon] has removed %PLAYER% for using Unfair Advantages. (%CHECK% %CHECKTYPE%)");

        if (!data.contains("Punishment.Command.Enabled")) data.set("Punishment.Command.Enabled", true);

        if (!data.contains("Punishment.Command.Global")) data.set("Punishment.Command.Global", true);

        if (!data.contains("Punishment.Command.Execute")) data.set("Punishment.Command.Execute",
                "/ban %PLAYER% %PREFIX% &cUnfair Advantage.");

//        if (!data.contains("Punishment.BanWave.Enabled")) data.set("Punishment.BanWave.Enabled", false);
//        if (!data.contains("Punishment.BanWave.Time")) data.set("Punishment.BanWave.Time", 5);
//        if (!data.contains("Punishment.BanWave.Timely")) data.set("Punishment.BanWave.Timely", false);
//        if (!data.contains("Punishment.BanWave.CheckUpTime")) data.set("Punishment.BanWave.CheckUpTime", 120);

        if (!data.contains("Punishment.Announce.Enabled")) data.set("Punishment.Announce.Enabled", true);

        List<String> broadcastMessages = new ArrayList<>();
        broadcastMessages.add("%LINE%");
        broadcastMessages.add("%PREFIX% &7has shined light on &f%PLAYER% &7for cheating and removed them from the network.");
        broadcastMessages.add("%LINE%");

        if (!data.contains("Punishment.Announce.Message")) data.set("Punishment.Announce.Message", broadcastMessages);

        if (!data.contains("Bypass.Op-Bypass")) data.set("Bypass.Op-Bypass", false);

        if (!data.contains("Mongo.UseCustom")) data.set("Mongo.UseCustom", false);

        if (!data.contains("Mongo.ClientURI")) data.set("Mongo.ClientURI", "mongodb://admin:password@127.0.0.1:27017" +
                "/DataBaseName?retryWrites=true&w=majority&authSource=admin");

        if (!data.contains("Mongo.DataBaseName")) data.set("Mongo.DataBaseName", "Anticheat");

        if (!data.contains("Mongo.CollectionsName")) data.set("Mongo.CollectionsName", "PlayerLogs");

//        if (!data.contains("FloodGate.Bedrock.Support")) data.set("FloodGate.Bedrock.Support", false);

        if (!data.contains("LagBacks.Enabled")) data.set("LagBacks.Enabled", false);

        if (!data.contains("LagBacks.SafeMode")) data.set("LagBacks.SafeMode", true);

        if (!data.contains("LagBacks.NoMovementAlert")) data.set("LagBacks.NoMovementAlert", true);

        if (!data.contains("BackTrack.Mitigate.Enabled")) data.set("BackTrack.Mitigate.Enabled", true);

        if (!data.contains("Reach.Detection.UseOld")) data.set("Reach.Detection.UseOld", true);
        if (!data.contains("Reach.Detection.Old.Strict")) data.set("Reach.Detection.Old.Strict", false);

        if (!data.contains("Spoofer.Health")) data.set("Spoofer.Health", true);
        if (!data.contains("Spoofer.Team")) data.set("Spoofer.Team", false);
        if (!data.contains("Spoofer.Equipment-Data")) data.set("Spoofer.Equipment-Data", false);

        if (!data.contains("Movement.Less-Brute-Force")) data.set("Movement.Less-Brute-Force", false);
        if (!data.contains("Movement.GhostBlock-Fall-Damage")) data.set("Movement.GhostBlock-Fall-Damage", true);
        if (!data.contains("Movement.Use-Strict")) data.set("Movement.Use-Strict", true);

        if (!data.contains("ViaMCP.Punish.Enabled")) data.set("ViaMCP.Punish.Enabled", true);

        if (!data.contains("ViaMCP.Punish.Command")) data.set("ViaMCP.Punish.Command",
                "/kick %PLAYER% Timed out.");

        if (!data.contains("GhostBlock.Support")) data.set("GhostBlock.Support", false);

        if (!data.contains("Velocity-Simulator.Amount")) data.set("Velocity-Simulator.Amount", 0.85);
        if (!data.contains("Velocity-Simulator.Active")) data.set("Velocity-Simulator.Active", true);

        if (!data.contains("Environment.Regen")) data.set("Environment.Regen", true);
        if (!data.contains("Environment.FastBow")) data.set("Environment.FastBow", true);
        if (!data.contains("Environment.NoSlowDown")) data.set("Environment.NoSlowDown", true);
        if (!data.contains("Environment.FastEat")) data.set("Environment.FastEat", true);
        if (!data.contains("Environment.DisableCancelable")) data.set("Environment.DisableCancelable", true);


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