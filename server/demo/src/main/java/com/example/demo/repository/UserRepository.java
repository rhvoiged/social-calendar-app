package com.example.demo.repository;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

//repository interface for User entity operations

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);// find a user by their unique email address
    boolean existsByEmail(String email); // check if a user with the given email already exists
    boolean existsByName(String name); // check if a user with the given name already exists
    List<User> findByNameContainingIgnoreCase(String name);  // search for users whose names contain the given string, ignoring case
}


/**
 1. when the server starts, Spring Boot creates a "Proxy" class in memory that implements this interface
 2. Spring uses the method names to write SQL queries
 - "findBy" becomes a SELECT * statement.
 - "UserIdAndFriendId" becomes the WHERE clause using 'userId' and 'friendId' columns
 */