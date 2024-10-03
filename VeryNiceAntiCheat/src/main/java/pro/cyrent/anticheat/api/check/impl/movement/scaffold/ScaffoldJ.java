package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "Scaffold",
        subName = "J",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        punishable = false,
        description = "Detects if the duration the player sneaks for is too consistent",
        state = CheckState.ALPHA)
public class ScaffoldJ extends Check {

    private double threshold;
    private int sneakingTick, stopSneaking = 100;
    private final List<Integer> sneakingSamples = new CopyOnWriteArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null
                || getData().isBedrock()) return;

        if (event.isMovement()) {

            this.sneakingTick++;
            this.stopSneaking++;

            if (getData().generalCancel()) return;

            if (getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 4 && this.stopSneaking < 20) {

                int delta = Math.abs(this.sneakingTick - this.stopSneaking);

                this.sneakingSamples.add(delta);

                if (this.sneakingSamples.size() >= 20) {
                    double std = StreamUtil.getStandardDeviation(this.sneakingSamples);

                    if (std < .2) {
                        if (++this.threshold > 1) {
                            this.fail(
                                    "std="+std);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .25);
                    }

                    this.sneakingSamples.clear();
                }
            }
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {

            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
                this.sneakingTick = 0;
            }

            if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
                this.stopSneaking = 0;
            }
        }
    }
}
