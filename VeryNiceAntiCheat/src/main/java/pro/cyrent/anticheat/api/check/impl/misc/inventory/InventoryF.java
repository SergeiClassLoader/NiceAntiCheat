package pro.cyrent.anticheat.api.check.impl.misc.inventory;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Inventory",
        subName = "F",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.INVENTORY,
        description = "Detects AutoSoup",
        state = CheckState.RELEASE)
public class InventoryF extends Check {

    private int lastSentItemSlot;
    private int slot = -1;

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            this.lastSentItemSlot -= Math.min(this.lastSentItemSlot, 1);
        }

        if (event.isPlace()) {
            WrapperPlayClientPlayerBlockPlacement packet =
                    new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

            if (this.lastSentItemSlot > 0 && this.slot != -1) {

                ItemStack itemInHand = getItemInHand(getData().getPlayer(), this.slot);

                if (itemInHand == null || !packet.getItemStack().isPresent()) return;

                Material materialItemStack = null;

                if (packet.getItemStack().isPresent()) {
                    String test = packet.getItemStack().get().getType().getName().toString();

                    //TODO: heavy method.
                    Material materialFound = Material.matchMaterial(test);

                    if (materialFound != null) {
                        materialItemStack = materialFound;
                    }
                }

                if ((itemInHand.getType() == Material.MUSHROOM_SOUP
                        || itemInHand.getType()  == Material.BOWL)
                        && materialItemStack != itemInHand.getType()) {
                    if (this.threshold++ > 3.5) {
                        this.fail("threshold=" + threshold);
                        event.getPacketReceiveEvent().setCancelled(true);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .25);
                }
            }
        }

    }

    public static ItemStack getItemInHand(Player player, int slotNumber) {
        if (slotNumber < 0 || slotNumber >= player.getInventory().getSize()) {
            // Slot number is out of range
            return null;
        }

        return player.getInventory().getItem(slotNumber);
    }
}
