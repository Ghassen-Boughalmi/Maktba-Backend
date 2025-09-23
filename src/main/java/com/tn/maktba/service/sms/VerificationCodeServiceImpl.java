package com.tn.maktba.service.sms;

import com.tn.maktba.exceptions.ExpiredVerificationCodeException;
import com.tn.maktba.exceptions.InvalidVerificationCodeException;
import com.tn.maktba.exceptions.ResourceNotFoundException;
import com.tn.maktba.model.sms.VerificationCode;
import com.tn.maktba.model.user.UserEntity;
import com.tn.maktba.repository.VerificationCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final SmsService smsService;

    public VerificationCodeServiceImpl(VerificationCodeRepository verificationCodeRepository, SmsService smsService) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.smsService = smsService;
    }

    @Override
    @Transactional
    public String generateVerificationCode(UserEntity user) {
        verificationCodeRepository.deleteByPhoneNumber(user.getPhoneNumber());

        String code = String.format("%06d", new SecureRandom().nextInt(999999));
        VerificationCode verificationCode = VerificationCode.builder()
                .code(code)
                .phoneNumber(user.getPhoneNumber())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .user(user)
                .build();

        verificationCodeRepository.save(verificationCode);
        String message = String.format("Your verification code is: %s", code);
        smsService.sendSms(user.getPhoneNumber(), message);
        return code;
    }

    @Override
    public VerificationCode fetchVerificationCodeByPhoneNumber(String phoneNumber) {
        return verificationCodeRepository.findLatestValidCodeByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Verification code not found for phone number: " + phoneNumber));
    }

    @Override
    @Transactional
    public void validateVerificationCode(String phoneNumber, String code) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Verification code cannot be null or empty");
        }

        try {
            VerificationCode storedCode = verificationCodeRepository.findValidVerificationCode(phoneNumber, code)
                    .orElseThrow(() -> new InvalidVerificationCodeException("Invalid verification code"));

            if (Instant.now().isAfter(storedCode.getExpiresAt())) {
                verificationCodeRepository.delete(storedCode);
                throw new ExpiredVerificationCodeException("Verification code has expired. Please request a new one.");
            }

            verificationCodeRepository.delete(storedCode);

        } catch (InvalidVerificationCodeException e) {
            System.out.println("Failed verification attempt for phone: " + phoneNumber);
            throw e;
        }
    }
}