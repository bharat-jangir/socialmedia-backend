package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Post;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.entity.Comment;
import com.bharat.springbootsocial.entity.Reels;
import com.bharat.springbootsocial.repository.PostRepo;
import com.bharat.springbootsocial.repository.UserRepo;
import com.bharat.springbootsocial.response.PaginatedResponse;
import com.bharat.springbootsocial.response.PostResponse;
import com.bharat.springbootsocial.response.CommentResponse;
import com.bharat.springbootsocial.response.ContentResponse;
import com.bharat.springbootsocial.response.ReelsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostServiceImp implements PostService {
    @Autowired
    private PostRepo postRepo;
    @Autowired
    private ServiceInt userService;
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private ReelsService reelsService;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    public Post createPost(Post post, UUID userId) throws Exception {
        User user = userService.getUserById(userId);
        Post newPost = new Post();
        newPost.setCaption(post.getCaption());
        newPost.setImage(post.getImage());
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setVideo(post.getVideo());
        newPost.setUser(user);

        postRepo.save(newPost);
        return newPost;
    }

    @Override
    public String deletePost(UUID postId, UUID userId) throws Exception {
        Post post = findPostById(postId);
        if (post.getUser().getId().equals(userId)) {
            postRepo.deleteById(postId);
            return "Post deleted successfully";
        } else {
            throw new Exception("You are not authorized to delete this post");
        }
    }

    @Override
    public List<Post> findPostsByUserId(UUID userId) throws Exception {
        List<Post> posts = postRepo.findPostByUserId(userId);
        // Initialize lazy collections for each post
        for (Post post : posts) {
            post.getLikedBy().size(); // Trigger lazy loading
        }
        return posts;
    }

    @Override
    public Post findPostById(UUID postId) throws Exception {
        Post post = postRepo.findByIdWithComments(postId);
        if (post != null) {
            // Initialize the lazy collection to load likes
            post.getLikedBy().size(); // This triggers the lazy loading
            return post;
        } else {
            throw new Exception("Post not found");
        }
    }

    @Override
    public List<Post> findAllPosts() throws Exception {
        List<Post> posts = postRepo.findAll();
        // Initialize lazy collections for each post
        for (Post post : posts) {
            post.getLikedBy().size(); // Trigger lazy loading
        }
        return posts;
    }

    @Override
    public Post savedPost(UUID postId, UUID userId) throws Exception {
        Post post = findPostById(postId);
        User user = userService.getUserById(userId);
        if (user.getSavedPosts().contains(post)) {
            user.getSavedPosts().remove(post);
            userRepo.save(user);
        } else {
            user.getSavedPosts().add(post);
            userRepo.save(user);
        }
        return post;
    }

    @Override
    @Transactional
    public Post likedPost(UUID postId, UUID userId) throws Exception {
        Post post = findPostById(postId); // fetch post from DB
        User user = userService.getUserById(userId); // fetch user from DB
    
        // Check if the user already liked the post
        boolean isLiked = post.getLikedBy().stream()
                .anyMatch(likedUser -> likedUser.getId().equals(userId));
    
        if (isLiked) {
            // Unlike (remove from likedBy list)
            post.getLikedBy().removeIf(likedUser -> likedUser.getId().equals(userId));
        } else {
            // Like (add user to likedBy list)
            post.getLikedBy().add(user);
            
            // Send like notification
            notificationService.sendLikeNotification(post.getUser(), user, "POST", post.getId());
        }
    
        // Save changes
        return postRepo.save(post);
    }
    

    @Override
    public List<Post> findSavedPostsByUserId(UUID userId) throws Exception {
        User user = userService.getUserById(userId);
        List<Post> savedPosts = user.getSavedPosts();
        
        // Initialize lazy collections for all posts to avoid LazyInitializationException
        for (Post post : savedPosts) {
            post.getLikedBy().size(); // Trigger lazy loading
            post.getComments().size(); // Trigger lazy loading
            if (post.getUser() != null) {
                post.getUser().getFname(); // Trigger lazy loading
            }
        }
        
        return savedPosts;
    }
    
    @Override
    public List<UUID> findSavedPostIdsByUserId(UUID userId) throws Exception {
        User user = userService.getUserById(userId);
        return user.getSavedPosts().stream()
                .map(Post::getId)
                .toList();
    }
    
    // Paginated methods implementation
    @Override
    public PaginatedResponse<Post> findAllPostsPaginated(int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepo.findAllPostsPaginated(pageable);
        
        // Initialize lazy collections for each post
        for (Post post : postPage.getContent()) {
            post.getLikedBy().size(); // Trigger lazy loading
        }
        
        return createPaginatedResponse(postPage);
    }
    
    @Override
    public PaginatedResponse<Post> findPostsByUserIdPaginated(UUID userId, int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepo.findPostsByUserIdPaginated(userId, pageable);
        
        // Initialize lazy collections for each post
        for (Post post : postPage.getContent()) {
            post.getLikedBy().size(); // Trigger lazy loading
        }
        
        return createPaginatedResponse(postPage);
    }
    
    @Override
    public PaginatedResponse<Post> findSavedPostsByUserIdPaginated(UUID userId, int page, int size) throws Exception {
        User user = userService.getUserById(userId);
        List<Post> allSavedPosts = user.getSavedPosts();
        
        // Initialize lazy collections for all posts to avoid LazyInitializationException
        for (Post post : allSavedPosts) {
            post.getLikedBy().size(); // Trigger lazy loading
            post.getComments().size(); // Trigger lazy loading
            if (post.getUser() != null) {
                post.getUser().getFname(); // Trigger lazy loading
            }
        }
        
        // Manual pagination for saved posts
        int totalElements = allSavedPosts.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        // Handle edge case where startIndex is out of bounds
        if (startIndex >= totalElements) {
            startIndex = totalElements;
        }
        
        List<Post> paginatedPosts = allSavedPosts.subList(startIndex, endIndex);
        
        PaginatedResponse<Post> response = new PaginatedResponse<>();
        response.setContent(paginatedPosts);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setHasNext(page < totalPages - 1);
        response.setHasPrevious(page > 0);
        response.setFirst(page == 0);
        response.setLast(page == totalPages - 1);
        
        return response;
    }
    
    private PaginatedResponse<Post> createPaginatedResponse(Page<Post> postPage) {
        PaginatedResponse<Post> response = new PaginatedResponse<>();
        response.setContent(postPage.getContent());
        response.setPage(postPage.getNumber());
        response.setSize(postPage.getSize());
        response.setTotalElements(postPage.getTotalElements());
        response.setTotalPages(postPage.getTotalPages());
        response.setHasNext(postPage.hasNext());
        response.setHasPrevious(postPage.hasPrevious());
        response.setFirst(postPage.isFirst());
        response.setLast(postPage.isLast());
        return response;
    }
    
    // Optimized methods for better UI performance
    @Override
    public List<PostResponse> findAllPostsOptimized(UUID currentUserId) throws Exception {
        List<Post> posts = postRepo.findAll();
        return convertToOptimizedResponse(posts, currentUserId);
    }
    
    @Override
    public PaginatedResponse<PostResponse> findAllPostsOptimizedPaginated(UUID currentUserId, int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepo.findAllPostsPaginated(pageable);
        
        List<PostResponse> optimizedPosts = convertToOptimizedResponse(postPage.getContent(), currentUserId);
        
        PaginatedResponse<PostResponse> response = new PaginatedResponse<>();
        response.setContent(optimizedPosts);
        response.setPage(postPage.getNumber());
        response.setSize(postPage.getSize());
        response.setTotalElements(postPage.getTotalElements());
        response.setTotalPages(postPage.getTotalPages());
        response.setHasNext(postPage.hasNext());
        response.setHasPrevious(postPage.hasPrevious());
        response.setFirst(postPage.isFirst());
        response.setLast(postPage.isLast());
        return response;
    }
    
    @Override
    public List<PostResponse> findPostsByUserIdOptimized(UUID userId, UUID currentUserId) throws Exception {
        List<Post> posts = postRepo.findPostByUserId(userId);
        return convertToOptimizedResponse(posts, currentUserId);
    }
    
    @Override
    public PaginatedResponse<PostResponse> findPostsByUserIdOptimizedPaginated(UUID userId, UUID currentUserId, int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepo.findPostsByUserIdPaginated(userId, pageable);
        
        List<PostResponse> optimizedPosts = convertToOptimizedResponse(postPage.getContent(), currentUserId);
        
        PaginatedResponse<PostResponse> response = new PaginatedResponse<>();
        response.setContent(optimizedPosts);
        response.setPage(postPage.getNumber());
        response.setSize(postPage.getSize());
        response.setTotalElements(postPage.getTotalElements());
        response.setTotalPages(postPage.getTotalPages());
        response.setHasNext(postPage.hasNext());
        response.setHasPrevious(postPage.hasPrevious());
        response.setFirst(postPage.isFirst());
        response.setLast(postPage.isLast());
        return response;
    }
    
    @Override
    public PaginatedResponse<PostResponse> findSavedPostsByUserIdOptimizedPaginated(UUID userId, int page, int size) throws Exception {
        User user = userService.getUserById(userId);
        List<Post> allSavedPosts = user.getSavedPosts();
        
        // Initialize lazy collections for all posts to avoid LazyInitializationException
        for (Post post : allSavedPosts) {
            post.getLikedBy().size(); // Trigger lazy loading
            post.getComments().size(); // Trigger lazy loading
            if (post.getUser() != null) {
                post.getUser().getFname(); // Trigger lazy loading
            }
        }
        
        // Convert to optimized response
        List<PostResponse> optimizedPosts = convertToOptimizedResponse(allSavedPosts, userId);
        
        // Manual pagination for saved posts
        int totalElements = optimizedPosts.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        // Handle edge case where startIndex is out of bounds
        if (startIndex >= totalElements) {
            startIndex = totalElements;
        }
        
        List<PostResponse> paginatedPosts = optimizedPosts.subList(startIndex, endIndex);
        
        PaginatedResponse<PostResponse> response = new PaginatedResponse<>();
        response.setContent(paginatedPosts);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setHasNext(page < totalPages - 1);
        response.setHasPrevious(page > 0);
        response.setFirst(page == 0);
        response.setLast(page == totalPages - 1);
        
        return response;
    }
    
    private List<PostResponse> convertToOptimizedResponse(List<Post> posts, UUID currentUserId) {
        User currentUser = null;
        if (currentUserId != null) {
            try {
                currentUser = userService.getUserById(currentUserId);
            } catch (Exception e) {
                // If user not found, continue without current user context
            }
        }
        
        final User finalCurrentUser = currentUser;
        
        return posts.stream().map(post -> {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setCaption(post.getCaption());
            response.setImage(post.getImage());
            response.setVideo(post.getVideo());
            response.setUser(post.getUser());
            response.setCreatedAt(post.getCreatedAt());
            
            // Initialize lazy collections
            post.getLikedBy().size();
            post.getComments().size();
            
            // Set total counts
            response.setTotalLikes(post.getLikedBy().size());
            response.setTotalComments(post.getComments().size());
            
            // Get recent 3 likes from following/followers
            List<User> recentLikedBy = getRecentLikedBy(post.getLikedBy(), finalCurrentUser);
            response.setRecentLikedBy(recentLikedBy);
            
            // Get recent 3 comments
            List<PostResponse.CommentResponse> recentComments = getRecentComments(post.getComments(), currentUserId);
            response.setRecentComments(recentComments);
            
            return response;
        }).collect(Collectors.toList());
    }
    
    private List<User> getRecentLikedBy(List<User> allLikedBy, User currentUser) {
        if (currentUser == null || currentUser.getFollowing() == null) {
            // If no current user or following list, return latest 3
            return allLikedBy.stream()
                    .limit(3)
                    .collect(Collectors.toList());
        }
        
        // First priority: Current user if they liked the post
        User currentUserLike = allLikedBy.stream()
                .filter(user -> user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);
        
        // Second priority: Likes from following/followers (excluding current user)
        List<User> followingFollowersLikes = allLikedBy.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()) && 
                               (currentUser.getFollowing().contains(user.getId()) || 
                                currentUser.getFollowers().contains(user.getId())))
                .limit(currentUserLike != null ? 2 : 3) // Reserve 1 spot for current user if they liked
                .collect(Collectors.toList());
        
        // Combine current user (if they liked) with following/followers likes
        List<User> result = new ArrayList<>();
        if (currentUserLike != null) {
            result.add(currentUserLike);
        }
        result.addAll(followingFollowersLikes);
        
        // Third priority: Other users to make it 3 total (excluding already added users)
        if (result.size() < 3) {
            List<UUID> alreadyAddedIds = result.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            
            List<User> additionalLikes = allLikedBy.stream()
                    .filter(user -> !alreadyAddedIds.contains(user.getId()))
                    .limit(3 - result.size())
                    .collect(Collectors.toList());
            
            result.addAll(additionalLikes);
        }
        
        return result;
    }
    
    @Override
    public PaginatedResponse<User> getPostLikesPaginated(UUID postId, int page, int size) throws Exception {
        Post post = findPostById(postId);
        
        // Initialize lazy collection
        post.getLikedBy().size();
        
        List<User> allLikes = post.getLikedBy();
        
        // Manual pagination
        int totalElements = allLikes.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        // Handle edge case where startIndex is out of bounds
        if (startIndex >= totalElements) {
            startIndex = totalElements;
        }
        
        List<User> paginatedLikes = allLikes.subList(startIndex, endIndex);
        
        PaginatedResponse<User> response = new PaginatedResponse<>();
        response.setContent(paginatedLikes);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setHasNext(page < totalPages - 1);
        response.setHasPrevious(page > 0);
        response.setFirst(page == 0);
        response.setLast(page == totalPages - 1);
        
        return response;
    }
    
    @Override
    public PaginatedResponse<CommentResponse> getPostCommentsPaginated(UUID postId, UUID currentUserId, int page, int size) throws Exception {
        Post post = findPostById(postId);
        
        // Initialize lazy collection
        post.getComments().size();
        
        List<Comment> allComments = post.getComments();
        
        // Sort comments by creation date (latest first)
        allComments.sort((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()));
        
        // Manual pagination
        int totalElements = allComments.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        // Handle edge case where startIndex is out of bounds
        if (startIndex >= totalElements) {
            startIndex = totalElements;
        }
        
        List<Comment> paginatedComments = allComments.subList(startIndex, endIndex);
        
        // Convert to CommentResponse with totalLikes and isLiked
        List<CommentResponse> commentResponses = paginatedComments.stream().map(comment -> {
            // Initialize lazy collections
            comment.getLikedBy().size(); // Trigger lazy loading
            if (comment.getUser() != null) {
                comment.getUser().getFname(); // Trigger lazy loading
            }
            
            CommentResponse response = new CommentResponse();
            response.setId(comment.getId());
            response.setContent(comment.getContent());
            response.setUser(comment.getUser());
            response.setCreatedAt(comment.getCreatedAt());
            
            // Set total likes count
            response.setTotalLikes(comment.getLikedBy().size());
            
            // Check if current user liked this comment
            boolean isLiked = currentUserId != null && comment.getLikedBy().stream()
                    .anyMatch(user -> user.getId().equals(currentUserId));
            response.setIsLiked(isLiked);
            
            return response;
        }).collect(Collectors.toList());
        
        PaginatedResponse<CommentResponse> response = new PaginatedResponse<>();
        response.setContent(commentResponses);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setHasNext(page < totalPages - 1);
        response.setHasPrevious(page > 0);
        response.setFirst(page == 0);
        response.setLast(page == totalPages - 1);
        
        return response;
    }
    
    private List<PostResponse.CommentResponse> getRecentComments(List<Comment> allComments, UUID currentUserId) {
        return allComments.stream()
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt())) // Latest first
                .limit(3)
                .map(comment -> {
                    PostResponse.CommentResponse commentResponse = new PostResponse.CommentResponse();
                    commentResponse.setId(comment.getId());
                    commentResponse.setContent(comment.getContent());
                    commentResponse.setUser(comment.getUser());
                    commentResponse.setCreatedAt(comment.getCreatedAt());
                    
                    // Initialize lazy collection for comment likes
                    comment.getLikedBy().size();
                    commentResponse.setTotalLikes(comment.getLikedBy().size());
                    
                    // Check if current user liked this comment
                    boolean isLiked = currentUserId != null && comment.getLikedBy().stream()
                            .anyMatch(user -> user.getId().equals(currentUserId));
                    commentResponse.setIsLiked(isLiked);
                    
                    return commentResponse;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Object> findPostsAndReelsByUserId(UUID userId) throws Exception {
        // Get posts for the user
        List<Post> posts = findPostsByUserId(userId);
        
        // Get reels for the user
        List<Reels> reels = reelsService.findReelsByUserId(userId);
        
        // Combine posts and reels into a single list
        List<Object> combinedContent = new ArrayList<>();
        combinedContent.addAll(posts);
        combinedContent.addAll(reels);
        
        // Sort by creation date (newest first)
        combinedContent.sort((a, b) -> {
            LocalDateTime dateA, dateB;
            if (a instanceof Post) {
                dateA = ((Post) a).getCreatedAt();
            } else {
                dateA = ((Reels) a).getCreatedAt();
            }
            if (b instanceof Post) {
                dateB = ((Post) b).getCreatedAt();
            } else {
                dateB = ((Reels) b).getCreatedAt();
            }
            return dateB.compareTo(dateA); // Newest first
        });
        
        return combinedContent;
    }
    
    @Override
    public PaginatedResponse<ContentResponse> findAllPostsAndReelsOptimizedPaginated(UUID currentUserId, int page, int size) throws Exception {
        // Get posts with pagination
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepo.findAllPostsPaginated(pageable);
        
        // Get reels with pagination
        PaginatedResponse<ReelsResponse> reelsPaginated = reelsService.getAllReelsPaginatedWithCounts(page, size);
        
        // Convert posts to ContentResponse
        List<ContentResponse> postResponses = postPage.getContent().stream()
                .map(post -> {
                    // Initialize lazy collections
                    post.getLikedBy().size();
                    post.getComments().size();
                    
                    // Get recent liked by and comments (similar to existing logic)
                    List<User> recentLikedBy = getRecentLikedBy(post.getLikedBy(), userService.getUserById(currentUserId));
                    List<ContentResponse.CommentResponse> recentComments = getRecentCommentsForContent(post.getComments(), currentUserId);
                    
                    return new ContentResponse(post, recentLikedBy, recentComments);
                })
                .collect(Collectors.toList());
        
        // Convert reels to ContentResponse
        List<ContentResponse> reelResponses = reelsPaginated.getContent().stream()
                .map(reelsResponse -> {
                    // We need to get the actual Reels entity to access lazy collections
                    // For now, we'll create a basic ContentResponse from ReelsResponse
                    ContentResponse contentResponse = new ContentResponse();
                    contentResponse.setId(reelsResponse.getId());
                    contentResponse.setType("reel");
                    contentResponse.setTitle(reelsResponse.getTitle());
                    contentResponse.setVideo(reelsResponse.getVideo());
                    contentResponse.setUser(reelsResponse.getUser());
                    contentResponse.setTotalLikes(reelsResponse.getTotalLikes());
                    contentResponse.setTotalComments(reelsResponse.getTotalComments());
                    contentResponse.setCreatedAt(reelsResponse.getCreatedAt());
                    contentResponse.setRecentLikedBy(new ArrayList<>()); // Empty for now
                    contentResponse.setRecentComments(new ArrayList<>()); // Empty for now
                    
                    return contentResponse;
                })
                .collect(Collectors.toList());
        
        // Combine and sort by creation date
        List<ContentResponse> combinedContent = new ArrayList<>();
        combinedContent.addAll(postResponses);
        combinedContent.addAll(reelResponses);
        
        // Sort by creation date (newest first)
        combinedContent.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // Calculate pagination info
        int totalElements = (int) (postPage.getTotalElements() + reelsPaginated.getTotalElements());
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // Apply pagination to combined content
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, combinedContent.size());
        
        if (startIndex >= combinedContent.size()) {
            startIndex = combinedContent.size();
        }
        
        List<ContentResponse> paginatedContent = combinedContent.subList(startIndex, endIndex);
        
        // Create paginated response
        PaginatedResponse<ContentResponse> response = new PaginatedResponse<>();
        response.setContent(paginatedContent);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setHasNext(page < totalPages - 1);
        response.setHasPrevious(page > 0);
        response.setFirst(page == 0);
        response.setLast(page == totalPages - 1);
        
        return response;
    }
    
    private List<ContentResponse.CommentResponse> getRecentCommentsForContent(List<Comment> allComments, UUID currentUserId) {
        return allComments.stream()
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt())) // Latest first
                .limit(3)
                .map(comment -> {
                    ContentResponse.CommentResponse commentResponse = new ContentResponse.CommentResponse();
                    commentResponse.setId(comment.getId());
                    commentResponse.setContent(comment.getContent());
                    commentResponse.setUser(comment.getUser());
                    commentResponse.setCreatedAt(comment.getCreatedAt());
                    
                    // Initialize lazy collection for comment likes
                    comment.getLikedBy().size();
                    commentResponse.setTotalLikes(comment.getLikedBy().size());
                    
                    // Check if current user liked this comment
                    boolean isLiked = currentUserId != null && comment.getLikedBy().stream()
                            .anyMatch(user -> user.getId().equals(currentUserId));
                    commentResponse.setIsLiked(isLiked);
                    
                    return commentResponse;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ContentResponse> findPostsAndReelsByUserIdWithType(UUID userId, UUID currentUserId) throws Exception {
        // Get posts for the user
        List<Post> posts = findPostsByUserId(userId);
        
        // Get reels for the user
        List<Reels> reels = reelsService.findReelsByUserId(userId);
        
        // Convert posts to ContentResponse
        List<ContentResponse> postResponses = posts.stream()
                .map(post -> {
                    // Initialize lazy collections
                    post.getLikedBy().size();
                    post.getComments().size();
                    
                    // Get recent liked by and comments
                    User currentUser = null;
                    if (currentUserId != null) {
                        try {
                            currentUser = userService.getUserById(currentUserId);
                        } catch (Exception e) {
                            // If user not found, continue without current user context
                        }
                    }
                    
                    List<User> recentLikedBy = getRecentLikedBy(post.getLikedBy(), currentUser);
                    List<ContentResponse.CommentResponse> recentComments = getRecentCommentsForContent(post.getComments(), currentUserId);
                    
                    return new ContentResponse(post, recentLikedBy, recentComments);
                })
                .collect(Collectors.toList());
        
        // Convert reels to ContentResponse
        List<ContentResponse> reelResponses = reels.stream()
                .map(reel -> {
                    // Initialize lazy collections
                    reel.getLikedBy().size();
                    reel.getComments().size();
                    
                    // Get recent liked by and comments
                    User currentUser = null;
                    if (currentUserId != null) {
                        try {
                            currentUser = userService.getUserById(currentUserId);
                        } catch (Exception e) {
                            // If user not found, continue without current user context
                        }
                    }
                    
                    List<User> recentLikedBy = getRecentLikedBy(reel.getLikedBy(), currentUser);
                    List<ContentResponse.CommentResponse> recentComments = getRecentCommentsForContent(reel.getComments(), currentUserId);
                    
                    return new ContentResponse(reel, recentLikedBy, recentComments);
                })
                .collect(Collectors.toList());
        
        // Combine posts and reels
        List<ContentResponse> combinedContent = new ArrayList<>();
        combinedContent.addAll(postResponses);
        combinedContent.addAll(reelResponses);
        
        // Sort by creation date (newest first)
        combinedContent.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        return combinedContent;
    }
    
    @Override
    public List<ContentResponse> findCombinedSavedContentByUserId(UUID userId) throws Exception {
        // Get saved posts for the user
        List<Post> savedPosts = findSavedPostsByUserId(userId);
        
        // Get saved reels for the user
        List<Reels> savedReels = reelsService.findSavedReelsByUserId(userId);
        
        // Convert saved posts to ContentResponse
        List<ContentResponse> postResponses = savedPosts.stream()
                .map(post -> {
                    // Initialize lazy collections
                    post.getLikedBy().size();
                    post.getComments().size();
                    
                    // Get recent liked by and comments
                    User currentUser = null;
                    try {
                        currentUser = userService.getUserById(userId);
                    } catch (Exception e) {
                        // If user not found, continue without current user context
                    }
                    
                    List<User> recentLikedBy = getRecentLikedBy(post.getLikedBy(), currentUser);
                    List<ContentResponse.CommentResponse> recentComments = getRecentCommentsForContent(post.getComments(), userId);
                    
                    return new ContentResponse(post, recentLikedBy, recentComments);
                })
                .collect(Collectors.toList());
        
        // Convert saved reels to ContentResponse
        List<ContentResponse> reelResponses = savedReels.stream()
                .map(reel -> {
                    // Initialize lazy collections
                    reel.getLikedBy().size();
                    reel.getComments().size();
                    
                    // Get recent liked by and comments
                    User currentUser = null;
                    try {
                        currentUser = userService.getUserById(userId);
                    } catch (Exception e) {
                        // If user not found, continue without current user context
                    }
                    
                    List<User> recentLikedBy = getRecentLikedBy(reel.getLikedBy(), currentUser);
                    List<ContentResponse.CommentResponse> recentComments = getRecentCommentsForContent(reel.getComments(), userId);
                    
                    return new ContentResponse(reel, recentLikedBy, recentComments);
                })
                .collect(Collectors.toList());
        
        // Combine saved posts and reels
        List<ContentResponse> combinedContent = new ArrayList<>();
        combinedContent.addAll(postResponses);
        combinedContent.addAll(reelResponses);
        
        // Sort by creation date (newest first)
        combinedContent.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        return combinedContent;
    }
    
    @Override
    public PaginatedResponse<ContentResponse> findCombinedSavedContentByUserIdPaginated(UUID userId, int page, int size) throws Exception {
        // Get all combined saved content
        List<ContentResponse> allContent = findCombinedSavedContentByUserId(userId);
        
        // Calculate pagination
        int totalElements = allContent.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // Get the page content
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        // Handle edge case where startIndex is out of bounds
        if (startIndex >= totalElements) {
            startIndex = totalElements;
        }
        
        List<ContentResponse> pageContent = allContent.subList(startIndex, endIndex);
        
        PaginatedResponse<ContentResponse> response = new PaginatedResponse<>();
        response.setContent(pageContent);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements((long) totalElements);
        response.setTotalPages(totalPages);
        response.setHasNext(page < totalPages - 1);
        response.setHasPrevious(page > 0);
        response.setFirst(page == 0);
        response.setLast(page == totalPages - 1);
        
        return response;
    }
}
