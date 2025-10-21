package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.response.ProfileResponse;
import com.bharat.springbootsocial.services.ServiceInt;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    @Autowired
    ServiceInt userServices;

    @GetMapping()
    public List<User> getUsers() {
        return userServices.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") UUID userId) {
        return userServices.getUserById(userId);
    }

    @PutMapping()
    public User updateUser(@RequestHeader("Authorization") String jwt, @RequestBody User user) {
        User reqUser = userServices.getUserFromToken(jwt);
        return userServices.editUser(reqUser.getId(), user);
    }

    @PutMapping("/profile-image")
    public User updateProfileImage(@RequestHeader("Authorization") String jwt, @RequestBody String profileImageUrl) {
        User reqUser = userServices.getUserFromToken(jwt);
        User user = new User();
        user.setProfileImage(profileImageUrl);
        return userServices.editUser(reqUser.getId(), user);
    }

    @PutMapping("/cover-image")
    public User updateCoverImage(@RequestHeader("Authorization") String jwt, @RequestBody String coverImageUrl) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        return userServices.updateCoverImage(reqUser.getId(), coverImageUrl);
    }

    @PutMapping("/bio")
    public User updateUserBio(@RequestHeader("Authorization") String jwt, @RequestBody String userBio) {
        User reqUser = userServices.getUserFromToken(jwt);
        User user = new User();
        user.setUserBio(userBio);
        return userServices.editUser(reqUser.getId(), user);
    }

    @DeleteMapping("{id}")
    public String deleteUser(@PathVariable UUID id) {
        userServices.deleteUser(id);
        return "User deleted successfully";
    }

    @PutMapping("/follow/{id2}")
    public User followUser(@RequestHeader("Authorization") String jwt, @PathVariable UUID id2) throws UserException {
        User reqUser = userServices.getUserFromToken(jwt);
        return userServices.followUser(reqUser.getId(), id2);
    }

    @PutMapping("/unfollow/{id2}")
    public User unfollowUser(@RequestHeader("Authorization") String jwt, @PathVariable UUID id2) throws UserException {
        User reqUser = userServices.getUserFromToken(jwt);
        return userServices.unfollowUser(reqUser.getId(), id2);
    }

    @GetMapping("/follow-status/{userId}")
    public boolean isFollowing(@RequestHeader("Authorization") String jwt, @PathVariable UUID userId)
            throws UserException {
        User reqUser = userServices.getUserFromToken(jwt);
        return userServices.isFollowing(reqUser.getId(), userId);
    }

    @GetMapping("/followers/{userId}")
    public List<User> getFollowers(@PathVariable UUID userId) {
        return userServices.getFollowers(userId);
    }

    @GetMapping("/following/{userId}")
    public List<User> getFollowing(@PathVariable UUID userId) {
        return userServices.getFollowing(userId);
    }

    @GetMapping("/search")
    public List<User> searchUser(@RequestParam("query") String query) {
        return userServices.searchUser(query);
    }

    @GetMapping("/profile") // access the token from the frontend request authorization header
    public User getUserFromToken(@RequestHeader("Authorization") String jwt) {

        User user = userServices.getUserFromToken(jwt);
        user.setPassword(null);
        return user;
    }

    @GetMapping("/profile/{userId}")
    public ProfileResponse getUserProfile(@PathVariable UUID userId) throws Exception {
        return userServices.getUserProfile(userId);
    }

    @GetMapping("/profile/me")
    public ProfileResponse getCurrentUserProfile(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        return userServices.getUserProfile(reqUser.getId());
    }
}
