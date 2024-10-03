package pro.cyrent.anticheat.util.banwave;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.util.auth.HTTPUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BanWaveManager {
    @Getter
    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, Long> playerMap = new ConcurrentHashMap<>(1);

    private boolean setValues = false,
            sentMessage = false, started = false, timely = false;

    private Timer timer = new Timer(), timelyTimer = new Timer();

    public void clearBanwave() {
        Map<String, String> headers = new HashMap<>();
        headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
        headers.put("mode", "clear");
        HTTPUtil.getResponse("https://backend.antiskid.club/service/banwave", headers);
        headers.clear();
    }

    public boolean addPlayer(String name) {
        Map<String, String> headers = new HashMap<>();
        headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
        headers.put("mode", "add");
        headers.put("user", name);
        String result = HTTPUtil.getResponse("https://backend.antiskid.club/service/banwave", headers);
        headers.clear();

        return result.equalsIgnoreCase("true");
    }

    public boolean removePlayer(String name) {
        Map<String, String> headers = new HashMap<>();
        headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
        headers.put("mode", "remove");
        headers.put("user", name);
        String result = HTTPUtil.getResponse("https://backend.antiskid.club/service/banwave", headers);
        headers.clear();

        return result.equalsIgnoreCase("true");
    }

    public List<String> getWaveList() {
        Map<String, String> headers = new HashMap<>();
        headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
        headers.put("mode", "list");
        String result = HTTPUtil.getResponse("https://backend.antiskid.club/service/banwave", headers);
        headers.clear();

        return (result.length() > 0 ? new ArrayList<>(Arrays.asList(result.split(","))) : new ArrayList<>());
    }

    public void doCheckUp() {

        if (!Anticheat.INSTANCE.getConfigValues().isBanWaveTimely()) return;

        long toMillis = Anticheat.INSTANCE.getConfigValues().getBanWaveCheckUpTime() * 1000L;

        this.timelyTimer = new Timer();

        this.timelyTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        players = getWaveList();
                        banWave();
                        timely = true;
                    }
                }, 1L, toMillis);
    }

    public void endCheckUp() {
        this.timelyTimer.cancel();
        this.timelyTimer = new Timer();
    }
    private List<String> players;

    public void commenceBanWave(CommandSender commandSender) {
        Bukkit.getScheduler().runTaskAsynchronously(Anticheat.INSTANCE.getPlugin(), () -> {
            players = getWaveList();
            if (players.size() < 1) {
                commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " No players are on the banwave!");
                started = false;
            } else {
                started = true;
                banWave();
            }
        });

    }

    public void banWave() {

        if (!started && players.size() > 0 && timely) {
            this.started = true;
            this.timely = false;
        }

        if (!this.sentMessage && this.started) {
            Bukkit.broadcastMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " The banwave is now commencing!");
            Bukkit.broadcastMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " Amount of players detected: " + players.size());

            this.timer = new Timer();

            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    doForLoop();

                    if (playerMap.size() == 1) {

                        String finalPlayer = "?";
                        long finalLong = 0L;

                        for (Map.Entry<String, Long> pair : playerMap.entrySet()) {
                            finalPlayer = pair.getKey();
                            finalLong = pair.getValue();

                            if (!finalPlayer.equalsIgnoreCase("?")
                                    && finalLong != 0L) {
                                setValues = true;
                            }
                        }

                        if (setValues) {
                            runBan(finalPlayer, finalLong);
                        }
                    }
                }
            }, 0, 1000L);

            this.sentMessage = true;
        }
    }

    public void doForLoop() {

        if (players.size() <= 0) {
            Bukkit.broadcastMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " No players are left on the banwave!");
            this.sentMessage = false;
            this.started = false;
            this.timer.cancel();
            this.timer = new Timer();

        }

        for (String player : players) {
            queuePlayer(player);
        }
    }

    public void stopBanWave(CommandSender commandSender) {
        this.timer.cancel();
        this.sentMessage = false;
        this.started = false;
        commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " The banwave should be stopping!");
    }

    public void clearPlayers() {
        Anticheat.INSTANCE.getExecutorService().execute(() -> clearBanwave());
        this.players.clear();
        this.playerMap.clear();
    }

    public void queuePlayer(String string) {
        if (this.playerMap.size() <= 0) {
            this.playerMap.put(string, System.currentTimeMillis());
        }
    }

    public void runBan(String player, long time) {

        if (this.playerMap.containsKey(player)) {

            long toMillis = Anticheat.INSTANCE.getConfigValues().getBanWaveTime() * 1000L;

            if ((System.currentTimeMillis() - time) >= toMillis) {

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                Anticheat.INSTANCE.getConfigValues()
                                        .getPunishCommand()
                                        .replace("%PLAYER%", player)
                                        .replace("%PREFIX%", Anticheat.INSTANCE.getConfigValues().getPrefix())
                                        .replaceFirst("/", ""));

                        if (Anticheat.INSTANCE.getConfigValues().isAnnounce()) {
                            for (String message : Anticheat.INSTANCE.getConfigValues().getAnnounceMessage()) {
                                Bukkit.broadcastMessage(Anticheat.INSTANCE.getConfigLoader().convertColor(
                                        message.replace("%LINE%", " ")
                                                .replace("%PLAYER%", player)
                                                .replace("%PREFIX%", Anticheat.INSTANCE.getConfigValues().getPrefix())));
                            }
                        }

                        removePlayer(player);
                        players.remove(player);
                        playerMap.clear();
                    }
                }.runTask(Anticheat.INSTANCE.getPlugin());
            }
        }
    }
}
