package utils.Protocols;

public class UserProtocol {
    // COMMAND CODES
    public static final String REGISTER = "REGISTER";
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";

    // RESPONSE CODES
    public static final String SUCCESS = "SUCCESS";
    public static final String INVALID_DETAILS = "INVALID_DETAILS";
    public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public static final String NO_USER = "NO_USER";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

    // WRONG REQUEST FORMAT CODES
    public static final String INVALID_FORMAT = "INVALID_FORMAT";
}
