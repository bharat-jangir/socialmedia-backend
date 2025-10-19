package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class SuggestedFriendsService {

    @Autowired
    private UserRepo userRepo;

    /**
     * Get suggested friends for a user with Instagram-like algorithm
     * Priority order:
     * 1. Mutual friends (highest priority)
     * 2. Same location/city
     * 3. Same interests/hobbies
     * 4. Random suggestions (lowest priority)
     */
    public List<SuggestedFriend> getSuggestedFriends(UUID userId, int limit) {
        User currentUser = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all users except current user
        List<User> allUsers = userRepo.findAll().stream()
                .filter(user -> !user.getId().equals(userId))
                .collect(Collectors.toList());

        // Get current user's following list
        Set<UUID> currentUserFollowing = getCurrentUserFollowing(userId);
        
        // Get users who follow current user
        Set<UUID> currentUserFollowers = getCurrentUserFollowers(userId);

        // Filter out already following users
        List<User> candidateUsers = allUsers.stream()
                .filter(user -> !currentUserFollowing.contains(user.getId()))
                .collect(Collectors.toList());

        // Calculate suggestion scores
        List<SuggestedFriend> suggestions = candidateUsers.stream()
                .map(user -> calculateSuggestionScore(currentUser, user, currentUserFollowing))
                .filter(suggestion -> suggestion.getScore() > 0) // Only include users with positive score
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore())) // Sort by score descending
                .limit(limit)
                .collect(Collectors.toList());

        return suggestions;
    }

    /**
     * Calculate suggestion score for a user based on various factors
     */
    private SuggestedFriend calculateSuggestionScore(User currentUser, User candidateUser, Set<UUID> currentUserFollowing) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1. Mutual following (highest weight: 50 points per mutual following)
        int mutualFollowing = getMutualFollowingCount(currentUser.getId(), candidateUser.getId());
        if (mutualFollowing > 0) {
            score += mutualFollowing * 50;
            reasons.add(mutualFollowing + " mutual following" + (mutualFollowing > 1 ? "s" : ""));
        }

        // 2. Same gender (weight: 20 points)
        if (currentUser.getGender() != null && candidateUser.getGender() != null &&
            currentUser.getGender().equalsIgnoreCase(candidateUser.getGender())) {
            score += 20;
            reasons.add("Same gender");
        }

        // 3. Similar name patterns (weight: 15 points)
        if (currentUser.getFname() != null && candidateUser.getFname() != null &&
            currentUser.getFname().toLowerCase().startsWith(candidateUser.getFname().toLowerCase().substring(0, 1))) {
            score += 15;
            reasons.add("Similar name pattern");
        }

        // 4. Same last name (weight: 25 points) - could be family
        if (currentUser.getLname() != null && candidateUser.getLname() != null &&
            currentUser.getLname().equalsIgnoreCase(candidateUser.getLname())) {
            score += 25;
            reasons.add("Same last name");
        }

        // 5. Has profile image (weight: 10 points) - more likely to be active
        if (candidateUser.getProfileImage() != null && !candidateUser.getProfileImage().isEmpty()) {
            score += 10;
            reasons.add("Has profile picture");
        }

        // 6. Has bio (weight: 5 points) - more complete profile
        if (candidateUser.getUserBio() != null && !candidateUser.getUserBio().isEmpty()) {
            score += 5;
            reasons.add("Has bio");
        }

        // 8. Random factor to ensure variety (weight: 1-10 points)
        score += new Random().nextInt(10) + 1;

        return new SuggestedFriend(candidateUser, score, reasons);
    }

    /**
     * Get count of mutual following between two users
     */
    private int getMutualFollowingCount(UUID userId1, UUID userId2) {
        Set<UUID> user1Following = getCurrentUserFollowing(userId1);
        Set<UUID> user2Following = getCurrentUserFollowing(userId2);
        
        user1Following.retainAll(user2Following); // Keep only mutual following
        return user1Following.size();
    }

    /**
     * Get current user's following list
     */
    private Set<UUID> getCurrentUserFollowing(UUID userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user != null && user.getFollowing() != null) {
            return new HashSet<>(user.getFollowing());
        }
        return new HashSet<>();
    }

    /**
     * Get current user's followers list
     */
    private Set<UUID> getCurrentUserFollowers(UUID userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user != null && user.getFollowers() != null) {
            return new HashSet<>(user.getFollowers());
        }
        return new HashSet<>();
    }

    /**
     * Get suggested friends based on mutual following only
     */
    public List<SuggestedFriend> getMutualFriendSuggestions(UUID userId, int limit) {
        User currentUser = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<UUID> currentUserFollowing = getCurrentUserFollowing(userId);

        // Get following of following (potential mutual following)
        Map<UUID, Integer> mutualFollowingCounts = new HashMap<>();
        
        for (UUID followingId : currentUserFollowing) {
            Set<UUID> followingOfFollowing = getCurrentUserFollowing(followingId);
            for (UUID potentialFollowing : followingOfFollowing) {
                if (!potentialFollowing.equals(userId) && 
                    !currentUserFollowing.contains(potentialFollowing)) {
                    mutualFollowingCounts.put(potentialFollowing, 
                        mutualFollowingCounts.getOrDefault(potentialFollowing, 0) + 1);
                }
            }
        }

        // Convert to suggested friends and sort by mutual following count
        return mutualFollowingCounts.entrySet().stream()
                .map(entry -> {
                    User user = userRepo.findById(entry.getKey()).orElse(null);
                    if (user != null) {
                        List<String> reasons = Arrays.asList(entry.getValue() + " mutual following" + 
                            (entry.getValue() > 1 ? "s" : ""));
                        return new SuggestedFriend(user, entry.getValue() * 50, reasons);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get suggested friends based on gender similarity
     */
    public List<SuggestedFriend> getGenderBasedSuggestions(UUID userId, int limit) {
        User currentUser = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getGender() == null) {
            return new ArrayList<>();
        }

        Set<UUID> currentUserFollowing = getCurrentUserFollowing(userId);

        return userRepo.findByGenderIgnoreCase(currentUser.getGender()).stream()
                .filter(user -> !user.getId().equals(userId))
                .filter(user -> !currentUserFollowing.contains(user.getId()))
                .map(user -> {
                    List<String> reasons = Arrays.asList("Same gender");
                    return new SuggestedFriend(user, 20, reasons);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Inner class to represent a suggested friend with score and reasons
     */
    public static class SuggestedFriend {
        private User user;
        private int score;
        private List<String> reasons;

        public SuggestedFriend(User user, int score, List<String> reasons) {
            this.user = user;
            this.score = score;
            this.reasons = reasons;
        }

        // Getters
        public User getUser() { return user; }
        public int getScore() { return score; }
        public List<String> getReasons() { return reasons; }

        // Setters
        public void setUser(User user) { this.user = user; }
        public void setScore(int score) { this.score = score; }
        public void setReasons(List<String> reasons) { this.reasons = reasons; }
    }
}
