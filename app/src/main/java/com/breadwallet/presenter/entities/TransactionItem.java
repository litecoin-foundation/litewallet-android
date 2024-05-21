package com.breadwallet.presenter.entities;

public class TransactionItem {
    public static final String TAG = TransactionItem.class.getName();

    public byte[] serializedTx;
    public String sendAddress;
    public String opsAddress;
    public long sendAmount;
    public long opsFee;
    //This variable was previously called -cn- which is too short to know what it means or does
    public String certifiedName;
    public boolean isAmountRequested;
    public String comment;

    public TransactionItem(String sendAddress, String opsAddress, byte[] tx, long sendAmount, long opsFee, String theCertifiedName, boolean isAmountRequested) {
        this.isAmountRequested = isAmountRequested;
        this.serializedTx = tx;
        this.sendAddress = sendAddress;
        this.opsAddress = opsAddress;
        this.sendAmount = sendAmount;
        this.opsFee = opsFee;
        this.certifiedName = theCertifiedName;
    }
    public TransactionItem(String sendAddress, String opsAddress, byte[] tx,long sendAmount, long opsFee, String theCertifiedName, boolean isAmountRequested, String comment) {
        this.isAmountRequested = isAmountRequested;
        this.serializedTx = tx;
        this.sendAddress = sendAddress;
        this.opsAddress = opsAddress;
        this.sendAmount = sendAmount;
        this.opsFee = opsFee;
        this.certifiedName = theCertifiedName;
        this.comment = comment;
    }

}
