package com.example.demo.controller;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

//@RequestBody    ->    data is hidden    http://localhost:8080/api/addEvent
//@RequestParam   ->   after the '?'         http://localhost:8080/api/weather?city=Bucuresti
//@PathVariable     ->    before the '?'      http://localhost:8080/api/friendCalendar/10


//controller for managing authentification

@CrossOrigin(origins = "*") //accept requests from anyone
@RestController // return JSON objects
@RequestMapping("/api") // adds prefix for Post/Get/Delete requests
public class AuthController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FriendshipRepository friendshipRepo;
    private final NotificationService notificationService;

    public AuthController(UserRepository userRepo, BCryptPasswordEncoder passwordEncoder, FriendshipRepository friendshipRepo, NotificationService notificationService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.friendshipRepo = friendshipRepo;
        this.notificationService = notificationService;
    }

    // login
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User u) {
        return userRepo.findByEmail(u.getEmail())
                .filter(user -> passwordEncoder.matches(u.getPassword(), user.getPassword()))
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // register new user
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User u) {
        if (userRepo.existsByEmail(u.getEmail())) return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", "Email exists"));
        if (userRepo.existsByName(u.getName())) return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", "Username taken"));
        u.setPassword(passwordEncoder.encode(u.getPassword())); // BCrypt encryption
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepo.save(u));
    }

    // tokens
    @PostMapping("/updateToken")
    public ResponseEntity<?> updateToken(@RequestParam Integer userId, @RequestParam String token, @RequestParam(required = false) Integer reminderMinutes) {
        userRepo.findAll().forEach(user -> {
            if (token.equals(user.getFcmToken()) && !userId.equals(user.getId())) {
                user.setFcmToken(null);
                userRepo.save(user);
            }
        });

        return userRepo.findById(userId).map(u -> {
            u.setFcmToken(token);
            if (reminderMinutes != null) u.setReminderMinutes(reminderMinutes);
            userRepo.save(u);

            // check for pending requests and notify now that we have the new token
            long pending = friendshipRepo.findByUserIdOrFriendId(0, u.getId()).stream().filter(f -> "PENDING".equals(f.getStatus()) && f.getFriendId().equals(u.getId())).count();
            if (pending > 0) {notificationService.sendNotification(u.getId(), "New friend request", "You've got " + pending + " friend requests!");}

            return ResponseEntity.ok(Collections.singletonMap("status", "Updated"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // search user in order to send friend requests
    @GetMapping("/users/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query, @RequestParam Integer currentUserId) {
        List<User> allMatches = userRepo.findByNameContainingIgnoreCase(query);

        // you can't send a friend request to yourself, to a friend or to a user who already has a pending friend request from you
        Set<Integer> exclude = new HashSet<>();
        exclude.add(currentUserId);
        friendshipRepo.findByUserIdOrFriendId(currentUserId, currentUserId).forEach(f -> {
            if (f.getUserId().equals(currentUserId)) exclude.add(f.getFriendId());
            else exclude.add(f.getUserId());
        });

        List<User> filtered = allMatches.stream().filter(u -> !exclude.contains(u.getId())).collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Integer id) {return userRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());}
}
