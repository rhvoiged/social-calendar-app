package com.example.demo.controller;
import com.example.demo.model.Event;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//@RequestBody    ->    data is hidden    http://localhost:8080/api/addEvent
//@RequestParam   ->   after the '?'         http://localhost:8080/api/weather?city=Bucuresti
//@PathVariable     ->    before the '?'      http://localhost:8080/api/friendCalendar/10


//controller for managing events

@CrossOrigin(origins = "*") //accept requests from anyone
@RestController // return JSON objects
@RequestMapping("/api") // adds prefix for Post/Get/Delete requests
public class CalendarController {

    private final EventRepository eventRepo;
    private final FriendshipRepository friendshipRepo;
    private final WeatherService weatherService;

    public CalendarController(EventRepository eventRepo, FriendshipRepository friendshipRepo, WeatherService weatherService) {
        this.eventRepo = eventRepo;
        this.friendshipRepo = friendshipRepo;
        this.weatherService = weatherService;
    }

    // returns the calendar of the current user
    @GetMapping("/myCalendar")
    public ResponseEntity<List<Event>> myCalendar(@RequestParam Integer userId) {return ResponseEntity.ok(eventRepo.findByOwnerId(userId));}

    // returns the calendar of a friend
    @GetMapping("/friendCalendar/{friendId}")
    public ResponseEntity<List<Event>> friendCalendar(@PathVariable Integer friendId, @RequestParam Integer requesterId) {
        boolean isFriend = friendshipRepo.findByUserIdAndFriendId(requesterId, friendId).map(f -> "ACCEPTED".equals(f.getStatus())).orElse(false) ||
                friendshipRepo.findByUserIdAndFriendId(friendId, requesterId).map(f -> "ACCEPTED".equals(f.getStatus())).orElse(false);

        if (isFriend) {return ResponseEntity.ok(eventRepo.findByOwnerId(friendId));}
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // adds new event to db
    @PostMapping("/addEvent")
    public ResponseEntity<?> addEvent(@RequestBody Event e) {
        if (e.getOwnerId() == null || e.getOwnerId() <= 0) {return ResponseEntity.badRequest().body("Invalid ownerId");}
        return ResponseEntity.status(HttpStatus.CREATED).body(eventRepo.save(e));
    }

    // deletes an event from db
    @DeleteMapping("/deleteEvent")
    public ResponseEntity<?> deleteEvent(@RequestParam Integer eventId) {
        eventRepo.deleteById(eventId);
        return ResponseEntity.ok("Deleted");
    }

    // overlaps calendar between two users
    @GetMapping("/overlapCalendar")
    public ResponseEntity<List<Event>> overlapCalendar(@RequestParam Integer userId, @RequestParam Integer friendId, @RequestParam String date) {
        LocalDate start = LocalDate.parse(date);
        List<Event> events = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            String day = start.plusDays(i).toString();
            events.addAll(eventRepo.findByOwnerIdAndDate(userId, day));
            events.addAll(eventRepo.findByOwnerIdAndDate(friendId, day));
        }
        return ResponseEntity.ok(events);
    }

    // getting weather for a city
    @GetMapping("/weather")
    public ResponseEntity<String> getWeather(@RequestParam String city) {return ResponseEntity.ok(weatherService.getWeather(city));}
}

