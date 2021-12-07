package com.breadwallet.presenter.entities;

public class PeerEntity {
    public static final String TAG = PeerEntity.class.getName();

    private byte[] peerAddress;
    private byte[] peerPort;
    private byte[] peerTimeStamp;

    public PeerEntity(byte[] peerAddress, byte[] peerPort, byte[] peerTimeStamp) {
        this.peerAddress = peerAddress;
        this.peerPort = peerPort;
        this.peerTimeStamp = peerTimeStamp;
    }

    private PeerEntity() {
    }

    public byte[] getPeerAddress() {
        return peerAddress;
    }

    public byte[] getPeerPort() {
        return peerPort;
    }

    public byte[] getPeerTimeStamp() {
        return peerTimeStamp;
    }
}
