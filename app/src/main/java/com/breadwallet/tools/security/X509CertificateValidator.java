package com.breadwallet.tools.security;

import android.util.Log;

import com.breadwallet.presenter.entities.PaymentRequestWrapper;
import com.breadwallet.exceptions.CertificateChainNotFound;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import timber.log.Timber;

public class X509CertificateValidator {
    private static final String TAG = X509CertificateValidator.class.getName();
    public static final String PKI_X509_SHA256 = "x509+sha256";
    public static final String PKI_X509_SHA1 = "x509+sha1";
    public static final String PKI_NONE = "none";
    public static final String ROOT_CERTS_DIR = "/system/etc/security/cacerts";

    public static String certificateValidation(List<X509Certificate> certList,
                                                PaymentRequestWrapper paymentRequest)
            throws KeyStoreException, CertificateChainNotFound {

        String result = null;
        if (certList.size() == 0) {
            throw new CertificateChainNotFound("no certificates supplied");
        }
        try {
//            Log.e(TAG, "The size of certList is: " + certList.size());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init((KeyStore) null);
            X509Certificate[] certListArray = new X509Certificate[certList.size()];
            for (int i = 0; i < certList.size(); i++) {
                certListArray[i] = certList.get(i);
            }
            TrustManager[] tms = tmf.getTrustManagers();
            for (TrustManager m : tms) {
                X509TrustManager xtm = (X509TrustManager) m;
//                Log.d(TAG, "checking chain with " + xtm + ", Alg: " + certListArray[0].getSigAlgName());
                xtm.checkClientTrusted(certListArray, certListArray[0].getSigAlgName());
            }
            PublicKey publicKey = certListArray[0].getPublicKey();
            Signature signature = Signature.getInstance(certList.get(0).getSigAlgName());
            signature.initVerify(publicKey);
            signature.update(paymentRequest.signature);
            signature.initVerify(publicKey);
            result = certList.get(0).getSubjectX500Principal().getName();
        } catch (CertificateException | InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
            Timber.e(e);
        }
        return result;
    }

    public static ArrayList<X509Certificate> getRootCerts() {
        ArrayList<X509Certificate> certificates = new ArrayList<>();
        try {
            KeyStore ks = KeyStore.getInstance("AndroidCAStore");
            ks.load(null, null);
            Enumeration aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                X509Certificate cert = (X509Certificate)
                        ks.getCertificate(alias);
                certificates.add(cert);
                Log.d(TAG, "Subject DN: " +
                        cert.getSubjectDN().getName());
                Log.d(TAG, "Issuer DN: " +
                        cert.getIssuerDN().getName());
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            Timber.e(e);
        }
        return certificates;
    }

    public static List<X509Certificate> getCertificateFromBytes(byte[] rawCerts) {
//        Log.e(TAG, "This is the rawCerts.length supplied for certificates: " + rawCerts.length);
        List<X509Certificate> theList = new ArrayList<>();
        byte[] result;
        int i = 0;
        try {
            CertificateFactory certFact = CertificateFactory.getInstance("X.509");
            while (true) {
                result = BitcoinUrlHandler.getCertificatesFromPaymentRequest(rawCerts, i++);
//                Log.e(TAG, "The result certificate #" + i + " : " + result.length);
                if (result.length > 0) {
                    X509Certificate certForValidation = (X509Certificate)
                            certFact.generateCertificate(new ByteArrayInputStream(result));
                    theList.add(certForValidation);
//                    Log.e(TAG, "THIS IS THE CERTIFICATE NAME: " + certForValidation.getIssuerDN().toString());
                } else {
                    break;
                }
            }
        } catch (CertificateException e) {
            Timber.e(e);
        }
        return theList;
    }

}