package com.nataliya.repository;

import com.nataliya.model.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    Optional<Resource> findByUserIdAndPath(Long userId, String path);

    Optional<Resource> findByUserIdAndParentIdAndResourceName(Long userId, UUID parentId, String resourceName);

    List<Resource> findAllByUserIdAndParentPath(Long userId, String parentPath);

    List<Resource> findByUserIdAndPathStartingWith(Long userId, String path);

    @Query("""
            SELECT r
            FROM Resource r
            WHERE r.user.id = :userId
            AND LOWER(r.resourceName) LIKE CONCAT('%', LOWER(:query), '%')
            """)
    List<Resource> searchByResourceName(Long userId, String query);

    boolean existsByUserIdAndPath(Long userId, String path);

    @Query("""
            SELECT COALESCE(SUM(r.size),0)
            FROM Resource r
            WHERE r.user.id = :userId
            AND r.resourceType = com.nataliya.model.ResourceType.FILE
            """)
    long getUserStorageUsage(Long userId);

    @Query("""
            SELECT COALESCE(SUM(r.size),0)
            FROM Resource r
            WHERE r.resourceType = com.nataliya.model.ResourceType.FILE
            """)
    long getTotalStorageUsage();
}
