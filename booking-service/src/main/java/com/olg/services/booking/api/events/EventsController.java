package com.olg.services.booking.api.events;

import com.olg.services.booking.api.ApiResponse;
import com.olg.services.booking.api.events.dto.EventResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventsController {


    private static final Logger log = LoggerFactory.getLogger(EventsController.class);
    private final EventsService eventsService;

    public EventsController(EventsService eventsService) {
        this.eventsService = eventsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEvents() {
        try{
            List<EventResponse> response = eventsService.getAll();

            return ResponseEntity.ok(new ApiResponse<>(MDC.get("requestId"), response));
        } catch (Exception e){
            log.error("Something went wrong", e);
            return ResponseEntity.badRequest().body(null);
        }
    }

//    @GetMapping("/{id}")
//    public EventDTO getEvent(@PathVariable Long id) {
//        // ...
//    }
//
//    @PostMapping
//    public EventDTO createEvent(@RequestBody EventDTO event) {
//        // ...
//    }
//
//    // etc.
}


