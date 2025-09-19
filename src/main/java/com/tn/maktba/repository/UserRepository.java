package com.tn.maktba.repository;

import com.tn.maktba.model.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByIdCartNumber(String idCartNumber);
    Optional<UserEntity> findByIdCartNumber(String idCartNumber);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
}