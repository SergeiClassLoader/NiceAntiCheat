package pro.cyrent.anticheat.api.check.impl.combat.reach;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.ReachData;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.processor.connection.tracker.NewTrackedEntity;

@CheckInformation(
        name = "Reach",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.REACH,
        description = "Detects if a player attacks from too far.",
        punishmentVL = 3,
        state = CheckState.RELEASE)
public class ReachA extends Check {

    private double threshold;
    public boolean cancelHits = false;

    public final String cancelPath = String.format("Check.%s.cancelHits", getFriendlyName());

    @Override
    public void onReach(ReachData reachData) {
        if (reachData != null) {

            if (reachData.isValidHitbox() && getData().getReachProcessor().isAttack()) {
                Entity entity = getData().getReachProcessor().getEntityTarget();

                if (entity == null) {
                    if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                            .getUniqueId().toString().equals(getData().getUuid().toString())) {
                        Anticheat.INSTANCE.getCheckPacketLog().add(
                                "Reach A: Null Entity\n");
                    }

                    return;
                }

                PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(
                        (Player) getData().getReachProcessor().getEntityTarget());

                NewTrackedEntity trackedEntity = getData().getReachProcessor().getTracked().get(
                        getData().getReachProcessor().getLastEntityID());


                double distance = 3.0001;

                getData().setReach(true);

                if (reachData.getDistance() >= distance && !exempt(reachData, trackedEntity, target)) {

                    if (++this.threshold > 1.95) {
                        this.fail("distance=" + reachData.getDistance(),
                                "max-distance=" + distance,
                                "positionSize=" + trackedEntity.getPositions().size(),
                                "clientVersion=" + getData().getProtocolVersion());

                        if (this.cancelHits) {
                            getData().getCombatProcessor().setCancelHits(8);
                        }
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .005);
                }
            }
        }
    }

    private boolean exempt(ReachData reachData, NewTrackedEntity trackedEntity, PlayerData target) {
        if (trackedEntity == null || target == null) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Reach A: Null Target\n");
            }
            return true;
        }

        if (!reachData.isValidHitbox()
                || !getData().getReachProcessor().isAttack()
                || getData().getCombatProcessor().getCancelTicks() > 0
                || (getData().getLastWorldChange().isSet()
                && getData().getLastWorldChange().getDelta() < 10)
                || (getData().getCombatProcessor().getLastCancel().isSet()
                && getData().getCombatProcessor().getLastCancel().getDelta() < 3)
                || getData().getPlayer().isSleeping()
                || getData().getReachProcessor().getRanTimes() < 4
                || getData().isBedrock()
                || (getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionAmplifier() > 90)
                || (getData().getMovementProcessor().getLastFlightTimer().isSet()
                && getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(19))
                || (getData().getActionProcessor().getLastVehicleTimer().isSet()
                && getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(19))
                || (getData().getReachProcessor().getLastEntitySwitch().isSet()
                && getData().getReachProcessor().getLastEntitySwitch().hasNotPassed(10))) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Reach A: Exempts | " +
                                "cancelHits=" + (getData().getCombatProcessor().getCancelTicks() > 0) +
                                "cancelDelta=" + (getData().getCombatProcessor().getLastCancel().getDelta() < 3) +
                                "sleep=" + getData().getPlayer().isSleeping() +
                                "flying=" +getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(19) +
                                "vehcile="+getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(19) +
                                "switch="+getData().getReachProcessor().getLastEntitySwitch().hasNotPassed(10)+
                                "\n");
            }
            return true;
        }


        if (getData().getBackTrackProcessor().isStartDelayer()
                || getData().getBackTrackProcessor().getLastDelay() < 100
                && Anticheat.INSTANCE.getConfigValues().isUseBackTrack()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Reach A: BackTrack\n");
            }
            this.threshold = 0;
            return true;
        }

        if (getData().getBackTrackProcessor().getLastCancel() < 60
                && Anticheat.INSTANCE.getConfigValues().isUseBackTrack() || getData().isMisplace()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Reach A: BackTrack #2\n");
            }
            return true;
        }
        if (target.generalCancel()
                || target.getActionProcessor().isTeleportingV2()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Reach A: General Cancel/Teleporting\n");
            }
            return true;
        }

        if (trackedEntity.trackedLocations < 1) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Reach A: No Tracked Locations\n");
            }
            return true;
        }

        if (target.getActionProcessor().getLastVehicleTimer().getDelta() < 20) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Reach A: Vehcile\n");
            }
            return true;
        }

        if (trackedEntity.getPositions().size() > 2
                || trackedEntity.isConfirming()
                || getData().getReachProcessor().getEntityUpdateTransactionTimer().passedNoPing()
                || !trackedEntity.isReallyUsingPrePost()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Reach A: General Exempts #2 | " +
                                "overPositionSize=" + (trackedEntity.getPositions().size() > 2) +
                                "confirming=" + trackedEntity.isConfirming() +
                                "updateTimer=" + getData().getReachProcessor().getEntityUpdateTransactionTimer().passedNoPing() +
                                "prePost="+ (!trackedEntity.isReallyUsingPrePost()) +
                                "\n");
            }
            return true;
        }

        return false;
    }
}