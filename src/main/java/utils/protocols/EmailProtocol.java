package utils.protocols;

public class EmailProtocol {
    // REQUEST CODES
    public static final String SEND = "SEND";
    public static final String LIST_INBOX = "LIST_INBOX";
    public static final String LIST_SENT = "LIST_SENT";
    public static final String READ = "READ";

    // RESPONSE CODES
    public static final String SEND_EMAIL = "SENDEMAIL";
    public static final String GET_EMAILS = "GETEMAILS";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String NO_EMAILS = "NO_EMAILS";
    public static final String RECIPIENT_NOT_FOUND = "RECIPIENT_NOT_FOUND";
    public static final String INVALID_DETAILS = "INVALID_DETAILS";

    // WRONG REQUEST FORMAT CODES
    public static final String INVALID_FORMAT = "INVALID_FORMAT";
    public static final String UNKNOWN_COMMAND = "UNKNOWN_COMMAND";
}
