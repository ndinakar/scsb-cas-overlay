package org.apereo.cas.config;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.apereo.cas.notifications.sms.SmsSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SnsSmsSender implements SmsSender {

    private final AmazonSNS snsClient;

    @Value("${aws.sns.region}")
    private String region;


    public SnsSmsSender() {
        this.snsClient = AmazonSNSClientBuilder.standard().withRegion(region).build();
    }

    @Override
    public boolean send(String from, String to,String message) {

        String fullPhoneNumber = "+91" + to;
        PublishRequest publishRequest = new PublishRequest()
                .withMessage(message)
                .withPhoneNumber(fullPhoneNumber);

        try {
            PublishResult result = snsClient.publish(publishRequest);
            System.out.println("Message sent with message ID: " + result.getMessageId());
        } catch (Exception e) {
            System.err.println("Error sending SMS: " + e.getMessage());
        }
        return true;
    }
}
