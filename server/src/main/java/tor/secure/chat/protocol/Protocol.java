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
    public static final byte REQUEST_NON_REPUDIATION_PROOF = 9;
    public static final byte SERVE_NON_REPUDIATION_PROOF   = 10;

    public static final byte CONNECTION_ERROR              = 0;
    public static final byte USER_NOT_FOUND_ERROR          = 1;
    public static final byte USERNAME_ALREADY_EXISTS_ERROR = 2;
    public static final byte WRONG_PASSWORD_ERROR          = 3;
    public static final byte ALREADY_LOGGED_ERROR          = 4;

    public class Crypto {
    
        public static byte[] salt(byte[] password, byte[] username) {
            byte[] result = new byte[password.length + username.length];

            for (int i = 0; i < username.length; i++) {
                result[i] = username[i];
            }

            for (int i = 0; i < password.length; i++) {
                result[i + username.length] = password[i];
            }

            return result;
        }

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

        public static byte[] saltPassword(byte[] password) {
            return null;
        }

        public static String computeFingerprint(byte[] publicKey1, byte[] publicKey2) {
            StringBuilder builder = new StringBuilder(4);
    
            byte[] publicKey1Hash = SHA256(publicKey1);
            byte[] publicKey2Hash = SHA256(publicKey2);
    
            byte[] merge = new byte[32];
    
            // merge publicKey1Hash and publicKey2Hash
            for (int i = 0; i < 32; i++) {
                merge[i] = (byte) (publicKey1Hash[i] ^ publicKey2Hash[i]);
            }
            
            for (int i = 0; i < 4; i++) {
                long v = 0;
    
                for (int j = 0; j < 8; j++) {
                    v |= merge[j + (i << 3)] << (j << 3);
                }

                v ^= v >>> 1;
                v <<= 1; // remove negative sign
                v >>>= 1;
                v %= Emojis.LIST.length;

                builder.append(Emojis.LIST[(int) v]);
            }
    
            return builder.toString();
        }
    
    }
    
}
