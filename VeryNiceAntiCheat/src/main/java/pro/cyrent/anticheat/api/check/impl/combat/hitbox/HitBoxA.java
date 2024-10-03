package pro.cyrent.anticheat.api.check.impl.combat.hitbox;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.ReachData;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.processor.connection.tracker.NewTrackedEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "HitBox",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.HITBOX,
        description = "Detects if a player attacks while not looking inside the hitbox",
        punishmentVL = 15,
        punishable = false,
        state = CheckState.PRE_RELEASE)
public class HitBoxA extends Check {

    private double threshold;

    public boolean cancelHits = false;

    public final String cancelPath = String.format("Check.%s.cancelHits", getFriendlyName());

    @Override
    public void onReach(ReachData reachData) {
        if (reachData != null) {

            if (getData().generalCancel()
                    || getData().isBedrock()
                    || getData().getCollisionProcessor().getNearBoatTicks() > 0
                    || getData().getActionProcessor().getLastVehicleTimer().getDelta() < 20
                    || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)
                    || getData().getReachProcessor().getLastEntitySwitch().getDelta() < 7
                    || getData().getPotionProcessor().getSpeedPotionAmplifier() > 90) {
                this.threshold -= Math.min(this.threshold, 0.03);
                return;
            }

            if (getData().getBackTrackProcessor().getLastCancel() < 60) {
                this.threshold -= Math.min(this.threshold, 0.03);
                return;
            }


            if (!reachData.isValidHitbox() && getData().getReachProcessor().isAttack()) {
                NewTrackedEntity trackedEntity = getData().getReachProcessor().getTracked().get(
                        getData().getReachProcessor().getLastEntityID());

                Entity entity = getData().getReachProcessor().getEntityTarget();

                if (entity == null) return;

                PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(
                        (Player) getData().getReachProcessor().getEntityTarget());

                if (getData().generalCancel() || getData().getCollisionProcessor().isBoat() || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)) {
                    this.threshold -= Math.min(this.threshold, 0.03);
                    return;
                }

                if (trackedEntity == null || target == null) return;

                if (target.generalCancel()
                        || getData().getActionProcessor().isTeleportingV2()
                        || target.getActionProcessor().isTeleportingV2()) {
                    this.threshold -= Math.min(this.threshold, 0.03);
                    return;
                }

                if (getData().getReachProcessor().getValidBoxTicks() < 1) {
                    this.threshold -= Math.min(this.threshold, 0.03);
                    return;
                }

                if (trackedEntity.getPositions().size() > 2
                        || trackedEntity.isConfirming()
                        || getData().getReachProcessor().getEntityUpdateTransactionTimer().passedNoPing()
                        || !trackedEntity.isReallyUsingPrePost()) {
                    this.threshold -= Math.min(this.threshold, 0.03);
                    return;
                }

                getData().setReach(true);

                if (++this.threshold > 12.0) {
                    this.fail(
                            "positionSize=" + trackedEntity.getPositions().size(),
                            "clientVersion=" + getData().getProtocolVersion());
                }
            } else {
                this.threshold -= Math.min(this.threshold, .75);
            }
        }
    }
}