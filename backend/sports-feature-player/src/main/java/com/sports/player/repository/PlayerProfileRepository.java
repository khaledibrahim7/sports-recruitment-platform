package com.sports.player.repository;

import com.sports.player.domain.PlayerPosition;
import com.sports.player.domain.PlayerProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
}
