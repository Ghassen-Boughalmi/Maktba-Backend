package com.tn.maktba.controller.sms;

import com.tn.maktba.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private final AuthService authService;

    public SmsController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String phoneNumber) {
        return authService.sendVerificationCode(phoneNumber);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestParam String phoneNumber, @RequestParam String code) {
        return authService.verifyCode(phoneNumber, code);
    }
}