package pro.cyrent.anticheat.impl.processor.actions;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;

import java.util.HashMap;
import java.util.Locale;


@Getter
public class TestPotionProcessor extends Event {
    private final PlayerData data;


    public boolean lastSpeed = false, lastSlowness = false;

    private boolean speedPotion, jumpPotion, slownessPotion, poisonPotion;
    private int speedPotionTicks, jumpPotionTicks, slownessTicks, poisonTicks;
    private int speedPotionAmplifier, jumpPotionAmplifier, slownessAmplifier, lastSpeedAmplifer;
    private Integer hasteAmplifier = null, miningFatigueAmplifier = null;

    private int invalidPotionAmpliferTicks;

    private final HashMap<Integer, Integer> potionEffectList = new HashMap<>();

    public TestPotionProcessor(PlayerData user) {
        this.data = user;
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {

                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());


                if (flying.hasPositionChanged()) {
                    this.invalidPotionAmpliferTicks++;
                }

                Player player = getData().getPlayer();

                if (player == null) return;

                this.lastSlowness = this.slownessPotion;
                this.lastSpeed = this.speedPotion;

                // use both this & packet since shit likes to break?!

                boolean speed = false;
                boolean slowness = false;
                boolean jump = false;
                boolean poison = false;

                int speedAmplifer = 0;
                int jumpAmplifer = 0;
                int slowAmplifer = 0;

                Integer speeding = null;
                Integer slowing = null;

                if (player.getActivePotionEffects() != null && !player.getActivePotionEffects().isEmpty()) {
                    for (PotionEffect potionEffect : player.getActivePotionEffects()) {

                        if (potionEffect == null) return;

                        PotionEffectType potionEffectType = potionEffect.getType();

                        if (potionEffectType == null) return;

                        if (potionEffectType.equals(PotionEffectType.SPEED)) {
                            speedAmplifer = potionEffect.getAmplifier() + 1;

                            if (speedAmplifer != this.speedPotionAmplifier) {
                                this.invalidPotionAmpliferTicks = 0;
                            }
                            speed = true;
                        }

                        if (potionEffectType.equals(PotionEffectType.SLOW)) {
                            slowAmplifer = potionEffect.getAmplifier() + 1;
                            slowness = true;
                        }

                        if (potionEffectType.equals(PotionEffectType.JUMP)) {
                            jumpAmplifer = potionEffect.getAmplifier() + 1;
                            jump = true;
                        }

                        if (potionEffectType.equals(PotionEffectType.POISON)
                                || potionEffectType.equals(PotionEffectType.HARM)
                                || potionEffectType.equals(PotionEffectType.WITHER)) {
                            poison = true;
                        }

                        if (potionEffectType.equals(PotionEffectType.FAST_DIGGING)) {
                            speeding = potionEffect.getAmplifier() + 1;
                        }

                        if (potionEffectType.equals(PotionEffectType.SLOW_DIGGING)) {
                            slowing = potionEffect.getAmplifier() + 1;
                        }
                    }
                }


                this.lastSpeedAmplifer = this.speedPotionAmplifier;
                this.speedPotionAmplifier = speedAmplifer;
                this.slownessAmplifier = slowAmplifer;
                this.jumpPotionAmplifier = jumpAmplifer;

                if (this.speedPotionAmplifier != this.lastSpeedAmplifer) {
                    this.invalidPotionAmpliferTicks = 0;
                }

                this.hasteAmplifier = speeding;
                this.miningFatigueAmplifier = slowing;

                this.speedPotion = speed;
                this.jumpPotion = jump;
                this.poisonPotion = poison;
                this.slownessPotion = slowness;

                if (this.speedPotion) {
                    if (this.speedPotionTicks < 20) speedPotionTicks++;
                } else {
                    if (speedPotionTicks > 0) speedPotionTicks--;
                }

                if (this.jumpPotion) {
                    if (this.jumpPotionTicks < 20) jumpPotionTicks++;
                } else {
                    if (jumpPotionTicks > 0) jumpPotionTicks--;
                }

                if (this.poisonPotion) {
                    if (this.poisonTicks < 20) poisonTicks++;
                } else {
                    if (poisonTicks > 0) poisonTicks--;
                }

                if (this.slownessPotion) {
                    if (this.slownessTicks < 20) slownessTicks++;
                } else {
                    if (slownessTicks > 0) slownessTicks--;
                }
            }
        }

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.ENTITY_EFFECT) {
                WrapperPlayServerEntityEffect entityEffect = new WrapperPlayServerEntityEffect(event.getPacketSendEvent());

                if (entityEffect.getEntityId() == getData().getPlayer().getEntityId()) {

                    String potionName = entityEffect.getPotionType().getName().getKey().toLowerCase(Locale.ROOT);

                    if (potionName.equals("speed") || potionName.equals("slowness")) {
                        getData().getHorizontalProcessor().setHasDesyncedSpeed(false);
                        getData().getHorizontalProcessor().setHasDesyncedSlow(false);
                        getData().getHorizontalProcessor().setDesyncResetTime(0);
                    }
                }
            }
        }
    }
}