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
import com.breadwallet.presenter.activities.util.ActivityUTILS;
import com.breadwallet.presenter.customviews.BRDialogView;
import com.breadwallet.presenter.entities.TransactionItem;
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
import com.platform.tools.KVStoreManager;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class PostAuth {
    private String phraseForKeyStore;
    public TransactionItem transactionItem;
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
                Timber.d("timber: %s: WARNING!!!! LOOP", new Object() {
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
                Timber.d("timber: %s: WARNING!!!! LOOP", new Object() {
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
                Timber.d("timber: %s: WARNING!!!! LOOP", new Object() {
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
                    Timber.e("timber:%s: WARNING!!!! LOOP", new Object() {
                    }.getClass().getEnclosingMethod().getName());
                    isStuckWithAuthLoop = true;
                }
                return;
            }

            if (!success) {
                if (authAsked) {
                    Timber.d("timber: onRecoverWalletAuth,!success && authAsked");
                }
            } else {
                if (phraseForKeyStore.length() != 0) {
                    BRSharedPrefs.putPhraseWroteDown(app, true);
                    Timber.d("timber: BRSharedPrefs.putPhraseWroteDown was set to true");
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
                Timber.d("timber: %s: WARNING!!!! LOOP", new Object() {
                }.getClass().getEnclosingMethod().getName());
                isStuckWithAuthLoop = true;
            }
            return;
        }
        if (rawSeed.length < 10) return;
        final byte[] seed = TypesConverter.getNullTerminatedPhrase(rawSeed);
        try {
            if (seed.length != 0) {
                if (transactionItem != null && transactionItem.serializedTx != null) {
                    byte[] txHash = walletManager.publishSerializedTransaction(transactionItem.serializedTx, seed);
                    Timber.d("timber: onPublishTxAuth: txhash:" + Arrays.toString(txHash));
                    if (Utils.isNullOrEmpty(txHash)) {
                        Timber.d("timber: onPublishTxAuth: publishSerializedTransaction returned FALSE");
                      } else {
                        TxMetaData txMetaData = new TxMetaData();
                        txMetaData.comment = transactionItem.comment;
                        KVStoreManager.getInstance().putTxMetaData(app, txMetaData, txHash);
                    }
                    transactionItem = null;
                } else {
                    throw new NullPointerException("payment item is null");
                }
            } else {
                Timber.d("timber: onPublishTxAuth: seed length is 0!");
                return;
            }
        } finally {
            Arrays.fill(seed, (byte) 0);
        }
    }

    public void onPaymentProtocolRequest(Activity app, boolean authAsked) {

        byte[] rawSeed;
        try {
            rawSeed = BRKeyStore.getPhrase(app, BRConstants.PAYMENT_PROTOCOL_REQUEST_CODE);
        } catch (UserNotAuthenticatedException e) {
            if (authAsked) {
                Timber.d("timber: %s: WARNING!!!! LOOP", new Object() {
                }.getClass().getEnclosingMethod().getName());
                isStuckWithAuthLoop = true;
            }
            return;
        }
        if (rawSeed == null || rawSeed.length < 10 || paymentRequest.serializedTx == null) {
            Timber.d("timber: onPaymentProtocolRequest() returned: rawSeed is malformed: %s", Arrays.toString(rawSeed));
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

    public void setTransactionItem(TransactionItem item) {
        this.transactionItem = item;
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
                Timber.d("timber: %s: WARNING!!!! LOOP", new Object() {
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
                    Timber.d("timber: %s: WARNING!!!! LOOP", new Object() {
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
                Timber.d("timber: onCanaryCheck: Canary wasn't there, but the phrase persists, adding canary to keystore.");
                try {
                    BRKeyStore.putCanary(BRConstants.CANARY_STRING, app, 0);
                } catch (UserNotAuthenticatedException e) {
                    if (authAsked) {
                        Timber.d("timber: %s: WARNING!!!! LOOP", new Object() {
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
