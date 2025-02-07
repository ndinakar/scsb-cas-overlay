package org.apereo.cas.config;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.call.PhoneCallRequest;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import util.MD5EncoderUtil;
import util.ScsbCasConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CustomCustomCommunicationsManager implements CommunicationsManager {

    private AmazonSNS snsClient;
    private JavaMailSender javaMailSender;
    private final JdbcTemplate jdbcTemplate;
    private final String from;
    private final String mailSubject;
    private final String sqlQuery;

    public CustomCustomCommunicationsManager(AmazonSNS snsClient, JavaMailSender javaMailSender, JdbcTemplate jdbcTemplate, String from, String mailSubject, String sqlQuery) {
        this.javaMailSender = javaMailSender;
        this.jdbcTemplate = jdbcTemplate;
        this.from = from;
        this.mailSubject = mailSubject;
        this.sqlQuery = sqlQuery;
        this.snsClient = snsClient;
    }

    @Override
    public boolean isMailSenderDefined() {
        return true;
    }

    @Override
    public boolean isSmsSenderDefined() {
        return true;
    }

    @Override
    public boolean isNotificationSenderDefined() {
        return false;
    }

    @Override
    public boolean isPhoneOperatorDefined() {
        return false;
    }

    @Override
    public boolean notify(Principal principal, String title, String body) {
        return false;
    }


    @Override
    public EmailCommunicationResult email(EmailMessageRequest emailRequest) {
        String username;
        String tempDir = System.getProperty(ScsbCasConstants.DIR);
        User user = new User();
        if (emailRequest.getPrincipal() == null) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(tempDir + ScsbCasConstants.SER);
                ObjectInputStream ois = new ObjectInputStream(fis);
                user = (User) ois.readObject();
                ois.close();
                File file = new File(tempDir + ScsbCasConstants.SER);
                Files.deleteIfExists(file.toPath());
            } catch (Exception e) {
                LOGGER.info(ScsbCasConstants.EXCEPTION_USER_REGISTRATION, e.getMessage());
            }
            String sql = "INSERT INTO USERS " +
                    "(USERNAME, PASSWORD, EMAIL, PHONE, FIRSTNAME, LASTNAME) VALUES (?, ?, ?, ?, ?, ?)";
            try {
                jdbcTemplate.update(sql, new Object[]{user.getUsername(),
                        MD5EncoderUtil.getMD5EncodingString(user.getPassword()),
                        user.getEmail(), user.getPhone(), user.getFirstName(), user.getLastName()});
            } catch (DataAccessException e) {
                LOGGER.info(ScsbCasConstants.EXCEPTION_SAVE_USER, e.getMessage());
            }
            return EmailCommunicationResult.builder().success(true).build();
        } else {
            username = emailRequest.getPrincipal().getId();
        }
        List<String> emails = new ArrayList<>();
        try {
            emails = jdbcTemplate.query(
                    sqlQuery,
                    new Object[]{username},
                    (rs, rowNum) -> rs.getString("email")
            );
        } catch (Exception e) {
            LOGGER.info(ScsbCasConstants.EXCEPTION_MESSAGE, "Email", username);
        }
        if (emails.isEmpty()) {
            LOGGER.info(ScsbCasConstants.NOT_AVAILABLE, "Email", username);
            return EmailCommunicationResult.builder().success(false).build();
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(emails.get(0));
        message.setSubject(mailSubject);
        message.setText(emailRequest.getBody());
        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            LOGGER.info(ScsbCasConstants.EXCEPTION, "EMAIL", e.getMessage());
        }
        return EmailCommunicationResult.builder().success(true).build();
    }

    @Override
    public boolean sms(SmsRequest smsRequest) {
        if (smsRequest.getPrincipal() == null) {
            List<String> phones = new ArrayList<>();
            String sqlMobile = ScsbCasConstants.SQL_MOBILE;
            String username = smsRequest.getPrincipal().getId();
            if (null != username && !username.isEmpty()) {
                try {
                    phones = jdbcTemplate.query(
                            sqlMobile,
                            new Object[]{username},
                            (rs, rowNum) -> rs.getString("phone")
                    );
                } catch (Exception e) {
                    LOGGER.info(ScsbCasConstants.EXCEPTION_MESSAGE, "Mobile Number", username);
                }
            }
            if (phones.isEmpty()) {
                LOGGER.info(ScsbCasConstants.NOT_AVAILABLE, "Mobile Number", username);
                return true;
            } else {
                String fullPhoneNumber = phones.get(0);
                PublishRequest publishRequest = new PublishRequest()
                        .withMessage("Hello! Your requested CAS token is " + smsRequest.getText())
                        .withPhoneNumber(fullPhoneNumber);
                try {
                    PublishResult result = snsClient.publish(publishRequest);
                } catch (Exception e) {
                    LOGGER.info(ScsbCasConstants.EXCEPTION, "SMS", e.getMessage());
                }
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean phoneCall(PhoneCallRequest request) throws Throwable {
        return false;
    }

    @Override
    public boolean validate() {
        return false;
    }
}