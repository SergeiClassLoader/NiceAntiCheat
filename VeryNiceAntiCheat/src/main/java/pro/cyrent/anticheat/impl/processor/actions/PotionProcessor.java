package pro.cyrent.anticheat.impl.processor.actions;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import io.netty.util.concurrent.CompleteFuture;
import lombok.AllArgsConstructor;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pro.cyrent.anticheat.util.task.TransactionPacketAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;


@Getter
public class PotionProcessor extends Event {
    private final PlayerData user;

    public boolean lastSpeed = false, lastSlowness = false;

    private boolean speedPotion, jumpPotion, slownessPotion, poisonPotion, hastePotion, miningFatiguePotion;
    private int speedPotionTicks, jumpPotionTicks, slownessTicks, poisonTicks, hasteTicks, miningFatigueTicks;
    private int speedPotionAmplifier, jumpPotionAmplifier, slownessAmplifier, poisonPotionAmplifier, hasteAmplifier, miningFatigueAmplifier, lastSpeedAmplifer;

    private final Map<PotionTypes, PotionData> potionMap = new ConcurrentHashMap<>();
    private final Map<Integer, TransactionPacketAction> transactionPacketActionMap = new ConcurrentHashMap<>();
    private long lastTry;

    public PotionProcessor(PlayerData user) {
        this.user = user;

        // on join, we need to grab all potions since the packet isn't send in our scope
        for (PotionEffect potionEffect : this.user.getPlayer().getActivePotionEffects()) {

            Optional<PotionTypes> enumType = Arrays.stream(PotionTypes.values()).filter(potionTypes ->
                            potionTypes.getTag().equalsIgnoreCase(potionEffect.getType().getName().toLowerCase()))
                    .findAny();

            enumType.ifPresent(potionType -> {
                PotionData potionData = new PotionData(potionEffect.getAmplifier(), potionEffect.getDuration());
                this.potionMap.put(potionType, potionData);
            });
        }
    }

    public void onServerTick() {
        long timestamp = System.currentTimeMillis();

        if ((timestamp - this.lastTry) >= 500L) {
            this.lastTry = timestamp;

            for (TransactionPacketAction action : this.transactionPacketActionMap.values()) {
                this.user.getTickHolder().confirmFunctionAndTick(action);
                this.user.getTickHolder().confirmFunction(action);
                CompletableFuture.runAsync(() -> this.user.getTickHolder().pushTick());
            }
        }
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getType() == PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
            WrapperPlayServerRemoveEntityEffect wrapperPlayServerRemoveEntityEffect =
                    new WrapperPlayServerRemoveEntityEffect(event.getPacketSendEvent());

            if (wrapperPlayServerRemoveEntityEffect.getEntityId() == this.user.getPlayer().getEntityId()) {
                Optional<PotionTypes> enumType = Arrays.stream(PotionTypes.values()).filter(potionTypes ->
                                potionTypes.getTag().equalsIgnoreCase(wrapperPlayServerRemoveEntityEffect
                                        .getPotionType().getName().getKey()))
                        .findAny();

                enumType.ifPresent(potionType -> this.confirm(() -> this.potionMap.remove(potionType)));
            }
        }

        if (event.getType() == PacketType.Play.Server.ENTITY_EFFECT) {
            WrapperPlayServerEntityEffect wrapperPlayServerEntityEffect =
                    new WrapperPlayServerEntityEffect(event.getPacketSendEvent());

            if (wrapperPlayServerEntityEffect.getEntityId() == this.user.getPlayer().getEntityId()) {
                Optional<PotionTypes> enumType = Arrays.stream(PotionTypes.values()).filter(potionTypes ->
                                potionTypes.getTag().equalsIgnoreCase(wrapperPlayServerEntityEffect
                                        .getPotionType().getName().getKey()))
                        .findAny();

                enumType.ifPresent(potionType -> this.confirm(() -> {
                    PotionData potionData = new PotionData(
                            wrapperPlayServerEntityEffect.getEffectAmplifier(),
                            wrapperPlayServerEntityEffect.getEffectDurationTicks()
                    );

                    this.potionMap.put(potionType, potionData);
                }));
            }
        }

        if (event.isMovement()) {

            this.lastSlowness = this.slownessPotion;
            this.lastSpeed = this.speedPotion;

            boolean slownessPotion = false;
            boolean hasJumpPotion = false;
            boolean hasSpeedPotion = false;
            boolean hasPoisonPotion = false;
            boolean hasHastePotion = false;
            boolean hasMiningFatiguePotion = false;

            int slownessPotionAmplifier = 0;
            int jumpPotionAmplifier = 0;
            int speedPotionAmplifier = 0;
            int poisonPotionAmplifier = 0;
            int hastePotionAmplifier = 0;
            int miningFatiguePotionAmplifier = 0;

            for (Map.Entry<PotionTypes, PotionData> potionTypes : this.potionMap.entrySet()) {
                PotionTypes type = potionTypes.getKey();
                PotionData data = potionTypes.getValue();

                switch (type) {
                    case SPEED -> {
                        hasSpeedPotion = true;
                        speedPotionAmplifier = data.getAmplifier() + 1;
                    }

                    case JUMP -> {
                        hasJumpPotion = true;
                        jumpPotionAmplifier = data.getAmplifier() + 1;
                    }

                    case SLOW -> {
                        slownessPotion = true;
                        slownessPotionAmplifier = data.getAmplifier() + 1;
                    }

                    case POISON -> {
                        hasPoisonPotion = true;
                        poisonPotionAmplifier = data.getAmplifier() + 1;
                    }

                    case HASTE -> {
                        hasHastePotion = true;
                        hastePotionAmplifier = data.getAmplifier() + 1;
                    }

                    case SLOW_DIGGING -> {
                        hasMiningFatiguePotion = true;
                        miningFatiguePotionAmplifier = data.getAmplifier() + 1;
                    }
                }
            }

            this.jumpPotion = hasJumpPotion;
            this.jumpPotionAmplifier = jumpPotionAmplifier;

            if (hasJumpPotion) {
                this.jumpPotionTicks += (this.jumpPotionTicks < 20 ? 1 : 0);
            } else {
                this.jumpPotionTicks -= (this.jumpPotionTicks > 0 ? 1 : 0);
            }

            this.speedPotion = hasSpeedPotion;
            this.speedPotionAmplifier = speedPotionAmplifier;
            this.lastSpeedAmplifer = speedPotionAmplifier;

            if (hasSpeedPotion) {
                this.speedPotionTicks += (this.speedPotionTicks < 20 ? 1 : 0);
            } else {
                this.speedPotionTicks -= (this.speedPotionTicks > 0 ? 1 : 0);
            }

            this.slownessPotion = slownessPotion;
            this.slownessAmplifier = slownessPotionAmplifier;

            if (slownessPotion) {
                this.slownessTicks += (this.slownessTicks < 20 ? 1 : 0);
            } else {
                this.slownessTicks -= (this.slownessTicks > 0 ? 1 : 0);
            }

            this.poisonPotion = hasPoisonPotion;
            this.poisonPotionAmplifier = poisonPotionAmplifier;

            if (hasPoisonPotion) {
                this.poisonTicks += (this.poisonTicks < 20 ? 1 : 0);
            } else {
                this.poisonTicks -= (this.poisonTicks > 0 ? 1 : 0);
            }

            this.hastePotion = hasHastePotion;
            this.hasteAmplifier = hastePotionAmplifier;

            if (hasHastePotion) {
                this.hasteTicks += (this.hasteTicks < 20 ? 1 : 0);
            } else {
                this.hasteTicks -= (this.hasteTicks > 0 ? 1 : 0);
            }

            this.miningFatiguePotion = hasMiningFatiguePotion;
            this.miningFatigueAmplifier = miningFatiguePotionAmplifier;

            if (hasMiningFatiguePotion) {
                this.miningFatigueTicks += (this.miningFatigueTicks < 20 ? 1 : 0);
            } else {
                this.miningFatigueTicks -= (this.miningFatigueTicks > 0 ? 1 : 0);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public enum PotionTypes {
        SPEED("speed"),
        JUMP("jump_boost"),
        SLOW("slowness"),
        POISON("poison"),
        HASTE("haste"),
        SLOW_DIGGING("mining_fatigue");

        private final String tag;
    }

    @Getter
    @AllArgsConstructor
    public static final class PotionData {
        private final int amplifier;
        private final int duration;
    }

    private void confirm(Runnable runnable) {
        int runID = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);

        TransactionPacketAction action = () -> {
            runnable.run();
            this.transactionPacketActionMap.remove(runID);
        };

        this.user.getTickHolder().confirmFunctionAndTick(action);
        this.user.getTickHolder().confirmFunction(action);

        CompletableFuture.runAsync(() -> this.user.getTickHolder().pushTick());

        this.transactionPacketActionMap.put(runID, action);
    }
}
