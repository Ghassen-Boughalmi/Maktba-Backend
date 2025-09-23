package com.tn.maktba.service.sms;

import com.tn.maktba.model.sms.VerificationCode;
import com.tn.maktba.model.user.UserEntity;

public interface VerificationCodeService {
    String generateVerificationCode(UserEntity user);
    VerificationCode fetchVerificationCodeByPhoneNumber(String phoneNumber);
    void validateVerificationCode(String phoneNumber, String code);
}