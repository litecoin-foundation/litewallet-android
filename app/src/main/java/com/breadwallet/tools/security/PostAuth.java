package com.breadwallet.tools.security;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.NetworkOnMainThreadException;
import android.security.keystore.UserNotAuthenticatedException;

import com.breadwallet.BreadApp;
import com.breadwallet.R;
import com.breadwallet.presenter.activities.PaperKeyActivity;
import com.breadwallet.presenter.activities.PaperKeyProveActivity;
import com.breadwallet.presenter.activities.SetPinActivity;
import com.breadwallet.presenter.activities.intro.WriteDownActivity;
import com.breadwallet.presenter.activities.settings.WithdrawBchActivity;
import com.breadwallet.presenter.activities.util.ActivityUTILS;
import com.breadwallet.presenter.customviews.BRDialogView;
import com.breadwallet.presenter.entities.PaymentItem;
import com.breadwallet.presenter.entities.PaymentRequestWrapper;
import com.breadwallet.tools.animation.BRDialog;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.tools.threads.PaymentProtocolPostPaymentTask;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.TypesConverter;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.BRWalletManager;
import com.platform.APIClient;
import com.platform.entities.TxMetaData;
import com.platform.tools.BRBitId;
import com.platform.tools.KVStoreManager;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 4/14/16.
 * Copyright (c) 2016 breadwallet LLC
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

public class PostAuth {
    private String phraseForKeyStore;
    public PaymentItem paymentItem;
    private PaymentRequestWrapper paymentRequest;
    public static boolean isStuckWithAuthLoop;

    private static PostAuth instance;

    private PostAuth() {
    }

    public static PostAuth getInstance() {
        if (instance == null) {
            instance = new PostAuth();
        }
        return instance;
    }

    public void onCreateWalletAuth(Activity app, boolean authAsked) {
        boolean success = BRWalletManager.getInstance().generateRandomSeed(app);
        if (success) {
            Intent intent = new Intent(app, WriteDownActivity.class);
            app.startActivity(intent);
            app.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } else {
            if (authAsked) {
                Timber.d("%s: WARNING!!!! LOOP", new Object() {
                }.getClass().getEnclosingMethod().getName());
                isStuckWithAuthLoop = true;
            }
            return;
        }
    }

    public void onPhraseCheckAuth(Activity app, boolean authAsked) {
        String cleanPhrase;
        try {
            byte[] raw = BRKeyStore.getPhrase(app, BRConstants.SHOW_PHRASE_REQUEST_CODE);
            if (raw == null) {
                NullPointerException ex = new NullPointerException("onPhraseCheckAuth: getPhrase = null");
                Timber.e(ex);
                throw ex;
            }
            cleanPhrase = new String(raw);
        } catch (UserNotAuthenticatedException e) {
            if (authAsked) {
                Timber.d("%s: WARNING!!!! LOOP", new Object() {
                }.getClass().getEnclosingMethod().getName());
                isStuckWithAuthLoop = true;
            }
            return;
        }
        Intent intent = new Intent(app, PaperKeyActivity.class);
        intent.putExtra("phrase", cleanPhrase);
        app.startActivity(intent);
        app.overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
    }

    public void onPhraseProveAuth(Activity app, boolean authAsked) {
        String cleanPhrase;
        try {
            cleanPhrase = new String(BRKeyStore.getPhrase(app, BRConstants.PROVE_PHRASE_REQUEST));
        } catch (UserNotAuthenticatedException e) {
            if (authAsked) {
                Timber.d("%s: WARNING!!!! LOOP", new Object() {
                }.getClass().getEnclosingMethod().getName());
                isStuckWithAuthLoop = true;
            }
            return;
        }
        Intent intent = new Intent(app, PaperKeyProveActivity.class);
        intent.putExtra("phrase", cleanPhrase);
        app.startActivity(intent);
        app.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    public void onBitIDAuth(Activity app, boolean authenticated) {
        BRBitId.completeBitID(app, authenticated);
    }

    public void onRecoverWalletAuth(Activity app, boolean authAsked) {
        if (phraseForKeyStore == null) {
            Timber.e(new NullPointerException("onRecoverWalletAuth: phraseForKeyStore is null"));
            return;
        }
        byte[] bytePhrase = new byte[0];

        try {
            boolean success;
            try {
                success = BRKeyStore.putPhrase(phraseForKeyStore.getBytes(),
                        app, BRConstants.PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE);
            } catch (UserNotAuthenticatedException e) {
                if (authAsked) {
                    Timber.e("%s: WARNING!!!! LOOP", new Object() {
                    }.getClass().getEnclosingMethod().getName());
                    isStuckWithAuthLoop = true;
                }
                return;
            }

            if (!success) {
                if (authAsked) {
                    Timber.d("onRecoverWalletAuth,!success && authAsked");
                }
            } else {
                if (phraseForKeyStore.length() != 0) {
                    BRSharedPrefs.putPhraseWroteDown(app, true);
                    bytePhrase = TypesConverter.getNullTerminatedPhrase(phraseForKeyStore.getBytes());
                    byte[] seed = BRWalletManager.getSeedFromPhrase(bytePhrase);
                    byte[] authKey = BRWalletManager.getAuthPrivKeyForAPI(seed);
                    BRKeyStore.putAuthKey(authKey, app);
                    byte[] pubKey = BRWalletManager.getInstance().getMasterPubKey(bytePhrase);
                    BRKeyStore.putMasterPublicKey(pubKey, app);
                    app.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                    Intent intent = new Intent(app, SetPinActivity.class);
                    intent.putExtra("noPin", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    app.startActivity(intent);
                    if (!app.isDestroyed()) app.finish();
                    phraseForKeyStore = null;
                }
            }

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            Arrays.fill(bytePhrase, (byte) 0);
        }
    }

    public void onPublishTxAuth(final Context app, boolean authAsked) {
        if (ActivityUTILS.isMainThread()) throw new NetworkOnMainThreadException();

        final BRWalletManager walletManager = BRWalletManager.getInstance();
        byte[] rawSeed;
        try {
            rawSeed = BRKeyStore.getPhrase(app, BRConstants.PAY_REQUEST_CODE);
        } catch (UserNotAuthenticatedException e) {
            if (authAsked) {
                Timber.d("%s: WARNING!!!! LOOP", new Object() {
                }.getClass().getEnclosingMethod().getName());
                isStuckWithAuthLoop = true;
            }
            return;
        }
        if (rawSeed.length < 10) return;
        final byte[] seed = TypesConverter.getNullTerminatedPhrase(rawSeed);
        try {
            if (seed.length != 0) {
                if (paymentItem != null && paymentItem.serializedTx != null) {
                    byte[] txHash = walletManager.publishSerializedTransaction(paymentItem.serializedTx, seed);
                    Timber.d("onPublishTxAuth: txhash:" + Arrays.toString(txHash));
                    if (Utils.isNullOrEmpty(txHash)) {
                        Timber.d("onPublishTxAuth: publishSerializedTransaction returned FALSE");
                        //todo fix this
//                        BRWalletManager.getInstance().offerToChangeTheAmount(app, new PaymentItem(paymentRequest.addresses, paymentItem.serializedTx, paymentRequest.amount, null, paymentRequest.isPaymentRequest));
                    } else {
                        TxMetaData txMetaData = new TxMetaData();
                        txMetaData.comment = paymentItem.comment;
                        KVStoreManager.getInstance().putTxMetaData(app, txMetaData, txHash);
                    }
                    paymentItem = null;
                } else {
                    throw new NullPointerException("payment item is null");
                }
            } else {
                Timber.d("onPublishTxAuth: seed length is 0!");
                return;
            }
        } finally {
            Arrays.fill(seed, (byte) 0);
        }
    }

    public void onSendBch(final Activity app, boolean authAsked, String bchAddress) {
        byte[] phrase;
        try {
            phrase = BRKeyStore.getPhrase(app, BRConstants.SEND_BCH_REQUEST);
        } catch (UserNotAuthenticatedException e) {
            if (authAsked) {
                Timber.d("%s: WARNING!!!! LOOP", new Object() {
                }.getClass().getEnclosingMethod().getName());
                isStuckWithAuthLoop = true;
            }
            return;
        }
        if (Utils.isNullOrEmpty(phrase)) {
            Timber.e(new RuntimeException("phrase is malformed: " + (phrase == null ? null : phrase.length)));
            return;
        }

        byte[] nullTerminatedPhrase = TypesConverter.getNullTerminatedPhrase(phrase);
        final byte[] serializedTx = BRWalletManager.sweepBCash(BRKeyStore.getMasterPublicKey(app), bchAddress, nullTerminatedPhrase);
        assert (serializedTx != null);
        if (serializedTx == null) {
            Timber.d("onSendBch:serializedTx is null");
            BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), app.getString(R.string.BCH_genericError), app.getString(R.string.AccessibilityLabels_close), null,
                    new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, null, 0);
        } else {
            Timber.d("onSendBch:serializedTx is:%s", serializedTx.length);
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    String title = "Failed";
                    String message = "";
                    String strUtl = "https://" + BreadApp.HOST + "/bch/publish-transaction";
                    Timber.d("url: %s", strUtl);
                    final MediaType type
                            = MediaType.parse("application/bchdata");
                    RequestBody requestBody = RequestBody.create(type, serializedTx);
                    Request request = new Request.Builder()
                            .url(strUtl)
                            .header("Content-Type", "application/bchdata")
                            .post(requestBody).build();
                    Response response = APIClient.getInstance(app).sendRequest(request, true, 0);
                    boolean success = true;
                    try {
                        String responseBody = null;
                        try {
                            responseBody = response == null ? null : response.body().string();
                        } catch (IOException e) {
                            Timber.e(e);
                        }
                        Timber.d("onSendBch:%s", (response == null ? "resp is null" : response.code() + ":" + response.message()));

                        if (response != null) {
                            title = app.getString(R.string.WipeWallet_failedTitle);
                            if (response.isSuccessful()) {
                                title = app.getString(R.string.Import_success);
                                message = "";
                            } else if (response.code() == 503) {
                                message = app.getString(R.string.BCH_genericError);
                            } else {
                                success = false;
                                message = "(" + response.code() + ")" + "[" + response.message() + "]" + responseBody;
                            }
                        } else {
                            title = app.getString(R.string.Alerts_sendFailure);
                            message = "Something went wrong";
                        }
                    } finally {
                        if (response != null) response.close();
                    }
                    if (!success) {
                        BRSharedPrefs.putBCHTxId(app, "");
                        WithdrawBchActivity.updateUi(app);
                    }

                    final String finalTitle = title;
                    final String finalMessage = message;
                    BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            BRDialog.showCustomDialog(app, finalTitle, finalMessage, app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                }
                            }, null, null, 0);
                        }
                    });
                }
            });
        }
    }

    public void onPaymentProtocolRequest(Activity app, boolean authAsked) {

        byte[] rawSeed;
        try {
            rawSeed = BRKeyStore.getPhrase(app, BRConstants.PAYMENT_PROTOCOL_REQUEST_CODE);
        } catch (UserNotAuthenticatedException e) {
            if (authAsked) {
                Timber.d("%s: WARNING!!!! LOOP", new Object() {
                }.getClass().getEnclosingMethod().getName());
                isStuckWithAuthLoop = true;
            }
            return;
        }
        if (rawSeed == null || rawSeed.length < 10 || paymentRequest.serializedTx == null) {
            Timber.d("onPaymentProtocolRequest() returned: rawSeed is malformed: %s", Arrays.toString(rawSeed));
            return;
        }

        final byte[] seed = TypesConverter.getNullTerminatedPhrase(rawSeed);

        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                byte[] txHash = BRWalletManager.getInstance().publishSerializedTransaction(paymentRequest.serializedTx, seed);
                if (Utils.isNullOrEmpty(txHash)) throw new NullPointerException("txHash is null!");
                PaymentProtocolPostPaymentTask.sent = true;
                Arrays.fill(seed, (byte) 0);
                paymentRequest = null;
            }
        });

    }

    public void setPhraseForKeyStore(String phraseForKeyStore) {
        this.phraseForKeyStore = phraseForKeyStore;
    }

    public void setPaymentItem(PaymentItem item) {
        this.paymentItem = item;
    }

    public void setTmpPaymentRequest(PaymentRequestWrapper paymentRequest) {
        this.paymentRequest = paymentRequest;
    }

    public void onCanaryCheck(final Activity app, boolean authAsked) {
        String canary;
        try {
            canary = BRKeyStore.getCanary(app, BRConstants.CANARY_REQUEST_CODE);
        } catch (UserNotAuthenticatedException e) {
            if (authAsked) {
                Timber.d("%s: WARNING!!!! LOOP", new Object() {
                }.getClass().getEnclosingMethod().getName());
                isStuckWithAuthLoop = true;
            }
            return;
        }
        if (canary == null || !canary.equalsIgnoreCase(BRConstants.CANARY_STRING)) {
            byte[] phrase;
            try {
                phrase = BRKeyStore.getPhrase(app, BRConstants.CANARY_REQUEST_CODE);
            } catch (UserNotAuthenticatedException e) {
                if (authAsked) {
                    Timber.d("%s: WARNING!!!! LOOP", new Object() {
                    }.getClass().getEnclosingMethod().getName());
                    isStuckWithAuthLoop = true;
                }
                return;
            }

            String strPhrase = new String((phrase == null) ? new byte[0] : phrase);
            if (strPhrase.isEmpty()) {
                BRWalletManager m = BRWalletManager.getInstance();
                m.wipeKeyStore(app);
                m.wipeWalletButKeystore(app);
            } else {
                Timber.d("onCanaryCheck: Canary wasn't there, but the phrase persists, adding canary to keystore.");
                try {
                    BRKeyStore.putCanary(BRConstants.CANARY_STRING, app, 0);
                } catch (UserNotAuthenticatedException e) {
                    if (authAsked) {
                        Timber.d("%s: WARNING!!!! LOOP", new Object() {
                        }.getClass().getEnclosingMethod().getName());
                        isStuckWithAuthLoop = true;
                    }
                    return;
                }
            }
        }
        BRWalletManager.getInstance().startTheWalletIfExists(app);
    }
}
