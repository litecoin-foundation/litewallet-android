package com.breadwallet.presenter.entities;

public class BRMerkleBlockEntity {

    private long id;
    private byte[] buff;
    private int blockHeight;

    private BRMerkleBlockEntity(){

    }

    public BRMerkleBlockEntity(byte[] merkleBlockBuff, int blockHeight){
        this.buff = merkleBlockBuff;
        this.blockHeight = blockHeight;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getBuff() {
        return buff;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }
}
