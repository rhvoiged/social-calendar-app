package com.example.demo.repository;
import com.example.demo.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// repository interface for Friendship entity operations

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    List<Friendship> findByUserIdOrFriendId(Integer userId, Integer friendId); // find all friendship relations (sent or received) for a specific user
    Optional<Friendship> findByUserIdAndFriendId(Integer userId, Integer friendId); // find a specific friendship relation between two users
}


/**
 1. when the server starts, Spring Boot creates a "Proxy" class in memory that implements this interface
 2. Spring uses the method names to write SQL queries
 - "findBy" becomes a SELECT * statement.
 - "UserIdAndFriendId" becomes the WHERE clause using 'userId' and 'friendId' columns
 */