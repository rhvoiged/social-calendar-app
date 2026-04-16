package com.example.demo.service;
import com.example.demo.model.Event;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// bg service that ensures reminders are sent at the right time
@Service
public class ReminderScheduler {

    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public ReminderScheduler(EventRepository eventRepo, UserRepository userRepo, NotificationService notificationService) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    // execute every minute
    @Scheduled(fixedRate = 60000)
    public void checkEventsAndSendReminders() {
        // current date
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // current time
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        // check today's events
        eventRepo.findByDate(today).forEach(event -> 
            userRepo.findById(event.getOwnerId()).ifPresent(user -> {
                try {
                    // event's start time
                    LocalTime eventStartTime = LocalTime.parse(event.getStartTime());
                    // reminder time
                    int reminderMin = user.getReminderMinutes();

                    // if current time matches the reminder time sends push notification
                    LocalTime reminderTime = eventStartTime.minusMinutes(reminderMin).withSecond(0).withNano(0);
                    if (now.equals(reminderTime)) {
                        notificationService.sendNotification(user.getId(), 
                            "Memento: " + event.getTitle(), 
                            "Event starts in " + reminderMin + " minutes!");
                    }
                } catch (Exception e) {}
            })
        );
    }
}