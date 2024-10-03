package pro.cyrent.anticheat.api.check.impl.misc.inventory;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Inventory",
        subName = "C",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.INVENTORY,
        description = "Detects if the player moves items while moving around (slow)",
        state = CheckState.RELEASE)
public class InventoryC extends Check {

    private double threshold;
    private long lastTime;

    private int lastJumped;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {

            long difference = System.currentTimeMillis() - this.lastTime;

            if (!getData().getMovementProcessor().getTo().isOnGround()
                    && getData().getMovementProcessor().getFrom().isOnGround() && difference > 250L) {
                this.lastJumped = 20;
            } else {
                this.lastJumped -= Math.min(this.lastJumped, 1);
            }
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow wrapperPlayClientClickWindow = new WrapperPlayClientClickWindow(event.getPacketReceiveEvent());
            double deltaXZ = getData().getMovementProcessor().getDeltaXZ();
            double lastDeltaXZ = getData().getMovementProcessor().getLastDeltaXZ();

            if (getData().generalCancel()
                    || getData().getMovementProcessor().getTicksSincePosition() > 0
                    || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                    || getData().getCollisionProcessor().getNearBoatTicks() > 0
                    || getData().getCollisionWorldProcessor().getLiquidTicks() > 0
                    || getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion()
                    || getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion()
                    || getData().getCollisionWorldProcessor().getIceTicks() > 0
                    || getData().getCollisionWorldProcessor().getMountTicks() > 0
                    || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(3)
                    || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                    || getData().getVelocityProcessor().getExtraVelocityTicks() < 40
                    || getData().getActionProcessor().isTeleportingV3()) {
                this.threshold -= Math.min(this.threshold, 0.03);
                return;
            }

            double difference = deltaXZ - lastDeltaXZ;

            long now = System.currentTimeMillis();

            long lastClick = (now - this.lastTime);

            double maxSpeed = 0.03;

            if (this.lastJumped > 0) {
                maxSpeed += 0.2;
            }

            if (getData().getPotionProcessor().getSpeedPotionTicks() > 0) {
                maxSpeed += getData().getPotionProcessor().getSpeedPotionAmplifier() * 0.030;
            }

            if (difference >= 0.0D && deltaXZ >= maxSpeed) {
                if (++this.threshold > 2.5D) {
                        this.fail("speed="+deltaXZ,
                                "maxSpeed="+maxSpeed,
                                "inventorySlot="+wrapperPlayClientClickWindow.getSlot(),
                                "lastClickedTime="+lastClick);
                    event.getPacketReceiveEvent().setCancelled(true);
                }
            } else {
                this.threshold -= Math.min(this.threshold, 0.07D);
            }

            this.lastTime = now;
        }
    }
}