package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import pro.cyrent.anticheat.util.block.box.HydroBB;
import pro.cyrent.anticheat.util.block.box.HydroMovingPosition;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.vec.Vec3;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.Arrays;

@CheckInformation(
        name = "Scaffold",
        subName = "E",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects if the player is looking forward while scaffolding",
        punishable = false,
        state = CheckState.BETA)
public class ScaffoldE extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isPlace()) {

            if (getData().generalCancel()
                    || getData().isBedrock()
                    || getData().getActionProcessor().isTeleportingV2()) {
                this.threshold = 0;
                return;
            }

            WrapperPlayClientPlayerFlying flying = getData().getMovementProcessor().getFlyingPacket();
            WrapperPlayClientPlayerFlying lastFlying = getData().getMovementProcessor().getLastFlyingPacket();
            WrapperPlayClientPlayerFlying lastLastFlying = getData().getMovementProcessor().getLastLastFlyingPacket();

            if (flying == null || lastFlying == null || lastLastFlying == null
                    || !flying.hasPositionChanged()
                    || !lastFlying.hasPositionChanged()
                    || !lastLastFlying.hasPositionChanged()) {
                return;
            }

            WrapperPlayClientPlayerBlockPlacement placement = new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

            if (placement.getBlockPosition() == null) return;

            if (getData().getBlockAgainst() != null && getData().getHorizontalProcessor().getKey() != null) {

                boolean keyPressed = getData().getHorizontalProcessor().getKey() == HorizontalProcessor.Keys.W
                        || getData().getHorizontalProcessor().getKey() == HorizontalProcessor.Keys.W_A
                        || getData().getHorizontalProcessor().getKey() == HorizontalProcessor.Keys.W_D;


                if (getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 3) {

                    if (getData().getMovementProcessor().getDeltaXZ() < .1) {
                        this.threshold -= Math.min(this.threshold, 0.05);
                        return;
                    }

                    if (keyPressed) {
                        if ((this.threshold += getData().getPlayer().isSneaking() ? 0.5 : 1) > 20) {
                            this.fail(
                                    "keyPressed=" + getData().getHorizontalProcessor().getKey().name());
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .25);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .25);
                }
            } else {
                this.threshold -= Math.min(this.threshold, .25);
            }
        }
    }
}
