package com.example.demo.controller;
import com.example.demo.model.Friendship;
import com.example.demo.model.User;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

//@RequestBody    ->    data is hidden    http://localhost:8080/api/addEvent
//@RequestParam   ->   after the '?'         http://localhost:8080/api/weather?city=Bucuresti
//@PathVariable     ->    before the '?'      http://localhost:8080/api/friendCalendar/10


//controller for managing friends

@CrossOrigin(origins = "*") //accept requests from anyone
@RestController // return JSON objects
@RequestMapping("/api") // adds prefix for Post/Get/Delete requests
public class FriendsController {

    private final FriendshipRepository friendshipRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public FriendsController(FriendshipRepository friendshipRepo, UserRepository userRepo, NotificationService notificationService) {
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    // sends a friend request
    @PostMapping("/addFriend")
    public ResponseEntity<?> addFriend(@RequestParam Integer userId, @RequestParam Integer friendId) {
        if (userId.equals(friendId)) return ResponseEntity.ok().build();

        // check if you are trying to friend yourself / already friends
        if (friendshipRepo.findByUserIdAndFriendId(userId, friendId).isPresent() || friendshipRepo.findByUserIdAndFriendId(friendId, userId).isPresent()) {
            return ResponseEntity.ok().build();
        }

        friendshipRepo.save(new Friendship(userId, friendId, "PENDING"));
        
        // push notification
        userRepo.findById(userId).ifPresent(sender -> {
            userRepo.findById(friendId).ifPresent(receiver -> {
                // notify only if they aren't on the same device
                if (receiver.getFcmToken() != null && !receiver.getFcmToken().equals(sender.getFcmToken())) {notificationService.sendNotification(friendId, "New friend request", "Somebody wants to be friends with you!");}
            });
        });

        return ResponseEntity.ok().build();
    }

    // friend requests list
    @GetMapping("/friendRequests/{userId}")
    public ResponseEntity<List<User>> getFriendRequests(@PathVariable Integer userId) {
        List<User> requesters = friendshipRepo.findByUserIdOrFriendId(0, userId).stream()
                .filter(f -> "PENDING".equals(f.getStatus()) && f.getFriendId().equals(userId))
                .map(f -> userRepo.findById(f.getUserId()).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requesters);
    }

    // accepts a friend request
    @PostMapping("/acceptFriend")
    public ResponseEntity<?> acceptFriend(@RequestParam Integer userId, @RequestParam Integer friendId) {
        return friendshipRepo.findByUserIdAndFriendId(friendId, userId)
                .filter(f -> "PENDING".equals(f.getStatus()))
                .map(f -> {
                    f.setStatus("ACCEPTED");
                    friendshipRepo.save(f);
                    
                    // notification when someone accepts your friend request
                    userRepo.findById(userId).ifPresent(me -> {
                        userRepo.findById(friendId).ifPresent(requester -> {
                            if (requester.getFcmToken() != null && !requester.getFcmToken().equals(me.getFcmToken())) {notificationService.sendNotification(friendId, "Friend request accepted", "User " + me.getName() + " accepted your friend request.");}
                        });
                    });
                    
                    return ResponseEntity.ok().build();
                }).orElse(ResponseEntity.ok().build());
    }

    // delete a friend request
    @PostMapping("/removeFriend")
    public ResponseEntity<?> removeFriend(@RequestParam Integer userId, @RequestParam Integer friendId) {
        friendshipRepo.findByUserIdAndFriendId(userId, friendId).ifPresent(f -> friendshipRepo.delete(f));
        friendshipRepo.findByUserIdAndFriendId(friendId, userId).ifPresent(f -> friendshipRepo.delete(f));
        return ResponseEntity.ok().build();
    }

    // friend list
    @GetMapping("/friends/{userId}")
    public ResponseEntity<List<User>> listFriends(@PathVariable Integer userId) {
        List<User> friends = friendshipRepo.findByUserIdOrFriendId(userId, userId).stream()
                .filter(f -> "ACCEPTED".equals(f.getStatus()))
                .map(f -> userRepo.findById(f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId()).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(friends);
    }
}
