package org.apereo.cas.config;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CustomCustomCommunicationsManager implements CommunicationsManager {

    private JavaMailSender javaMailSender;
    private final JdbcTemplate jdbcTemplate;
    private final String from;
    private final String mailSubject;
    private final String sqlQuery;

    public CustomCustomCommunicationsManager(JavaMailSender javaMailSender, JdbcTemplate jdbcTemplate, String from, String mailSubject, String sqlQuery) {
        this.javaMailSender = javaMailSender;
        this.jdbcTemplate = jdbcTemplate;
        this.from = from;
        this.mailSubject = mailSubject;
        this.sqlQuery = sqlQuery;
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
        String tempDir = System.getProperty("java.io.tmpdir");
        User user = new User();
        if (emailRequest.getPrincipal() == null) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(tempDir+"user.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                user = (User) ois.readObject();
                ois.close();
                File file = new File(tempDir+"user.ser");
                Files.deleteIfExists(file.toPath());
            } catch (Exception e) {
                LOGGER.info("Exception occurred while User Registration");
            }
            String sql = "INSERT INTO USERS " +
                    "(USERNAME, PASSWORD,EMAIL,FIRSTNAME,LASTNAME) VALUES (?, ?,?, ?, ?)";
            try {
                jdbcTemplate.update(sql, new Object[]{user.getUsername(),
                        MD5EncoderUtil.getMD5EncodingString(user.getPassword()),
                        user.getEmail(), user.getFirstName(), user.getLastName()});
                LOGGER.info("User registered successfully");
            } catch (DataAccessException e) {
                LOGGER.info("Exception occurred while save user details:{}",e.getMessage());
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
            LOGGER.info("Exception occurred while pulling email for user: {}", username);
        }
        if (emails.isEmpty()) {
            LOGGER.info("Email is  not available for user: {}", username);
            return EmailCommunicationResult.builder().success(false).build();
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(emails.get(0));
        message.setSubject(mailSubject);
        message.setText(emailRequest.getBody());
        try {
            javaMailSender.send(message);
            LOGGER.info("Email has sent to :{},{}",emails.get(0),emailRequest.getPrincipal());
        } catch (Exception e) {
            LOGGER.info("Exception occurred while sending email to: {}, message is: {}", emails.get(0),e.getMessage());
        }
        return EmailCommunicationResult.builder().success(true).build();
    }

    @Override
    public boolean sms(SmsRequest smsRequest) {
        LOGGER.info("SMS send method invoked");
        try {
           // smsService.sendToken(smsRequest.getText(),"9164283325");
        } catch (Throwable e) {
            LOGGER.info("Exception occurred while sending sms, message is: {}",e.getMessage());
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