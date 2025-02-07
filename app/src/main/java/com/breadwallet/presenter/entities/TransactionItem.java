package com.breadwallet.presenter.entities;

public class TransactionItem {

    public static final String TAG = TransactionItem.class.getName();

    public byte[] serializedTx;
    public String sendAddress;
    public long sendAmount;
    //This variable was previously called -cn- which is too short to know what it means or does
    public String certifiedName;
    public boolean isAmountRequested;
    public String comment;

    public TransactionItem(
        String sendAddress,
        byte[] tx,
        long sendAmount,
        String theCertifiedName,
        boolean isAmountRequested
    ) {
        this.isAmountRequested = isAmountRequested;
        this.serializedTx = tx;
        this.sendAddress = sendAddress;
        this.sendAmount = sendAmount;
        this.certifiedName = theCertifiedName;
    }

    public TransactionItem(
        String sendAddress,
        byte[] tx,
        long sendAmount,
        String theCertifiedName,
        boolean isAmountRequested,
        String comment
    ) {
        this.isAmountRequested = isAmountRequested;
        this.serializedTx = tx;
        this.sendAddress = sendAddress;
        this.sendAmount = sendAmount;
        this.certifiedName = theCertifiedName;
        this.comment = comment;
    }
}
