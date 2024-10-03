package pro.cyrent.anticheat.api.event;

import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class EventBus {
    private final List<Event> events = new CopyOnWriteArrayList<>();
}
