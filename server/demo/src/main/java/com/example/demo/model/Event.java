package com.example.demo.model;
import jakarta.persistence.*;

// defines an event entity

@Entity
@Table(name = "Event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // unique id

    @Column(nullable = false)
    private String title; // title

    private String description; // optional description

    @Column(nullable = false)
    private String date; // "yyyy-MM-dd"

    @Column(name = "startTime", nullable = false)
    private String startTime; // "HH:mm"

    @Column(name = "endTime", nullable = false)
    private String endTime; // "HH:mm"

    @Column(name = "ownerId", nullable = false)
    private Integer ownerId; // who is the owner of the event

    // constructors
    public Event() {}

    public Event(Integer id, String title, String description, String date, String startTime, String endTime, Integer ownerId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ownerId = ownerId;
    }

    // getters + setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }
}