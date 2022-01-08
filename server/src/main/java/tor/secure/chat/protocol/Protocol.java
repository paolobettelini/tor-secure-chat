package tor.secure.chat.protocol;

import static tor.secure.chat.common.byteutils.CryptoUtils.*;

import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import tor.secure.chat.common.byteutils.CryptoUtils;

public class Protocol {

    public static final byte ERROR                         = 1;
    public static final byte LOGIN                         = 2;
    public static final byte REGISTER                      = 3;
    public static final byte REQUEST_PUB_KEY               = 4;
    public static final byte SEND_MESSAGE                  = 5;
    public static final byte SERVE_MESSAGES                = 6;
    public static final byte SERVE_KEY_PAIR                = 7;
    public static final byte SERVE_PUB_KEY                 = 8;
    public static final byte REQUEST_NON_REPUDIATION_PROOF = 9;
    public static final byte SERVE_NON_REPUDIATION_PROOF   = 10;

    public static final byte CONNECTION_ERROR              = 0;
    public static final byte USER_NOT_FOUND_ERROR          = 1;
    public static final byte USERNAME_ALREADY_EXISTS_ERROR = 2;
    public static final byte WRONG_PASSWORD_ERROR          = 3;
    public static final byte ALREADY_LOGGED_ERROR          = 4;

    private static SecureRandom secureRandom;

    static {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public static boolean isUsernameValid(String username) {
        return true;
    }

    public static byte[] generateSecurePassword() {
        byte[] password = new byte[128];
        secureRandom.nextBytes(password);

        return password;
    }

    public class Crypto {

        public static PublicKey getPublicKey(byte[] publicKey) {
            return CryptoUtils.getPublicKey(publicKey);
        }

        public static PrivateKey getPrivateKey(byte[] privateKey) {
            return CryptoUtils.getPrivateKey(privateKey);
        }

        public static boolean isKeyPairValid(KeyPair keyPair) {
            return CryptoUtils.isKeyPairValid(keyPair);
        }

        public static KeyPair generateKeyPair() {
            return CryptoUtils.generateKeyPair();
        }
    
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
                    v |= (merge[j + (i << 3)] & 0xFFL) << (j << 3);
                }

                v ^= v >>> 1;
                v <<= 1;  // remove negative sign
                v >>>= 1; //
                v %= Emojis.LIST.length;

                builder.append(Emojis.LIST[(int) v]);
            }
    
            return builder.toString();
        }

        public static byte[] sign(byte[] data, PrivateKey privateKey) {
            return CryptoUtils.signSHA256withRSA(data, privateKey);
        }

        public static boolean verify (byte[] data, byte[] signature, PublicKey publicKey) {
            return CryptoUtils.verifySHA256withRSA(data, signature, publicKey);
        }
    
    }
    
/**
    Send to server
    username, password, publicKey, privateKey

    Problem 0
    General security
    Solution
    Messages must not be persistent
    Messages must be stored by the server only if the receiver is offline

    Problem 1
    The server knows the ip of the interlocutors
    Solution:
    Send everything through the Tor network

    Problem 2
    The server can decrypt messages using the privateKey
    Solution:
    Simmetrically encrypt the privateKey with the password
    Problem 2.1
    The server can decrypt the privateKey using the password
    Solution:
    Instead of sending the password, send the hash

    Problem 3
    Two identical password result in the same hash
    Solution:
    Salt the password with the username

    Problem 4
    Man-in-the-middle: the server could generate a key pair,
    send his public key instead of request one and gain control over every message sent
    Solution:
    Out-of-band verification
    Sender and receiver must bith compute a value using their publicKey and the other end's publicKey
    such that f(publicKeyA, publicKeyB) = f(publicKeyB, publicKeyA)
    This value is then converted into something readable (e.g. 4 emojis)
    The interlocutors must then verify that they are the same
    A user must must check if the keypair is valid

    Problem 5
    The exit node can sniff traffic data (e.g. sniff hash(password)):
    - Can login with someone's elses account
        - Delete the user unread messages
        - Send messages pretending to be the user
        (can't read incoming messages, therefore deleting them from the server)
    Solution:
    The server must send the user some random data to sign to prove that he has the private key

    Problem 6
    The server can send messages pretending to be somebody
    Solution
    Every message sent must be signed with the privateKey (and then verified by the receiver)

    NOTE;
    Don't send sensitive messages if the receiver is not online
    
    TODO:
    regex username check
    max 256 unread messages
    prevent server exception

    Possible improvements:
    Using ECC instead of RSA
    Changing keypair frequently
 */

}
