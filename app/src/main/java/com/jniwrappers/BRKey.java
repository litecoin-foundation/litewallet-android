package com.jniwrappers;

import android.util.Log;

import com.breadwallet.tools.util.Utils;

import java.util.Arrays;

public class BRKey {
    public static final String TAG = BRKey.class.getName();

    public BRKey(byte[] key) throws IllegalArgumentException {
        if (Utils.isNullOrEmpty(key)) throw new NullPointerException("key is empty");
        if (!setPrivKey(key)) {
            throw new IllegalArgumentException("Failed to setup the key: " + Arrays.toString(key));
        }
    }

    public BRKey(String hexSecret) {
        setSecret(Utils.hexToBytes(hexSecret));
    }

    private native boolean setPrivKey(byte[] privKey);

    private native void setSecret(byte[] secret);

    public native byte[] compactSign(byte[] data);

    public native byte[] encryptNative(byte[] data, byte[] nonce);

    public native byte[] decryptNative(byte[] data, byte[] nonce);

    public native String address();

}
