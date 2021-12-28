package tor.secure.chat.protocol;

public class Protocol {

    public static final byte ERROR              = 1;
    public static final byte LOGIN              = 2;
    public static final byte REGISTER           = 3;
    public static final byte REQUEST_PUB_KEY    = 4;
    public static final byte SEND_MESSAGE       = 5;
    public static final byte SERVE_MESSAGES     = 6;
    public static final byte SERVE_PGP_KEYS     = 7;
    public static final byte SERVE_PUB_KEY      = 8;

}
