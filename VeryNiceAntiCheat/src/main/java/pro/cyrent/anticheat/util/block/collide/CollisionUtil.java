package pro.cyrent.anticheat.util.block.collide;

import org.bukkit.Material;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.location.FlyingLocation;

public class CollisionUtil {
    private static final double[] WALL_MODULOS = {

            // Full Blocks
            .699999988079071D,
            .30000001192092896D,

            // Glass Panes
            .13749998807907104D,
            .862500011920929D,

             // Cobblestone Walls
            .050000011920928955D,
            .949999988079071D,
            .012499988079071045D,
            .987500011920929D,

            // Fences
            .07499998807907104D,
            .925000011920929D,

            // Chests
            .23750001192092896D,
            .762499988079071D,

            // Heads
            .19999998807907104D,
            .800000011920929D,

            // Chains
            .10624998807907104D,
            .893750011920929D,

            // Bamboo
            .9895833283662796D,
            .35624998807907104D,
            .7770833522081375D,
            .14375001192092896D,

            // Anvils
            .824999988079071D,
            .17500001192092896D,
            .11250001192092896D,
            .887499988079071D
    };


    public static boolean isNearWall(FlyingLocation location) {

        double x = Math.abs(location.getPosX() % 1);
        double z = Math.abs(location.getPosZ() % 1);

        for (double modulo : WALL_MODULOS) {

            double moduloX = Math.abs(x - modulo);
            double moduloZ = Math.abs(z - modulo);

            /*
             * This is the correct amount we need to check for
             * since this accounts for all the collision changes.
             */

            if (moduloX < 1.706E-13 || moduloZ < 1.706E-13) return true;
        }

        return false;
    }

    private static final double[] EDGE_MODULOS = {
            0.7D,
            0.3D
    };

    public static boolean isNearEdge(FlyingLocation location) {

        double x = Math.abs(location.getPosX() % 1);
        double z = Math.abs(location.getPosZ() % 1);

        for (double modulo : EDGE_MODULOS) {

            double moduloX = Math.abs(x - modulo);
            double moduloZ = Math.abs(z - modulo);

            /*
             * This is the correct amount we need to check for
             * since this accounts for all the collision changes.
             */

            if (moduloX < 0.03D || moduloZ < 0.03D) return true;
        }

        return false;
    }

    public static boolean isNearNewEdge(PlayerData data, FlyingLocation location, FlyingLocation previous) {
        double x = Math.abs(location.getPosX() % 1);
        double z = Math.abs(location.getPosX() % 1);

        double xFrom = Math.abs(previous.getPosX() % 1);
        double zFrom = Math.abs(previous.getPosX() % 1);

        double offsetX = Math.abs(xFrom - x);
        double offsetZ = Math.abs(zFrom - z);

        if (offsetX == 0 || offsetZ == 0) {
            data.setSneakCollideTicks(data.getSneakCollideTicks() + 5);
        } else {
            data.setSneakCollideTicks(data.getSneakCollideTicks() - 1);
        }

        Material material = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(data.getPlayer().getWorld(),
                (int) location.getPosX(), location.getPosY() - .5, (int) location.getPosZ());

        if (material == Material.AIR) {
            data.setAirBelowSneakTicks(60);
        } else {
            data.setAirBelowSneakTicks(data.getAirBelowSneakTicks() - 2);
        }

        return data.getSneakCollideTicks() > 6 && data.getAirBelowSneakTicks() > 0;
    }
}