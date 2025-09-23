package com.tn.maktba.repository;

import com.tn.maktba.model.token.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
@Transactional(readOnly = true)
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    @Query("SELECT RT " +
            "FROM RefreshToken RT " +
            "WHERE RT.refreshToken = :refreshToken")
    Optional<RefreshToken> fetchByToken(@Param("refreshToken") String refreshToken);

    @Query("SELECT RT " +
            "FROM RefreshToken RT " +
            "WHERE RT.user.id = :userId")
    List<RefreshToken> fetchAllRefreshTokenByUserId(@Param("userId") Long userId);
}