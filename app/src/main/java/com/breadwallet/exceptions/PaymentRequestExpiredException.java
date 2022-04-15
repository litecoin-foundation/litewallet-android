package com.breadwallet.exceptions;

import java.security.GeneralSecurityException;

public class PaymentRequestExpiredException extends GeneralSecurityException {
    public static final String TAG = PaymentRequestExpiredException.class.getName();

    private PaymentRequestExpiredException(){
        super();
    }

    public PaymentRequestExpiredException(String msg){
        super("The request is expired!");
    }

    private PaymentRequestExpiredException(String msg, Throwable cause){
        super(msg,cause);
    }

    private PaymentRequestExpiredException(Throwable cause){
        super(cause);
    }

}

