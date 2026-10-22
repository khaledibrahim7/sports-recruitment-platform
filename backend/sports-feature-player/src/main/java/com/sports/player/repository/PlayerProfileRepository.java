package com.sports.player.repository;

import com.sports.player.domain.PlayerPosition;
import com.sports.player.domain.PlayerProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "videoUrls"})
    List<PlayerProfile> findAll();

    @Override
    @EntityGraph(attributePaths = {"user", "videoUrls"})
    Optional<PlayerProfile> findById(Long id);

    @EntityGraph(attributePaths = {"user", "videoUrls"})
    Optional<PlayerProfile> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "videoUrls"})
    @Query("SELECT DISTINCT p FROM PlayerProfile p WHERE p.position = :position OR :position MEMBER OF p.secondaryPositions")
    List<PlayerProfile> findByPosition(@Param("position") PlayerPosition position);

    @EntityGraph(attributePaths = {"user", "videoUrls"})
    List<PlayerProfile> findByAge(Integer age);

    @EntityGraph(attributePaths = {"user", "videoUrls"})
    @Query("SELECT DISTINCT p FROM PlayerProfile p WHERE (p.position = :position OR :position MEMBER OF p.secondaryPositions) AND p.age = :age")
    List<PlayerProfile> findByPositionAndAge(@Param("position") PlayerPosition position, @Param("age") Integer age);

    @Query(value = "SELECT DISTINCT p.id FROM PlayerProfile p WHERE (:position IS NULL OR p.position = :position OR :position MEMBER OF p.secondaryPositions) " +
            "AND (:age IS NULL OR p.age = :age) AND (:query IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(COALESCE(p.bio, '')) LIKE LOWER(CONCAT('%', :query, '%')))",
            countQuery = "SELECT COUNT(DISTINCT p) FROM PlayerProfile p WHERE (:position IS NULL OR p.position = :position OR :position MEMBER OF p.secondaryPositions) " +
                    "AND (:age IS NULL OR p.age = :age) AND (:query IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(COALESCE(p.bio, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Long> searchPublicPlayerIds(@Param("position") PlayerPosition position, @Param("age") Integer age,
                                     @Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"videoUrls"})
    List<PlayerProfile> findAllByIdIn(List<Long> ids);
}
