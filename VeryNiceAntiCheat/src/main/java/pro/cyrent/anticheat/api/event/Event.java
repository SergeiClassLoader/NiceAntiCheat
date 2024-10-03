package pro.cyrent.anticheat.api.event;

import pro.cyrent.anticheat.api.check.data.PredictionData;
import pro.cyrent.anticheat.api.check.data.ReachData;
import pro.cyrent.anticheat.api.check.data.TimerData;
import pro.cyrent.anticheat.api.check.data.VelocityCheckData;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;


public class Event implements IEvent {

    @Override
    public void onPacket(PacketEvent event) {
        //
    }

    @Override
    public void onServerTick(PlayerData playerData) {

    }

    @Override
    public void onClientTransaction(PlayerData playerData, long timestamp) {

    }

    @Override
    public void onPacketReach(PacketEvent event) {
        //
    }

    @Override
    public void onTimer(TimerData event) {
        //
    }


    @Override
    public void onPrediction(PredictionData predictionData) {

    }

    @Override
    public void onBetterClientTransaction() {
        //
    }

    @Override
    public void onVelocityDetection(VelocityCheckData data) {

    }

    @Override
    public void onReach(ReachData reachData) {

    }
}
