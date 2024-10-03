package pro.cyrent.anticheat.api.check.impl.misc.inventory;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.Bukkit;


@CheckInformation(
        name = "Inventory",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.INVENTORY,
        experimental = true,
        description = "Detects if the player sends rotation packets while in their inventory",
        state = CheckState.BETA)
public class InventoryA extends Check {

    private double threshold;
    private int lastRot;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {

            if (getData().generalCancel()
                    || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                    || getData().getCollisionProcessor().getNearBoatTicks() > 0
                    || getData().getCollisionWorldProcessor().getLiquidTicks() > 0
                    || getData().getCollisionWorldProcessor().getIceTicks() > 0
                    || getData().getCollisionWorldProcessor().getMountTicks() > 0
                    || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(3)
                    || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                    || getData().getVelocityProcessor().getExtraVelocityTicks() < 40
                    || getData().getActionProcessor().isTeleportingV3()) {
                this.threshold = 0;
                return;
            }

            if (this.lastRot > 1) {
                if (++this.threshold > 1.0) {
                    this.fail("Rotating while clicking in their inventory");
                }
            }
        }

        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());
            this.lastRot = 0;

            if (flying.hasRotationChanged()) {
                this.lastRot++;
            }
        }
    }
}
