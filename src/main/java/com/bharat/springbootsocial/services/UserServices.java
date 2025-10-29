package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.config.JwtProvider;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.repository.UserRepo;
import com.bharat.springbootsocial.repository.PostRepo;
import com.bharat.springbootsocial.repository.ReelsRepo;
import com.bharat.springbootsocial.response.ProfileResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServices implements ServiceInt {
    @Autowired
    UserRepo userRepo;
    
    @Autowired
    private PostRepo postRepo;
    
    @Autowired
    private ReelsRepo reelsRepo;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    public User createUser(User user) {
        Optional<User> userOptional = userRepo.findUserByEmail(user.getEmail());
        if (userOptional.isPresent()) {
            throw new IllegalStateException("email is already present");
        }
        return userRepo.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userRepo.findAll();
        return users;
    }

    @Override
    public User getUserById(UUID id) {
        Optional<User> userOptional = userRepo.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            throw new IllegalStateException("User Id " + id + " does not exist");
        }
    }

    @Override
    public User editUser(UUID id, User user) {
        User existingUser = userRepo.findById(id).orElseThrow(() -> new IllegalStateException("User id not found"));

        if (user.getFname() != null)
            existingUser.setFname(user.getFname());
        if (user.getLname() != null)
            existingUser.setLname(user.getLname());
        if (user.getEmail() != null)
            existingUser.setEmail(user.getEmail());
        if (user.getPassword() != null)
            existingUser.setPassword(user.getPassword());
        if (user.getGender() != null)
            existingUser.setGender(user.getGender());
        if (user.getProfileImage() != null)
            existingUser.setProfileImage(user.getProfileImage());
        if (user.getCoverImage() != null)
            existingUser.setCoverImage(user.getCoverImage());
        if (user.getUserBio() != null)
            existingUser.setUserBio(user.getUserBio());

        userRepo.save(existingUser);

        return existingUser;
    }

    @Override
    public void deleteUser(UUID id) {
        Optional<User> userOptional = Optional
                .ofNullable(userRepo.findById(id).orElseThrow(() -> new IllegalStateException("User id not found")));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            userRepo.delete(user);
        }
    }

    @Override
    public User followUser(UUID reqUserId, UUID id2) throws UserException {
        User reqUser = getUserById(reqUserId);
        User user2 = getUserById(id2);

        if (!reqUser.getFollowing().contains(id2.toString())) {
            user2.getFollowers().add(reqUser.getId().toString());
            reqUser.getFollowing().add(user2.getId().toString());

            userRepo.save(reqUser);
            userRepo.save(user2);
            
            // Send follow notification
            notificationService.sendFollowNotification(user2, reqUser);
        }

        return reqUser;
    }

    @Override
    public User unfollowUser(UUID reqUserId, UUID id2) throws UserException {
        User reqUser = getUserById(reqUserId);
        User user2 = getUserById(id2);

        if (reqUser.getFollowing().contains(id2.toString())) {
            user2.getFollowers().remove(reqUser.getId().toString());
            reqUser.getFollowing().remove(user2.getId().toString());

            userRepo.save(reqUser);
            userRepo.save(user2);
        }

        return reqUser;
    }

    @Override
    public boolean isFollowing(UUID id1, UUID id2) throws UserException {
        User user1 = getUserById(id1);
        return user1.getFollowing().contains(id2.toString());
    }

    @Override
    public List<User> getFollowers(UUID userId) {
        User user = getUserById(userId);
        return user.getFollowers().stream()
                .map(id -> getUserById(UUID.fromString(id)))
                .toList();
    }

    @Override
    public List<User> getFollowing(UUID userId) {
        User user = getUserById(userId);
        return user.getFollowing().stream()
                .map(id -> getUserById(UUID.fromString(id)))
                .toList();
    }

    @Override
    public User findUserByEmail(String email) {
        Optional<User> userOptional = userRepo.findUserByEmail(email);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            throw new IllegalStateException("email id not found");
        }
    }

    @Override
    public List<User> searchUser(String query) {
        return userRepo.searchUser(query);
    }

    @Override
    public User getUserFromToken(String jwt) {
        try {
            String email = JwtProvider.getEmailFromJwtToken(jwt);
            User user = findUserByEmail(email);
            if (user == null) {
                throw new IllegalArgumentException("User not found for email: " + email);
            }
            return user;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage());
        }
    }

    @Override
    public ProfileResponse getUserProfile(UUID userId) throws Exception {
        User user = getUserById(userId);
        user.setPassword(null); // Don't expose password

        // Get posts count for the user (posts + reels)
        int postsCount = postRepo.countPostsByUserId(userId).intValue();
        int reelsCount = reelsRepo.countReelsByUserId(userId).intValue();
        int totalPostsCount = postsCount + reelsCount;

        // Create a simple profile response with just user data and counts
        // Posts and reels will be fetched separately by the frontend
        return new ProfileResponse(
                user,
                null, // posts - will be fetched separately
                null, // savedPosts - will be fetched separately
                null, // reels - will be fetched separately
                totalPostsCount, // postsCount - includes both posts and reels
                0, // savedPostsCount - will be calculated by frontend
                0, // reelsCount - will be calculated by frontend
                user.getFollowers().size(),
                user.getFollowing().size());
    }

    @Override
    public User updateCoverImage(UUID userId, String coverImageUrl) throws Exception {
        User user = getUserById(userId);
        user.setCoverImage(coverImageUrl);
        return userRepo.save(user);
    }

}
