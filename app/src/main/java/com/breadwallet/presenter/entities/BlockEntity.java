package com.breadwallet.presenter.entities;

public class BlockEntity {
    public static final String TAG = BlockEntity.class.getName();

    private byte[] blockBytes;
    private int blockHeight;


    public BlockEntity(byte[] blockBytes, int blockHeight) {
        this.blockBytes = blockBytes;
        this.blockHeight = blockHeight;
    }

    private BlockEntity() {
    }

    public byte[] getBlockBytes() {
        return blockBytes;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

}
