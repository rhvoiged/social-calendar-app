package com.example.calendarapp.model;
import com.google.gson.annotations.SerializedName;
public class User {
    public Integer id;
    public String email, name, password, fcmToken;
    public String status, error;
    public User() {}
    public User(Integer id, String email, String name) {this.id = id;this.email = email;this.name = name;}
}
