package com.github.naiithink.app.util.events.managers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.naiithink.app.hotspot.Hotspot;
import com.github.naiithink.app.util.events.listeners.EventListener;

public final class EventManager {

    private Map<Hotspot.EventType, List<EventListener>> listenerTable;

    public EventManager(Hotspot.EventType... eventTypes) {
        listenerTable = new ConcurrentHashMap<>();

        for (Hotspot.EventType eventType : eventTypes) {
            listenerTable.put(eventType, new CopyOnWriteArrayList<>());
        }
    }

    public
    synchronized
    boolean subscribe(Hotspot.EventType eventType,
                      EventListener listener) {

        if (listenerTable.containsKey(eventType) == false) {
            return false;
        }

        listenerTable.get(eventType).add(listener);

        return true;
    }

    public
    synchronized
    boolean unsubscribe(Hotspot.EventType eventType,
                        EventListener listener) {

        if (listenerTable.containsKey(eventType) == false) {
            return false;
        }

        listenerTable.get(eventType).remove(listener);

        return true;
    }

    public
    synchronized
    int publish(Hotspot.EventType eventType,
                Object context) {

        int clientCount = 0;
        List<EventListener> listeners = listenerTable.get(eventType);

        for (EventListener listener : listeners) {
            listener.update(context);
            clientCount++;
        }

        return clientCount;
    }
}
