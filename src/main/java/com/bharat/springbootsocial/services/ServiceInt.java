package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.response.ProfileResponse;

import java.util.List;
import java.util.UUID;

public interface ServiceInt {
    User createUser(User user);

    List<User> getAllUsers();

    User getUserById(UUID id);

    User editUser(UUID id, User user);

    void deleteUser(UUID id);

    User followUser(UUID id1, UUID id2) throws UserException;

    User unfollowUser(UUID id1, UUID id2) throws UserException;

    boolean isFollowing(UUID id1, UUID id2) throws UserException;

    List<User> getFollowers(UUID userId);

    List<User> getFollowing(UUID userId);

    User findUserByEmail(String email);

    List<User> searchUser(String query);

    User getUserFromToken(String jwt);

    ProfileResponse getUserProfile(UUID userId) throws Exception;

    User updateCoverImage(UUID userId, String coverImageUrl) throws Exception;
}
