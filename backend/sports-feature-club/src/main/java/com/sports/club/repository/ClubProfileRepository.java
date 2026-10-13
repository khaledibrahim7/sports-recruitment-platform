package com.sports.club.repository;

import com.sports.club.domain.ClubProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubProfileRepository extends JpaRepository<ClubProfile, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<ClubProfile> findAll();

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<ClubProfile> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<ClubProfile> findByUserId(Long userId);
}
