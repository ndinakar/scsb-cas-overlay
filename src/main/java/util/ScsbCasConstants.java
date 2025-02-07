package util;

public class ScsbCasConstants {
public static final String NOT_AVAILABLE = "{} is not available for user: {}";
public static final String EXCEPTION_MESSAGE= "exception occurred while pulling {} for user: {}";
public static final String EXCEPTION= "exception while sending {}: {}";
public static final String SQL_MOBILE= "SELECT phone FROM users WHERE username = ?";
public static final String EXCEPTION_SAVE_USER= "exception while save user details: {}";
public static final String EXCEPTION_USER_REGISTRATION= "exception while User Registration: {}";
public static final String DIR= "java.io.tmpdir";
public static final String SER= "user.ser";

    private ScsbCasConstants() {
    }
}
