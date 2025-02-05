package org.apereo.cas.config;


import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.notifications.sms.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsService {
    @Autowired
    private SmsSender smsSender;
    public void sendToken(String token, String mobile){
        String message = String.format("Hello! Your requested CAS token is %s",token);
        try {
            smsSender.send("","+91 9164283325",message);
        } catch (Throwable e) {
            LOGGER.info("Exception occured while sendig email: {}",e.getMessage());
        }
    }
}
