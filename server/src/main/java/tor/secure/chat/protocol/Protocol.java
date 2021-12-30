package tor.secure.chat.protocol;

import static tor.secure.chat.common.byteutils.CryptoUtils.*;

import java.security.Key;

public class Protocol {

    public static final byte ERROR                         = 1;
    public static final byte LOGIN                         = 2;
    public static final byte REGISTER                      = 3;
    public static final byte REQUEST_PUB_KEY               = 4;
    public static final byte SEND_MESSAGE                  = 5;
    public static final byte SERVE_MESSAGES                = 6;
    public static final byte SERVE_PGP_KEYS                = 7;
    public static final byte SERVE_PUB_KEY                 = 8;

    public static final byte CONNECTION_ERROR_ERROR        = 0; //
    public static final byte USER_NOT_FOUND_ERROR          = 1;
    public static final byte USERNAME_ALREADY_EXISTS_ERROR = 2;
    public static final byte WRONG_PASSWORD_ERROR          = 3;

    public class Crypto {
    
        public static byte[] hash(byte[] digest) {
            return SHA256(SHA256(digest));
        }

        public static byte[] encryptAsimmetrically(byte[] data, Key publicKey) {
            return encryptRSA(data, publicKey);
        }

        public static byte[] decryptAsimmetrically(byte[] data, Key privateKey) {
            return decryptRSA(data, privateKey);
        }

        public static byte[] encryptSymmetrically(byte[] data, byte[] key) {
            byte[] leftPart = new byte[16];
            byte[] rightPart = new byte[16];
    
            byte[] keyHash = SHA256(key);
    
            for (int i = 0; i < 16; i++) {
                leftPart[i] = keyHash[i];
                rightPart[i] = keyHash[i + 16];
            }

            return encryptAES(data, leftPart, rightPart);
        }

        public static byte[] decryptSymmetrically(byte[] data, byte[] key) {
            byte[] leftPart = new byte[16];
            byte[] rightPart = new byte[16];
    
            byte[] keyHash = SHA256(key);
    
            for (int i = 0; i < 16; i++) {
                leftPart[i] = keyHash[i];
                rightPart[i] = keyHash[i + 16];
            }

            return decryptAES(data, leftPart, rightPart);
        }
    
    }
    
}
