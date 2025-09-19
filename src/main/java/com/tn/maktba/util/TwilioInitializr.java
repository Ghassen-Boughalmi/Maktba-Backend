package com.tn.maktba.util;

import com.tn.maktba.model.sms.TwilioConfiguration;
import com.twilio.Twilio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioInitializr {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioInitializr.class);

    public TwilioInitializr(TwilioConfiguration twilioConfiguration) {
        Twilio.init(
                twilioConfiguration.getAccountSid(),
                twilioConfiguration.getAuthToken()
        );
        LOGGER.info("Twilio initialized with account SID: {}, trial number: {}",
                twilioConfiguration.getAccountSid(),
                twilioConfiguration.getTrialNumber());
    }
}