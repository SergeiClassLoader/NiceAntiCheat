package pro.cyrent.anticheat.util.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConfigValues {
    private boolean broadcastPayload = true, ownMongo, demonEye = false, alertCommand, replay, consoleAlerts, punish,
            announce, debugMessage, banWave, banWaveTimely, allowOp, bungeeCord, kickForLag,
            discord, usingGlobalBans, lagbacks, safeMode, noAlertMovement, healthSpoofer, teamSpoofer, disableBlock, Regen, FastBow, NoSlowDown, FastEat;

    private int banWaveTime, banWaveCheckUpTime;

    private boolean ghostBlockSupport, kickForViaMCP, bedrockSupport, disableAlerts, ownerJoinMessage, alternativePotions, useBackTrack, strictReach,
    lessBruteForce = false, UseStrict = true, AntiEquipment, SimulateVelocity;

    private boolean waitBeforeJoining, ghostFallDamage, hover, apiViolation;
    private int minutesBeforeJoin;

    private double velocityAmount;

    private String
            clientURI,
            dataBaseName,
            collectionsName,
            demonEyeMessage,
            alertCommandString,
            devAlertMessage,
            punishCommand, prefix, alertsMessage, discordWebURL, viaCommand;

    private List<String> announceMessage = new ArrayList<>();

    private String discordBanMessage;

    private String primaryColorHover, secondaryColorHover, teleportTextColor;


    private String commandName;

    private int skippedPackets;
}