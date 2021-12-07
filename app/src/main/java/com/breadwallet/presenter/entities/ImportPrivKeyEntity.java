package com.breadwallet.presenter.entities;

public class ImportPrivKeyEntity {
    public static final String TAG = ImportPrivKeyEntity.class.getName();

    private byte[] tx;

    public byte[] getTx() {
        return tx;
    }

    public long getAmount() {
        return amount;
    }

    public long getFee() {
        return fee;
    }

    private long amount;
    private long fee;

    private ImportPrivKeyEntity() {
    }

    public ImportPrivKeyEntity(byte[] tx, long amount, long fee) {
        this.tx = tx;
        this.amount = amount;
        this.fee = fee;
    }

}
