package com.jniwrappers;

public class BRBIP32Sequence {
    public static final String TAG = BRBIP32Sequence.class.getName();

    public static BRBIP32Sequence instance;

    private BRBIP32Sequence() {
    }

    public static BRBIP32Sequence getInstance() {
        if (instance == null) {
            instance = new BRBIP32Sequence();
        }
        return instance;
    }

    public native byte[] bip32BitIDKey(byte[] seed, int index, String uri);
}
