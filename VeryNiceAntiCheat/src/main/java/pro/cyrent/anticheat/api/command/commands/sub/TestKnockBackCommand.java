package pro.cyrent.anticheat.api.command.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.math.MathUtil;

import java.util.Arrays;
import java.util.List;

public class TestKnockBackCommand {

    private final List<Double> velocities = Arrays.asList(0.475, 0.42, 0.4, .38, .33);

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (commandSender instanceof Player) {
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));


            try {
                if (user == null) {
                    commandSender.sendMessage("If you see this message contact moose1301");
                    return;
                }

                if (args.length < 2) {
                    commandSender.sendMessage(ChatColor.RED + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName()
                            + " testkb (player)");
                    return;
                }
                String targetName = args[1];

                if (targetName.isEmpty()) {
                    commandSender.sendMessage("Please enter a valid username.");
                    return;
                }
                PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
                if (target == null) {
                    commandSender.sendMessage(ChatColor.RED + "[Error!] The player your trying to ban is offline, " +
                            "please try another name.");
                    return;
                }
                commandSender.sendMessage(ChatColor.GREEN
                        + "Now applying velocity to the player: " + targetName);


                float max = 0.535f;
                float x = (float) MathUtil.round(max - MathUtil.getRandomFloat(
                        0.05f, 0.245f), 4);
                float z = (float) MathUtil.round(max - MathUtil.getRandomFloat(
                        0.05f, 0.245f), 4);

                // make it harder for there to be abnormal velocities
                if (x + z > max) {
                    x -= 0.1f;
                    z -= 0.1f;
                } else {
                    float temp = max - x - z;

                    temp /= 2;

                    x += temp;
                    z += temp;
                }

                // add slightly more randomization
                if (Math.random() < 0.5) {
                    x = -x;
                } else {
                    z = -z;
                }

                // both being 0 has happened, this will fix hopefully
                if (x == 0) {
                    x = MathUtil.getRandomFloat(-0.2f, 0.2f);
                }

                if (z == 0) {
                    z = MathUtil.getRandomFloat(-0.2f, 0.2f);
                }

                double y = MathUtil.selectRandomNumber(this.velocities);

                target.getPlayer().setVelocity(new Vector(x, y, z));

                if (args.length >= 3) {
                    if (args[2] != null && args[2].equalsIgnoreCase("silent")) {
                        return;
                    }
                }

                target.getPlayer().damage(0);


            } catch (NullPointerException ignored) {

            }
        } else {

            try {
                if (args.length < 2) {
                    commandSender.sendMessage(ChatColor.RED + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName() + " testkb (player)");
                    return;
                }
                String targetName = args[1];

                if (targetName.isEmpty()) {
                    commandSender.sendMessage("Please enter a valid username.");
                    return;
                }
                PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
                if (target == null) {
                    commandSender.sendMessage(ChatColor.RED + "[Error!] The player your trying to ban is offline, " +
                                    "please try another name.");
                    return;
                }
                commandSender.sendMessage(ChatColor.GREEN
                                + "Now applying velocity to the player: " + targetName);
                target.getPlayer().setVelocity(new Vector(0.12, .42f, 0.08));


            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
    }
}
