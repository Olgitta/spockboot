package com.olg.services.booking.api.events.dto;

import com.olg.postgressql.events.Event;
import java.util.List;

public class EventMapper {

    public static EventResponse map(Event event){
        return new EventResponse(event.getId(),
                event.getName(),
                event.getType(),
                event.getDateTime(),
                event.getVenueId());
    }

    public static List<EventResponse> map(List<Event> events) {
        List<EventResponse> response = events.stream()
                .map(EventMapper::map)
                .toList();

        return response;
    }
}
