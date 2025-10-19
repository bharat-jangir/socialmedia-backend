package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface GroupRepo extends JpaRepository<Group, UUID> {
    
    // Find groups by user membership
    @Query("SELECT g FROM Group g JOIN g.members gm WHERE gm.user.id = :userId AND gm.status = 'ACTIVE' AND g.status = 'ACTIVE'")
    List<Group> findGroupsByUserId(@Param("userId") UUID userId);
    
    // Find groups by user membership with pagination
    @Query("SELECT g FROM Group g JOIN g.members gm WHERE gm.user.id = :userId AND gm.status = 'ACTIVE' AND g.status = 'ACTIVE'")
    Page<Group> findGroupsByUserIdPaginated(@Param("userId") UUID userId, Pageable pageable);
    
    // Find groups created by user
    @Query("SELECT g FROM Group g WHERE g.createdBy.id = :userId")
    List<Group> findGroupsCreatedByUserId(@Param("userId") UUID userId);
    
    // Find groups where user is admin
    @Query("SELECT g FROM Group g WHERE g.admin.id = :userId")
    List<Group> findGroupsWhereUserIsAdmin(@Param("userId") UUID userId);
    
    // Find public groups
    @Query("SELECT g FROM Group g WHERE g.isPublic = true AND g.status = 'ACTIVE'")
    List<Group> findPublicGroups();
    
    // Find public groups with pagination
    @Query("SELECT g FROM Group g WHERE g.isPublic = true AND g.status = 'ACTIVE'")
    Page<Group> findPublicGroupsPaginated(Pageable pageable);
    
    // Search groups by name
    @Query("SELECT g FROM Group g WHERE g.name LIKE %:query% AND g.status = 'ACTIVE'")
    List<Group> searchGroupsByName(@Param("query") String query);
    
    // Search groups by name with pagination
    @Query("SELECT g FROM Group g WHERE g.name LIKE %:query% AND g.status = 'ACTIVE'")
    Page<Group> searchGroupsByNamePaginated(@Param("query") String query, Pageable pageable);
    
    // Find groups by type
    @Query("SELECT g FROM Group g WHERE g.groupType = :groupType AND g.status = 'ACTIVE'")
    List<Group> findGroupsByType(@Param("groupType") Group.GroupType groupType);
    
    // Find groups by type with pagination
    @Query("SELECT g FROM Group g WHERE g.groupType = :groupType AND g.status = 'ACTIVE'")
    Page<Group> findGroupsByTypePaginated(@Param("groupType") Group.GroupType groupType, Pageable pageable);
    
    // Find groups with recent activity
    @Query("SELECT g FROM Group g WHERE g.lastActivity >= :since AND g.status = 'ACTIVE' ORDER BY g.lastActivity DESC")
    List<Group> findGroupsWithRecentActivity(@Param("since") java.time.LocalDateTime since);
    
    // Count groups by user
    @Query("SELECT COUNT(g) FROM Group g JOIN g.members gm WHERE gm.user.id = :userId AND gm.status = 'ACTIVE'")
    Long countGroupsByUserId(@Param("userId") UUID userId);
    
    // Find group by name (for uniqueness check)
    Optional<Group> findByName(String name);
    
    // Find groups that user can join (public groups where user is not a member)
    @Query("SELECT g FROM Group g WHERE g.isPublic = true AND g.status = 'ACTIVE' AND g.id NOT IN " +
           "(SELECT gm.group.id FROM GroupMember gm WHERE gm.user.id = :userId AND gm.status = 'ACTIVE')")
    List<Group> findJoinableGroupsForUser(@Param("userId") UUID userId);
    
    // Find groups that user can join with pagination
    @Query("SELECT g FROM Group g WHERE g.isPublic = true AND g.status = 'ACTIVE' AND g.id NOT IN " +
           "(SELECT gm.group.id FROM GroupMember gm WHERE gm.user.id = :userId AND gm.status = 'ACTIVE')")
    Page<Group> findJoinableGroupsForUserPaginated(@Param("userId") UUID userId, Pageable pageable);
}
