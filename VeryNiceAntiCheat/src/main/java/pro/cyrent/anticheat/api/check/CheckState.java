package pro.cyrent.anticheat.api.check;

public enum CheckState {
    DEV("§4Dev§r"),
    PRE_ALPHA("§cPre-Alpha§r"),
    ALPHA("§cAlpha§r"),
    PRE_BETA("§ePre-Beta§r"),
    BETA("§6Beta§r"),
    PRE_RELEASE("§2Pre-Release§r"),
    RELEASE("§aRelease§r");

    final String name;

    CheckState(String name) {
        this.name = name;
    }
}
