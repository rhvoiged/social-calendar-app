package com.example.demo.model;
import jakarta.persistence.*;

// defines a user entity

@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // unique id

    @Column(nullable = false, unique = true)
    private String email; // unique email

    @Column(nullable = false)
    private String name; // ursername

    @Column(nullable = false)
    private String password; // password

    @Column(name = "fcm_token")
    private String fcmToken; // token for push notifications

    @Column(name = "reminder_minutes")
    private Integer reminderMinutes = 15; // reminder default time

    // constructors
    public User() {}

    public User(Integer id, String email, String name, String password, String fcmToken, Integer reminderMinutes) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.fcmToken = fcmToken;
        this.reminderMinutes = reminderMinutes;
    }

    // getters + setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public Integer getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(Integer reminderMinutes) { this.reminderMinutes = reminderMinutes; }
}