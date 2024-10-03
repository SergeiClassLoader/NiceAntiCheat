package pro.cyrent.anticheat.api.check;

import pro.cyrent.anticheat.Anticheat;

import pro.cyrent.anticheat.api.check.impl.combat.aim.*;
import pro.cyrent.anticheat.api.check.impl.combat.aimanalysis.*;
import pro.cyrent.anticheat.api.check.impl.combat.autoclicker.*;
import pro.cyrent.anticheat.api.check.impl.combat.killaura.*;
import pro.cyrent.anticheat.api.check.impl.combat.throwpot.ThrowPotA;
import pro.cyrent.anticheat.api.check.impl.combat.velocity.*;
import pro.cyrent.anticheat.api.check.impl.combat.wtap.*;
import pro.cyrent.anticheat.api.check.impl.misc.badpackets.*;
import pro.cyrent.anticheat.api.check.impl.misc.inventory.*;
import pro.cyrent.anticheat.api.check.impl.misc.netanalysis.*;
import pro.cyrent.anticheat.api.check.impl.misc.pingspoof.PingSpoofA;
import pro.cyrent.anticheat.api.check.impl.misc.timer.TimerC;
import pro.cyrent.anticheat.api.check.impl.movement.fly.FlyB;
import pro.cyrent.anticheat.api.check.impl.movement.freeze.FreezeA;
import pro.cyrent.anticheat.api.check.impl.movement.invalidmove.*;
import pro.cyrent.anticheat.api.check.impl.bedrock.misc.GeyserPacketsA;
import pro.cyrent.anticheat.api.check.impl.bedrock.move.fly.GeyserFlyA;
import pro.cyrent.anticheat.api.check.impl.bedrock.move.speed.GeyserSpeedA;
import pro.cyrent.anticheat.api.check.impl.bedrock.move.speed.GeyserSpeedB;
import pro.cyrent.anticheat.api.check.impl.combat.entity.EntityA;
import pro.cyrent.anticheat.api.check.impl.combat.hitbox.HitBoxA;
import pro.cyrent.anticheat.api.check.impl.combat.hitbox.HitBoxB;
import pro.cyrent.anticheat.api.check.impl.combat.reach.ReachA;
import pro.cyrent.anticheat.api.check.impl.misc.timer.TimerA;
import pro.cyrent.anticheat.api.check.impl.movement.fly.FlyA;
import pro.cyrent.anticheat.api.check.impl.movement.noslow.NoSlowA;
import pro.cyrent.anticheat.api.check.impl.movement.scaffold.*;
import pro.cyrent.anticheat.api.check.impl.movement.speed.SpeedA;
import pro.cyrent.anticheat.api.check.impl.movement.speed.SpeedB;
import pro.cyrent.anticheat.api.check.impl.movement.speed.SpeedC;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.bukkit.RunUtils;
import pro.cyrent.anticheat.util.config.ChecksFile;
import lombok.Getter;

import java.util.*;

@Getter
public class CheckManager {
    //keep this like this for best performance....
    public final Map<Class<? extends Check>, Check> checks = new HashMap<>();
    private List<Check> cachedSorted = null;

    private static boolean CHECK_CACHE = false;

    private boolean setup = false;

    private final PlayerData data;

    public CheckManager(PlayerData playerData) {
        this.data = playerData;
        this.loadChecks();
    }

    public void loadChecks() {

        //combat
        addCheck(new AutoClickerA());
        addCheck(new AutoClickerB());
        addCheck(new AutoClickerC());
        addCheck(new AutoClickerD());
        addCheck(new AutoClickerE());
        addCheck(new AutoClickerF());
        addCheck(new AutoClickerG());
        addCheck(new AutoClickerH());
        addCheck(new AutoClickerI());
        addCheck(new AutoClickerJ());
        addCheck(new AutoClickerK());
        addCheck(new AutoClickerL());
        addCheck(new AutoClickerM());
        addCheck(new AutoClickerN());
        addCheck(new AutoClickerO());
        addCheck(new AutoClickerP());
        addCheck(new AutoClickerQ());
        addCheck(new AutoClickerR());
        addCheck(new AutoClickerS());
        addCheck(new AutoClickerT());
        addCheck(new AutoClickerU());
        addCheck(new AutoClickerV());
        addCheck(new AutoClickerW());
        addCheck(new AutoClickerX());

        addCheck(new AnalysisA());
        addCheck(new AnalysisB());
        addCheck(new AnalysisC());
        addCheck(new AnalysisD());
        addCheck(new AnalysisE());
        addCheck(new AnalysisF());
        addCheck(new AnalysisG());
        addCheck(new AnalysisH());
        addCheck(new AnalysisI());
        addCheck(new AnalysisJ());
        addCheck(new AnalysisK());
        addCheck(new AnalysisL());


        addCheck(new AimA());
        addCheck(new AimB());
        addCheck(new AimC());
        addCheck(new AimC1());
        addCheck(new AimD());
        addCheck(new AimE());

        addCheck(new AimF());
        addCheck(new AimG());
        addCheck(new AimH());
        addCheck(new AimI());
        addCheck(new AimJ());
        addCheck(new AimK());
        addCheck(new AimL());
        addCheck(new AimM());
        addCheck(new AimN());
        addCheck(new AimO());
        addCheck(new AimP());
        addCheck(new AimQ());
        addCheck(new AimR());
        addCheck(new AimR1());
        addCheck(new AimS());
        addCheck(new AimT());

        //new experimental
        addCheck(new AimU());
        addCheck(new AimV());
        addCheck(new AimW());
        addCheck(new AimX());
        addCheck(new AimY());
        addCheck(new AimZ());
        addCheck(new AimA1());
        addCheck(new AimB1());


        addCheck(new EntityA());

        addCheck(new KillAuraA());
        addCheck(new KillAuraB());
        addCheck(new KillAuraC());
        addCheck(new KillAuraD());
        addCheck(new KillAuraE());
        addCheck(new KillAuraF());
        addCheck(new KillAuraG());
        addCheck(new KillAuraH());
        addCheck(new KillAuraI());
        addCheck(new KillAuraJ());
        addCheck(new KillAuraK());
        addCheck(new KillAuraL());
        addCheck(new KillAuraM());
        addCheck(new KillAuraN());
        addCheck(new KillAuraO());
        addCheck(new KillAuraP());
        addCheck(new KillAuraQ());


        addCheck(new HitBoxA());
        addCheck(new HitBoxB());

        addCheck(new ReachA());

        addCheck(new ThrowPotA());


        addCheck(new VelocityA());
        addCheck(new VelocityB());
        addCheck(new VelocityC());
        addCheck(new VelocityC1());
        addCheck(new VelocityD());
        addCheck(new VelocityE());

        addCheck(new WTapA());
        addCheck(new WTapB());
        addCheck(new WTapC());
        addCheck(new WTapD());
        addCheck(new WTapE());
        addCheck(new WTapF());
        addCheck(new WTapG());

        //movement
        addCheck(new FlyA());
        addCheck(new FlyB());

        addCheck(new FreezeA());

        addCheck(new InvalidMoveA());
        addCheck(new InvalidMoveB());
        addCheck(new InvalidMoveC());
        addCheck(new InvalidMoveD());
        addCheck(new InvalidMoveE());
        addCheck(new InvalidMoveF());
        // addCheck(new InvalidMoveG());
        addCheck(new InvalidMoveH());
        addCheck(new InvalidMoveI());
        addCheck(new InvalidMoveJ());
        addCheck(new InvalidMoveK());
        addCheck(new InvalidMoveL());
        addCheck(new InvalidMoveM());
        addCheck(new InvalidMoveN());
        addCheck(new InvalidMoveO());
        addCheck(new InvalidMoveP());
        addCheck(new InvalidMoveQ());
        addCheck(new InvalidMoveR());
        addCheck(new InvalidMoveS());
        addCheck(new InvalidMoveT());
        addCheck(new InvalidMoveU());

        addCheck(new NoSlowA());

        addCheck(new SpeedA());
        addCheck(new SpeedB());
        addCheck(new SpeedC());

        addCheck(new ScaffoldA());
        addCheck(new ScaffoldB());
        addCheck(new ScaffoldC());
        addCheck(new ScaffoldD());
        addCheck(new ScaffoldE());
        addCheck(new ScaffoldF());
        addCheck(new ScaffoldG());
        addCheck(new ScaffoldH());
        addCheck(new ScaffoldI());
        addCheck(new ScaffoldJ());
        addCheck(new ScaffoldK());
        addCheck(new ScaffoldL());
        addCheck(new ScaffoldM());
        addCheck(new ScaffoldN());

        //misc
        addCheck(new BadPacketsA());
        addCheck(new BadPacketsB());
        addCheck(new BadPacketsC());
        addCheck(new BadPacketsD());
        addCheck(new BadPacketsE());
        addCheck(new BadPacketsF());
        addCheck(new BadPacketsG());
        addCheck(new BadPacketsH());
        addCheck(new BadPacketsI());
        addCheck(new BadPacketsJ());
        addCheck(new BadPacketsK());
        addCheck(new BadPacketsL());
        addCheck(new BadPacketsM());
        addCheck(new BadPacketsN());
        addCheck(new BadPacketsO());
        addCheck(new BadPacketsP());
        addCheck(new BadPacketsQ());
        addCheck(new BadPacketsR());
        addCheck(new BadPacketsS());
        addCheck(new BadPacketsT());
        addCheck(new BadPacketsU());
        addCheck(new BadPacketsV());
        addCheck(new BadPacketsW());
        addCheck(new BadPacketsX());
        addCheck(new BadPacketsY());
        addCheck(new BadPacketsZ());

        addCheck(new BadPacketsB1());
        addCheck(new BadPacketsD1());
        addCheck(new BadPacketsE1());
        addCheck(new BadPacketsF1());
        addCheck(new BadPacketsG1());
        addCheck(new BadPacketsJ1());
        addCheck(new BadPacketsK1());

//        addCheck(new GhostHandA());

        addCheck(new InventoryA());
        addCheck(new InventoryB());
        addCheck(new InventoryC());
        addCheck(new InventoryD());
        addCheck(new InventoryE());
        addCheck(new InventoryF());
        addCheck(new InventoryG());

        // Refill checkers!
        addCheck(new InventoryH());
        addCheck(new InventoryI());
        addCheck(new InventoryJ());
        addCheck(new InventoryK());

        addCheck(new ConnectionA());
        addCheck(new ConnectionB());
        addCheck(new ConnectionC());
        addCheck(new ConnectionD());
        addCheck(new ConnectionE());
        addCheck(new ConnectionF());

        //TODO: test more
        addCheck(new ConnectionG());
        addCheck(new ConnectionH());
        addCheck(new ConnectionI());
        addCheck(new ConnectionJ());
        addCheck(new ConnectionL());
        addCheck(new ConnectionR());
        addCheck(new ConnectionK());

        addCheck(new PingSpoofA());

        addCheck(new TimerA());
        addCheck(new TimerC());

        // register bedrock checks!
        if (Anticheat.INSTANCE.getConfigValues().isBedrockSupport()) {
            addCheck(new GeyserPacketsA());

            addCheck(new GeyserFlyA());

            addCheck(new GeyserSpeedA());
            addCheck(new GeyserSpeedB());
        }

        this.setupCheck();
    }


    public void saveChecks() {
        Anticheat.INSTANCE.getExecutorService().execute(() -> {
            ChecksFile.getInstance().setup(Anticheat.INSTANCE.getPlugin());

            this.sortChecksAlphabetically().forEach((check) -> {
                String checkName = check.getFriendlyName();

                ChecksFile.getInstance().getData().set("Check." + checkName + ".enabled", check.isEnabled());

                ChecksFile.getInstance().getData().set("Check." + checkName + ".name", check.getReadingCheckName());
                ChecksFile.getInstance().getData().set("Check." + checkName + ".type", check.getReadingCheckType());

                ChecksFile.getInstance().getData().set("Check." + checkName + ".violation.punishable",
                        check.isPunishable());
                ChecksFile.getInstance().getData().set("Check." + checkName
                        + ".violation.max", check.getPunishmentVL());

                ChecksFile.getInstance().getData().set("Check." + checkName + ".punish.command",
                        check.getCommand());

                Anticheat.INSTANCE.getUserManager().getUserMap().values().stream().filter(user -> !user.getUuid()
                        .toString().equalsIgnoreCase(this.data.getUuid().toString())).forEach(user -> {
                    Optional<Check> foundCheck = this.sortChecksAlphabetically().stream().filter(c ->
                            c.getFriendlyName().equalsIgnoreCase(check.getFriendlyName())).findFirst();

                    foundCheck.ifPresent(c -> {
                        c.setEnabled(check.isEnabled());
                        c.setPunishable(check.isPunishable());
                        c.setPunishmentVL(check.getPunishmentVL());
                        c.setCommand(check.getCommand());
                        c.setReadingCheckName(check.getReadingCheckName());
                        c.setReadingCheckType(check.getReadingCheckType());
                    });
                });

                // update cached checks to match gui & checks.yml file.
                Anticheat.INSTANCE.getUserManager().getUserMap().values().stream().filter(user -> !user.getUuid()
                        .toString().equalsIgnoreCase(this.data.getUuid().toString())).forEach(user -> {
                    Optional<Check> foundCheck = getData().getCachedChecks().stream().filter(c ->
                            c.getFriendlyName().equalsIgnoreCase(check.getFriendlyName())).findFirst();

                    foundCheck.ifPresent(c -> {
                        c.setEnabled(check.isEnabled());
                        c.setPunishable(check.isPunishable());
                        c.setPunishmentVL(check.getPunishmentVL());
                        c.setCommand(check.getCommand());
                        c.setReadingCheckName(check.getReadingCheckName());
                        c.setReadingCheckType(check.getReadingCheckType());
                    });
                });
            });

            ChecksFile.getInstance().saveData();
        });
    }


    private void setupCheck() {
        Anticheat.INSTANCE.getExecutorService().execute(() -> {
            ChecksFile.getInstance().setup(Anticheat.INSTANCE.getPlugin());

            // if the checks.yml doesn't exist or a check doesn't exist in the data file
            // put it there
            if (!CHECK_CACHE) {
                CHECK_CACHE = true;

                this.sortChecksAlphabetically().forEach((check) -> {
                    String checkName = check.getFriendlyName();

                    if (check.getClass().isAnnotationPresent(CheckInformation.class)) {
                        CheckInformation checkInformation = check.getClass().getAnnotation(CheckInformation.class);

                        if (check.getCheckType().equalsIgnoreCase("a")
                                && check.getCheckName().equalsIgnoreCase("reach")) {

                            String hitPath = String.format("Check.%s.cancelHits", check.getFriendlyName());

                            if (!ChecksFile.getInstance().getData().contains(hitPath)) {
                                ChecksFile.getInstance().getData().set(hitPath, false);
                            }
                        }

                        if (check.getCheckType().equalsIgnoreCase("a")
                                && check.getCheckName().equalsIgnoreCase("hitbox")) {

                            String hitPath = String.format("Check.%s.cancelHits", check.getFriendlyName());

                            if (!ChecksFile.getInstance().getData().contains(hitPath)) {
                                ChecksFile.getInstance().getData().set(hitPath, true);
                            }
                        }


                        if (!ChecksFile.getInstance().getData().contains("Check." + checkName + ".enabled")) {

                            ChecksFile.getInstance().getData().set("Check." + checkName + ".enabled",
                                    checkInformation.enabled());

                            ChecksFile.getInstance().getData().set("Check." + checkName + ".violation.punishable",
                                    checkInformation.punishable());

                            ChecksFile.getInstance().getData().set("Check." + checkName
                                    + ".violation.max", checkInformation.punishmentVL());

                            ChecksFile.getInstance().getData().set("Check." + checkName + ".punish.command",
                                    check.getCommand());
                        }

                        if (!ChecksFile.getInstance().getData().contains("Check." + checkName + ".name")) {
                            ChecksFile.getInstance().getData().set("Check." + checkName + ".name",
                                    checkInformation.name());
                        }

                        if (!ChecksFile.getInstance().getData().contains("Check." + checkName + ".type")) {
                            ChecksFile.getInstance().getData().set("Check." + checkName + ".type",
                                    checkInformation.subName());
                        }
                    }
                });

                ChecksFile.getInstance().saveData();
            }

            // load checks from checks.yml
            for (String s : ChecksFile.getInstance().getData().getKeys(true)) {
                if (s.contains(".")) {
                    String[] split = s.split("\\.");

                    if (split.length >= 3) {
                        String checkName = split[1];

                        boolean enabled = ChecksFile.getInstance().getData()
                                .getBoolean("Check." + checkName + ".enabled");

                        String name = ChecksFile.getInstance().getData().getString("Check." + checkName
                                + ".name");

                        String type = ChecksFile.getInstance().getData().getString("Check." + checkName
                                + ".type");

                        boolean bans = ChecksFile.getInstance().getData()
                                .getBoolean("Check." + checkName + ".violation.punishable");

                        double banVL = ChecksFile.getInstance().getData().getDouble("Check." + checkName
                                + ".violation.max");

                        String command = ChecksFile.getInstance().getData()
                                .getString("Check." + checkName + ".punish.command");

                        // ensure check exists in current data
                        Optional<Check> checkOptional = this.sortChecksAlphabetically().stream()
                                .filter(check -> check.getFriendlyName()
                                .equalsIgnoreCase(checkName)).findFirst();

                        checkOptional.ifPresent(check -> {
                            check.setEnabled(enabled);
                            check.setPunishable(bans);
                            check.setPunishmentVL(banVL);
                            check.setCommand(command);

                            check.setReadingCheckName(name);
                            check.setReadingCheckType(type);
                        });
                    }
                }
            }
        });


        // Setup checks.yml stuff for certain checks later or issues happen cuz gay (not the best but works)
        RunUtils.taskLaterAsync(() -> {
            if (getData().getCheckManager() != null) {

                HitBoxA hitBoxA = (HitBoxA) getData()
                        .getCheckManager().forClass(HitBoxA.class);

                if (hitBoxA != null) {
                    hitBoxA.cancelHits = ChecksFile.getInstance()
                            .getData().getBoolean(hitBoxA.cancelPath);
                }

                ReachA reachA = (ReachA) getData()
                        .getCheckManager().forClass(ReachA.class);

                if (reachA != null) {
                    reachA.cancelHits = ChecksFile.getInstance()
                            .getData().getBoolean(reachA.cancelPath);
                }

            }
        }, 2000L);
    }

    public void addCheck(Check check) {
        check.setEnabled(false);
        check.setData(this.data);
        this.checks.put(check.getClass(), check);
    }

    public void reloadAnticheat() {

        ReachA reachA = (ReachA) getData()
                .getCheckManager().forClass(ReachA.class);

        if (reachA != null) {
            reachA.cancelHits = ChecksFile.getInstance()
                    .getData().getBoolean(reachA.cancelPath);
        }

        HitBoxA hitBoxA = (HitBoxA) getData()
                .getCheckManager().forClass(HitBoxA.class);

        if (hitBoxA != null) {
            hitBoxA.cancelHits = ChecksFile.getInstance()
                    .getData().getBoolean(hitBoxA.cancelPath);
        }

        ChecksFile.getInstance().setup(Anticheat.INSTANCE.getPlugin());

        // load checks from checks.yml
        for (String s : ChecksFile.getInstance().getData().getKeys(true)) {
            if (s.contains(".")) {
                String[] split = s.split("\\.");

                if (split.length >= 3) {
                    String checkName = split[1];

                    boolean enabled = ChecksFile.getInstance().getData()
                            .getBoolean("Check." + checkName + ".enabled");


                    String name = ChecksFile.getInstance().getData().getString("Check." + checkName
                            + ".name");

                    String type = ChecksFile.getInstance().getData().getString("Check." + checkName
                            + ".type");

                    boolean bans = ChecksFile.getInstance().getData()
                            .getBoolean("Check." + checkName + ".violation.punishable");

                    double banVL = ChecksFile.getInstance().getData().getDouble("Check." + checkName
                            + ".violation.max");

                    String command = ChecksFile.getInstance().getData()
                            .getString("Check." + checkName + ".punish.command");

                    // ensure check exists in current data
                    Optional<Check> checkOptional = this.sortChecksAlphabetically().stream()
                            .filter(check -> check.getFriendlyName()
                            .equalsIgnoreCase(checkName)).findFirst();

                    checkOptional.ifPresent(check -> {
                        check.setEnabled(enabled);
                        check.setPunishable(bans);
                        check.setPunishmentVL(banVL);
                        check.setCommand(command);

                        check.setReadingCheckName(name);
                        check.setReadingCheckType(type);
                    });
                }
            }
        }
    }

    public List<Check> sortChecksAlphabetically() {

        if (this.cachedSorted == null) {
            List<Check> sorted = new ArrayList<>();

            this.checks.forEach((aClass, check) -> sorted.add(check));
            sorted.sort(Comparator.comparing(Check::getFriendlyName));

            this.cachedSorted = sorted;
        }

        return this.cachedSorted;
    }

    public <T extends Check> Check forClass(Class<T> aClass) {
        return checks.get(aClass);
    }
}