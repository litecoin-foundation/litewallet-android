package com.breadwallet.tools.animation;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;

import androidx.annotation.NonNull;

import com.breadwallet.presenter.customviews.BRDialogView;
import com.breadwallet.tools.threads.BRExecutor;

import timber.log.Timber;

public class BRDialog {
    private static BRDialogView dialog;

    /**
     * Safe from any threads
     *
     * @param app needs to be activity
     */
    public static void showCustomDialog(@NonNull final Context app, @NonNull final String title, @NonNull final String message,
                                        @NonNull final String posButton, final String negButton, final BRDialogView.BROnClickListener posListener,
                                        final BRDialogView.BROnClickListener negListener, final DialogInterface.OnDismissListener dismissListener, final int iconRes) {
        if (((Activity) app).isDestroyed()) {
            Timber.d("timber: showCustomDialog: FAILED, context is destroyed");
            return;
        }

        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                dialog = new BRDialogView();
                dialog.setTitle(title);
                dialog.setMessage(message);
                dialog.setPosButton(posButton);
                dialog.setNegButton(negButton);
                dialog.setPosListener(posListener);
                dialog.setNegListener(negListener);
                dialog.setDismissListener(dismissListener);
                dialog.setIconRes(iconRes);
                dialog.show(((Activity) app).getFragmentManager(), dialog.getClass().getName());
            }
        });

    }

    //same but with a SpannableString as message to be able to click on a portion of the text with a listener
    public static void showCustomDialog(@NonNull final Context app, @NonNull final String title, @NonNull final SpannableString message,
                                        @NonNull final String posButton, final String negButton, final BRDialogView.BROnClickListener posListener,
                                        final BRDialogView.BROnClickListener negListener, final DialogInterface.OnDismissListener dismissListener, final int iconRes) {
        if (((Activity) app).isDestroyed()) {
            Timber.d("timber: showCustomDialog: FAILED, context is destroyed");
            return;
        }

        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                dialog = new BRDialogView();
                dialog.setTitle(title);
                dialog.setSpan(message);//setting Span instead of String
                dialog.setPosButton(posButton);
                dialog.setNegButton(negButton);
                dialog.setPosListener(posListener);
                dialog.setNegListener(negListener);
                dialog.setDismissListener(dismissListener);
                dialog.setIconRes(iconRes);
                dialog.show(((Activity) app).getFragmentManager(), dialog.getClass().getName());
            }
        });
    }

    public static void hideDialog() {
        if (dialog != null) dialog.dismiss();
    }
}
