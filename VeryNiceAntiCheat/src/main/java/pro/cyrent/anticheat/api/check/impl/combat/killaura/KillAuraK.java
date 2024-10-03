package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "KillAura",
        subName = "K",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        experimental = true,
        description = "Detects attacking at a specific hurt-time consistently",
        state = CheckState.PRE_BETA)
public class KillAuraK extends Check {
    private double threshold;
    private int lastHurtTime, lastNonFixed;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

                Entity entity = SpigotReflectionUtil.getEntityById(interactEntity.getEntityId());

                if (entity instanceof Player) {

                    PlayerData targetUser = Anticheat.INSTANCE.getUserManager().getUser((Player) entity);

                    if (targetUser != null) {

                        int targetHurtTime = targetUser.getCombatProcessor().getHurtTime();

                        double deltaYawAbs = getData().getMovementProcessor().getDeltaYawAbs();

                        if (targetHurtTime > 8 && this.lastNonFixed == 0 && deltaYawAbs > 1.5f) {

                            if (this.lastHurtTime == targetHurtTime) {
                                if (++this.threshold > 7) {
                                    this.fail("absYaw=" + deltaYawAbs, "targetHurtTime=" + targetHurtTime);
                                }
                            } else {
                                this.threshold -= Math.min(this.threshold, 1);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, 0.07);
                        }

                        if (targetHurtTime > 0) {
                            this.lastHurtTime = targetHurtTime;
                        }

                        this.lastNonFixed = targetHurtTime;
                    }
                }
            }
        }
    }
}
