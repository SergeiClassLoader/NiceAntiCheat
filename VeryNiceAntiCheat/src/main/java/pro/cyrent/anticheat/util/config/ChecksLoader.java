package pro.cyrent.anticheat.util.config;

import pro.cyrent.anticheat.Anticheat;

public class ChecksLoader {

    public void load() {
        ChecksSettingsFile.getInstance().setup(Anticheat.INSTANCE.getPlugin());
        ChecksSettingsFile.getInstance().writeDefaults();
        
        Anticheat.INSTANCE.getChecksValues().setAutoClickerACPS(
                ChecksSettingsFile.getInstance().getData().getInt("Check.AutoClickerA.Max-Cps"));
        Anticheat.INSTANCE.getChecksValues().setAutoClickerCancelHits(
                ChecksSettingsFile.getInstance().getData().getBoolean("Check.AutoClickerA.CancelHits"));
        Anticheat.INSTANCE.getChecksValues().setAlertAutoClickerA(
                ChecksSettingsFile.getInstance().getData().getBoolean("Check.AutoClickerA.Warning"));
        Anticheat.INSTANCE.getChecksValues().setAutoClickerAMessage(
                this.convertColor(ChecksSettingsFile.getInstance().getData().getString("Check.AutoClickerA.Warning-Message")));
        Anticheat.INSTANCE.getChecksValues().setAutoClickerAAlert(
                ChecksSettingsFile.getInstance().getData().getBoolean("Check.AutoClickerA.Send-Alert"));
    }

    public String convertColor(String in) {
        return in.replace("&", "§");
    }

}