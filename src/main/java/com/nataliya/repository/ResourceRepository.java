package com.nataliya.repository;

import com.nataliya.model.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    Optional<Resource> findByUserIdAndPath(Long userId, String path);

    Optional<Resource> findByUserIdAndParentIdAndResourceName(Long userId, UUID parentId, String resourceName);

    List<Resource> findAllByUserIdAndParentPath(Long userId, String parentPath);

    boolean existsByUserIdAndPath(Long userId, String parentPath);

    boolean existsByUserIdAndParentIdAndResourceName(Long userId, UUID parentId, String resourceName);

}
