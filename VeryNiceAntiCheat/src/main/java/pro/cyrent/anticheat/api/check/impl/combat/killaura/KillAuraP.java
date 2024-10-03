package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

import java.util.Deque;
import java.util.List;

@CheckInformation(
        name = "KillAura",
        subName = "P",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        experimental = true,
        description = "Detects average hit miss ratio in fights",
        state = CheckState.RELEASE)
public class KillAuraP extends Check {

    private final Deque<Double> ratioSamples = new EvictingList<>(10);
    private double attacks, swings;
    private double threshold, lastHitRatio;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity =
                        new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    return;
                }

                if (getData().getMovementProcessor().getDeltaYawAbs() > 0.5F
                        && getData().getMovementProcessor().getDeltaPitchAbs() > 0.5F) {
                    this.attacks++;
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {

                if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 100) {
                    return;
                }

                this.swings++;
            }

            if (event.isMovement()) {

                if (this.swings > 100 || this.attacks > 100) {

                    if (this.attacks > 0 && this.swings > 0
                            && getData().getCombatProcessor().getLastCancel().getDelta() > 50
                            && !getData().generalCancel()) {
                        double ratio = this.attacks / this.swings;
                        this.ratioSamples.add(ratio);
                    }

                    this.swings = this.attacks = 0;
                }

                if (this.ratioSamples.size() >= 10) {
                    double hitMissRatio = StreamUtil.getAverage(this.ratioSamples);

                    if (hitMissRatio > 85.0 && this.lastHitRatio != hitMissRatio) {
                        if (++this.threshold > 2.75) {
                            this.fail("averageRatio="+hitMissRatio);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .25);
                    }

                    this.lastHitRatio = hitMissRatio;
                }
            }
        }
    }
}
