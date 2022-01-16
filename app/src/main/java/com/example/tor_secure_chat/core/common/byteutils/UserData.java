package com.example.tor_secure_chat.core.common.byteutils;

public class UserData {

    private final String username;
    private final byte[] password;
    private final byte[] publicKey;
    private final byte[] privateKey;

    public UserData(String username, byte[] password, byte[] publicKey, byte[] privateKey) {
        this.username = username;
        this.password = password;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String username() {
        return username;
    }

    public byte[] password() {
        return password;
    }

    public byte[] publicKey() {
        return publicKey;
    }

    public byte[] privateKey() {
        return privateKey;
    }

}
