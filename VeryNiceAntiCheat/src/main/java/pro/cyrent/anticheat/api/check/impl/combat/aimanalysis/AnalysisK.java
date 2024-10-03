package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInformation(
        name = "Analysis",
        subName = "K",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM_ANALYSIS,
        description = "Finds invalid sensitivity data.",
        experimental = true,
        state = CheckState.ALPHA)
public class AnalysisK extends Check {

    private double threshold;
    private float lastOffset;

    private double verbose;

    @Override
    public void onPacket(PacketEvent event) {
        if (!event.isMovement()) return;

        WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

        if (!flying.hasRotationChanged()) return;

        // Get current and previous rotations
        float currentYaw = getData().getMovementProcessor().getTo().getYaw();
        float previousYaw = getData().getMovementProcessor().getFrom().getYaw();

        float currentPitch = getData().getMovementProcessor().getTo().getPitch();
        float previousPitch = getData().getMovementProcessor().getFrom().getPitch();

        // Calculate rotation deltas
        float deltaYaw = currentYaw - previousYaw;
        float deltaPitch = currentPitch - previousPitch;

        // Skip negligible rotations
        if (Math.abs(deltaYaw) < 0.005F && Math.abs(deltaPitch) < 0.005F) {
            this.verbose -= Math.min(this.verbose, .015);
            return;
        }

        // Obtain player's mouse sensitivity (MCP float value)
        float mouseSensitivitySetting = getData().getSensitivityProcessor().getSensitivityY();
        float sensitivity = getData().getSensitivityProcessor().getSensitivity();

        // Under no circumstances do we want to detect when a player has 0 or less than 0 sensitivity.
        // If the sensitivity has been set for at least 200+ ticks, then we can continue.
        // Ignore cinematic, server lag, and non-attacking states.
        // Ignore low movement speed as niggers don't need to flag while their moving slowly.
        if (mouseSensitivitySetting <= 0
                || sensitivity <= 0
                || getData().getSensitivityProcessor().getSensitivityCycles() < 200
                || getData().getSensitivityProcessor().isCinematic()
                || getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 10
                || getData().generalCancel()
                || getData().getMovementProcessor().getDeltaXZ() < 0.05) {
            return;
        }

        // Calculate the "divisor" also known as the min value
        // that minecraft would use to make your player rotate based on sensitivity
        float f = mouseSensitivitySetting * 0.6F + 0.2F;
        float f1 = f * f * f * 8.0F;

        // Predict mouse deltas
        int predictedDeltaX = Math.round((deltaYaw / 0.15F) / f1);
        int predictedDeltaY = Math.round((deltaPitch / 0.15F) / f1);


        // Recalculate rotation changes from predicted mouse deltas
        float predictedYawChange = predictedDeltaX * f1;
        float predictedPitchChange = predictedDeltaY * f1;

        // Apply the rotation factor
        predictedYawChange *= 0.15F;
        predictedPitchChange *= 0.15F;

        // Compute expected rotations
        float expectedYaw = previousYaw + predictedYawChange;
        float expectedPitch = previousPitch + predictedPitchChange;

        // Calculate offsets
        float yawOffset = Math.abs(expectedYaw - currentYaw);
        float pitchOffset = Math.abs(expectedPitch - currentPitch);

        // If the player has had constant yaw offset of 0, and their mouse is moving more than 5
        // they are likely to be cheating.
        boolean invalid = yawOffset == 0 && this.lastOffset == 0 && Math.abs(predictedDeltaX) > 5
                && Math.abs(deltaYaw) > 0.0F && Math.abs(deltaPitch) > 0.0F && predictedDeltaY != 0;

        boolean didFail = false;

        if (invalid) {
            // Yaw can seem to be wrong sometimes due to some weird issues
            // So we made this a "separate check" like the pitch but its less strict.
            if (++this.threshold > 16) {
                // Reduce once flagged.
                this.threshold = 6;

                // Up final version for the flag.
                this.verbose++;
                didFail = true;
            }
        } else {
            this.threshold -= Math.min(this.threshold, 3);
            this.verbose -= Math.min(this.verbose, .015);
        }

        // Max verbose for the flag
        if (this.verbose > 10.0 && didFail) {
            this.fail(
                    "yaw-offset="+yawOffset,
                    "pitch-offset="+pitchOffset,
                    "mouseDeltaX="+predictedDeltaX,
                    "mouseDeltaY="+predictedDeltaY,
                    "deltaYaw="+deltaYaw,
                    "deltaPitch="+deltaPitch,
                    "mcp-value="+mouseSensitivitySetting,
                    "sensitivity="+sensitivity);
            this.verbose = 8;
        }

        // Set last offsets.
        this.lastOffset = yawOffset;
    }
}