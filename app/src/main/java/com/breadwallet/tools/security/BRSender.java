package com.breadwallet.tools.security;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.breadwallet.R;
import com.breadwallet.presenter.customviews.BRDialogView;
import com.breadwallet.presenter.entities.TransactionItem;
import com.breadwallet.presenter.interfaces.BRAuthCompletion;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.animation.BRDialog;
import com.breadwallet.tools.manager.BRApiManager;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.manager.FeeManager;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.BRCurrency;
import com.breadwallet.tools.util.BRExchange;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.BRWalletManager;
import com.breadwallet.tools.manager.AnalyticsManager;

import java.math.BigDecimal;
import java.util.Locale;

import timber.log.Timber;

public class BRSender {
    private static BRSender instance;
    private final static long FEE_EXPIRATION_MILLIS = 72 * 60 * 60 * 1000L;
    private boolean timedOut, sending;
    private BRSender() {
    }
    public static BRSender getInstance() {
        if (instance == null) instance = new BRSender();
        return instance;
    }

    //Create tx from the TransactionItem object and try to send it
    public void sendTransaction(final Context app, final TransactionItem transactionItem) {
        //array in order to be able to modify the first element from an inner block (can't be final)
        final String[] errTitle = {null};
        final String[] errMessage = {null};
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (sending) {
                        Timber.e(new NullPointerException("sendTransaction returned because already sending.."));
                        return;
                    }
                    sending = true;
                    long now = System.currentTimeMillis();
                    //if the fee was updated more than 24 hours ago then try updating the fee
                    if (now - BRSharedPrefs.getFeeTime(app) >= FEE_EXPIRATION_MILLIS) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    Timber.e(e);
                                }

                                if (sending) timedOut = true;
                            }
                        }).start();
                        FeeManager.updateFeePerKb(app);
                        //if the fee is STILL out of date then fail with network problem message
                        long time = BRSharedPrefs.getFeeTime(app);
                        if (time <= 0 || now - time >= FEE_EXPIRATION_MILLIS) {
                            Timber.d("timber: sendTransaction: fee out of date even after fetching...");

                            AnalyticsManager.logCustomEvent(BRConstants._20200111_FNI);

                            throw new FeeOutOfDate(time, now);
                        }
                    }
                    if (!timedOut)
                        tryPay(app, transactionItem);
                    else
                        Timber.e(new NullPointerException("did not send, timedOut!"));
                    return; //return so no error is shown
                } catch (InsufficientFundsException ignored) {
                    errTitle[0] = app.getString(R.string.Alerts_sendFailure);
                } catch (AmountSmallerThanMinException e) {
                    long minAmount = BRWalletManager.getInstance().getMinOutputAmountRequested();
                    errTitle[0] = app.getString(R.string.Alerts_sendFailure);
                    errMessage[0] = String.format(Locale.getDefault(), app.getString(R.string.PaymentProtocol_Errors_smallPayment),
                            BRConstants.litecoinLowercase + new BigDecimal(minAmount).divide(new BigDecimal(100), BRConstants.ROUNDING_MODE));
                } catch (SpendingNotAllowed spendingNotAllowed) {
                    showSpendNotAllowed(app);
                    return;
                } catch (FeeNeedsAdjust feeNeedsAdjust) {
                    //offer to change amount, so it would be enough for fee
                    showAdjustFee((Activity) app, transactionItem);
                    return;
                } catch (FeeOutOfDate ex) {
                    //Fee is out of date, show not connected error
                    Timber.e(ex);
                    BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            BRDialog.showCustomDialog(app, app.getString(R.string.Alerts_sendFailure), app.getString(R.string.NodeSelector_notConnected), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismiss();
                                }
                            }, null, null, 0);
                        }
                    });
                    return;
                } finally {
                    sending = false;
                    timedOut = false;
                }

                //show the message if we have one to show
                if (errTitle[0] != null && errMessage[0] != null)
                    BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            BRDialog.showCustomDialog(app, errTitle[0], errMessage[0], app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismiss();
                                }
                            }, null, null, 0);
                        }
                    });
            }
        });
    }

    //Try transaction and throw appropriate exceptions if something was wrong
    private void tryPay(final Context app, final TransactionItem transactionItem) throws InsufficientFundsException,
            AmountSmallerThanMinException, SpendingNotAllowed, FeeNeedsAdjust {
        if (transactionItem == null || transactionItem.sendAddress == null) {
            Timber.d("timber: handlePay: WRONG PARAMS");
            String message = transactionItem == null ? "transactionItem is null" : "addresses is null";
            RuntimeException ex = new RuntimeException("transactionItem is malformed: " + message);
            Timber.e(ex);
            throw ex;
        }

        long sendAmount = transactionItem.sendAmount + transactionItem.opsFee;
        long balance = BRWalletManager.getInstance().getBalance(app);
        final BRWalletManager m = BRWalletManager.getInstance();
        long minOutputAmount = BRWalletManager.getInstance().getMinOutputAmount();
        final long maxOutputAmount = BRWalletManager.getInstance().getMaxOutputAmount();

        // check if spending is allowed
        if (!BRSharedPrefs.getAllowSpend(app)) {
            throw new SpendingNotAllowed();
        }

        //check if amount isn't smaller than the min amount
        if (isSmallerThanMin(transactionItem)) {
            throw new AmountSmallerThanMinException(sendAmount, balance);
        }

        //amount is larger than balance
        if (isLargerThanBalance(app, transactionItem)) {
            throw new InsufficientFundsException(sendAmount, balance);
        }

        //not enough for fee
        if (notEnoughForFee(transactionItem)) {
            //weird bug when the core BRWalletManager is NULL
            if (maxOutputAmount == -1) {
                RuntimeException ex = new RuntimeException("getMaxOutputAmount is -1, meaning _wallet is NULL");
                Timber.e(ex);
                throw ex;
            }
            // max you can spend is smaller than the min you can spend
            if (maxOutputAmount < minOutputAmount) {
                throw new InsufficientFundsException(sendAmount, balance);
            }

            long feeForTx = m.feeForTransaction(transactionItem.sendAddress, transactionItem.sendAmount + transactionItem.opsFee);
            throw new FeeNeedsAdjust(sendAmount, balance, feeForTx);
        }
        // payment successful
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                byte[] tmpTx = m.tryTransactionWithOps(transactionItem.sendAddress,
                        transactionItem.sendAmount,
                        transactionItem.opsAddress,
                        transactionItem.opsFee);
                if (tmpTx == null) {
                    //something went wrong, failed to create tx
                    ((Activity) app).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BRDialog.showCustomDialog(app, "", app.getString(R.string.Alerts_sendFailure), app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismiss();
                                }
                            }, null, null, 0);

                        }
                    });
                    return;
                }
                transactionItem.serializedTx = tmpTx;
                PostAuth.getInstance().setTransactionItem(transactionItem);
                confirmPay(app, transactionItem);
            }
        });
    }

    private void showAdjustFee(final Activity app, TransactionItem item) {
        BRWalletManager m = BRWalletManager.getInstance();
        long maxAmountDouble = m.getMaxOutputAmount();
        if (maxAmountDouble == -1) {
            Timber.e(new RuntimeException("getMaxOutputAmount is -1, meaning _wallet is NULL"));
            return;
        }
        if (maxAmountDouble == 0) {
            BRDialog.showCustomDialog(app, app.getString(R.string.Alerts_sendFailure), "Insufficient amount for transaction fee", app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                @Override
                public void onClick(BRDialogView brDialogView) {
                    brDialogView.dismissWithAnimation();
                }
            }, null, null, 0);
        } else {
            BRDialog.showCustomDialog(app, app.getString(R.string.Alerts_sendFailure), "Insufficient amount for transaction fee", app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                @Override
                public void onClick(BRDialogView brDialogView) {
                    brDialogView.dismissWithAnimation();
                }
            }, null, null, 0);
            //todo fix this fee adjustment
        }
    }

    private void confirmPay(final Context ctx, final TransactionItem transactionItem) {
        if (ctx == null) {
            Timber.i("timber: confirmPay: context is null");
            return;
        }

        String message = createConfirmation(ctx, transactionItem);

        double minOutput;
        if (transactionItem.isAmountRequested) {
            minOutput = BRWalletManager.getInstance().getMinOutputAmountRequested();
        } else {
            minOutput = BRWalletManager.getInstance().getMinOutputAmount();
        }

        //amount can't be less than the min
        if (transactionItem.sendAmount < minOutput) {
            final String bitcoinMinMessage = String.format(Locale.getDefault(), ctx.getString(R.string.PaymentProtocol_Errors_smallTransaction),
                    BRConstants.litecoinLowercase + new BigDecimal(minOutput).divide(new BigDecimal("100")));

            ((Activity) ctx).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BRDialog.showCustomDialog(ctx, ctx.getString(R.string.Alerts_sendFailure), bitcoinMinMessage, ctx.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismiss();
                        }
                    }, null, null, 0);
                }
            });
            return;
        }
        boolean forcePin = false;

        Timber.d("timber: confirmPay: totalSent: %s, request.amount: %s, total limit: %s, limit: %s", BRWalletManager.getInstance().getTotalSent(),transactionItem.sendAmount,AuthManager.getInstance().getTotalLimit(ctx),BRKeyStore.getSpendLimit(ctx));


        if (BRWalletManager.getInstance().getTotalSent() + transactionItem.sendAmount > AuthManager.getInstance().getTotalLimit(ctx)) {
            forcePin = true;
        }

        //successfully created the transaction, authenticate user
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                PostAuth.getInstance().onPublishTxAuth(ctx, false);
                BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        BRAnimator.killAllFragments((FragmentActivity) ctx);
                        BRAnimator.startBreadIfNotStarted((Activity) ctx);
                    }
                });
            }
        });

    }

    private String createConfirmation(Context ctx, TransactionItem transactionItem) {
        String receiver = getReceiver(transactionItem);
        String iso = BRSharedPrefs.getIsoSymbol(ctx);
        BRWalletManager m = BRWalletManager.getInstance();

        long feesForTx = m.feeForTransaction(transactionItem.sendAddress, transactionItem.sendAmount);
        long opsFee = Utils.tieredOpsFee(ctx,transactionItem.sendAmount);
        if (feesForTx == 0) {
            long maxAmount = m.getMaxOutputAmount();
            if (maxAmount == -1) {
                RuntimeException ex = new RuntimeException("getMaxOutputAmount is -1, meaning _wallet is NULL");
                Timber.e(ex);
                throw ex;
            }
            if (maxAmount == 0) {
                BRDialog.showCustomDialog(ctx, "", ctx.getString(R.string.Alerts_sendFailure), ctx.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);

                return null;
            }
            feesForTx = m.feeForTransaction(transactionItem.sendAddress, maxAmount);
            feesForTx += (BRWalletManager.getInstance().getBalance(ctx) - transactionItem.sendAmount) % 100;
            feesForTx += opsFee;
        }
        final long total = transactionItem.sendAmount + feesForTx + opsFee;
        String formattedAmountLTC = BRCurrency.getFormattedCurrencyString(ctx, "LTC", BRExchange.getLitecoinForLitoshis(ctx, new BigDecimal(transactionItem.sendAmount)));
        String formattedFeesLTC = BRCurrency.getFormattedCurrencyString(ctx, "LTC", BRExchange.getLitecoinForLitoshis(ctx, new BigDecimal(feesForTx)));
        String formattedTotalLTC = BRCurrency.getFormattedCurrencyString(ctx, "LTC", BRExchange.getLitecoinForLitoshis(ctx, new BigDecimal(total)));

        String formattedAmount = BRCurrency.getFormattedCurrencyString(ctx, iso, BRExchange.getAmountFromLitoshis(ctx, iso, new BigDecimal(transactionItem.sendAmount)));
        String formattedFees = BRCurrency.getFormattedCurrencyString(ctx, iso, BRExchange.getAmountFromLitoshis(ctx, iso, new BigDecimal(feesForTx)));
        String formattedTotal = BRCurrency.getFormattedCurrencyString(ctx, iso, BRExchange.getAmountFromLitoshis(ctx, iso, new BigDecimal(total)));

        //formatted text
        return receiver + "\n\n"
                + ctx.getString(R.string.Confirmation_amountLabel) + " " + formattedAmountLTC + " (" + formattedAmount + ")"
                + "\n" + ctx.getString(R.string.ConfirmationAllFees_Label) + " " + formattedFeesLTC + " (" + formattedFees + ")"
                + "\n" + ctx.getString(R.string.Confirmation_totalLabel) + " " + formattedTotalLTC + " (" + formattedTotal + ")"
                + (transactionItem.comment == null ? "" : "\n\n" + transactionItem.comment);
    }

    String getReceiver(TransactionItem item) {
        boolean certified = item.certifiedName != null && item.certifiedName.length() != 0;
        return certified ? "certified: " + item.certifiedName : item.sendAddress;
    }
    private boolean isSmallerThanMin(TransactionItem transactionItem) {
        long minAmount = BRWalletManager.getInstance().getMinOutputAmountRequested();
        return transactionItem.sendAmount < minAmount;
    }

    private boolean isLargerThanBalance(Context app, TransactionItem transactionItem) {
        return transactionItem.sendAmount > 0 && transactionItem.sendAmount > BRWalletManager.getInstance().getBalance(app);
    }

    private boolean notEnoughForFee(TransactionItem transactionItem) {
        BRWalletManager m = BRWalletManager.getInstance();
        long feeForTx = m.feeForTransaction(transactionItem.sendAddress, transactionItem.sendAmount);
        if (feeForTx == 0) {
            feeForTx = m.feeForTransaction(transactionItem.sendAddress, m.getMaxOutputAmount());
            return feeForTx != 0;
        }
        return false;
    }
    private static void showSpendNotAllowed(final Context app) {
        Timber.d("timber: showSpendNotAllowed");
        ((Activity) app).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), app.getString(R.string.Send_isRescanning), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
            }
        });
    }
    private static class InsufficientFundsException extends Exception {
        InsufficientFundsException(long amount, long balance) {
            super("Balance: " + balance + " satoshis, amount: " + amount + " satoshis.");
        }
    }

    private static class AmountSmallerThanMinException extends Exception {
        AmountSmallerThanMinException(long amount, long balance) {
            super("Balance: " + balance + " satoshis, amount: " + amount + " satoshis.");
        }
    }
    private static class SpendingNotAllowed extends Exception {
        SpendingNotAllowed() {
            super("spending is not allowed at the moment");
        }
    }
    private static class FeeNeedsAdjust extends Exception {
        FeeNeedsAdjust(long amount, long balance, long fee) {
            super("Balance: " + balance + " satoshis, amount: " + amount + " satoshis, fee: " + fee + " satoshis.");
        }
    }
    private static class FeeOutOfDate extends Exception {
        FeeOutOfDate(long timestamp, long now) {
            super("FeeOutOfDate: timestamp: " + timestamp + ",now: " + now);
        }
    }
}
