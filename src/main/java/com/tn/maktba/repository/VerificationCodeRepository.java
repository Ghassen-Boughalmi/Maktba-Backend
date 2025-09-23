package com.tn.maktba.repository;

import com.tn.maktba.model.sms.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Integer> {

    @Query("SELECT v " +
            "FROM VerificationCode v " +
            "WHERE v.phoneNumber = :phoneNumber " +
            "AND v.code = :code AND v.expiresAt > CURRENT_TIMESTAMP")
    Optional<VerificationCode> findValidVerificationCode(@Param("phoneNumber") String phoneNumber, @Param("code") String code);

    @Query("SELECT v " +
            "FROM VerificationCode v " +
            "WHERE v.phoneNumber = :phoneNumber " +
            "AND v.expiresAt > CURRENT_TIMESTAMP " +
            "ORDER BY v.createdAt " +
            "DESC")
    Optional<VerificationCode> findLatestValidCodeByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode v " +
            "WHERE v.phoneNumber = :phoneNumber")
    void deleteByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}