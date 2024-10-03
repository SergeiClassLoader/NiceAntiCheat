package pro.cyrent.anticheat.api.event;

import pro.cyrent.anticheat.api.check.data.PredictionData;
import pro.cyrent.anticheat.api.check.data.ReachData;
import pro.cyrent.anticheat.api.check.data.TimerData;
import pro.cyrent.anticheat.api.check.data.VelocityCheckData;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;


public interface IEvent {
    void onPacket(PacketEvent event);

    void onPacketReach(PacketEvent event);

    void onTimer(TimerData event);

    void onServerTick(PlayerData playerData);

    void onClientTransaction(PlayerData playerData, long timestamp);

    void onPrediction(PredictionData predictionData);

    void onVelocityDetection(VelocityCheckData data);

    void onReach(ReachData reachData);

    void onBetterClientTransaction();
}
