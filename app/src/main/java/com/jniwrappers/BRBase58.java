package com.jniwrappers;

public class BRBase58 {
    public static final String TAG = BRBase58.class.getName();

    public static BRBase58 instance;

    private BRBase58() {
    }

    public static BRBase58 getInstance() {
        if (instance == null) {
            instance = new BRBase58();
        }
        return instance;
    }
}
