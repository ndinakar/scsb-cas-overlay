package org.apereo.cas.config;


import org.apereo.cas.notifications.sms.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmsService {
    @Autowired
    private SmsSender smsSender;
    public void sendToken(String token, String mobil){
        String message = String.format("Hello! Your requested CAS token is %s",token);
        try {
            smsSender.send("","",message);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
