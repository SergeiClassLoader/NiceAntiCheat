package pro.cyrent.anticheat.impl.processor.prediction.noslowdown;

import org.bukkit.World;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.impl.movement.noslow.NoSlowA;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.MoveFlyingResult;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.magic.MagicValues;
import pro.cyrent.anticheat.util.math.Motion;
import pro.cyrent.anticheat.util.minecraftmaths.MinecraftMath;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Getter
@Setter
public class NoSlowDownProcessor extends Event {

    private final PlayerData data;

    private int waitTicks;
    private boolean
            blockingItem = false, blockingValidItem = false;
    private int ticksSinceItemBlock;
    private int tickSinceSlotChange, lastTicksSinceSlotChange;

    private double buffer;

    private final EventTimer preventInteractionTimer;

    private int ticksToIgnore;
    private boolean flagNoSlow = false;

    private boolean blockingPacketsA1 = false;


    public NoSlowDownProcessor(PlayerData user) {
        this.data = user;
        this.preventInteractionTimer = new EventTimer(20, user);
    }

    public void onPrePredictionProcess(PlayerData user, List<HorizontalProcessor.TransactionVelocityEntry> velocityEntries,
                                       WrapperPlayClientPlayerFlying flyingPacket, int delay, boolean zeroThree,
                                       boolean usingFastMath, boolean timerPacket) {

        double max = getData().getHorizontalProcessor().getClientCollideTicks()
                < 10 || getData().getCollisionWorldProcessor().getCollideHorizontalTicks() > 0
                ? 0.002 : getData().getProtocolVersion() > 47 ? 1E-4 :
                usingFastMath ? 7E-5 : 1E-7;

        if (zeroThree && !timerPacket) {
            max = getData().getProtocolVersion() > 47
                    ? 0.06D * delay
                    : 0.03D * delay;
        }

        // run the prediction, this will only execute when the player is interacting with an item
        double finalMax = max;

        this.handlePrediction(user, flyingPacket, velocityEntries, predicted -> {
            double predictionValue = predicted.getSmallest();

            //Bukkit.broadcastMessage(""+predictionValue + " "+finalMax);

            if (predictionValue > finalMax) {

                getData().getSetBackProcessor().setInvalid(true);

                if (this.buffer++ > 15) {
                    NoSlowA noSlowA = (NoSlowA) getData().getCheckManager().forClass(NoSlowA.class);

                    if (noSlowA != null && noSlowA.isEnabled()) {
                        noSlowA.fail("Potentially using NoSlowDown (Canceling Actions)");
                    }

                    this.buffer = 6;

                    // Bukkit.broadcastMessage("failed noslow");
                    this.resetItem();
                }
            } else {
                this.buffer -= Math.min(this.buffer, .1);
                this.flagNoSlow = false;
            }

        }, () -> {
            this.buffer -= Math.min(this.buffer, .1);
            this.flagNoSlow = false;
        });
    }

    public void resetItem() {

        ItemStack currentItem = getData().getPlayer().getItemInHand();

        //this has a duplication exploit in it.
        if (currentItem != null && currentItem.getType() != Material.AIR) {

            // remove the current item from the hand
            //      getData().getPlayer().getItemInHand().setType(Material.AIR);

            // this should reset the item in use
            //        RunUtils.taskLater(() -> this.getData().getPlayer().setItemInHand(currentItem), 1L);

            // we need to reset the blocking states otherwise it will false if this falsely triggers
            //    this.blockingValidItem = false;
            //     this.blockingItem = false;
        }

        this.flagNoSlow = false;
        this.preventInteractionTimer.resetBoth();
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {

                if (getData().getPlayer().getItemInHand() != null
                        && getData().getPlayer().getItemInHand().getType() != null) {

                    boolean holdingValidItem = getData().isUsableItem(getData().getPlayer().getItemInHand());

                    if (isBlockingItem()
                            && getTicksSinceItemBlock() > 3 && holdingValidItem) {
                        this.blockingValidItem = true;
                    }

                    //  Bukkit.broadcastMessage(""+this.tickSinceSlotChange);

                    if (!holdingValidItem) {
                        this.blockingItem = this.blockingValidItem = false;
                    }
                }

                this.ticksSinceItemBlock++;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                //    this.tickSinceSlotChange++;
                this.waitTicks++;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
                WrapperPlayClientPlayerBlockPlacement wrapped =
                        new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

                if (wrapped.getBlockPosition() != null) {

                    Vector originalPosition = new Vector(wrapped.getBlockPosition().getX(),
                            wrapped.getBlockPosition().getY(),
                            wrapped.getBlockPosition().getZ());

                    List<Vector> offsets = Arrays.asList(
                            new Vector(0, 0, 0),
                            new Vector(1, 0, 0),
                            new Vector(-1, 0, 0),
                            new Vector(0, 1, 0),
                            new Vector(0, -1, 0),
                            new Vector(0, 0, 1),
                            new Vector(0, 0, -1)
                    );

                    Material material = null;

                    for (Vector offset : offsets) {
                        Vector position = originalPosition.clone().add(offset);
                        Material currentMaterial = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                                getData().getPlayer().getWorld(), position.getX(), position.getY(), position.getZ()
                        );

                        if (currentMaterial != null) {

                            // Check if the material found is valid (you can define what constitutes valid)
                            if (currentMaterial == Material.ENCHANTMENT_TABLE
                                    || currentMaterial == Material.WORKBENCH
                                    || currentMaterial == Material.NOTE_BLOCK
                                    || currentMaterial.name().toLowerCase(Locale.ROOT).contains("door")
                                    || currentMaterial.name().toLowerCase(Locale.ROOT).contains("fence")
                                    || currentMaterial == Material.IRON_TRAPDOOR
                                    || currentMaterial == Material.JUKEBOX
                                    || currentMaterial == Material.SIGN
                                    || currentMaterial == Material.CHEST
                                    || currentMaterial == Material.ENDER_CHEST
                                    || currentMaterial == Material.TRAPPED_CHEST
                                    || currentMaterial.name().toLowerCase(Locale.ROOT).contains("bed") &&
                                    !currentMaterial.name().toLowerCase(Locale.ROOT).contains("rock")
                                    || currentMaterial == Material.SIGN_POST
                                    || currentMaterial == Material.LEVER
                                    || currentMaterial == Material.WOOD_BUTTON
                                    || currentMaterial == Material.STONE_BUTTON
                                    || currentMaterial == Material.DAYLIGHT_DETECTOR
                                    || currentMaterial == Material.DAYLIGHT_DETECTOR_INVERTED
                                    || currentMaterial == Material.WALL_SIGN
                                    || currentMaterial == Material.ANVIL
                                    || currentMaterial == Material.BREWING_STAND
                                    || currentMaterial == Material.TRAP_DOOR) {
                                material = currentMaterial;
                                break;
                            }
                        }
                    }

                    if (material == null) {
                        this.blockingItem = true;
                        this.ticksSinceItemBlock = 0;
                    }

                    if (material == null && getData().getPlayer().getItemInHand() != null
                            && getData().isSword(getData().getPlayer().getItemInHand())) {
                        this.blockingPacketsA1 = true;
                    }
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                WrapperPlayClientPlayerDigging digging =
                        new WrapperPlayClientPlayerDigging(event.getPacketReceiveEvent());

                if (digging.getBlockPosition() != null && digging.getAction() == DiggingAction.RELEASE_USE_ITEM) {
                    this.blockingItem = false;
                    this.blockingValidItem = false;
                    this.blockingPacketsA1 = false;
                }

                if (digging.getAction() == DiggingAction.DROP_ITEM
                        || digging.getAction() == DiggingAction.DROP_ITEM_STACK) {
                    this.blockingItem = false;
                    this.blockingValidItem = false;
                    this.waitTicks = 0;
                    this.blockingPacketsA1 = false;
                }
            }

            // Slot change so their no longer holding the item.
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.SLOT_STATE_CHANGE) {
                this.blockingItem = false;
                this.tickSinceSlotChange = 0;
                this.blockingPacketsA1 = false;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
                this.blockingItem = false;
                this.tickSinceSlotChange = 0;
                this.blockingPacketsA1 = false;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity =
                        new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {

                    // Fixes swords...
                    if (this.blockingItem && this.blockingValidItem) {
                        this.blockingItem = this.blockingValidItem = false;
                    }
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
                this.blockingPacketsA1 = false;
            }
        }

        if (event.getPacketSendEvent() != null) {

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
                this.blockingPacketsA1 = false;
            }

            // Fixes when the server opens a window for the player when they use a usable item.
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
                this.blockingItem = this.blockingValidItem = false;
                this.blockingPacketsA1 = false;
            }
        }
    }

    private void handlePrediction(PlayerData user, WrapperPlayClientPlayerFlying currentFlying,
                                  List<HorizontalProcessor.TransactionVelocityEntry> velocityEntries,
                                  Consumer<Predicted> consumer, Runnable exemptRunnable) {

        // pre-checks
        if (user.generalCancel()
                || (user.getPotionProcessor().getSpeedPotionTicks() > 0 && !user.getPotionProcessor().isSpeedPotion())
                || (user.getPotionProcessor().getSlownessTicks() > 0 && !user.getPotionProcessor().isSlownessPotion())
                || user.getMovementProcessor().getPositionTicks() < 7
                || !isBlockingValidItem()
                || this.waitTicks < 2
                || getData().isBedrock()
                || getData().getActionProcessor().getLastWalkSpeedTimer().getDelta() < 20
                || getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion()
                || getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion()
                || user.getMovementProcessor().getClientWallCollision().hasNotPassed(8)
                || this.exempt(getData())
                || user.getCollisionWorldProcessor().getCollidingHorizontallyTimer().hasNotPassed(8)
                || user.getActionProcessor().isTeleportingV2() && getData().getSetBackProcessor().getSetBackTick() <= 0) {
            exemptRunnable.run();
            return;
        }

        WrapperPlayClientPlayerFlying wrappedInFlyingPacket = user.getMovementProcessor().getFlyingPacket();
        WrapperPlayClientPlayerFlying lastFlying = user.getMovementProcessor().getLastFlyingPacket();
        WrapperPlayClientPlayerFlying lastLastLast = user.getMovementProcessor().getLastLastFlyingPacket();

        if (wrappedInFlyingPacket == null || lastFlying == null || lastLastLast == null) return;

        World world = getData().getPlayer().getWorld();

        if (world == null) return;

        // zero zero three
        if ((currentFlying.hasPositionChanged() && lastFlying.hasPositionChanged() && lastLastLast.hasPositionChanged())) {
            double smallest = Double.MAX_VALUE;
            double motionX = 0;
            double motionZ = 0;

            float blockFriction = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                    getData().getMovementProcessor().getTo()
                            .toLocation(world));

            float blockFrictionFrom = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                    getData().getMovementProcessor().getFrom()
                            .toLocation(world));

            float blockFrictionLast = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                    getData().getMovementProcessor().getFromFrom()
                            .toLocation(world));

            if (blockFrictionFrom != blockFriction
                    || blockFrictionFrom != blockFrictionLast
                    || blockFriction != blockFrictionLast) {
                this.ticksToIgnore = 20;
                return;
            }

            if (this.ticksToIgnore-- > 0) {
                return;
            }

            // prediction block
            prediction:
            {
                final Motion realMotion = new Motion(
                        user.getHorizontalProcessor().getDeltaX(),
                        0, user.getHorizontalProcessor().getDeltaZ()
                );

                final Motion motion = new Motion(
                        user.getHorizontalProcessor().getLastDeltaX(),
                        0,
                        user.getHorizontalProcessor().getLastDeltaZ()
                );

                // this is where we get the specific data needed
                boolean[] usingIterations = this.getUsingIterations();

                // copy of the horizontal movement runner, but because the processor version isn't designed to work
                // with hit slow down and keep sprint, this one will handle it
                // the processor version is designed to deal with invalid movement and velocity
                for (HorizontalProcessor.TransactionVelocityEntry velocityEntry : velocityEntries) {
                    for (int forwardBrutes = -1; forwardBrutes < 2; ++forwardBrutes) {
                        for (int strafeBrutes = -1; strafeBrutes < 2; ++strafeBrutes) {
                            for (boolean sneaking : MagicValues.TRUE_FALSE) {
                                for (boolean jump : MagicValues.TRUE_FALSE) {
                                    for (boolean sprint : MagicValues.TRUE_FALSE) {
                                        for (boolean using : usingIterations) {
                                            for (boolean hitSlowdown : MagicValues.TRUE_FALSE) {

                                                motion.set(user.getHorizontalProcessor().getLastDeltaX(),
                                                        0, user.getHorizontalProcessor().getLastDeltaZ()
                                                );

                                                boolean ground = lastFlying.isOnGround();
                                                float forward = forwardBrutes;
                                                float strafe = strafeBrutes;

                                                // ignore sprint and no forward movement
                                                if (sprint && forward <= 0) {
                                                    continue;
                                                }

                                                // walkSpeed / 2 is the same as the nms move speed
                                                double attributeSpeed =
                                                        this.getData().getActionProcessor().getWalkSpeed();

                                                // apply the clients sprint speed value
                                                if (sprint) {
                                                    attributeSpeed += (attributeSpeed * .30000001192092896);
                                                }

                                                // apply the current speed potion multiplier to the move speed
                                                if (user.getPotionProcessor().isSpeedPotion()) {
                                                    attributeSpeed += user.
                                                            getPotionProcessor().getSpeedPotionAmplifier()
                                                            * 0.20000000298023224D * attributeSpeed;
                                                }

                                                // apply the current slowness potion multiplier to the move speed
                                                if (user.getPotionProcessor().isSlownessPotion()) {
                                                    attributeSpeed += user.
                                                            getPotionProcessor().getSlownessAmplifier() *
                                                            -.15000000596046448D * attributeSpeed;
                                                }

                                                // sneaking logic
                                                if (sneaking) {
                                                    forward *= (float) 0.3D;
                                                    strafe *= (float) 0.3D;
                                                }

                                                // using logic
                                                if (using) {
                                                    forward *= 0.2F;
                                                    strafe *= 0.2F;
                                                }

                                                // forward & strafe logic
                                                forward *= 0.98F;
                                                strafe *= 0.98F;

                                                //       blockFriction = getData().getCollisionProcessor().isWaterFully()
                                                //             && sprint && Anticheat.INSTANCE.getVersionSupport()
                                                //           .getClientProtocol(getData()) > 340 ? 0.9F : 0.8F;

                                                // check if they were last on ground 3 ticks ago
                                                if (lastLastLast.isOnGround()) {
                                                    motion.getMotionX().multiply(blockFrictionLast * 0.91F);
                                                    motion.getMotionZ().multiply(blockFrictionLast * 0.91F);
                                                } else {
                                                    motion.getMotionX().multiply(0.91F);
                                                    motion.getMotionZ().multiply(0.91F);
                                                }

                                                // apply the velocity from the last velocity tick
                                                if (velocityEntry != null) {
                                                    motion.getMotionX().set(velocityEntry.getX());

                                                    motion.getMotionZ().set(velocityEntry.getZ());
                                                }

                                                // add hit slowdown to the current prediction
                                                if (hitSlowdown) {
                                                    motion.getMotionX().multiply(0.6D);
                                                    motion.getMotionZ().multiply(0.6D);
                                                }

                                                // round the prediction
                                                motion.round();

                                                float slipperiness = 0.91F;

                                                // apply the blocks below friction
                                                if (ground) slipperiness = blockFrictionLast * 0.91F;

                                                float moveSpeed = (float) attributeSpeed;
                                                final float moveFlyingFriction;

                                                // apply the move speed multiplier if on ground
                                                if (ground) {
                                                    float moveSpeedMultiplier = 0.16277136F /
                                                            (slipperiness * slipperiness * slipperiness);

                                                    // 1.13+ only
                                                    if (Anticheat.INSTANCE.getVersionSupport()
                                                            .getClientProtocol(getData()) >= 393) {
                                                        moveSpeedMultiplier =
                                                                0.21600002F / (blockFrictionLast
                                                                        * blockFrictionLast * blockFrictionLast);
                                                    }

                                                    moveFlyingFriction = moveSpeed * moveSpeedMultiplier;

                                                    // append the acceleration from sprint jumping
                                                    if (jump && sprint) {
                                                        final float radians = user.getHorizontalProcessor()
                                                                .getYaw() * 0.017453292F;

                                                        motion.getMotionX().subtract(MinecraftMath.sin(0, radians) * 0.2F);

                                                        motion.getMotionZ().add(MinecraftMath.cos(0, radians) * 0.2F);
                                                    }
                                                } else {
                                                    // apply the air movement acceleration
                                                    moveFlyingFriction = (float)
                                                            (sprint ? ((double) 0.02F +
                                                                    (double) 0.02F * 0.3D) : 0.02F);
                                                }

                                                MoveFlyingResult moveFlyingResult = MagicValues.moveFlyingResult(
                                                        getData(),
                                                        forward, strafe,
                                                        user.getHorizontalProcessor().getYaw(),
                                                        moveFlyingFriction,
                                                        0,
                                                        getData().getProtocolVersion()
                                                );

                                                // apply the move flying we got above
                                                motion.apply(moveFlyingResult);

                                                // set the motionY to 0 since we don't need it
                                                motion.getMotionY().set(0.0);

                                                // calculate the delta X & Y to generate the square root
                                                double delta = realMotion.distanceSquared(motion);

                                                if (delta < smallest) {
                                                    smallest = delta;
                                                    motionX = motion.getMotionX().get();
                                                    motionZ = motion.getMotionZ().get();

                                                    // we don't need good accuracy for this check, this will do
                                                    if (delta < 1E-7) {
                                                        break prediction;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // accept consumer with predicted data
            if (smallest != Double.MAX_VALUE) {
                consumer.accept(new Predicted(smallest, motionX, motionZ));
            }
        }
    }

    private boolean[] getUsingIterations() {
        return isBlockingValidItem()
                && isBlockingItem()
                ? MagicValues.TRUE : MagicValues.TRUE_FALSE;
    }

    private boolean exempt(PlayerData user) {
        return (user.getCollisionWorldProcessor().getHalfBlockTicks() > 0
                || user.getCollisionWorldProcessor().getCollidingHorizontallyTimer().hasNotPassed()
                || user.getCollisionWorldProcessor().isCollidingHorizontal()
                || user.getCollisionProcessor().getLiquidTicks() > 0
                || user.getCollisionWorldProcessor().isDoor()
                || user.getActionProcessor().isTeleportingV2()
                || user.getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || user.getCollisionWorldProcessor().getAnvilTicks() > 0
                || getData().getActionProcessor().getRespawnTimer().getDelta() < 20
                || getData().getMovementProcessor().getPositionTicks() < 150
                || getData().getActionProcessor().getRespawnTimer().getDelta() < 20
                || user.getCollisionWorldProcessor().getWallTicks() > 0
                || user.getActionProcessor().getLastVehicleTimer().getDelta() < 20
                //      || user.getCombatProcessor().lastServerY > 2.3
                //       || user.getCombatProcessor().lastServerXZ > .9999
                || user.getLastExplosionTimer().getDelta() < 20
                || user.getCollisionProcessor().getWebTicks() > 0
                || user.getCollisionWorldProcessor().getClimbableTicks() > 0
                || user.getCollisionWorldProcessor().getPistionTicks() > 0
                //   || user.getOtherProcessor().getCloseToBorderTimer().hasNotPassed(5)
                || user.getPotionProcessor().getSlownessTicks() > 0
                //      || user.getCombatData().isRespawn()
                //       || user.getCombatData().isServerRespawn()
                //            || user.getHorizontalPredictionRunner().getBaseSpeedTicks() > 0
                || user.getCollisionWorldProcessor().getSoulSandTicks() > 0);
    }

    @Getter
    @AllArgsConstructor
    private final static class Predicted {
        private final double smallest;
        private final double motionX, motionZ;
    }
}