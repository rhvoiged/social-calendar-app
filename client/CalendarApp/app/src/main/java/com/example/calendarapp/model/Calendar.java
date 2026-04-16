package com.example.calendarapp.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class Calendar {
    public Integer id, userId;
    public List<Event> events;
}
