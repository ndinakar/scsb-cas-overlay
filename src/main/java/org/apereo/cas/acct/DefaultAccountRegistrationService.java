package org.apereo.cas.acct;

import entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.acct.provision.AccountRegistrationProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import util.ScsbCasConstants;

import java.io.*;

/**
 * This is {@link DefaultAccountRegistrationService}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@AllArgsConstructor
@Getter
@Slf4j
@Service
public class DefaultAccountRegistrationService implements AccountRegistrationService {


    private final AccountRegistrationPropertyLoader accountRegistrationPropertyLoader;

    private final CasConfigurationProperties casProperties;

    private final CipherExecutor<Serializable, String> cipherExecutor;

    private final AccountRegistrationUsernameBuilder accountRegistrationUsernameBuilder;

    private AccountRegistrationProvisioner accountRegistrationProvisioner;


    @Override
    public AccountRegistrationRequest validateToken(final String token) throws Exception {
        return new AccountRegistrationRequest();
    }

    @Override
    public String createToken(final AccountRegistrationRequest registrationRequest) {

        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(registrationRequest.getPassword());
        user.setEmail(registrationRequest.getEmail());
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        user.setPhone(registrationRequest.getPhone());

        try {
            String tempDir = System.getProperty(ScsbCasConstants.DIR);
            FileOutputStream fos = new FileOutputStream(tempDir + ScsbCasConstants.SER);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(user);
            oos.close();
        } catch (Exception e) {
            LOGGER.info(ScsbCasConstants.EXCEPTION_USER_REGISTRATION, e.getMessage());
        }
        return "success";
    }
}