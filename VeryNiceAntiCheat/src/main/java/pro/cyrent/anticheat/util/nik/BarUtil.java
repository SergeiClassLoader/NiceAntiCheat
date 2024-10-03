package pro.cyrent.anticheat.util.nik;

public class BarUtil {

    public static String generateBar(final double current, final double max) {

        final StringBuilder msg = new StringBuilder("§8[");

        final int percentage = (int) (100 / (max / current));

        final String fill = "§f|";

        for (int i = 0; i <= percentage / 5.5 - 1; i++) msg.append(fill);

        final String empty = "§7|";

        for (int i = 0; i <= (100 - percentage) / 5.5 - 1; i++) msg.append(empty);

        msg.append("§8]");

        return msg.toString();
    }
}
