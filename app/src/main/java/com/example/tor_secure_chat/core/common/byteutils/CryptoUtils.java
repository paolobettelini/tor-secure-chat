package com.example.tor_secure_chat.core.common.byteutils;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
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
    
    private static SHA256Digest sha256Digest;
    private static Cipher aesCipher;
    private static Cipher rsaCipher;
    private static KeyPairGenerator rsaGenerator;
    private static KeyFactory rsaKeyFactory;
    private static Signature rsaSignature;
    private static SecureRandom secureRandom;

    static {
        try {
            Security.addProvider(new BouncyCastleProvider());

            //sha256Digest = MessageDigest.getInstance("SHA-256", "BC");
            sha256Digest = new SHA256Digest();

            aesCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            rsaCipher = Cipher.getInstance("RSA", "BC");

            rsaGenerator = KeyPairGenerator.getInstance("RSA");
            rsaGenerator.initialize(4096);

            rsaKeyFactory = KeyFactory.getInstance("RSA");
            rsaSignature = Signature.getInstance("SHA256withRSA");
            secureRandom = new SecureRandom();
            //secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public static byte[] generateSecurePassword(int size) {
        byte[] password = new byte[size];
        secureRandom.nextBytes(password);
        return password;
    }

    public static byte[] signSHA256withRSA(byte[] data, PrivateKey privateKey) {
        try {
            rsaSignature.initSign(privateKey);
            rsaSignature.update(data);
            return rsaSignature.sign();
        } catch (InvalidKeyException | SignatureException e) {
            return null;
        }
    }

    public static boolean verifySHA256withRSA(byte[] data, byte[] signature, PublicKey publicKey) {
        try {
            rsaSignature.initVerify(publicKey);
            rsaSignature.update(data);
            return rsaSignature.verify(signature);
        } catch (InvalidKeyException | SignatureException e) {
            return false;
        }
    }
    
    public static byte[] SHA256(byte[] digest) {
        sha256Digest.update(digest, 0, digest.length);
        byte[] result = new byte[sha256Digest.getDigestSize()];
        sha256Digest.doFinal(result, 0);
        return result;
        //return sha256Digest.digest(digest);
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

    public static PublicKey getPublicKey(byte[] publicKey) {
        try {
            return rsaKeyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PrivateKey getPrivateKey(byte[] privateKey) {
        try {
            return rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isKeyPairValid(KeyPair keyPair) {
        //if (keyPair.getPrivate() instanceof RSAPrivateCrtKey privateKey &&
        //    keyPair.getPublic() instanceof RSAPublicKey publicKey) {
        if (keyPair.getPrivate() instanceof RSAPrivateCrtKey &&
                keyPair.getPublic() instanceof RSAPublicKey) {
            RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            return
                publicKey.getModulus().equals(privateKey.getModulus()) &&
                publicKey.getPublicExponent().equals(privateKey.getPublicExponent());
        }

        return false;
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
