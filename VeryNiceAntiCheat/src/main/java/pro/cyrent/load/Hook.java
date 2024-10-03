package pro.cyrent.load;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @date 4/20/2024
 * @author Moose1301
 */
public interface Hook  {
    public void enablePlugin(JavaPlugin plugin);
    public void disablePlugin(JavaPlugin plugin);
}
