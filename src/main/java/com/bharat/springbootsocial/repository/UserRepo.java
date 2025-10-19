package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findUserByEmail(String email);

    @Query("select u from User u where u.fname LIKE %:query% OR u.lname LIKE %:query% OR u.email LIKE %:query%")
    List<User> searchUser(@Param("query") String query);
    
    // Find users by gender (case insensitive)
    List<User> findByGenderIgnoreCase(String gender);
    
    // Find users by first name (case insensitive)
    List<User> findByFnameIgnoreCase(String fname);
    
    // Find users by last name (case insensitive)
    List<User> findByLnameIgnoreCase(String lname);
}
