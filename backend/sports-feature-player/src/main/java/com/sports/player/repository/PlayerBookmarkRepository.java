package com.sports.player.repository;

import com.sports.player.domain.PlayerBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerBookmarkRepository extends JpaRepository<PlayerBookmark, Long> {

    boolean existsByScoutUserIdAndPlayerId(Long scoutUserId, Long playerId);

    Optional<PlayerBookmark> findByScoutUserIdAndPlayerId(Long scoutUserId, Long playerId);

    @Query("SELECT b FROM PlayerBookmark b JOIN FETCH b.player p WHERE b.scoutUserId = :scoutUserId ORDER BY b.createdAt DESC")
    List<PlayerBookmark> findAllByScoutUserIdWithPlayer(@Param("scoutUserId") Long scoutUserId);

    @Query("SELECT b.player.id FROM PlayerBookmark b WHERE b.scoutUserId = :scoutUserId")
    List<Long> findBookmarkedPlayerIdsByScoutUserId(@Param("scoutUserId") Long scoutUserId);

    void deleteByScoutUserIdAndPlayerId(Long scoutUserId, Long playerId);
}
