package utils.Protocols;

public class EmailProtocol {
    // REQUEST CODES
    public static final String SEND = "SEND";
    public static final String LIST_INBOX = "LIST_INBOX";
    public static final String LIST_SENT = "LIST_SENT";
    public static final String READ = "READ";

    // RESPONSE CODES
    public static final String EMAIL_SENT = "EMAIL_SENT";
    public static final String EMAIL_NOT_FOUND = "EMAIL_NOT_FOUND";
    public static final String INBOX_EMPTY = "INBOX_EMPTY";
    public static final String SENT_EMPTY = "SENT_EMPTY";

    // WRONG REQUEST FORMAT CODES
    public static final String INVALID_FORMAT = "INVALID_FORMAT";
    public static final String UNKNOWN_COMMAND = "UNKNOWN_COMMAND";
}
