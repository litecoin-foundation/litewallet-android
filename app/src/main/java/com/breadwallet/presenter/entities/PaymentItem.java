package com.breadwallet.presenter.entities;

public class PaymentItem {
    public static final String TAG = PaymentItem.class.getName();

    public byte[] serializedTx;
    public String[] addresses;
    public String opsAddress;
    public long amount;
    //This variable was previously called -cn- which is too short to know what it means or does
    public String certifiedName;
    public boolean isAmountRequested;
    public String comment;

    public PaymentItem(String[] addresses, String opsAddress, byte[] tx, long theAmount, String theCertifiedName, boolean isAmountRequested) {
        this.isAmountRequested = isAmountRequested;
        this.serializedTx = tx;
        this.addresses = addresses;
        this.opsAddress = opsAddress;
        this.amount = theAmount;
        this.certifiedName = theCertifiedName;
    }

    public PaymentItem(String[] addresses, String opsAddress, byte[] tx,long theAmount, String theCertifiedName, boolean isAmountRequested, String comment) {
        this.isAmountRequested = isAmountRequested;
        this.serializedTx = tx;
        this.addresses = addresses;
        this.opsAddress = opsAddress;
        this.amount = theAmount;
        this.certifiedName = theCertifiedName;
        this.comment = comment;
    }

}
