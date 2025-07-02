package com.olg.services.booking.api.events;

import com.olg.mysql.events.Event;
import com.olg.mysql.events.EventRepository;
import com.olg.services.booking.api.events.dto.EventMapper;
import com.olg.services.booking.api.events.dto.EventResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventsService {

    private static final Logger log = LoggerFactory.getLogger(EventsService.class);
    private final EventRepository eventRepository;

    public EventsService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventResponse> getAll(){
        List<Event> events = eventRepository.findAll();

        return EventMapper.map(events);
    }
}
