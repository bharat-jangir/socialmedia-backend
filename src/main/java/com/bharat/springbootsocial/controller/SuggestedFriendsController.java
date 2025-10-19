package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.services.SuggestedFriendsService;
import com.bharat.springbootsocial.services.ServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suggested-friends")
@CrossOrigin(origins = "*")
public class SuggestedFriendsController {

    @Autowired
    private SuggestedFriendsService suggestedFriendsService;

    @Autowired
    private ServiceInt userService;

    /**
     * Get suggested friends for a user (Instagram-like algorithm)
     * GET /api/suggested-friends/{userId}?limit=20
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getSuggestedFriends(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit) {
        
        try {
            // Validate user exists
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("User not found"));
            }

            // Get suggested friends
            List<SuggestedFriendsService.SuggestedFriend> suggestions = 
                suggestedFriendsService.getSuggestedFriends(userId, limit);

            // Format response
            List<Map<String, Object>> formattedSuggestions = suggestions.stream()
                .map(this::formatSuggestion)
                .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("message", "Suggested friends retrieved successfully");
            response.put("data", formattedSuggestions);
            response.put("count", formattedSuggestions.size());
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Failed to get suggested friends: " + e.getMessage()));
        }
    }

    /**
     * Get mutual friend suggestions only
     * GET /api/suggested-friends/{userId}/mutual?limit=10
     */
    @GetMapping("/{userId}/mutual")
    public ResponseEntity<Map<String, Object>> getMutualFriendSuggestions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("User not found"));
            }

            List<SuggestedFriendsService.SuggestedFriend> suggestions = 
                suggestedFriendsService.getMutualFriendSuggestions(userId, limit);

            List<Map<String, Object>> formattedSuggestions = suggestions.stream()
                .map(this::formatSuggestion)
                .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("message", "Mutual friend suggestions retrieved successfully");
            response.put("data", formattedSuggestions);
            response.put("count", formattedSuggestions.size());
            response.put("type", "mutual_friends");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Failed to get mutual friend suggestions: " + e.getMessage()));
        }
    }

    /**
     * Get gender-based suggestions
     * GET /api/suggested-friends/{userId}/gender?limit=10
     */
    @GetMapping("/{userId}/gender")
    public ResponseEntity<Map<String, Object>> getGenderBasedSuggestions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("User not found"));
            }

            List<SuggestedFriendsService.SuggestedFriend> suggestions = 
                suggestedFriendsService.getGenderBasedSuggestions(userId, limit);

            List<Map<String, Object>> formattedSuggestions = suggestions.stream()
                .map(this::formatSuggestion)
                .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("message", "Gender-based suggestions retrieved successfully");
            response.put("data", formattedSuggestions);
            response.put("count", formattedSuggestions.size());
            response.put("type", "gender_based");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Failed to get gender-based suggestions: " + e.getMessage()));
        }
    }

    /**
     * Get all types of suggestions in one call
     * GET /api/suggested-friends/{userId}/all?limit=20
     */
    @GetMapping("/{userId}/all")
    public ResponseEntity<Map<String, Object>> getAllSuggestions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit) {
        
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("User not found"));
            }

            // Get different types of suggestions
            List<SuggestedFriendsService.SuggestedFriend> allSuggestions = 
                suggestedFriendsService.getSuggestedFriends(userId, limit);
            List<SuggestedFriendsService.SuggestedFriend> mutualSuggestions = 
                suggestedFriendsService.getMutualFriendSuggestions(userId, limit / 2);
            List<SuggestedFriendsService.SuggestedFriend> genderSuggestions = 
                suggestedFriendsService.getGenderBasedSuggestions(userId, limit / 2);

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("message", "All suggestions retrieved successfully");
            response.put("data", Map.of(
                "all_suggestions", allSuggestions.stream().map(this::formatSuggestion).collect(java.util.stream.Collectors.toList()),
                "mutual_friends", mutualSuggestions.stream().map(this::formatSuggestion).collect(java.util.stream.Collectors.toList()),
                "gender_based", genderSuggestions.stream().map(this::formatSuggestion).collect(java.util.stream.Collectors.toList())
            ));
            response.put("counts", Map.of(
                "all", allSuggestions.size(),
                "mutual", mutualSuggestions.size(),
                "gender", genderSuggestions.size()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Failed to get all suggestions: " + e.getMessage()));
        }
    }

    /**
     * Format suggestion for API response
     */
    private Map<String, Object> formatSuggestion(SuggestedFriendsService.SuggestedFriend suggestion) {
        Map<String, Object> formatted = new HashMap<>();
        
        // User basic info
        User user = suggestion.getUser();
        formatted.put("id", user.getId());
        formatted.put("fname", user.getFname());
        formatted.put("lname", user.getLname());
        formatted.put("email", user.getEmail());
        formatted.put("profilePicture", user.getProfileImage());
        formatted.put("gender", user.getGender());
        formatted.put("bio", user.getUserBio());
        
        // Suggestion specific info
        formatted.put("suggestionScore", suggestion.getScore());
        formatted.put("suggestionReasons", suggestion.getReasons());
        
        // Additional info for frontend
        formatted.put("displayName", (user.getFname() != null ? user.getFname() : "") + " " + (user.getLname() != null ? user.getLname() : ""));
        formatted.put("isOnline", false); // Default to false since we don't have this field
        
        return formatted;
    }

    /**
     * Create error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", false);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}
