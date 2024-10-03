package pro.cyrent.load;

import pro.cyrent.anticheat.Anticheat;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@Setter
public class PluginHook extends JavaPlugin {

    private final boolean dev = true;

    @Override
    public void onEnable() {

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false);
        PacketEvents.getAPI().load();

        Anticheat.INSTANCE.listenersToRegister(consumer -> consumer.forEach(listener ->
                getServer().getPluginManager().registerEvents(listener, this)));

        Anticheat.INSTANCE.enable(this);

        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        Anticheat.INSTANCE.disable(this);
    }
}
