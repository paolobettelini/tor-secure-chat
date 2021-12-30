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
import java.security.spec.PKCS8EncodedKeySpec;
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
            return rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] encryptAES(byte[] data, byte[] key, byte[] iv) {
        return operateAESCipher(data, key, iv, Cipher.ENCRYPT_MODE);
    }

    public static byte[] decryptAES(byte[] data, byte[] key, byte[] iv) {
        return operateAESCipher(data, key, iv, Cipher.DECRYPT_MODE);
    }

    private static byte[] operateAESCipher(byte[] input, byte[] key, byte[] iv, int cipherMode) {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

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