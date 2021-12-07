package com.breadwallet.presenter.entities;

public class PaymentRequestWrapper {
    public final String TAG = PaymentRequestWrapper.class.getName();

    //error types
    public static final int INSUFFICIENT_FUNDS_ERROR = 1;
    public static final int SIGNING_FAILED_ERROR = 2;
    public static final int INVALID_REQUEST_ERROR = 3;
    public static final int REQUEST_TOO_LONG_ERROR = 4;
    public static final int AMOUNTS_ERROR = 5;

    //errors
    public int error = 0;

    //response
    public byte[] payment;
    public byte[] serializedTx;

    //Protocol
    public boolean isPaymentRequest;
    public byte[] signature;
    public byte[] pkiData;
    public String pkiType;

    //Protocol Details
    public String network;
    public long time;
    public long expires;
    public String memo;
    public String paymentURL;
    public byte[] merchantData;

    //Outputs
    public String[] addresses;
    public long amount;
    public long fee;

    private PaymentRequestWrapper() {
    }

    public void byteSignature(byte[] fromJNI) {
        this.signature = fromJNI;
    }

    public void pkiData(byte[] pkiData) {
        this.pkiData = pkiData;
    }

    public void merchantData(byte[] merchantData) {
        this.merchantData = merchantData;
    }

    public void payment(byte[] payment) {
        this.payment = payment;
    }

    public void serializedTx(byte[] serializedTx) {
        this.serializedTx = serializedTx;
    }

}
