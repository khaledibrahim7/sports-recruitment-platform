package com.sports.player.repository;

import com.sports.player.domain.TrialOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrialOfferRepository extends JpaRepository<TrialOffer, Long> {

    @Query("SELECT t FROM TrialOffer t JOIN FETCH t.player p WHERE p.user.id = :playerUserId ORDER BY t.createdAt DESC")
    List<TrialOffer> findByPlayerUserIdWithPlayer(@Param("playerUserId") Long playerUserId);

    @Query("SELECT t FROM TrialOffer t JOIN FETCH t.player p WHERE t.scoutUserId = :scoutUserId ORDER BY t.createdAt DESC")
    List<TrialOffer> findByScoutUserIdWithPlayer(@Param("scoutUserId") Long scoutUserId);
}
