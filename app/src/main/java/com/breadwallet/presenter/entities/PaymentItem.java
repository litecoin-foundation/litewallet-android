package com.breadwallet.presenter.entities;

public class PaymentItem {
    public static final String TAG = PaymentItem.class.getName();

    public byte[] serializedTx;
    public String[] addresses;
    public long amount;
    public String cn;
    public boolean isAmountRequested;
    public String comment;

    public PaymentItem(String[] addresses, byte[] tx, long theAmount, String theCn, boolean isAmountRequested) {
        this.isAmountRequested = isAmountRequested;
        this.serializedTx = tx;
        this.addresses = addresses;
        this.amount = theAmount;
        this.cn = theCn;
    }

    public PaymentItem(String[] addresses, byte[] tx,long theAmount, String theCn, boolean isAmountRequested, String comment) {
        this.isAmountRequested = isAmountRequested;
        this.serializedTx = tx;
        this.addresses = addresses;
        this.amount = theAmount;
        this.cn = theCn;
        this.comment = comment;
    }

}
