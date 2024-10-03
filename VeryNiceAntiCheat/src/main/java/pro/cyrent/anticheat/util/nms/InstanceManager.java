package pro.cyrent.anticheat.util.nms;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.util.nms.instances.Instance1_8_R3;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class InstanceManager {
    private Instance instance;
    public final String version = Bukkit.getServer().getClass().getPackage().getName()
            .replace(".", ",").split(",")[3];


    public void create() {
        /**
         *  FOR LUNA
         */

        // TODO: if you want 1.20 support delete Instance1_8_R3 and uncomment Instance1_20_R3 code
        //  and import that into here, you may need to update it to work with latest minecraft
        //  as the code is very unfinished for it.
        //  also in the Collision Processor you will have to update the block data for the newer spigot
        //  this also includes changing the API number inside the plugin.yml.


        // TODO: fork this and remove instance 1.8 and replace with Instance1_20_R4 (adding later)
        if (version.equalsIgnoreCase("v1_8_R3")) {
            this.instance = new Instance1_8_R3();
            Anticheat.INSTANCE.serverVersion = 18;
        } else {
            this.instance = null;
        }
    }
}