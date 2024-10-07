package com.breadwallet.presenter.entities;

public class BRPeerEntity {

    private int id;
    private byte[] address;
    private byte[] port;
    private byte[] timeStamp;

    private BRPeerEntity(){

    }

    public BRPeerEntity(byte[] address, byte[] port, byte[] timeStamp){
        this.address = address;
        this.port = port;
        this.timeStamp = timeStamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getPort() {
        return port;
    }

    public byte[] getTimeStamp() {
        return timeStamp;
    }

    public byte[] getAddress() {
        return address;
    }

}
