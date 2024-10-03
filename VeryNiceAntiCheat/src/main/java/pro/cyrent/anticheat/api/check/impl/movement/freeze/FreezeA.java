package pro.cyrent.anticheat.api.check.impl.movement.freeze;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.bukkit.RunUtils;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

@CheckInformation(
        name = "Freeze",
        checkNameEnum = CheckName.FREEZE,
        checkType = CheckType.MOVEMENT,
        punishmentVL = 8,
        punishable = false,
        experimental = true,
        description = "Prevents when a player abuses ender-pearl freezing",
        state = CheckState.ALPHA)
public class FreezeA extends Check {

    private int stage = 0;
    private int amount;

    // This check was made for astral to prevent freezing your game after ender-pearling to gain an advantage
    // By continuously going up wall with ender pearls (freezing their game until the timer goes away).

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            if (this.stage == 1) {
                this.stage = 2;
            }
            getData().setLastFlying(0);
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {

            if (getData().getPlayer().getWorld() == null
                    || getData().generalCancel()
                    || getData().getCollisionProcessor().getLiquidTicks() > 0
                    || getData().getCollisionProcessor().isWebFullCheck()
                    || getData().getCollisionProcessor().getWebFullTicks() > 0
                    || getData().getCollisionProcessor().getWebTicks() > 0
                    || getData().getLastWorldChange().getDelta() < 50) {
                this.stage = 0;
                return;
            }

            // recent teleport
            if (getData().getLastEnderPearl().getDelta() < 100) {

                // flying or transaction not being sent this long.
                if (!getData().getMovementProcessor().getTo().isOnGround()) {

                    if (getData().getLastFlying() > 200 || getData().getLastTransaction() > 200) {
                        if (this.stage == 0) {
                            this.stage = 1;
                        } else if (stage == 2) {
                            this.stage = 3;
                        }
                    }
                }
            }


            if (this.stage == 3 && getData().getGhostBlockProcessor()
                    .getProperLocation(getData().getPlayer().getWorld()) != null) {
                Location location = getData().getGhostBlockProcessor()
                        .getProperLocation(getData().getPlayer().getWorld());

                RunUtils.task(() -> getData().getPlayer().teleport(location.clone().add(0, .42F, 0),
                        PlayerTeleportEvent.TeleportCause.UNKNOWN));

                if (++this.amount > 7) {
                    this.stage = 0;
                    this.amount = 0;
                }
            }
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            if (this.stage == 1) {
                this.stage = 2;
            }
            getData().setLastTransaction(0);
        }
    }
}
