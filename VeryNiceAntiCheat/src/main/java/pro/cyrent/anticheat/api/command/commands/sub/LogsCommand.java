package pro.cyrent.anticheat.api.command.commands.sub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.database.api.InputData;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.auth.HTTPUtil;
import pro.cyrent.anticheat.util.json.JSONArray;
import pro.cyrent.anticheat.util.json.JSONObject;
import pro.cyrent.anticheat.util.paste.PasteUtil;
import pro.cyrent.anticheat.util.time.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogsCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (!(commandSender instanceof Player)) {
            return;
        }

        if (args.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "/" + Anticheat.INSTANCE.getConfigValues().getCommandName() +
                    " logs (player) (total/session)");
            return;
        }

        String playerName = args[1];

        if (playerName == null) return;

        if (playerName.length() < 1) {
            commandSender.sendMessage(ChatColor.RED + "Please specify a players name!");
            return;
        }
        String type = "total";
        if (args.length >= 3) {
            type = args[2];
        }

        PlayerData playerData = Anticheat.INSTANCE.getUserManager().getUser((Player) commandSender);

        if (playerData == null) return;
        if (Anticheat.INSTANCE.getConfigValues().isOwnMongo()) {
            this.getLogMongo(commandSender, args, playerName);
        } else {
            getPlayerLogs((Player) commandSender, playerName, type);
        }


    }

    public void getLogMongo(CommandSender commandSender, String[] args, String playerName) {

        Map<String, Double> integerMap = new HashMap<>();

        new Thread(() -> {
            List<InputData> logs = Anticheat.INSTANCE.getDatabaseManager().getMongoManager()
                    .getLogs(playerName);

            if (logs.isEmpty()) {
                commandSender.sendMessage(ChatColor.GRAY + playerName + " " + ChatColor.RED + "has no logs.");
                return;
            }
            StringBuilder logBuilder = new StringBuilder();

            // logs
            commandSender.sendMessage("\n" + Anticheat.INSTANCE.getConfigValues().getPrefix()
                    + ChatColor.GREEN + " All violations for "
                    + ChatColor.GRAY + playerName + ChatColor.GRAY + " (" +
                    ChatColor.RED + logs.size() + ChatColor.GRAY + ")");

            String beginning = "\n\nPaste generated for: " + playerName
                    + ", Requested at: " + TimeUtils.getSystemTime()
                    + " by " + commandSender.getName()
                    + "(" + ((Player) commandSender).getUniqueId().toString() + ")"
                    + "\n\n";


            logBuilder.append(beginning);

            logs.forEach(inputData -> {
                String name = inputData.getCheckName() + " " + inputData.getCheckType();
                integerMap.put(name, integerMap.getOrDefault(name, 0.0)
                        + (inputData.isExperimental() ? .5 : 1.0));

                String checkData = inputData.getCheckData();

                String fixedData = checkData.replaceAll("\\*\\*\\*\\[EXTRA DEBUG\\]\\*\\*\\*", "");

                // Use a regular expression to extract the part that matches the pattern
                Pattern pattern = Pattern.compile("\\*\\*\\*\\[EXTRA DEBUG\\]\\*\\*\\*(.*?)\\*\\*\\*\\[DATE\\]\\*\\*\\*");
                Matcher matcher = pattern.matcher(checkData);

                String extraDebug = "null";

                if (matcher.find()) {
                    extraDebug = matcher.group(1);
                }

                String fullData = "\nCheck: " + inputData.getCheckName()
                        + " (" + inputData.getCheckType() + ")"
                        + " Violation: " + inputData.getViolation()
                        + " Experimental: " + inputData.isExperimental()
                        + " \nCheckData: " + fixedData
                        + "\nEXTRA-DEBUG: " + extraDebug + "\n";

                logBuilder.append(fullData);

            });

            integerMap.forEach((s1, integer) -> commandSender.sendMessage(ChatColor.GRAY
                    + " - " + ChatColor.RED
                    + s1 + ChatColor.DARK_GRAY + " x" + ChatColor.GRAY + integer));


            Anticheat.INSTANCE.getExecutorService().execute(() -> {


                String paste = PasteUtil.createCustom(logBuilder.toString());

                if (paste != null) {
                    commandSender.sendMessage(ChatColor.GREEN + "Paste Link: " + ChatColor.GRAY + paste);
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Unable to paste? [Error]");
                }


            });

            integerMap.clear();
            logs.clear();

        }).start();
    }

    private static List<WebResult> fetchTotalLogs(Player sender, String playerName) {
        Map<String, String> headers = new HashMap<>();
        headers.put("player", playerName);
        headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
        headers.put("mode", "VIEW_LOGS");
        headers.put("executor", sender.getUniqueId().toString());
        String result = HTTPUtil.getResponse("https://backend.antiskid.club/", headers);

        if (result == null) {
            sender.sendMessage(ChatColor.RED + "There was an error contacting the logs database.");
            return null;
        }
        JSONArray jsonArray = new JSONArray(result);
        List<WebResult> results = new ArrayList<>();
        jsonArray.forEach(object -> {
            if (object instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) object;

                if (jsonObject.has("checkName") && jsonObject.has("checkViolation")
                        && jsonObject.has("checkType")) {
                    String name = jsonObject.getString("checkName");
                    String type = jsonObject.getString("checkType");
                    int violation = jsonObject.getInt("checkViolation");
                    boolean experimental = jsonObject.getBoolean("experimental");
                    String licensing = jsonObject.getString("license");
                    String checkData = jsonObject.getString("checkData");

                    if (licensing.equalsIgnoreCase(Anticheat.INSTANCE.getLicense())) {
                        results.add(
                                new WebResult(name, type, violation, experimental, licensing, checkData)
                        );
                    }
                }
            }
        });
        return results;
    }

    private static List<WebResult> fetchSessionLogs(Player sender, String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + playerName + " is not online.");
            return null;
        }
        PlayerData playerData = Anticheat.INSTANCE.getUserManager().getUser(player);
        if (playerData == null) {
            sender.sendMessage(ChatColor.RED + playerName + " is not online.");
            return null;
        }
        return new ArrayList<>(playerData.getSessionLogs());
    }

    public static void getPlayerLogs(Player commandSender, String playerName, String type) {


        Map<String, Double> integerMap = new HashMap<>();

        new Thread(() -> {
            commandSender.sendMessage("");
            commandSender.sendMessage(ChatColor.AQUA + "Lumos " + ChatColor.YELLOW + "Contacting the database...");
            commandSender.sendMessage("");
            List<WebResult> webResults = (type.equals("total") ? fetchTotalLogs(commandSender, playerName) : fetchSessionLogs(commandSender, playerName));
            if (webResults == null) {
                return;
            }


            if (webResults.isEmpty()) {
                if (type.equalsIgnoreCase("total"))
                    commandSender.sendMessage(ChatColor.GRAY + playerName + " " + ChatColor.RED + "has no logs.");
                else if (type.equalsIgnoreCase("session"))
                    commandSender.sendMessage(ChatColor.GRAY + playerName + " " + ChatColor.RED + "has no logs this session.");
                return;
            }
            StringBuilder logBuilder = new StringBuilder();

            // logs
            commandSender.sendMessage(ChatColor.YELLOW + "Showing " +
                    ChatColor.AQUA + playerName + ChatColor.YELLOW + "'s logs from " + (type.equalsIgnoreCase("total") ? "all the time" : "this session") +
                    ChatColor.AQUA + " (" + webResults.size() + ")");

            String beginning = "\n\nPaste generated for: " + playerName
                    + ", Requested at: " + TimeUtils.getSystemTime()
                    + " by " + commandSender.getName()
                    + "(" + commandSender.getUniqueId().toString() + ")"
                    + "\n\n";
            if (type.equalsIgnoreCase("session")) {
                beginning += "\nThis is a Session Log so it will look differently to a total logs";
            }


            logBuilder.append(beginning);

            webResults.forEach(inputData -> {
                String name = inputData.getName() + " " + inputData.getType();
                integerMap.put(name, integerMap.getOrDefault(name, 0.0)
                        + (inputData.isExperimental() ? .5 : 1.0));

                String checkData = inputData.getCheckData();
                String fixedData = checkData.replaceAll("\\*\\*\\*\\[EXTRA DEBUG\\]\\*\\*\\*\\*(.*$)", "");
                Pattern extraDebugPattern = Pattern.compile("\\*\\*\\*\\[EXTRA DEBUG\\]\\*\\*\\*\\*(.*?)\\*\\*\\*\\[DATE\\]\\*\\*\\*");
                Pattern datePattern = Pattern.compile("\\*\\*\\*\\[DATE\\]\\*\\*\\*(.*$)");
                Matcher extraDebugMatcher = extraDebugPattern.matcher(checkData);
                Matcher dateMatcher = datePattern.matcher(checkData);

                String extraDebug = "null";
                String date = "null";

                if (extraDebugMatcher.find()) {
                    extraDebug = extraDebugMatcher.group(1);
                }
                if (dateMatcher.find()) {
                    date = dateMatcher.group(1);
                }

                String fullData = "\nCheck: " + inputData.getName()
                        + " (" + inputData.getType() + ")"
                        + " Violation: " + inputData.getViolation()
                        + " Experimental: " + inputData.isExperimental()
                        + " \nCheckData: " + fixedData
                        + " \nDate: " + date
                        + "\nEXTRA-DEBUG: " + extraDebug
                        + "\n";

                logBuilder.append(fullData);

            });

            integerMap.forEach((s1, integer) -> commandSender.sendMessage(ChatColor.YELLOW
                    + " - " + ChatColor.AQUA
                    + s1 + ChatColor.YELLOW + " (x" + integer + ")"));

            Anticheat.INSTANCE.getExecutorService().execute(() -> {
                String paste = PasteUtil.createCustom(logBuilder.toString());

                if (paste != null) {
                    commandSender.sendMessage(ChatColor.GREEN + "Paste Link: " + ChatColor.AQUA + paste);
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Unable to paste? [Error]");
                }
            });

            integerMap.clear();
            webResults.clear();
        }).start();
    }

    @Getter
    @AllArgsConstructor
    public static final class WebResult {
        private final String name, type;
        private final int violation;
        private final boolean experimental;
        private final String license, checkData;
    }

    @Getter
    @AllArgsConstructor
    public static final class WebResultTop {
        private final String name, type;
        private final int violation;
        private final boolean experimental;
        private final String uuid, playerName;
        private int size;
    }
}