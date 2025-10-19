package com.bharat.springbootsocial.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtProvider {

    private static SecretKey key = Keys.hmacShaKeyFor(JwtConstants.SECRET_KEY.getBytes());
    private static final JwtParserBuilder jwtParserBuilder = Jwts.parser()
            .setSigningKey(key);

    public static String generateToken(Authentication auth) {
        String jwt = Jwts.builder()
                .setIssuer("bharat")
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 86400000))
                .claim("email", auth.getName())
                .signWith(Keys.hmacShaKeyFor(JwtConstants.SECRET_KEY.getBytes()))
                .compact();
        return jwt;
    }

    public static String getEmailFromJwtToken(String jwt) {
        if (jwt == null || !jwt.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        
        try {
            jwt = jwt.substring(7);
            Claims claims = jwtParserBuilder.build().parseClaimsJws(jwt).getBody();
            String email = (String) claims.get("email");
            if (email == null) {
                throw new IllegalArgumentException("Email not found in JWT token");
            }
            return email;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JWT token: " + e.getMessage());
        }
    }
}

