package com.breadwallet.exceptions;

import java.security.GeneralSecurityException;

public class CertificateChainNotFound extends GeneralSecurityException {
    public static final String TAG = CertificateChainNotFound.class.getName();

    private CertificateChainNotFound() {
        super();
    }

    public CertificateChainNotFound(String msg) {
        super(msg);
    }

    private CertificateChainNotFound(String msg, Throwable cause) {
        super(msg, cause);
    }

    private CertificateChainNotFound(Throwable cause) {
        super(cause);
    }

}

