package com.breadwallet.tools.security;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import com.breadwallet.presenter.BreadWalletApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail on 9/29/15.
 * Copyright (c) 2015 Mihail Gutan <mihail@breadwallet.com>
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class KeyStoreManager {
    public static final String TAG = KeyStoreManager.class.getName();

    static final String CIPHER_TYPE = "RSA/ECB/PKCS1Padding";
    static final String CIPHER_PROVIDER = "AndroidOpenSSL";
    public static final String PHRASE_ALIAS = "phrase";
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    public static final String BREADWALLET_X500 = "CN=Breadwallet";
    public static final String PHRASE_FILENAME = "my_phrase";

    @SuppressWarnings("deprecation") // for api < 23
    public static boolean setKeyStoreString(String strToStore, Context context) {

        if (strToStore == null || strToStore.length() == 0) return false;
        //return the new method if the API is 23+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return setKeyStoreStringAPI23(PHRASE_FILENAME, strToStore, PHRASE_ALIAS, context);
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            int nBefore = keyStore.size();

            // Create the keys if necessary
            if (!keyStore.containsAlias(PHRASE_ALIAS)) {

                Calendar notBefore = Calendar.getInstance();
                Calendar notAfter = Calendar.getInstance();
                notAfter.add(Calendar.YEAR, 99);
                KeyPairGenerator generator = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(PHRASE_ALIAS)
                        .setKeySize(2048)
                        .setSubject(new X500Principal(BREADWALLET_X500))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(notBefore.getTime())
                        .setEndDate(notAfter.getTime())
                        .setEncryptionRequired()
                        .build();
                generator.initialize(spec);
                KeyPair keyPair = generator.generateKeyPair(); // needs to be here
                Log.v(TAG, "The keypair" + keyPair.toString());
            }
            int nAfter = keyStore.size();
            Log.v(TAG, "Before = " + nBefore + " After = " + nAfter);

            // Retrieve the keys
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(PHRASE_ALIAS, null);
            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();
            Log.v(TAG, "private key = " + privateKey.toString());
            Log.v(TAG, "public key = " + publicKey.toString());

            // Encrypt the text
            String filesDirectory = context.getFilesDir().getAbsolutePath();
            String encryptedDataFilePath = filesDirectory + File.separator + PHRASE_FILENAME;
//            Log.v(TAG, "strPhrase = " + strToStore);
//            Log.v(TAG, "dataDirectory = " + dataDirectory);
//            Log.v(TAG, "filesDirectory = " + filesDirectory);
//            Log.v(TAG, "encryptedDataFilePath = " + encryptedDataFilePath);

            Cipher inCipher = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER);
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    new FileOutputStream(encryptedDataFilePath), inCipher);
            cipherOutputStream.write(strToStore.getBytes("UTF-8"));
            cipherOutputStream.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean setKeyStoreStringAPI23(String fileName, String strToStore, String alias, Context context) {

        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);

            int nBefore = keyStore.size();

            // Create the keys if necessary
            if (!keyStore.containsAlias(alias)) {
                Calendar notBefore = Calendar.getInstance();
                Calendar notAfter = Calendar.getInstance();
                notAfter.add(Calendar.YEAR, 1);
                KeyPairGenerator generator = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);

                Log.e(TAG, "Android API LEVEL is 23+");
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setDigests(KeyProperties.DIGEST_SHA256,
                                KeyProperties.DIGEST_SHA512)
                        .setUserAuthenticationRequired(false)
                        .build();
                generator.initialize(spec);
                KeyPair keyPair = generator.generateKeyPair(); // needs to be here

                int nAfter = keyStore.size();
                Log.v(TAG, "Before = " + nBefore + " After = " + nAfter);
            }

            KeyStore.Entry entry = keyStore.getEntry(alias, null);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                Log.w(TAG, "Not an instance of a PrivateKeyEntry");
                return false;
            }
            RSAPublicKey pub = (RSAPublicKey) ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey();

            // Encrypt the text
//            String dataDirectory = context.getApplicationInfo().dataDir;
            String filesDirectory = context.getFilesDir().getAbsolutePath();
            String encryptedDataFilePath = filesDirectory + File.separator + fileName;

//            Log.v(TAG, "strPhrase = " + strToStore);
//            Log.v(TAG, "dataDirectory = " + dataDirectory);
//            Log.v(TAG, "filesDirectory = " + filesDirectory);
//            Log.v(TAG, "encryptedDataFilePath = " + encryptedDataFilePath);

            Cipher inCipher = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER);
            inCipher.init(Cipher.ENCRYPT_MODE, pub);

            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    new FileOutputStream(encryptedDataFilePath), inCipher);
            cipherOutputStream.write(strToStore.getBytes("UTF-8"));
            cipherOutputStream.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return true;
    }

    public static String getKeyStoreString(final Context context) {

        final long startTime = System.currentTimeMillis();
        //return the new method if the API is 23+
        ((BreadWalletApp) context.getApplicationContext()).checkAndPromptForAuthentication((Activity) context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!((BreadWalletApp) context.getApplicationContext()).allowKeyStoreAccess) {
                    try {
                        Thread.sleep(500);
                        Log.e(TAG, "after sleep ......");
                        if (System.currentTimeMillis() - startTime > 60000) break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        Log.e(TAG, "after while and allowKeyStoreAccess: " + ((BreadWalletApp) context.getApplicationContext()).allowKeyStoreAccess);
        if (!((BreadWalletApp) context.getApplicationContext()).allowKeyStoreAccess) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return getKeyStoreStringAPI23(PHRASE_FILENAME, PHRASE_ALIAS, context);
        KeyStore keyStore;
        String recoveredSecret = "";
        String filesDirectory = context.getFilesDir().getAbsolutePath();
        String encryptedDataFilePath = filesDirectory + File.separator + PHRASE_FILENAME;
        RSAPrivateKey privateKey;
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)
                    keyStore.getEntry(PHRASE_ALIAS, null);
            privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();
            Cipher outCipher = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER);
            if (privateKey != null) {
                outCipher.init(Cipher.DECRYPT_MODE, privateKey);
            } else {
                throw new RuntimeException("private key is null");
            }
            CipherInputStream cipherInputStream = new CipherInputStream(
                    new FileInputStream(encryptedDataFilePath), outCipher);
            byte[] roundTrippedBytes = new byte[1000]; //TODO: dynamically resize as we get more data
            int index = 0;
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                roundTrippedBytes[index] = (byte) nextByte;
                index++;
            }
            recoveredSecret = new String(roundTrippedBytes, 0, index, "UTF-8");
            Log.e(TAG, "round tripped string = " + recoveredSecret);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(TAG, "recovered: " + recoveredSecret);
        return recoveredSecret;
    }

    public static String getKeyStoreStringAPI23(String fileName, String alias, Context context) {

        KeyStore keyStore;
        String recoveredSecret = "";
        String filesDirectory = context.getFilesDir().getAbsolutePath();
        String encryptedDataFilePath = filesDirectory + File.separator + fileName;
        PrivateKey privateKey = null;
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
//            KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(new CallbackHandler() {
//                @Override
//                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
//                    Log.e(TAG, " inside handle callback! ***********");
//                }
//            });
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)
                    keyStore.getEntry(alias, null);
            privateKey = privateKeyEntry.getPrivateKey();
            Cipher outCipher = Cipher.getInstance(CIPHER_TYPE);

            if (privateKey != null) {
                outCipher.init(Cipher.DECRYPT_MODE, privateKey);
            } else {
                throw new RuntimeException("private key is null");
            }
            CipherInputStream cipherInputStream = new CipherInputStream(
                    new FileInputStream(encryptedDataFilePath), outCipher);
            byte[] roundTrippedBytes = new byte[1000]; // TODO: dynamically resize as we get more data
            int index = 0;
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                roundTrippedBytes[index] = (byte) nextByte;
                index++;
            }
            recoveredSecret = new String(roundTrippedBytes, 0, index, "UTF-8");
            Log.e(TAG, "round tripped string = " + recoveredSecret);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(TAG, "recovered: " + recoveredSecret);
        return recoveredSecret;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean createPrivateKeyAPI23(String alias) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEY_STORE);
            kpg.initialize(new KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setDigests(KeyProperties.DIGEST_SHA256,
                            KeyProperties.DIGEST_SHA512)
                    .build());

            KeyPair kp = kpg.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean deleteKeyStoreEntry(String alias) {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            keyStore.deleteEntry(alias);
        } catch (CertificateException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        } catch (KeyStoreException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deleteAllKeyStoreEntries() {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Log.e(TAG, "Deleting alias: " + alias);
                keyStore.deleteEntry(alias);
            }
        } catch (CertificateException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        } catch (KeyStoreException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
