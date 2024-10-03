package pro.cyrent.anticheat.util.messages;


import pro.cyrent.anticheat.Anticheat;

public class MessageLoader {

    public void load() {
        MessagesFile.getInstance().setup(Anticheat.INSTANCE.getPlugin());
        MessagesFile.getInstance().writeDefaults();

        Anticheat.INSTANCE.getMessageValues().setLineMessage(this.convert(MessagesFile.getInstance().getData()
                .getString("Command-Messages.Line")));

        Anticheat.INSTANCE.getMessageValues().setBeginCommandLine(this.convert(MessagesFile.getInstance().getData()
                .getString("Command-Messages.Beginning")));

        Anticheat.INSTANCE.getMessageValues().setSubCommandNoExist(this.convert(MessagesFile.getInstance().getData()
                .getString("Command-Messages.SubCommandExist")));

        Anticheat.INSTANCE.getMessageValues().setNoPermission(this.convert(MessagesFile.getInstance().getData()
                .getString("Command-Messages.NoPermission")));

    }

    public String convert(String in) {
        return in.replace("&", "§").replace("%VERSION%", Anticheat.INSTANCE.getVersion());
    }


}