package com.breadwallet.presenter.entities;

public class BRTransactionEntity {
    private byte[] buff;
    private int blockheight;
    private long timestamp;
    private String txHash;

    public long getBlockheight() {
        return blockheight;
    }

    public void setBlockheight(int blockheight) {
        this.blockheight = blockheight;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public BRTransactionEntity(byte[] txBuff, int blockheight, long timestamp, String txHash) {
        this.blockheight = blockheight;
        this.timestamp = timestamp;
        this.buff = txBuff;
        this.txHash = txHash;
    }

    public byte[] getBuff() {
        return buff;
    }

}
