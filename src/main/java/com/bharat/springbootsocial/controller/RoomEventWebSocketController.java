package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.services.ServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class RoomEventWebSocketController {
    
    @Autowired
    private ServiceInt userService;
    
    // Helper method to ensure JWT token has Bearer prefix
    private String ensureBearerPrefix(String jwt) {
        if (jwt != null && !jwt.startsWith("Bearer ")) {
            return "Bearer " + jwt;
        }
        return jwt;
    }
    
    // Subscribe to room events
    @MessageMapping("/room-events/subscribe")
    public void subscribeToRoomEvents(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== 📢 ROOM EVENTS SUBSCRIPTION ===");
            System.out.println("📥 Received room events subscription: " + payload);
            
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                System.err.println("❌ No JWT token provided for room events subscription");
                return;
            }
            
            User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
            String roomId = (String) payload.get("roomId");
            
            System.out.println("👤 User: " + user.getId() + " (" + user.getFname() + " " + user.getLname() + ")");
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("✅ Room events subscription successful");
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to subscribe to room events: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Unsubscribe from room events
    @MessageMapping("/room-events/unsubscribe")
    public void unsubscribeFromRoomEvents(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== 📢 ROOM EVENTS UNSUBSCRIPTION ===");
            System.out.println("📥 Received room events unsubscription: " + payload);
            
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                System.err.println("❌ No JWT token provided for room events unsubscription");
                return;
            }
            
            User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
            String roomId = (String) payload.get("roomId");
            
            System.out.println("👤 User: " + user.getId() + " (" + user.getFname() + " " + user.getLname() + ")");
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("✅ Room events unsubscription successful");
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to unsubscribe from room events: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
