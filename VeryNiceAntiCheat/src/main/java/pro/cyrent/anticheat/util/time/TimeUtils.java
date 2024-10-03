package pro.cyrent.anticheat.util.time;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtils {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public static long currentMS() {
        return System.currentTimeMillis();
    }

    public static boolean Passed(long from, long required) {
        return System.currentTimeMillis() - from > required;
    }

    public static boolean elapsed(long time, long needed) {
        return Math.abs(System.currentTimeMillis() - time) >= needed;
    }

    public static String getDate() {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return format.format(now);
    }

    public static String getDataLight() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return now.format(formatter);
    }


    public static long remainder(long start, long required) {
        return required + start - System.currentTimeMillis();
    }

    public static long elapsed(long time) {
        return System.currentTimeMillis() - time;
    }

    public static long secondsFromLong(long time) {
        long now = System.currentTimeMillis();
        long date = now - time;
        return date / 1000 % 60;
    }

    public static String getSystemTime2() {
        return simpleDateFormat2.format(new Date(System.currentTimeMillis()));
    }

    public static long secondsFromLong(long time, long now) {
        return (now - time) / 1000 % 60;
    }

    public static String getSystemTime() {
        String out;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        out = formatter.format(date);
        return out;
    }


    public static boolean passed(long from, long required) {
        return System.currentTimeMillis() - from > required;
    }

    public static boolean passed(long from, long required, long ms) {
        return ms - from > required;
    }


}
