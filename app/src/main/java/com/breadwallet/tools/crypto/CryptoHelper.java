package com.breadwallet.tools.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

public class CryptoHelper {
    public static final String TAG = CryptoHelper.class.getName();

    public static String base58ofSha256(byte[] toEncode) {
        byte[] sha256First = sha256(toEncode);
        return Base58.encode(sha256First);
    }

    public static byte[] doubleSha256(byte[] data) {
        byte[] sha256First = sha256(data);
        return sha256(sha256First);
    }

    public static byte[] sha256(byte[] data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Timber.e(e);
            return null;
        }
        return digest.digest(data);
    }

    public static byte[] md5(byte[] data){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Timber.e(e);
            return null;
        }
        return digest.digest(data);
    }

}
