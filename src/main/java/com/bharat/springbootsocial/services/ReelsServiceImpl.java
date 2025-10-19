package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Comment;
import com.bharat.springbootsocial.entity.Reels;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.repository.CommentRepo;
import com.bharat.springbootsocial.repository.ReelsRepo;
import com.bharat.springbootsocial.response.PaginatedResponse;
import com.bharat.springbootsocial.response.ReelsResponse;
import com.bharat.springbootsocial.response.CommentResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReelsServiceImpl implements ReelsService{
    @Autowired
    private ReelsRepo reelsRepo;
    @Autowired
    private CommentRepo commentRepo;
    @Autowired
    private ServiceInt userService;
    @Autowired
    private NotificationService notificationService;

    @Override
    public Reels createReel(Reels reels, User user) {
        Reels createdReel = new Reels();
        createdReel.setTitle(reels.getTitle());
        createdReel.setVideo(reels.getVideo());
        createdReel.setUser(user);
        createdReel.setCreatedAt(java.time.LocalDateTime.now());
        return reelsRepo.save(createdReel);
    }

    @Override
    public List<Reels> findAllReels() {
        return reelsRepo.findAll();
    }

    @Override
    public List<Reels> findReelsByUserId(UUID userId) {
        userService.getUserById(userId);
        return reelsRepo.findByUserId(userId);
    }
    
    // Paginated methods implementation
    @Override
    public PaginatedResponse<Reels> findAllReelsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reels> reelsPage = reelsRepo.findAllReelsPaginated(pageable);
        
        // Initialize lazy collections for each reel
        for (Reels reel : reelsPage.getContent()) {
            reel.getLikedBy().size(); // Trigger lazy loading
        }
        
        return createPaginatedResponse(reelsPage);
    }
    
    @Override
    public PaginatedResponse<Reels> findReelsByUserIdPaginated(UUID userId, int page, int size) {
        userService.getUserById(userId); // Validate user exists
        Pageable pageable = PageRequest.of(page, size);
        Page<Reels> reelsPage = reelsRepo.findReelsByUserIdPaginated(userId, pageable);
        
        // Initialize lazy collections for each reel
        for (Reels reel : reelsPage.getContent()) {
            reel.getLikedBy().size(); // Trigger lazy loading
        }
        
        return createPaginatedResponse(reelsPage);
    }
    
    private PaginatedResponse<Reels> createPaginatedResponse(Page<Reels> reelsPage) {
        PaginatedResponse<Reels> response = new PaginatedResponse<>();
        response.setContent(reelsPage.getContent());
        response.setPage(reelsPage.getNumber());
        response.setSize(reelsPage.getSize());
        response.setTotalElements(reelsPage.getTotalElements());
        response.setTotalPages(reelsPage.getTotalPages());
        response.setHasNext(reelsPage.hasNext());
        response.setHasPrevious(reelsPage.hasPrevious());
        response.setFirst(reelsPage.isFirst());
        response.setLast(reelsPage.isLast());
        return response;
    }
    
    // Like/Unlike methods implementation
    @Override
    public Reels findReelById(UUID reelId) throws Exception {
        Reels reel = reelsRepo.findByIdWithLikes(reelId);
        if (reel != null) {
            return reel;
        } else {
            throw new Exception("Reel not found");
        }
    }
    
    @Override
    @org.springframework.transaction.annotation.Transactional
    public Reels likeReel(UUID reelId, UUID userId) throws Exception {
        Reels reel = findReelById(reelId); // fetch reel from DB
        User user = userService.getUserById(userId); // fetch user from DB
    
        // Check if the user already liked the reel
        boolean isLiked = reel.getLikedBy().stream()
                .anyMatch(likedUser -> likedUser.getId().equals(userId));
    
        if (isLiked) {
            // Unlike (remove from likedBy list)
            reel.getLikedBy().removeIf(likedUser -> likedUser.getId().equals(userId));
        } else {
            // Like (add user to likedBy list)
            reel.getLikedBy().add(user);
        }
    
        // Save changes
        return reelsRepo.save(reel);
    }
    
    @Override
    @org.springframework.transaction.annotation.Transactional
    public String deleteReel(UUID reelId, UUID userId) throws Exception {
        Reels reel = findReelById(reelId);
        
        // Check if the user is the owner of the reel
        if (!reel.getUser().getId().equals(userId)) {
            throw new Exception("You can only delete your own reels");
        }
        
        // Delete the reel
        reelsRepo.delete(reel);
        
        return "Reel deleted successfully";
    }
    
    // Comment methods implementation
    @Override
    public Comment addCommentToReel(UUID reelId, String content, UUID userId) throws Exception {
        // Use findByIdWithComments to load comments
        Reels reel = reelsRepo.findByIdWithComments(reelId);
        if (reel == null) {
            throw new Exception("Reel not found");
        }
        
        User user = userService.getUserById(userId);
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setCreatedAt(java.time.LocalDateTime.now());
        
        // Save the comment first to get the ID
        Comment savedComment = commentRepo.save(comment);
        
        // Add the saved comment to the reel
        reel.getComments().add(savedComment);
        reelsRepo.save(reel);
        
        // Return the saved comment with ID
        return savedComment;
    }
    
    @Override
    public Comment updateCommentOnReel(UUID reelId, UUID commentId, String content, UUID userId) throws Exception {
        // Use findByIdWithComments to load comments
        Reels reel = reelsRepo.findByIdWithComments(reelId);
        if (reel == null) {
            throw new Exception("Reel not found");
        }
        
        // Find the comment to update
        Comment commentToUpdate = reel.getComments().stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElse(null);
        
        if (commentToUpdate == null) {
            throw new Exception("Comment not found");
        }
        
        // Check if the user is the owner of the comment
        if (!commentToUpdate.getUser().getId().equals(userId)) {
            throw new Exception("You can only update your own comments");
        }
        
        // Update the comment content
        commentToUpdate.setContent(content);
        
        // Save the reel (which will save the updated comment)
        reelsRepo.save(reel);
        
        // Return the updated comment
        return commentToUpdate;
    }
    
    @Override
    public Reels deleteCommentFromReel(UUID reelId, UUID commentId, UUID userId) throws Exception {
        // Use findByIdWithComments to load comments
        Reels reel = reelsRepo.findByIdWithComments(reelId);
        if (reel == null) {
            throw new Exception("Reel not found");
        }
        
        // Find the comment and check if user is authorized to delete it
        Comment commentToDelete = reel.getComments().stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new Exception("Comment not found"));
        
        // Check if user is the comment author or reel owner
        if (!commentToDelete.getUser().getId().equals(userId) && !reel.getUser().getId().equals(userId)) {
            throw new Exception("You are not authorized to delete this comment");
        }
        
        reel.getComments().remove(commentToDelete);
        return reelsRepo.save(reel);
    }
    
    @Override
    public Comment likeCommentOnReel(UUID reelId, UUID commentId, UUID userId) throws Exception {
        Reels reel = findReelById(reelId);
        User user = userService.getUserById(userId);
        
        // Find the comment in the reel's comments
        Comment comment = reel.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new Exception("Comment not found in this reel"));
        
        // Check if user has already liked the comment using ID comparison
        boolean isLiked = comment.getLikedBy().stream()
                .anyMatch(likedUser -> likedUser.getId().equals(userId));
        
        if (isLiked) {
            // Remove like
            comment.getLikedBy().removeIf(likedUser -> likedUser.getId().equals(userId));
        } else {
            // Add like
            comment.getLikedBy().add(user);
            
            // Send like notification
            notificationService.sendLikeNotification(comment.getUser(), user, "COMMENT", comment.getId());
        }
        
        return commentRepo.save(comment);
    }
    
    @Override
    public Comment likeCommentOnReelById(UUID commentId, UUID userId) throws Exception {
        // Find the reel that contains this comment
        Reels reel = reelsRepo.findAll().stream()
                .filter(r -> r.getComments().stream()
                        .anyMatch(c -> c.getId().equals(commentId)))
                .findFirst()
                .orElseThrow(() -> new Exception("Reel containing this comment not found"));
        
        // Use the existing method with the found reel ID
        return likeCommentOnReel(reel.getId(), commentId, userId);
    }
    
    // Paginated comment methods implementation
    @Override
    public PaginatedResponse<CommentResponse> getCommentsByReelIdPaginated(UUID reelId, UUID currentUserId, int page, int size) throws Exception {
        // Validate that reel exists
        findReelById(reelId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> commentsPage = commentRepo.findCommentsByReelIdPaginated(reelId, pageable);
        
        // Convert to CommentResponse with totalLikes and isLiked
        List<CommentResponse> commentResponses = commentsPage.getContent().stream().map(comment -> {
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
        }).collect(java.util.stream.Collectors.toList());
        
        PaginatedResponse<CommentResponse> response = new PaginatedResponse<>();
        response.setContent(commentResponses);
        response.setPage(commentsPage.getNumber());
        response.setSize(commentsPage.getSize());
        response.setTotalElements(commentsPage.getTotalElements());
        response.setTotalPages(commentsPage.getTotalPages());
        response.setHasNext(commentsPage.hasNext());
        response.setHasPrevious(commentsPage.hasPrevious());
        response.setFirst(commentsPage.isFirst());
        response.setLast(commentsPage.isLast());
        
        return response;
    }
    
    private PaginatedResponse<Comment> createCommentPaginatedResponse(Page<Comment> commentsPage) {
        PaginatedResponse<Comment> response = new PaginatedResponse<>();
        response.setContent(commentsPage.getContent());
        response.setPage(commentsPage.getNumber());
        response.setSize(commentsPage.getSize());
        response.setTotalElements(commentsPage.getTotalElements());
        response.setTotalPages(commentsPage.getTotalPages());
        response.setHasNext(commentsPage.hasNext());
        response.setHasPrevious(commentsPage.hasPrevious());
        response.setFirst(commentsPage.isFirst());
        response.setLast(commentsPage.isLast());
        return response;
    }
    
    // Response methods with counts implementation
    @Override
    public ReelsResponse getReelByIdWithCounts(UUID reelId) throws Exception {
        Reels reel = findReelById(reelId);
        return new ReelsResponse(reel);
    }
    
    @Override
    public List<ReelsResponse> getAllReelsWithCounts() {
        List<Reels> reels = findAllReels();
        return reels.stream()
                .map(ReelsResponse::new)
                .toList();
    }
    
    @Override
    public List<ReelsResponse> getReelsByUserIdWithCounts(UUID userId) {
        List<Reels> reels = findReelsByUserId(userId);
        return reels.stream()
                .map(ReelsResponse::new)
                .toList();
    }
    
    @Override
    public PaginatedResponse<ReelsResponse> getAllReelsPaginatedWithCounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reels> reelsPage = reelsRepo.findAllReelsPaginated(pageable);
        
        // Initialize lazy collections for each reel (only likedBy, not comments)
        for (Reels reel : reelsPage.getContent()) {
            reel.getLikedBy().size(); // Trigger lazy loading for likes
            // Don't load comments since we're not sending them
        }
        
        // Convert to ReelsResponse
        List<ReelsResponse> reelsResponses = reelsPage.getContent().stream()
                .map(ReelsResponse::new)
                .toList();
        
        PaginatedResponse<ReelsResponse> response = new PaginatedResponse<>();
        response.setContent(reelsResponses);
        response.setPage(reelsPage.getNumber());
        response.setSize(reelsPage.getSize());
        response.setTotalElements(reelsPage.getTotalElements());
        response.setTotalPages(reelsPage.getTotalPages());
        response.setHasNext(reelsPage.hasNext());
        response.setHasPrevious(reelsPage.hasPrevious());
        response.setFirst(reelsPage.isFirst());
        response.setLast(reelsPage.isLast());
        return response;
    }
    
    @Override
    public PaginatedResponse<ReelsResponse> getReelsByUserIdPaginatedWithCounts(UUID userId, int page, int size) {
        userService.getUserById(userId); // Validate user exists
        Pageable pageable = PageRequest.of(page, size);
        Page<Reels> reelsPage = reelsRepo.findReelsByUserIdPaginated(userId, pageable);
        
        // Initialize lazy collections for each reel (only likedBy, not comments)
        for (Reels reel : reelsPage.getContent()) {
            reel.getLikedBy().size(); // Trigger lazy loading for likes
            // Don't load comments since we're not sending them
        }
        
        // Convert to ReelsResponse
        List<ReelsResponse> reelsResponses = reelsPage.getContent().stream()
                .map(ReelsResponse::new)
                .toList();
        
        PaginatedResponse<ReelsResponse> response = new PaginatedResponse<>();
        response.setContent(reelsResponses);
        response.setPage(reelsPage.getNumber());
        response.setSize(reelsPage.getSize());
        response.setTotalElements(reelsPage.getTotalElements());
        response.setTotalPages(reelsPage.getTotalPages());
        response.setHasNext(reelsPage.hasNext());
        response.setHasPrevious(reelsPage.hasPrevious());
        response.setFirst(reelsPage.isFirst());
        response.setLast(reelsPage.isLast());
        return response;
    }
    
    // Save/Unsave methods implementation
    @Override
    public Reels savedReel(UUID reelId, UUID userId) throws Exception {
        Reels reel = findReelById(reelId);
        User user = userService.getUserById(userId);
        if (user.getSavedReels().contains(reel)) {
            user.getSavedReels().remove(reel);
            userService.editUser(user.getId(), user);
        } else {
            user.getSavedReels().add(reel);
            userService.editUser(user.getId(), user);
        }
        return reel;
    }
    
    @Override
    public List<Reels> findSavedReelsByUserId(UUID userId) throws Exception {
        User user = userService.getUserById(userId);
        List<Reels> savedReels = user.getSavedReels();
        
        // Initialize lazy collections for all reels to avoid LazyInitializationException
        for (Reels reel : savedReels) {
            reel.getLikedBy().size(); // Trigger lazy loading
            reel.getComments().size(); // Trigger lazy loading
            if (reel.getUser() != null) {
                reel.getUser().getFname(); // Trigger lazy loading
            }
        }
        
        return savedReels;
    }
    
    @Override
    public List<UUID> findSavedReelIdsByUserId(UUID userId) throws Exception {
        User user = userService.getUserById(userId);
        return user.getSavedReels().stream()
                .map(Reels::getId)
                .toList();
    }
    
    @Override
    public PaginatedResponse<Reels> findSavedReelsByUserIdPaginated(UUID userId, int page, int size) throws Exception {
        User user = userService.getUserById(userId);
        List<Reels> allSavedReels = user.getSavedReels();
        
        // Initialize lazy collections for all reels to avoid LazyInitializationException
        for (Reels reel : allSavedReels) {
            reel.getLikedBy().size(); // Trigger lazy loading
            reel.getComments().size(); // Trigger lazy loading
            if (reel.getUser() != null) {
                reel.getUser().getFname(); // Trigger lazy loading
            }
        }
        
        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, allSavedReels.size());
        
        List<Reels> paginatedReels = allSavedReels.subList(start, end);
        
        PaginatedResponse<Reels> response = new PaginatedResponse<>();
        response.setContent(paginatedReels);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements((long) allSavedReels.size());
        response.setTotalPages((int) Math.ceil((double) allSavedReels.size() / size));
        response.setHasNext(end < allSavedReels.size());
        response.setHasPrevious(page > 0);
        response.setFirst(page == 0);
        response.setLast(end >= allSavedReels.size());
        
        return response;
    }
    
    @Override
    public PaginatedResponse<ReelsResponse> findSavedReelsByUserIdOptimizedPaginated(UUID userId, int page, int size) throws Exception {
        User user = userService.getUserById(userId);
        List<Reels> allSavedReels = user.getSavedReels();
        
        // Initialize lazy collections for all reels to avoid LazyInitializationException
        for (Reels reel : allSavedReels) {
            reel.getLikedBy().size(); // Trigger lazy loading
            reel.getComments().size(); // Trigger lazy loading
            if (reel.getUser() != null) {
                reel.getUser().getFname(); // Trigger lazy loading
            }
        }
        
        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, allSavedReels.size());
        
        List<Reels> paginatedReels = allSavedReels.subList(start, end);
        
        // Convert to ReelsResponse
        List<ReelsResponse> reelsResponses = paginatedReels.stream()
                .map(ReelsResponse::new)
                .toList();
        
        PaginatedResponse<ReelsResponse> response = new PaginatedResponse<>();
        response.setContent(reelsResponses);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements((long) allSavedReels.size());
        response.setTotalPages((int) Math.ceil((double) allSavedReels.size() / size));
        response.setHasNext(end < allSavedReels.size());
        response.setHasPrevious(page > 0);
        response.setFirst(page == 0);
        response.setLast(end >= allSavedReels.size());
        
        return response;
    }
}
