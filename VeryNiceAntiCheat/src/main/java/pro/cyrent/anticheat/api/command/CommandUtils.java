package pro.cyrent.anticheat.api.command;


import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.HashMap;

public class CommandUtils {

    public static void registerCommand(Command commandObject) {
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register(commandObject.getCommand(), commandObject.getCommandExecutor());
            bukkitCommandMap.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void unRegisterBukkitCommand(Command commandObject) {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap1 = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            org.bukkit.command.Command command = commandMap1.getCommand(commandObject.getCommand());
            if (command != null) {
                try {
                    Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
                    SimpleCommandMap commandMap = (SimpleCommandMap) result;
                    Object map = getPrivateField(commandMap, "knownCommands");
                    @SuppressWarnings("unchecked")
                    HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
                    knownCommands.remove(command.getName());
                    for (String alias : command.getAliases()) {
                        knownCommands.remove(alias);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object getPrivateField(Object object, String field) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }
}
