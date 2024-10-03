package pro.cyrent.anticheat.api.check.impl.combat.throwpot;


import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.inventory.ItemStack;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;

import java.util.LinkedList;
import java.util.List;

@CheckInformation(
        name = "ThrowPot",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.THROWPOT,
        description = "Detects constant & quick throw pots on clients",
        punishable = false,
        experimental = true,
        state = CheckState.DEV)
public class ThrowPotA extends Check {

    private int ticksSinceSwitch, ticksSinceHoldingPotion;
    private final List<Integer> tickSwitchData = new LinkedList<>(), tickSincePlaceData = new LinkedList<>();
    private boolean expectingSwitch = false;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() == null) {
            return;
        }

        if (event.isMovement()) {
            this.ticksSinceHoldingPotion++;
            this.ticksSinceSwitch++;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            if (this.expectingSwitch && this.ticksSinceSwitch < 20 && getData().getPlayer().getInventory() != null) {

                this.tickSwitchData.add(this.ticksSinceSwitch);
                this.tickSincePlaceData.add(this.ticksSinceHoldingPotion);

                if (this.tickSwitchData.size() >= 5 && this.tickSincePlaceData.size() >= 5) {

                    double stdSwitch = StreamUtil.getStandardDeviation(this.tickSwitchData);
                    double stdSincePlace = StreamUtil.getStandardDeviation(this.tickSincePlaceData);

                    int max = StreamUtil.getMaximumInt(this.tickSwitchData);

                    if (stdSwitch < .402 && stdSincePlace < .402 && max <= 2) {
                        this.fail("std-switch="+stdSwitch,
                                "std-since-place="+stdSincePlace);
                    }

                    this.tickSincePlaceData.clear();
                    this.tickSwitchData.clear();
                }

                this.expectingSwitch = false;
            }

            this.ticksSinceSwitch = 0;
        }

        if (event.isPlace()) {
            WrapperPlayClientPlayerBlockPlacement placement =
                    new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

            if (placement.getItemStack().isPresent() && placement.getItemStack().get().getType() != null) {
                ItemStack stack = SpigotReflectionUtil.encodeBukkitItemStack(placement.getItemStack().get());

                if (stack != null && getData().isSplashPotion(stack)) {
                    this.expectingSwitch = true;
                    this.ticksSinceHoldingPotion = 0;
                }
            }
        }
    }
}
