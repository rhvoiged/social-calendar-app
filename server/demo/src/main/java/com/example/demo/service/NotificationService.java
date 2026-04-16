package com.example.demo.service;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.util.Optional;

// service for sending push notifications via Firebase Cloud Messaging

@Service
public class NotificationService {

    private final UserRepository userRepo;

    // accessing user's token
    public NotificationService(UserRepository userRepo) {this.userRepo = userRepo;}

    // sending push notification
    public void sendNotification(Integer userId, String title, String body) {
        // check if Firebase is initialized before proceeding
        if (FirebaseApp.getApps().isEmpty()) {System.err.println("Firebase is not initialized. Notification skipped.");return;}

        // find the user and verify if they have a valid token
        Optional<User> user = userRepo.findById(userId);
        if (user.isPresent() && user.get().getFcmToken() != null && !user.get().getFcmToken().isEmpty()) {
            String token = user.get().getFcmToken();

            // build the notification message
            Message message = Message.builder().setToken(token).setNotification(Notification.builder().setTitle(title).setBody(body).build()).build();
            // sending the message
            try {
                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println("Successfully sent message: " + response);
            } catch (Exception e) {System.err.println("Error sending Firebase message: " + e.getMessage());}
        }
    }
}
