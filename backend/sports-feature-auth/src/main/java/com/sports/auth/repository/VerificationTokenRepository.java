package com.sports.auth.repository;

import com.sports.auth.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from VerificationToken t where t.tokenHash = :hash and t.type = :type and t.consumedAt is null and t.expiresAt > :now")
    Optional<VerificationToken> findValidForUpdate(@Param("hash") String tokenHash,
                                                   @Param("type") VerificationToken.Type type,
                                                   @Param("now") Instant now);
    void deleteByUser_IdAndType(Long userId, VerificationToken.Type type);
    Optional<VerificationToken> findTopByUser_IdAndTypeOrderByIdDesc(Long userId, VerificationToken.Type type);
}
