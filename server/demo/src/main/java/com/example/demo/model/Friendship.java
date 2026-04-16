package com.example.demo.model;
import jakarta.persistence.*;

// defines a friendship entity

@Entity
@Table(name = "Friend")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // unique id

    @Column(name = "userId", nullable = false)
    private Integer userId; // the user who sent the request

    @Column(name = "friendId", nullable = false)
    private Integer friendId; // the user who received the request

    @Column(nullable = false)
    private String status; // "PENDING", "ACCEPTED"

    // constructors
    public Friendship() {}

    public Friendship(Integer userId, Integer friendId, String status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }

    // getters + setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getFriendId() { return friendId; }
    public void setFriendId(Integer friendId) { this.friendId = friendId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}