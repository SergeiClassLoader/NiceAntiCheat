package pro.cyrent.load;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.util.auth.StringUtils;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @date 4/20/2024
 * @author Moose1301
 */
public class LoaderHook implements Hook{


    public void enablePlugin(JavaPlugin plugin) {
        if(System.getProperty(
                StringUtils.decode(
                        "q6gHZ4w1nBlorE8B1aPDvTuFSt0ieQJXOU/ElT9JVohKwmsl1i/dGA==",
                        "3NNXPOR1K71M13EZ6I9BOJFYT9FGCEGIOSUQU5RRJ1LIGZ3A7SBC3HKARKV11Z9LKDITKXHBTOPUNH4JT4TDBD4LP1ETUC1KI1JT9YEN46X89GDBLH32K9M6NOIYC4AM"
                )) == null) {
            return;
        }



        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false);
        PacketEvents.getAPI().load();

        Anticheat.INSTANCE.listenersToRegister(consumer -> consumer.forEach(listener ->
                Bukkit.getServer().getPluginManager().registerEvents(listener, plugin)));

        Anticheat.INSTANCE.enable(plugin);

        PacketEvents.getAPI().init();
    }

    public void disablePlugin(JavaPlugin plugin) {
        PacketEvents.getAPI().terminate();
        Anticheat.INSTANCE.disable(plugin);

    }
}
