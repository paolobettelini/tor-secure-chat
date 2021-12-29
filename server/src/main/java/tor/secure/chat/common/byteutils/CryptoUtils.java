package tor.secure.chat.common.byteutils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    
    private static MessageDigest sha256Digest;
    private static Cipher aesCipher;
    private static Cipher rsaCipher;
    private static KeyPairGenerator rsaGenerator;
    private static KeyFactory rsaKeyFactory;

    static {
        try {
            sha256Digest = MessageDigest.getInstance("SHA-256");
            aesCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            rsaCipher = Cipher.getInstance("RSA");
            rsaGenerator = KeyPairGenerator.getInstance("RSA");
            rsaGenerator.initialize(4096);
            rsaKeyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }
    
    public static byte[] SHA256(byte[] digest) {
        return sha256Digest.digest(digest);
    }

    public static byte[] encryptAES(byte[] input, byte[] key) {
        return operateAESCipher(input, key, Cipher.ENCRYPT_MODE);
    }

    public static byte[] decryptAES(byte[] input, byte[] key) {
        return operateAESCipher(input, key, Cipher.DECRYPT_MODE);
    }

    public static KeyPair generateKeyPair() {
        return rsaGenerator.generateKeyPair();
    }

    public static byte[] encryptRSA(byte[] input, Key key) {
        return operateRSACipher(input, key, Cipher.ENCRYPT_MODE);
    }

    public static byte[] decryptRSA(byte[] input, Key key) {
        return operateRSACipher(input, key, Cipher.DECRYPT_MODE);
    }

    public static Key getPublicKey(byte[] publicKey) {
        try {
            return rsaKeyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public static Key getPrivateKey(byte[] privateKey) {
        try {
            return rsaKeyFactory.generatePrivate(new X509EncodedKeySpec(privateKey));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] operateAESCipher(byte[] input, byte[] key, int cipherMode) {
        byte[] leftPart = new byte[16];
        byte[] rightPart = new byte[16];

        byte[] keyHash = SHA256(key);

        for (int i = 0; i < 16; i++) {
            leftPart[i] = keyHash[i];
            rightPart[i] = keyHash[i + 16];
        }

        SecretKeySpec keySpec = new SecretKeySpec(leftPart, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(rightPart);

        try {
            aesCipher.init(cipherMode, keySpec, ivSpec);
            return aesCipher.doFinal(input);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] operateRSACipher(byte[] input, Key key, int cipherMode) {
        try {
            rsaCipher.init(cipherMode, key);
            return rsaCipher.doFinal(input);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

}
