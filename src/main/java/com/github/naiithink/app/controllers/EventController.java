package com.github.naiithink.app.controllers;

import com.github.naiithink.app.hotspot.Hotspot;
import com.github.naiithink.app.util.events.managers.EventManager;

public final class EventController {
    private EventController() {}

    private static EventManager events;

    static {
        events = new EventManager(Hotspot.EventType.values());

        events.subscribe(Hotspot.EventType.RECORD_CHANGED, HomeController.getInstance());
    }

    public static void publish(Hotspot.EventType eventType,
                               Object context) {

        events.publish(eventType, context);
    }
}
