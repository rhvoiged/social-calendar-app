package com.example.demo.repository;
import com.example.demo.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// repository interface for Event entity operations

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByOwnerId(Integer ownerId); // find all events belonging to a specific user
    List<Event> findByOwnerIdAndDate(Integer ownerId, String date);  // find all events for a specific user on a specific date (yyyy-MM-dd)
    List<Event> findByDate(String date); // find all events for a specific date (used for daily reminders)
}


/**
 1. when the server starts, Spring Boot creates a "Proxy" class in memory that implements this interface
 2. Spring uses the method names to write SQL queries
 - "findBy" becomes a SELECT * statement.
 - "UserIdAndFriendId" becomes the WHERE clause using 'userId' and 'friendId' columns
 */