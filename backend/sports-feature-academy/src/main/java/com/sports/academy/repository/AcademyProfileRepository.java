package com.sports.academy.repository;

import com.sports.academy.domain.AcademyProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademyProfileRepository extends JpaRepository<AcademyProfile, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<AcademyProfile> findAll();

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<AcademyProfile> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<AcademyProfile> findByUserId(Long userId);
}
