package pro.cyrent.anticheat.impl.processor.basic;


import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientResourcePackStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResourcePackSend;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.Check;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.Locale;

@Getter
@Setter
public class CustomPayloadProcessor extends Event {
    private final PlayerData data;

    private BrandTypes currentBrandType = BrandTypes.NONE;

    private boolean doStep = false;

    private String channel = "(not set)", brand = "(not set)";

    public CustomPayloadProcessor(PlayerData user) {
        this.data = user;
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
                WrapperPlayClientPluginMessage pluginMessage =
                        new WrapperPlayClientPluginMessage(event.getPacketReceiveEvent());

                String test = new String(pluginMessage.getData());

                if (brand.equals("(not set)") && pluginMessage.getChannelName().equals("MC|Brand")) {
                    this.brand = test.substring(1);
                }

                if (!this.doStep) {
                    this.channel = pluginMessage.getChannelName();
                }

                if (this.brand.equals("vanilla")) {
                    this.currentBrandType = BrandTypes.Vanilla;
                    this.doStep = true;
                }

                if (this.brand.equals("fabric")) {
                    this.currentBrandType = BrandTypes.Fabric;
                    this.doStep = true;
                }

                if (this.brand.equals("fml,forge")
                        || this.channel.equals("FML|HS")
                        || this.channel.equals("l:fmlhs")
                        || this.channel.equals("fml:handshake")
                        || this.brand.equals("fml:handshake")
                        || this.brand.equals("FML|HS")
                        || this.brand.equals("l:fmlhs")) {
                    this.currentBrandType = BrandTypes.Forge;
                    this.doStep = true;
                }

                if (this.brand.contains("lunarclient:")) {
                    this.currentBrandType = BrandTypes.Lunar;
                    this.doStep = true;
                }

                if (this.brand.toLowerCase(Locale.ROOT).contains("salwyrr")) {
                    this.currentBrandType = BrandTypes.Sawyer;
                    this.doStep = true;
                }

                if (this.brand.equals("CB-Client") || this.channel.equals("CB-Client")) {
                    this.currentBrandType = BrandTypes.CheatBreaker;
                    this.doStep = true;
                }

                if (this.brand.toLowerCase(Locale.ROOT).contains("hyperium")
                        || this.channel.toLowerCase(Locale.ROOT).contains("hyperium")) {
                    this.currentBrandType = BrandTypes.Hyperium;
                    this.doStep = true;
                }

                if (this.channel.contains("MC|Pixel") || this.brand.contains("MC|Pixel")) {
                    this.currentBrandType = BrandTypes.PixelClient;
                    this.doStep = true;
                }

                if (this.channel.contains("PLC18") || this.brand.contains("PLC18")) {
                    this.currentBrandType = BrandTypes.PvPLounge;
                    this.doStep = true;
                }


                //vape v2.06 crack
                if (channel.contains("L0LIMAHACKER")
                        || channel.equals("LOLIMAHACKER")
                        || brand.contains("LOLIMAHACKER")
                        || brand.contains("L0LIMAHACKER")) {
                    Anticheat.INSTANCE.getPlugin().getServer()
                            .getConsoleSender().sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " " + ChatColor.RED
                                    + "Banning " + ChatColor.GOLD + getData().getUsername()
                                    + ChatColor.RED + " for using Cracked Vape v2.06");
                    Check.punishPlayer(getData(), false, 40, 1,
                            "Cracked", "Vape",
                            1, null);
                }

                //shit clients
                if (channel.equals("cock")
                        || channel.equals("MCnetHandler")
                        || channel.equals("lmaohax")
                        || channel.equals("TcpNoDelayMod-2.0")
                        || channel.equals("timechanger")
                        || channel.equals("Schematica")
                        || channel.equals("OpenComputers")
                        || channel.equals("wigger")
                        || channel.equals("mergeclient")
                        || channel.equals("Aimbot")
                        || channel.equals("gc")
                        || channel.equals("n")
                        || channel.equals("CPS_BAN_THIS_NIGGER")
                        || channel.equals("EROUAXWASHERE")
                        || channel.equals("unbanearwax")
                        || channel.equals("gg")
                        || brand.contains("Synergy")
                        || brand.contains("Created By ")
                        || brand.contains("\\u0007Created By ")
                        || brand.contains("CRYSTAL|")
                        || channel.contains("EARWAXWASHERE")
                        || channel.equals("ethylene")
                        || channel.equals("1946203560")
                        || channel.equals("reach")
                        || channel.equals("customGuiOpenBspkrs")
                        || channel.equals("BLC|M")
                        || channel.equals("CRYSTAL|6LAKS0TRIES")
                        || channel.equals("CRYSTAL|KZ1LM9TO")
                        || channel.equals("XDSMKDKFDKSDAKDFkEJF")
                        || channel.equals("mincraftpvphcker")
                        || channel.equals("0SO1Lk2KASxzsd")) {
                    Anticheat.INSTANCE.getPlugin().getServer()
                            .getConsoleSender().sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " " + ChatColor.RED
                                    + "Banning " + ChatColor.GOLD + getData().getUsername()
                                    + ChatColor.RED + " for using Old Payload Client");
                    Check.punishPlayer(getData(), false, 40, 1,
                            "Payload", "Client",
                            1, null);
                }

                //old sparky disabler payload.
                if (channel.equals("40413eb1") && brand.contains("40413eb1")) {
                    Anticheat.INSTANCE.getPlugin().getServer()
                            .getConsoleSender().sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " " + ChatColor.RED
                                    + "Banning " + ChatColor.GOLD + getData().getUsername()
                                    + ChatColor.RED + " for using Old Leaked Sparky Payload.");
                    Check.punishPlayer(getData(), false, 40, 1,
                            "Old Sparky", "Payload",
                            1, null);
                }

                if (brand.equals("Vanilla")) {
                    Anticheat.INSTANCE.getPlugin().getServer()
                            .getConsoleSender().sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " " + ChatColor.RED
                                    + "Banning " + ChatColor.GOLD + getData().getUsername()
                                    + ChatColor.RED + " for using Jigsaw Client/Modified minecraft payload.");
                    Check.punishPlayer(getData(), false, 40, 1,
                            "Jigsaw Client", "Invalid Payload", 1, null);
                }


                if (brand.length() > 16) {
                    this.brand = this.brand.substring(0, 16);
                }

                if (this.channel.length() > 16) {
                    this.channel = this.channel.substring(0, 16);
                }
            }
        }
    }

    public enum BrandTypes {
        Vanilla,
        Forge,
        Fabric,
        Sawyer,
        Lunar,
        CheatBreaker,
        PixelClient,
        Hyperium,
        PvPLounge,
        BlazingPack,

        NONE,
    }
}