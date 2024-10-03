package pro.cyrent.anticheat.util.config;


import pro.cyrent.anticheat.Anticheat;

public class ConfigLoader {

    public void load() {
        ConfigFile.getInstance().setup(Anticheat.INSTANCE.getPlugin());
        ConfigFile.getInstance().writeDefaults();

        Anticheat.INSTANCE.getConfigValues().setCommandName(ConfigFile.getInstance().getData()
                .getString("Command.Name"));

        Anticheat.INSTANCE.getConfigValues().setDisableAlerts(ConfigFile.getInstance().getData()
                .getBoolean("Alert.Disable-Alerts"));
        Anticheat.INSTANCE.getConfigValues().setConsoleAlerts(ConfigFile.getInstance().getData()
                .getBoolean("Alert.Console-Alerts"));
        Anticheat.INSTANCE.getConfigValues().setPrefix(this.convertColor(ConfigFile.getInstance().getData()
                .getString("Alert.Prefix")));
        Anticheat.INSTANCE.getConfigValues().setAlertsMessage(this.convertColor(ConfigFile.getInstance().getData()
                .getString("Alert.Alert-Message")));

        Anticheat.INSTANCE.getConfigValues().setHover(ConfigFile.getInstance().getData()
                .getBoolean("Alert.Hover"));
        Anticheat.INSTANCE.getConfigValues().setPrimaryColorHover(this.convertColor(ConfigFile.getInstance().getData()
                .getString("Alert.Hover-Color-Primary")));
        Anticheat.INSTANCE.getConfigValues().setSecondaryColorHover(this.convertColor(ConfigFile.getInstance().getData()
                .getString("Alert.Hover-Color-Secondary")));
        Anticheat.INSTANCE.getConfigValues().setTeleportTextColor(this.convertColor(ConfigFile.getInstance().getData()
                .getString("Alert.Hover-Color-TeleportText")));

        Anticheat.INSTANCE.getConfigValues().setAlertCommand(ConfigFile.getInstance().getData()
                .getBoolean("Alert.Alert-CMD-Enabled"));
        Anticheat.INSTANCE.getConfigValues().setAlertCommandString(ConfigFile.getInstance().getData()
                .getString("Alert.Alert-Command"));

        Anticheat.INSTANCE.getConfigValues().setOwnerJoinMessage(ConfigFile.getInstance().getData()
                .getBoolean("Alert.On-Join-Owner-Message"));


        Anticheat.INSTANCE.getConfigValues().setDiscord(ConfigFile.getInstance().getData()
                .getBoolean("Alert.Discord"));
        Anticheat.INSTANCE.getConfigValues().setDiscordWebURL(ConfigFile.getInstance().getData()
                .getString("Alert.Discord-WebhookURL"));
        Anticheat.INSTANCE.getConfigValues().setDiscordBanMessage(ConfigFile.getInstance().getData()
                .getString("Alert.Discord-Ban-Message"));

        Anticheat.INSTANCE.getConfigValues().setPunish(ConfigFile.getInstance().getData()
                .getBoolean("Punishment.Command.Enabled"));

        Anticheat.INSTANCE.getConfigValues().setUsingGlobalBans(ConfigFile.getInstance().getData()
                .getBoolean("Punishment.Command.Global"));

        Anticheat.INSTANCE.getConfigValues().setPunishCommand(this.convertColor(ConfigFile.getInstance().getData()
                .getString("Punishment.Command.Execute")));
        Anticheat.INSTANCE.getConfigValues().setAnnounce(ConfigFile.getInstance().getData()
                .getBoolean("Punishment.Announce.Enabled"));
        Anticheat.INSTANCE.getConfigValues().setAnnounceMessage(ConfigFile.getInstance().getData()
                .getStringList("Punishment.Announce.Message"));

        Anticheat.INSTANCE.getConfigValues().setBanWave(ConfigFile.getInstance().getData()
                .getBoolean("Punishment.BanWave.Enabled"));
        Anticheat.INSTANCE.getConfigValues().setBanWaveTime(ConfigFile.getInstance().getData()
                .getInt("Punishment.BanWave.Time"));

        Anticheat.INSTANCE.getConfigValues().setBanWaveTimely(ConfigFile.getInstance().getData()
                .getBoolean("Punishment.BanWave.Timely"));
        Anticheat.INSTANCE.getConfigValues().setBanWaveCheckUpTime(ConfigFile.getInstance().getData()
                .getInt("Punishment.BanWave.CheckUpTime"));


        Anticheat.INSTANCE.getConfigValues().setAllowOp(ConfigFile.getInstance().getData()
                .getBoolean("Bypass.Op-Bypass"));

        Anticheat.INSTANCE.getConfigValues().setBungeeCord(ConfigFile.getInstance().getData()
                .getBoolean("BungeeCord.Enabled"));


        Anticheat.INSTANCE.getConfigValues().setOwnMongo(ConfigFile.getInstance().getData()
                .getBoolean("Mongo.UseCustom"));

        Anticheat.INSTANCE.getConfigValues().setClientURI(ConfigFile.getInstance().getData()
                .getString("Mongo.ClientURI"));

        Anticheat.INSTANCE.getConfigValues().setDataBaseName(ConfigFile.getInstance().getData()
                .getString("Mongo.DataBaseName"));

        Anticheat.INSTANCE.getConfigValues().setCollectionsName(ConfigFile.getInstance().getData()
                .getString("Mongo.CollectionsName"));


        Anticheat.INSTANCE.getConfigValues().setBedrockSupport(ConfigFile.getInstance().getData()
                .getBoolean("FloodGate.Bedrock.Support"));

        Anticheat.INSTANCE.getConfigValues().setDisableAlerts(ConfigFile.getInstance().getData()
                .getBoolean("Hits.DisableBlock", false));


        Anticheat.INSTANCE.getConfigValues().setLagbacks(ConfigFile.getInstance().getData()
                .getBoolean("LagBacks.Enabled"));

        Anticheat.INSTANCE.getConfigValues().setSafeMode(ConfigFile.getInstance().getData()
                .getBoolean("LagBacks.SafeMode"));

        Anticheat.INSTANCE.getConfigValues().setNoAlertMovement(ConfigFile.getInstance().getData()
                .getBoolean("LagBacks.NoMovementAlert"));

        Anticheat.INSTANCE.getConfigValues().setUseBackTrack(ConfigFile.getInstance().getData()
                .getBoolean("BackTrack.Mitigate.Enabled"));

        Anticheat.INSTANCE.getConfigValues().setStrictReach(ConfigFile.getInstance().getData()
                .getBoolean("Reach.Detection.Old.Strict"));

        Anticheat.INSTANCE.getConfigValues().setHealthSpoofer(ConfigFile.getInstance().getData()
                .getBoolean("Spoofer.Health"));

        Anticheat.INSTANCE.getConfigValues().setTeamSpoofer(ConfigFile.getInstance().getData()
                .getBoolean("Spoofer.Team"));

        Anticheat.INSTANCE.getConfigValues().setAntiEquipment(ConfigFile.getInstance().getData()
                .getBoolean("Spoofer.Equipment-Data"));

        Anticheat.INSTANCE.getConfigValues().setLessBruteForce(ConfigFile.getInstance().getData()
                .getBoolean("Movement.Less-Brute-Force"));

        Anticheat.INSTANCE.getConfigValues().setUseStrict(ConfigFile.getInstance().getData()
                .getBoolean("Movement.Use-Strict"));

        Anticheat.INSTANCE.getConfigValues().setGhostFallDamage(ConfigFile.getInstance().getData()
                .getBoolean("Movement.GhostBlock-Fall-Damage"));

        Anticheat.INSTANCE.getConfigValues().setKickForViaMCP(ConfigFile.getInstance().getData()
                .getBoolean("ViaMCP.Punish.Enabled"));
        Anticheat.INSTANCE.getConfigValues().setViaCommand(ConfigFile.getInstance().getData()
                .getString("ViaMCP.Punish.Command"));

        Anticheat.INSTANCE.getConfigValues().setGhostBlockSupport(ConfigFile.getInstance().getData()
                .getBoolean("GhostBlock.Support"));

        Anticheat.INSTANCE.getConfigValues().setVelocityAmount(ConfigFile.getInstance().getData().getDouble("Velocity-Simulator.Amount"));


        Anticheat.INSTANCE.getConfigValues().setSimulateVelocity((ConfigFile.getInstance().getData().getBoolean("Velocity-Simulator.Active")));

        Anticheat.INSTANCE.getConfigValues().setRegen((ConfigFile.getInstance().getData().getBoolean("Environment.Regen")));
        Anticheat.INSTANCE.getConfigValues().setFastBow((ConfigFile.getInstance().getData().getBoolean("Environment.FastBow")));
        Anticheat.INSTANCE.getConfigValues().setNoSlowDown((ConfigFile.getInstance().getData().getBoolean("Environment.NoSlowDown")));
        Anticheat.INSTANCE.getConfigValues().setFastEat(ConfigFile.getInstance().getData().getBoolean("Environment.FastEat"));
        Anticheat.INSTANCE.getConfigValues().setDisableBlock(ConfigFile.getInstance().getData().getBoolean("Environment.DisableCancelable"));


    }

    public String convertColor(String in) {
        return in.replace("&", "§");
    }


}