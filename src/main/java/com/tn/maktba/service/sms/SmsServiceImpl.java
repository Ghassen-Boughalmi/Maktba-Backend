package com.tn.maktba.service.sms;

import com.tn.maktba.model.sms.TwilioConfiguration;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    private final TwilioConfiguration twilioConfiguration;

    public SmsServiceImpl(TwilioConfiguration twilioConfiguration) {
        this.twilioConfiguration = twilioConfiguration;
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        try {
            String toPhoneNumber = phoneNumber.startsWith("+216") ? phoneNumber : "+216" + phoneNumber;
            String fromPhoneNumber = twilioConfiguration.getTrialNumber();
            if (fromPhoneNumber == null || fromPhoneNumber.isEmpty()) {
                throw new IllegalArgumentException("Twilio trial number is not configured");
            }

            Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    message
            ).create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }
}