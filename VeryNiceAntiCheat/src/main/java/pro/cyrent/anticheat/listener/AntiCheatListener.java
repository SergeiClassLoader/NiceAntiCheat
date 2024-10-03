package pro.cyrent.anticheat.listener;


/*
public class AntiCheatListener implements AntiCheatEventListener {

    /**
     * This is just a basic example to give you a feel on what it would look like in code.
     */

/*
    @Override
    public void onPlayerFlagEvent(PlayerFlagEvent event) {

    }

    @Override
    public void onPlayerLogsEvent(PlayerLogsEvent event) {
    }

    @Override
    public void onPlayerViolationSetEvent(PlayerSetViolationEvent event) {
        if (!event.isExperimental() && !event.isCanceled()) {

            Player player = event.getPlayer();

            if (player != null) {

                // Player only exists after they join.
                // Make sure to wait some time after they join to call this event inside the api / your plugin
                PlayerData playerData = Anticheat.INSTANCE.getUserManager().getUser(player);

                if (playerData != null) {
                    playerData.getCachedChecks().forEach(check -> {

                        // individual check
                        if (event.getCheckMap() == null || event.getCheckMap().isEmpty()) {

                            // Set based on the name & type in the event
                            if (check.getCheckName().equalsIgnoreCase(event.getCheckName())
                                    && check.getCheckType().equalsIgnoreCase(event.getCheckType())) {

                                // Set violation of the event
                                check.setViolations(event.getCurrentViolation());
                            }
                        }

                        // Make sure the map contains something.
                        if (event.getCheckMap() != null && !event.getCheckMap().isEmpty()) {

                            // All checks in the map.
                            event.getCheckMap().forEach((checkName, violation) -> {

                                // Looks for the friendly name which would look like "SpeedA", "FlyA", or "KillAuraA"
                                if (checkName.equalsIgnoreCase(check.getFriendlyName())) {

                                    // Set violation to the hashmap's violation.
                                    check.setViolations(violation);
                                }
                            });
                        }
                    });
                }
            }
        }
    }
}*/