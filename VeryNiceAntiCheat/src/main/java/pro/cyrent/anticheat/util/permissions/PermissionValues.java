package pro.cyrent.anticheat.util.permissions;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.permissions.Permissible;

@Getter
@Setter
public class PermissionValues {

    private String alert, alertCommand,
            bypass, pingCommand, versionCommand, testKnockbackCommand, reloadCommand,
            logsCommand, infoCommand, guiCommand, forcebanCommand, debugCommand, crashCommand, banwaveCommand,
            clearLogsCommand, packetLogCommand, defaultCommand, forceBot, top, cpsCommand, statsGuiCommand, devAlerts, PanelCommand;


    public boolean hasGuiPermission(Permissible permissible) {
        if (permissible.hasPermission(guiCommand)) {
            return true;
        } else if (permissible.hasPermission(top)) {
            return true;
        }
        return permissible.hasPermission(statsGuiCommand);
    }
}