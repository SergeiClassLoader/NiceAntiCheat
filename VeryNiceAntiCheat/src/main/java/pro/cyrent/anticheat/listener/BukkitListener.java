package pro.cyrent.anticheat.listener;

import org.bukkit.Difficulty;
import org.bukkit.entity.*;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.Check;
import pro.cyrent.anticheat.api.check.CheckName;
import pro.cyrent.anticheat.api.check.CheckState;
import pro.cyrent.anticheat.api.check.CheckType;
import pro.cyrent.anticheat.api.command.commands.sub.LogsCommand;
import pro.cyrent.anticheat.api.command.commands.sub.TopViolationsCommand;
import pro.cyrent.anticheat.api.command.commands.util.GUIUtils;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.math.MathUtil;
import pro.cyrent.anticheat.util.nms.Instance;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

public class BukkitListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        Anticheat.INSTANCE.getUserManager().removeUser(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        this.processEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {

        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {

            if (!Anticheat.INSTANCE.getConfigValues().isBedrockSupport()) {
                if (event.getPlayer().getUniqueId().getMostSignificantBits() == 0) {

                    Anticheat.INSTANCE.getPlugin().getServer().getConsoleSender().sendMessage(
                            Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " " + ChatColor.GOLD + event.getPlayer().getName() + ChatColor.RED
                                    + " was detected using Minecraft Bedrock Edition (Not injecting)");
                    return;
                }
            }

            Anticheat.INSTANCE.getUserManager().addUser(event.getPlayer());
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        this.processEvent(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        this.processEvent(event);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        this.processEvent(event);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        this.processEvent(event);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onVelocity(PlayerVelocityEvent event) {
        this.processEvent(event);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onHealthUpdate(EntityRegainHealthEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onFishEvent(PlayerFishEvent event) {
        this.processEvent(event);
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent event) {
        this.processEvent(event);
    }

    @EventHandler
    public void onPiston(BlockPistonExtendEvent event) {
        Location location = event.getBlock().getLocation();

        if (location != null) {
            location.getWorld().getNearbyEntities(location, 3, 3, 3)
                    .stream().filter(entity -> entity != null
                            && entity.getType() == EntityType.PLAYER).forEach(entity -> {

                        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser((Player) entity);

                        if (user != null) {
                            user.getPistonUpdateTimer().resetBoth();
                        }
                    });
        }
    }


    void processEvent(Event event) {
        if (event instanceof InventoryClickEvent
                || event instanceof PlayerChangedWorldEvent) {
            process(event);
        } else {
            Anticheat.INSTANCE.getExecutorService().execute(() -> this.process(event));
        }
    }

    void process(Event event) {
        if (event instanceof PlayerChangedWorldEvent) {
            PlayerData data = Anticheat.INSTANCE.getUserManager().getUser(((PlayerChangedWorldEvent) event).getPlayer());

            if (data != null) {

                data.getLastWorldChange().reset();
            }
        }

        if (event instanceof PlayerTeleportEvent) {
            PlayerData data = Anticheat.INSTANCE.getUserManager().getUser(((PlayerTeleportEvent) event).getPlayer());

            if (data != null) {

                if (((PlayerTeleportEvent) event).getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN) {
                    data.getLastTeleport().reset();
                }

                if (((PlayerTeleportEvent) event).getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
                    data.getLastEnderPearl().reset();
                }
            }
        }

        if (event instanceof PlayerInteractEvent) {
            PlayerData data = Anticheat.INSTANCE.getUserManager().getUser(((PlayerInteractEvent) event).getPlayer());

            if (data == null) return;

            if (((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_AIR
                    || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_BLOCK
                    || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_AIR) {

                if (data.getPlayer().getItemInHand() != null) {
                    //set valid blocking
                    data.getNoSlowDownProcessor().setBlockingValidItem(data
                            .isUsableItem(data.getPlayer().getItemInHand()));
                }
            }

            if (data.getNoSlowDownProcessor().getPreventInteractionTimer().getDelta() < 40
                    && data.getNoSlowDownProcessor().isBlockingValidItem()) {
                data.sendDevAlert("NoSlowDown",
                        "Interact Valid Item",
                        "last-prevent-time=" + data.getNoSlowDownProcessor().getPreventInteractionTimer().getDelta());
                if (Anticheat.INSTANCE.getConfigValues().isNoSlowDown()) {
                    ((PlayerInteractEvent) event).setCancelled(true);
                }
            }

        }

        if (event instanceof EntityDamageByEntityEvent) {


            if (((EntityDamageByEntityEvent) event).getEntity() instanceof Player) {

                PlayerData damagedUser = Anticheat.INSTANCE.getUserManager().getUser((Player)
                        ((EntityDamageByEntityEvent) event).getEntity());

                if (damagedUser == null) return;

                if (damagedUser.getHorizontalProcessor().getVelocitySimulator().getCurrentYaw() == null) {

                    if (((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {
                        damagedUser.getHorizontalProcessor().getVelocitySimulator().setProjectile(false);
                    } else {
                        damagedUser.getHorizontalProcessor().getVelocitySimulator().setProjectile(true);
                    }

                    damagedUser.getHorizontalProcessor().getVelocitySimulator().setCurrentYaw(
                            ((EntityDamageByEntityEvent) event).getDamager().getLocation().getYaw());
                }

                if (((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile) ((EntityDamageByEntityEvent) event).getDamager();

                    // projectile.getLocation().getYaw();
                }


                if (((EntityDamageByEntityEvent) event).getDamager() instanceof WitherSkull
                        || ((EntityDamageByEntityEvent) event).getDamager() instanceof Wither
                        || ((EntityDamageByEntityEvent) event).getDamager() instanceof IronGolem) {
                    damagedUser.getWitherTimer().reset();
                }

                if (((EntityDamageByEntityEvent) event).getDamager() instanceof EnderDragon) {
                    damagedUser.setEnderDragon(true);
                } else {
                    damagedUser.setEnderDragon(false);
                }

                if (((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile) {

                    if (damagedUser.getPunchPower() > 0
                            && damagedUser.getMovementProcessor()
                            .getLastPunchBowInHandTimer().hasNotPassed(20)) {
                        damagedUser.getLastBowBoostTimer().reset();
                        damagedUser.setUsedPunch(true);
                    }

                    damagedUser.getLastProjectileDamage().reset();
                }
            }

            if (((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {

                PlayerData dmgUser = Anticheat.INSTANCE.getUserManager().getUser((Player)
                        ((EntityDamageByEntityEvent) event).getDamager());

                if (dmgUser != null && ((EntityDamageByEntityEvent) event).getEntity() != null) {

                    if (((EntityDamageByEntityEvent) event).getEntityType() == EntityType.PLAYER) {

                        if (((EntityDamageByEntityEvent) event).getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {


                            if (((EntityDamageByEntityEvent) event).isCancelled()) {

                                dmgUser.getCombatProcessor().getLastCancel().reset();

                                dmgUser.getCombatProcessor().setCancelTicks(20);
                            }

                            if (!((EntityDamageByEntityEvent) event).isCancelled()) {

                                if (dmgUser.getCombatProcessor().getCancelTicks() > 0) {
                                    dmgUser.getCombatProcessor().setCancelTicks(dmgUser
                                            .getCombatProcessor().getCancelTicks() - .5);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (event instanceof PlayerFishEvent) {
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((PlayerFishEvent) event).getPlayer());

            if (user != null) {

                if (((PlayerFishEvent) event).getState() == PlayerFishEvent.State.FAILED_ATTEMPT) {
                    user.getFishingRodTimer().reset();
                }

                if (((PlayerFishEvent) event).getState() == PlayerFishEvent.State.IN_GROUND) {
                    user.getFishingRodTimer().reset();
                }
            }
        }


        if (event instanceof EntityDamageEvent) {
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser((Player) ((EntityDamageEvent) event).getEntity());

            if (user != null) {

                if (((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.FALL) {
                    user.getLastFallDamageTimer().reset();
                }

                if (((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.FIRE
                        || ((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                    user.getLastFireTickTimer().reset();
                }

                if (((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                        || ((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                    user.getLastExplosionTimer().reset();
                }

                if (((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                    user.getLastSuffocationTimer().reset();
                }

                if (((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    user.getLastGotAttacked().reset();
                }

                if (((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.THORNS) {
                    user.getLastCactusDamageTimer().reset();
                }

                if (((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.CONTACT) {
                    user.getLastCactusDamageTimer().reset();
                }
            }
        }

        if (event instanceof BlockPlaceEvent) {
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((BlockPlaceEvent) event).getPlayer());

            if (user != null) {

                user.setLastBlockPlaced(user.getBlockPlaced());
                user.setBlockPlaced(((BlockPlaceEvent) event).getBlockPlaced());

                user.setBlockAgainst(((BlockPlaceEvent) event).getBlockAgainst());

                user.getLastBlockPlaceTimer().reset();

                if (((BlockPlaceEvent) event).isCancelled()) {
                    user.getLastBlockPlaceCancelTimer().reset();
                }

                user.setLastLastBlockPlace(user.getLastBlockPlaceEvent());
                user.setLastBlockPlaceEvent((BlockPlaceEvent) event);
            }
        }

        if (event instanceof BlockBreakEvent) {
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((BlockBreakEvent) event).getPlayer());

            if (user != null) {
                user.getLastBlockBreakTimer().reset();

                if (((BlockBreakEvent) event).getBlock().getType() == Material.BED
                        || ((BlockBreakEvent) event).getBlock().getType() == Material.BED_BLOCK) {
                    user.setLastBlockBreakLocation(((BlockBreakEvent) event).getBlock().getLocation());
                }
            }
        }

        if (Anticheat.INSTANCE.getConfigValues().isFastBow()) {
            if (event instanceof EntityShootBowEvent) {

                if (((EntityShootBowEvent) event).getEntity() instanceof Player) {

                    PlayerData data = Anticheat.INSTANCE.getUserManager().getUser((Player)
                            ((EntityShootBowEvent) event).getEntity());

                    if (data != null) {

                        float force = ((EntityShootBowEvent) event).getForce();


                        if (data.getNoSlowDownProcessor().getPreventInteractionTimer().getDelta() < 40) {
                            data.sendDevAlert("NoSlowDown",
                                    "Bow & Prediction",
                                    "prevention-time=" + data.getNoSlowDownProcessor()
                                            .getPreventInteractionTimer().getDelta());
                            ((EntityShootBowEvent) event).setCancelled(true);
                        }

                        if (data.getNoSlowDownProcessor().getTickSinceSlotChange() < 3
                                && data.getNoSlowDownProcessor().getLastTicksSinceSlotChange() < 3) {
                            data.sendDevAlert("NoSlowDown",
                                    "Bow & Slot Change",
                                    "slot-time=" + data.getNoSlowDownProcessor().getTickSinceSlotChange());
                            ((EntityShootBowEvent) event).setCancelled(true);
                        }

                        int stage = MathUtil.getFirstDecimalDigit(force);
                        int toTick = 1;

                        // we dynamically change the max tick depending on the bows force, this will make it stricter
                        switch (stage) {
                            case 1: {
                                toTick = 4;
                                break;
                            }

                            case 2:
                            case 3: {
                                toTick = 7;
                                break;
                            }

                            case 4:
                            case 5: {
                                toTick = 11;
                                break;
                            }

                            case 6:
                            case 7: {
                                toTick = 15;
                                break;
                            }

                            case 8:
                            case 9: {
                                toTick = 18;
                                break;
                            }

                            case 0: {
                                toTick = 20;
                                break;
                            }
                        }

                        // if the tick is below expected we cancel the event
                        if (data.getTicksSinceBow() < toTick) {
                            data.sendDevAlert("FastBow",
                                    "Shooting too quickly",
                                    "ticks-since-bow=" + data.getTicksSinceBow(),
                                    "max-bow-tick=" + toTick);
                            ((EntityShootBowEvent) event).setCancelled(true);
                        }

                        data.setTicksSinceBow(0);

                        data.getDesyncProcessor().setLastServerBowTick(
                                data.getDesyncProcessor().getServerTicks()
                        );
                    }
                }
            }
        }

        if (Anticheat.INSTANCE.getConfigValues().isNoSlowDown()) {
            if (event instanceof PlayerInteractEvent) {
                PlayerData data = Anticheat.INSTANCE.getUserManager().getUser(((PlayerInteractEvent) event).getPlayer());

                if (data == null) return;

                if (((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_AIR
                        || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_BLOCK
                        || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_AIR) {

                    if (data.getPlayer().getItemInHand() != null) {
                        //set valid blocking
                        data.getNoSlowDownProcessor().setBlockingValidItem(data
                                .isUsableItem(data.getPlayer().getItemInHand()));
                    }
                }

                if (data.getNoSlowDownProcessor().getPreventInteractionTimer().getDelta() < 40
                        && data.getNoSlowDownProcessor().isBlockingValidItem()) {
                    data.sendDevAlert("NoSlowDown",
                            "Interact Valid Item",
                            "last-prevent-time=" + data.getNoSlowDownProcessor().getPreventInteractionTimer().getDelta());
                    ((PlayerInteractEvent) event).setCancelled(true);
                }

            }
        }

        if (Anticheat.INSTANCE.getConfigValues().isFastEat()) {
            if (event instanceof PlayerItemConsumeEvent) {

                PlayerData data = Anticheat.INSTANCE.getUserManager().getUser(((PlayerItemConsumeEvent) event).getPlayer());

                if (data == null) return;

                if (data.getNoSlowDownProcessor().getPreventInteractionTimer().getDelta() < 40
                        && data.getNoSlowDownProcessor().isBlockingValidItem()) {
                    data.sendDevAlert("NoSlowDown",
                            "Eating & Prediction",
                            "last-prevent-time=" + data.getNoSlowDownProcessor().getPreventInteractionTimer().getDelta());
                    ((PlayerItemConsumeEvent) event).setCancelled(true);
                }

                if (data.getNoSlowDownProcessor().getTickSinceSlotChange() < 3) {
                    data.sendDevAlert("NoSlowDown",
                            "Eat & Slot Change",
                            "slot-time=" + data.getNoSlowDownProcessor().getTickSinceSlotChange());
                    ((PlayerItemConsumeEvent) event).setCancelled(true);
                }

                if (((PlayerItemConsumeEvent) event).getItem().getType().isEdible()
                        || data.getNoSlowDownProcessor().isBlockingValidItem()) {

                    // should not be possible, from testing it's no
                    if (data.getTicksSinceEat() < 25) {
                        ((PlayerItemConsumeEvent) event).setCancelled(true);
                        data.sendDevAlert("FastEat",
                                "Time",
                                "eat-tick-time=" + data.getTicksSinceEat());
                    }

                    data.setTicksSinceEat(0);
                }
            }
        }

        if (event instanceof PlayerMoveEvent) {
            PlayerData data = Anticheat.INSTANCE.getUserManager().getUser(((PlayerMoveEvent) event).getPlayer());

            if (data != null) {
                if (((PlayerMoveEvent) event).isCancelled()) {
                    data.getLastMovementCancelTimer().reset();
                }
            }
        }

        if (Anticheat.INSTANCE.getConfigValues().isRegen()) {
            if (event instanceof EntityRegainHealthEvent) {

                if (((EntityRegainHealthEvent) event).getEntity() instanceof Player) {
                    PlayerData data = Anticheat.INSTANCE.getUserManager().getUser((Player)
                            ((EntityRegainHealthEvent) event).getEntity());

                    if (data == null) return;


                    if (((EntityRegainHealthEvent) event).getRegainReason()
                            != EntityRegainHealthEvent.RegainReason.SATIATED) {
                        data.getDesyncProcessor().setExemptTicks(10);
                    }


                    switch (((EntityRegainHealthEvent) event).getRegainReason()) {
                        case REGEN: {
                            if (data.getRegenServerTick() < 20) {
                                if (data.getPlayer().getWorld().getDifficulty() == Difficulty.PEACEFUL) return;

                                data.sendDevAlert("Regen",
                                        "REGEN/POTION",
                                        "regen-tick=" + data.getRegenServerTick());
                                ((EntityRegainHealthEvent) event).setCancelled(true);
                            }

                            data.setRegenServerTick(0);
                            break;
                        }

                        case SATIATED: {
                            if (data.getSatiatedServerTick() < 78 && data.getDesyncProcessor().getExemptTicks() < 1) {
                                if (data.getPlayer().getWorld().getDifficulty() == Difficulty.PEACEFUL) return;

                                data.sendDevAlert("Regen",
                                        "SATIATED",
                                        "satiated-tick=" + data.getSatiatedServerTick());
                                ((EntityRegainHealthEvent) event).setCancelled(true);
                            }
                            data.setSatiatedServerTick(0);
                            break;
                        }
                    }
                }
            }
        }
    }


    //GUI SHIT
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final PlayerData user = Anticheat.INSTANCE.getUserManager().getUser((Player) event.getWhoClicked());

        if (user == null) return;

        if (event.getClickedInventory() == null || event.getClickedInventory().getName() == null
                || (!Anticheat.INSTANCE.getPermissionValues().hasGuiPermission(event.getWhoClicked()) &&
                !user.isDev((Player) event.getWhoClicked()))) return;

        final String guiName = ChatColor.stripColor(event.getClickedInventory().getName());

        final String anticheatName = Anticheat.INSTANCE.getAnticheatName();

        if (event.getWhoClicked().hasPermission(Anticheat.INSTANCE.getPermissionValues().getGuiCommand())) {
            if (ChatColor.stripColor(event.getClickedInventory().getName()).contains("Information »")) {
                event.setCancelled(true);
            }
        }

        if (event.getWhoClicked().hasPermission(Anticheat.INSTANCE.getPermissionValues().getCrashCommand()) &&
                event.getClickedInventory().getName().equalsIgnoreCase(ChatColor.RED + "Select a crash method.")) {
            final int slot = event.getSlot();
            event.setCancelled(true);

            if (user.getTargetCrashPlayer() != null) {
                final PlayerData target = user.getTargetCrashPlayer();
                switch (slot) {
                    case 0 -> {
                        Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .crashClient(target, Instance.CrashTypes.EXPLODE);

                        user.getPlayer().sendMessage(ChatColor.GREEN +
                                target.getUsername() + " will now crash.");

                        user.closeInventory();
                    }
                    case 1 -> {

                        new BukkitRunnable() {
                            final Player player = target.getPlayer();

                            @Override
                            public void run() {
                                if (player.isOnline()) {
                                    int max = 60;
                                    for (int i = 0; i < max; i++) {
                                        Anticheat.INSTANCE.getInstanceManager().getInstance()
                                                .dropFPS(target);
                                    }

                                    player.closeInventory();
                                } else {
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(Anticheat.INSTANCE.getPlugin(), 0L, 0L);

                        user.getPlayer().sendMessage(ChatColor.GREEN
                                + target.getUsername() + " will now slowly begin to lag.");
                        user.closeInventory();
                    }
                    case 2 -> {

                        Anticheat.INSTANCE.getInstanceManager().getInstance().potionCrash(target);
                        user.getPlayer().sendMessage(ChatColor.GREEN + target.getUsername()
                                + " will fully crash when they open their inventory.");

                        user.closeInventory();
                    }
                    case 3 -> {

                        new BukkitRunnable() {
                            final Player player = target.getPlayer();

                            @Override
                            public void run() {
                                if (player.isOnline()) {
                                    Anticheat.INSTANCE.getInstanceManager().getInstance().showDemo(target);
                                } else {
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(Anticheat.INSTANCE.getPlugin(), 0L, 0L);

                        user.getPlayer().sendMessage(ChatColor.GREEN + target.getUsername()
                                + " will now see the demo menu");
                        user.closeInventory();
                    }
                    case 4 -> {
                        user.closeInventory();
                        user.getPlayer().sendMessage(ChatColor.GREEN + target.getUsername()
                                + " will no-longer receive server packets or be able to send them.");

                        Anticheat.INSTANCE.getInstanceManager().getInstance().disconnect(target);
                    }
                    case 5 -> {
                        user.closeInventory();
                        Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .crashClient(target, Instance.CrashTypes.PARTICLE);

                        user.getPlayer().sendMessage(ChatColor.GREEN +
                                target.getUsername() + " will now crash.");
                    }
                    case 6 -> {
                        user.closeInventory();

                        Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .crashClient(target, Instance.CrashTypes.WINDOW);

                        user.getPlayer().sendMessage(ChatColor.GREEN +
                                target.getUsername() + " will now crash.");
                    }
                }
            }
        }
        else if(event.getWhoClicked().hasPermission(Anticheat.INSTANCE.getPermissionValues().getStatsGuiCommand()) &&
                event.getClickedInventory().getName().equalsIgnoreCase(ChatColor.RED + "Lumos Statistics") ||
                event.getClickedInventory().getName().startsWith(ChatColor.RED + "Lumos | Client » ")) {
            event.setCancelled(true);
        }
        if (event.getWhoClicked().hasPermission(Anticheat.INSTANCE.getPermissionValues().getGuiCommand()) &&
                event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {

            final String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if (guiName.equalsIgnoreCase(anticheatName)) {
                event.setCancelled(true);

                if (itemName.equalsIgnoreCase("Checks")) {
                    this.clickChecks(event);
                }
                return;
            }

            if (getCheckNameFromGUIName(guiName) != null) {

                event.setCancelled(true);

                Check clickedCheck = user.getCheckManager().sortChecksAlphabetically().stream()
                        .filter(check -> (check.getCheckName() + check.getCheckType()).equalsIgnoreCase(itemName))
                        .findAny().orElse(null);

                switch (event.getClick()) {
                    case LEFT -> {
                        if (clickedCheck != null) {

                            clickedCheck.setEnabled(!clickedCheck.isEnabled());

                            Anticheat.INSTANCE.getUserManager().getUserMap().values().forEach(user1 ->
                                    user1.getCheckManager().sortChecksAlphabetically().stream().filter(check ->
                                            (check.getCheckName() + check.getCheckType()).equalsIgnoreCase
                                                    ((clickedCheck.getCheckName()
                                                            + clickedCheck.getCheckType()))).forEach(check ->
                                            check.setEnabled(clickedCheck.isEnabled())));

                            this.openCheckCategoryInventories(event, user,
                                    this.getCheckNameFromGUIName(guiName));

                            user.getCheckManager().saveChecks();

                        }

                        if (event.getSlot() == 53 && itemName.equalsIgnoreCase("back")) {
                            this.showChecksUI(event, itemName, false);
                        }
                    }
                    case RIGHT -> {
                        if (clickedCheck != null) {

                            clickedCheck.setPunishable(!clickedCheck.isPunishable());

                            Anticheat.INSTANCE.getUserManager().getUserMap().values().forEach(user1 ->
                                    user1.getCheckManager().sortChecksAlphabetically().stream().filter(check ->
                                            (check.getCheckName() + check.getCheckType()).equalsIgnoreCase
                                                    ((clickedCheck.getCheckName()
                                                            + clickedCheck.getCheckType()))).forEach(check ->
                                            check.setPunishable(clickedCheck.isPunishable())));

                            this.openCheckCategoryInventories(event, user,
                                    this.getCheckNameFromGUIName(guiName));
                        }

                        if (clickedCheck != null) {
                            user.getCheckManager().saveChecks();
                        }

                        if (event.getSlot() == 53 && itemName.equalsIgnoreCase("back")) {
                            this.showChecksUI(event, itemName, false);
                        }
                    }
                    case MIDDLE -> {
                        if (clickedCheck != null) {

                            clickedCheck.setEnabled(!clickedCheck.isEnabled());
                            clickedCheck.setPunishable(!clickedCheck.isPunishable());

                            Anticheat.INSTANCE.getUserManager().getUserMap().values().forEach(user1 ->
                                    user1.getCheckManager().sortChecksAlphabetically().stream().filter(check ->
                                            (check.getCheckName() + check.getCheckType()).equalsIgnoreCase
                                                    ((clickedCheck.getCheckName()
                                                            + clickedCheck.getCheckType()))).forEach(check -> {
                                        check.setEnabled(clickedCheck.isEnabled());
                                        check.setPunishable(clickedCheck.isPunishable());
                                    }));

                            this.openCheckCategoryInventories(event, user,
                                    this.getCheckNameFromGUIName(guiName));
                        }

                        if (clickedCheck != null) {
                            user.getCheckManager().saveChecks();
                        }

                        if (event.getSlot() == 53 && itemName.equalsIgnoreCase("back")) {
                            this.showChecksUI(event, itemName, false);
                        }
                    }
                }

                if (clickedCheck != null) {
                    user.getCheckManager().saveChecks();
                }
            }

            switch (guiName) {
                case "Combat", "Movement", "Page 2", "Development", "Misc" -> {
                    event.setCancelled(true);

                    CheckName value = this.getCheckNameFromGUIName(itemName);

                    switch (event.getClick()) {
                        case LEFT -> {
                            if (value != null && value != CheckName.NULL) {

                           /*     clickedCheck.setEnabled(!clickedCheck.isEnabled());

                                Anticheat.INSTANCE.getUserManager().getUserMap().forEach((uuid, user1) ->
                                        user1.getCheckManager().checks.stream().filter(check ->
                                                (check.getCheckName() + check.getCheckType()).equalsIgnoreCase
                                                        ((clickedCheck.getCheckName()
                                                                + clickedCheck.getCheckType()))).forEach(check ->
                                                check.setEnabled(clickedCheck.isEnabled())));*/

                                this.openCheckCategoryInventories(event, user, value);
                            }

                            if (event.getSlot() == 53 && itemName.equalsIgnoreCase("back")) {
                                this.clickChecks(event);
                            }
                        }
                        case RIGHT -> {
                            if (value != null && value != CheckName.NULL) {
                                this.openCheckCategoryInventories(event, user, value);
                            }

                            if (event.getSlot() == 53 && itemName.equalsIgnoreCase("back")) {
                                this.clickChecks(event);
                            }
                        }
                        case MIDDLE -> {
                            if (value != null && value != CheckName.NULL) {
                                this.openCheckCategoryInventories(event, user, value);
                            }

                            if (event.getSlot() == 53 && itemName.equalsIgnoreCase("back")) {
                                this.clickChecks(event);
                            }
                        }
                    }
                }
                case "Checks" -> {

                    switch (itemName) {
                        case "Combat", "Movement", "Misc" -> {
                            if (user.getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getTop())
                                    || user.isDev(user.getPlayer())) {
                                user.setLastUIName(itemName);
                                this.showChecksUI(event, itemName, false);
                            }
                        }
                        case "Development" -> {
                            if (user.getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getTop())
                                    || user.isDev(user.getPlayer())) {
                                user.setLastUIName(itemName);
                                this.showChecksUI(event, itemName, false);
                            }
                        }
                        case "Next Page" -> {
                            if (user.getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getTop())
                                    || user.isDev(user.getPlayer())) {
                                this.openChecksManagerGUI(event, user, user.nextPageChecks, 2);
                            }
                        }
                    }

                }
            }
        }

        switch (guiName) {
            case "Combat", "Page 2", "Development", "Movement", "Misc", "Checks", "Lumos" -> {
                if (user.getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getGuiCommand())
                        || user.isDev(user.getPlayer())) {
                    event.setCancelled(true);
                }
            }
            case "Top Player Violations" -> {

                if (user.getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getTop())
                        || user.isDev(user.getPlayer())) {

                    if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {

                        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

                        if (event.getCurrentItem().getType() == Material.SKULL
                                || event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                            LogsCommand.getPlayerLogs(user.getPlayer(), itemName, "total");
                            user.getPlayer().closeInventory();
                        }

                        if (event.getSlot() == 53 && itemName.equalsIgnoreCase("Next Page")) {
                            if (user.currentPage < ((user.results.size() - 1) / user.pageSize)) {
                                user.currentPage++;
                                TopViolationsCommand.openGUI(user.getPlayer(), user.results, user.isOnlineOnly());
                            }
                        }

                        if (event.getSlot() == 45 && itemName.equalsIgnoreCase("Back")) {
                            if (user.currentPage > 0) {
                                user.currentPage--;
                                TopViolationsCommand.openGUI(user.getPlayer(), user.results, user.isOnlineOnly());
                            }
                        }

                        if (event.getSlot() == 49 && itemName.equalsIgnoreCase("Show online players only")) {
                            user.setOnlineOnly(true);
                            user.setCurrentPage(0); // Reset current page to 0
                            TopViolationsCommand.openGUI(user.getPlayer(), user.results, user.isOnlineOnly());
                        }


                        if (event.getSlot() == 49 && itemName.equalsIgnoreCase("Show all players")) {
                            user.setOnlineOnly(false);
                            TopViolationsCommand.openGUI(user.getPlayer(), user.results, user.isOnlineOnly());
                        }
                    }

                    event.setCancelled(true);
                }
            }
        }
    }

    public void clickChecks(InventoryClickEvent event) {
        event.getWhoClicked().closeInventory();

        Inventory inventory = getServer().createInventory(null, 27,
                ChatColor.RED + "Checks");

        inventory.setItem(11, GUIUtils.generateItem(
                new ItemStack(Material.DIAMOND_SWORD, 1), ChatColor.GREEN + "Combat",
                Collections.singletonList(ChatColor.GRAY + "Click to manage all combat checks.")));

        inventory.setItem(13, GUIUtils.generateItem(
                new ItemStack(Material.FEATHER, 1), ChatColor.GREEN + "Movement",
                Collections.singletonList(ChatColor.GRAY + "Click to manage all movement checks.")));

        inventory.setItem(15, GUIUtils.generateItem(
                new ItemStack(Material.ANVIL, 1), ChatColor.GREEN + "Misc",
                Collections.singletonList(ChatColor.GRAY + "Click to manage all misc checks.")));

        inventory.setItem(26, GUIUtils.generateItem(
                new ItemStack(Material.REDSTONE, 1), ChatColor.GREEN + "Development",
                Collections.singletonList(ChatColor.GRAY + "Click to manage all development checks " +
                        "(dev only).")));

        for (int slots = 0; slots < 27; slots++) {
            if (inventory.getItem(slots) == null) inventory.setItem(slots,
                    GUIUtils.createSpacer((byte) 7));
        }

        event.getWhoClicked().openInventory(inventory);
    }

    private CheckName getCheckNameFromGUIName(String guiName) {
        for (CheckName checkName : CheckName.values()) {
            if (checkName.name().equals(guiName)) {
                return checkName;
            }
        }
        return null; // Return null if no match is found
    }

    private void openCheckCategoryInventories(InventoryClickEvent event, PlayerData user, CheckName checkName) {

        Inventory inventory = getServer().createInventory(null, 54, ChatColor.RED + checkName.toString());

        List<Check> categoryChecks = user.getCheckManager().sortChecksAlphabetically().stream()
                .filter(check -> check.getCheckNameEnum() == checkName)
                .toList();

        for (int i = 0; i < categoryChecks.size(); i++) {
            Check check = categoryChecks.get(i);
            ItemStack itemStack = GUIUtils.generateItem(
                    new ItemStack((check.isEnabled()
                            ? (check.isPunishable() && !check.isExperimental() ? Material.EMERALD_BLOCK
                            : Material.GOLD_BLOCK) : Material.REDSTONE_BLOCK), 1),
                    (check.isEnabled() ? (check.isPunishable() ? ChatColor.GREEN
                            : ChatColor.YELLOW) : ChatColor.RED) +
                            check.getCheckName() + check.getCheckType(), Arrays.asList(
                            ChatColor.GRAY + "Enabled: " + (check.isEnabled() ?
                                    ChatColor.GREEN : ChatColor.RED) + check.isEnabled(),
                            ChatColor.GRAY + "Ban: " + (check.isPunishable() && !check.isExperimental() ?
                                    ChatColor.GREEN : ChatColor.RED) + (check.isPunishable()
                                    && !check.isExperimental()),
                            ChatColor.GRAY + "Check State: " +
                                    (check.getState() == CheckState.RELEASE ? ChatColor.GREEN
                                            : check.getState() == CheckState.DEV ? ChatColor.DARK_RED
                                            : check.getState() == CheckState.ALPHA ? ChatColor.RED
                                            : ChatColor.YELLOW) + check.getState().toString(),
                            ChatColor.GRAY + "Description: " + ChatColor.WHITE + check.getDescription(),
                            check.isExperimental() ? ChatColor.RED + "(*EXPERIMENTAL DETECTION*)" : ""
                    ));
            inventory.setItem(i, itemStack);
        }

        for (int slots = categoryChecks.size(); slots < 53; slots++) {
            inventory.setItem(slots, GUIUtils.createSpacer((byte) 7));
        }

        inventory.setItem(53, GUIUtils.generateItem(new ItemStack(Material.ARROW, 1),
                ChatColor.GRAY + "Back", null));

        event.getWhoClicked().openInventory(inventory);
    }

    private void showChecksUI(InventoryClickEvent event, String itemName, boolean mode) {
        if (!mode) {
            event.getWhoClicked().closeInventory();
        }

        PlayerData data = Anticheat.INSTANCE.getUserManager().getUser((Player) event.getWhoClicked());

        if (data.getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getGuiCommand()) || data.isDev(data.getPlayer())) {

            if (itemName.equalsIgnoreCase("back")) {
                itemName = data.getLastUIName();
            }

            Inventory inventory = getServer().createInventory(null, 54, ChatColor.RED + itemName);

            boolean nextPage = event.getInventory().getName().equalsIgnoreCase("Page 2");
            AtomicInteger slot = new AtomicInteger();

            CheckType toSearch = CheckType.COMBAT;

            switch (itemName) {
                case "Movement" -> {
                    toSearch = CheckType.MOVEMENT;
                }
                case "Misc" -> {
                    toSearch = CheckType.MISC;
                }
                case "Development" -> {
                    if (data.isDev(data.getPlayer())) {
                        toSearch = CheckType.DEVELOPMENT;
                    }
                }
            }

            if (mode) {
                switch (data.getLastUIName()) {
                    case "Movement" -> {
                        toSearch = CheckType.MOVEMENT;
                    }
                    case "Misc" -> {
                        toSearch = CheckType.MISC;
                    }
                    case "Development" -> {
                        if (data.isDev(data.getPlayer())) {
                            toSearch = CheckType.DEVELOPMENT;
                        }
                    }
                }
            }

            CheckType finalToSearch = toSearch;

            if (!data.isDev(data.getPlayer()) && finalToSearch == CheckType.DEVELOPMENT) {
                event.getWhoClicked().sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " Developers can only access this!");
                return;
            }

            final Set<CheckName> uniqueCheckNames = new HashSet<>();
            final List<Check> leftOverChecks = new ArrayList<>();
            final List<Check> firstPageChecks = new ArrayList<>();
            final AtomicInteger checkAmount = new AtomicInteger();

            data.getCheckManager().sortChecksAlphabetically().stream()
                    .filter(check -> check.getType() == finalToSearch).forEach(check -> {
                CheckName checkNameEnum = check.getCheckNameEnum();
                if (uniqueCheckNames.add(checkNameEnum)) {
                    // Only add if the enum is not already added
                    if (slot.get() < 27) {
                        inventory.setItem(slot.getAndIncrement(),
                                GUIUtils.generateItem(new ItemStack(Material.CHEST, 1),
                                        ChatColor.GREEN + checkNameEnum.name(),
                                        Collections.singletonList(ChatColor.GRAY + "Click to manage checks in this category.")));
                    }
                }

                checkAmount.getAndIncrement();

                if (!nextPage) {
                    if (checkAmount.get() > 27) {
                        leftOverChecks.add(check);
                    } else {
                        firstPageChecks.add(check);
                    }
                }
            });

            for (int slots = 0; slots < 27; slots++) {
                if (inventory.getItem(slots) == null) {
                    inventory.setItem(slots, GUIUtils.createSpacer((byte) 7));
                }
            }

            if (mode) {
                if (event.getSlot() == 27) {
                    data.nextPageChecks.clear();
                    data.nextPageChecks.addAll(leftOverChecks);
                }
            }

            leftOverChecks.clear();

            inventory.setItem(53, GUIUtils.generateItem(new ItemStack(Material.ARROW, 1),
                    ChatColor.GRAY + "Back", null));

            if (!mode) {
                event.getWhoClicked().openInventory(inventory);
            }
        }
    }


    public void openChecksManagerGUI(InventoryClickEvent e, PlayerData player, List<Check> checks, int page) {

        if (player.getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getGuiCommand())
                || player.isDev(player.getPlayer())) {

            final String itemName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

            e.getWhoClicked().closeInventory();

            Inventory inventory = getServer().createInventory(null, 54,
                    ChatColor.RED + "Page 2");

            inventory.setItem(53, GUIUtils.generateItem(new ItemStack(Material.ARROW, 1),
                    ChatColor.GRAY + "Back", null));

            CheckType toSearch = CheckType.COMBAT;

            switch (itemName) {
                case "Movement" -> {
                    toSearch = CheckType.MOVEMENT;
                }
                case "Misc" -> {
                    toSearch = CheckType.MISC;
                }
                case "Development" -> {
                    toSearch = CheckType.DEVELOPMENT;
                }
            }

            switch (player.getLastUIName()) {
                case "Movement" -> {
                    toSearch = CheckType.MOVEMENT;
                }
                case "Misc" -> {
                    toSearch = CheckType.MISC;
                }
                case "Development" -> {
                    toSearch = CheckType.DEVELOPMENT;
                }
            }

            CheckType finalToSearch = toSearch;

            AtomicInteger slot = new AtomicInteger();

            checks.stream().filter(check ->
                    check.getType() == finalToSearch).forEach(check ->
                    inventory.setItem(slot.getAndIncrement(),
                            GUIUtils.generateItem(new ItemStack((check.isEnabled()
                                            ? (check.isPunishable() && !check.isExperimental() ? Material.EMERALD_BLOCK
                                            : Material.GOLD_BLOCK) : Material.REDSTONE_BLOCK), 1),

                                    (check.isEnabled() ? (check.isPunishable() ? ChatColor.GREEN
                                            : ChatColor.YELLOW) : ChatColor.RED)
                                            + check.getCheckName() + check.getCheckType(), Arrays.asList(
                                            ChatColor.GRAY + "Enabled: " + (check.isEnabled() ?
                                                    ChatColor.GREEN : ChatColor.RED) + check.isEnabled(),
                                            ChatColor.GRAY + "Ban: " + (check.isPunishable() && !check.isExperimental() ?
                                                    ChatColor.GREEN : ChatColor.RED) + (check.isPunishable()
                                                    && !check.isExperimental()),
                                            //RELEASE = GREEN, DEV = DARK RED, ALPHA = RED, BETA = YELLOW
                                            ChatColor.GRAY + "Check State: " +
                                                    (check.getState() == CheckState.RELEASE ? ChatColor.GREEN
                                                            : check.getState() == CheckState.DEV ? ChatColor.DARK_RED
                                                            : check.getState() == CheckState.ALPHA ? ChatColor.RED
                                                            : ChatColor.YELLOW) + check.getState().toString(),
                                            ChatColor.GRAY + "Description: " + ChatColor.WHITE + check.getDescription(),
                                            check.isExperimental() ? ChatColor.RED + "(*EXPERIMENTAL DETECTION*)" : "",
                                            "",
                                            ChatColor.GRAY + ChatColor.ITALIC.toString() + "Left click " + ChatColor.GRAY + "to disable the check.",
                                            ChatColor.GRAY + ChatColor.ITALIC.toString() + "Right click " + ChatColor.GRAY + "to disable autobans."
                                    ))));

            for (int slots = 0; slots < 53; slots++) {
                if (inventory.getItem(slots) == null) {
                    inventory.setItem(slots, GUIUtils.createSpacer((byte) 7));
                }
            }

            e.getWhoClicked().openInventory(inventory);
        }
    }
}