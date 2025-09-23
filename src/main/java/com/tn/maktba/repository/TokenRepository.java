package com.tn.maktba.repository;

import com.tn.maktba.model.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("SELECT t " +
            "FROM Token t " +
            "WHERE t.user.id = :userId " +
            "AND t.tokenType = 'ACCESS' " +
            "AND t.expired = false " +
            "AND t.revoked = false")
    List<Token> findAllValidTokensByUser(@Param("userId") Long userId);

    Optional<Token> findByToken(String token);
}