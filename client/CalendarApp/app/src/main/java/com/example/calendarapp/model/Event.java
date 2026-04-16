package com.example.calendarapp.model;
import com.google.gson.annotations.SerializedName;
public class Event {
    public Integer id, ownerId;
    public String title, description, date, startTime, endTime;
    public Event() {}
    public Event(Integer id, String title, String description, String date, String startTime, String endTime, Integer ownerId) {this.id = id;this.title = title;this.description = description;this.date = date;this.startTime = startTime;this.endTime = endTime;this.ownerId = ownerId;}
}
