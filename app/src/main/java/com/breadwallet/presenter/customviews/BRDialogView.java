package com.breadwallet.presenter.customviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.breadwallet.R;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.util.Utils;

import timber.log.Timber;

public class BRDialogView extends DialogFragment {

    private static final String TAG = BRDialogView.class.getName();

    private String title = "";
    private String message = "";
    private String posButton = "";
    private String negButton = "";
    private BRDialogView.BROnClickListener posListener;
    private BRDialogView.BROnClickListener negListener;
    private DialogInterface.OnDismissListener dismissListener;
    private int iconRes = 0;
    private Button negativeButton;
    private LinearLayout buttonsLayout;

    //provide the way to have clickable span in the message
    private SpannableString spanMessage;

    private ConstraintLayout mainLayout;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.bread_alert_dialog, null);
        TextView titleText = (TextView) view.findViewById(R.id.dialog_title);
        TextView messageText = (TextView) view.findViewById(R.id.dialog_text);
        Button positiveButton = (Button) view.findViewById(R.id.pos_button);
        negativeButton = (Button) view.findViewById(R.id.neg_button);
//        ImageView icon = (ImageView) view.findViewById(R.id.dialog_icon);
        mainLayout = (ConstraintLayout) view.findViewById(R.id.main_layout);
        buttonsLayout = (LinearLayout) view.findViewById(R.id.linearLayout3);

        titleText.setText(title);
        messageText.setText(message);
        if (spanMessage != null) {
            messageText.setText(spanMessage);
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
        }
        positiveButton.setText(posButton);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                if (posListener != null)
                    posListener.onClick(BRDialogView.this);
            }
        });
        if (Utils.isNullOrEmpty(negButton)) {
            Timber.e("timber:onCreateDialog: removing negative button");
            buttonsLayout.removeView(negativeButton);
            buttonsLayout.requestLayout();
        }
        negativeButton.setText(negButton);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                if (negListener != null)
                    negListener.onClick(BRDialogView.this);
            }
        });
//        if (iconRes != 0)
//            icon.setImageResource(iconRes);

        builder.setView(view);
//        builder.setOnDismissListener(dismissListener);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null)
            dismissListener.onDismiss(dialog);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSpan(SpannableString message) {
        if (message == null) {
            Timber.e(new NullPointerException("setSpan with null message"));
            return;
        }
        this.spanMessage = message;
    }

    public void setPosButton(@NonNull String posButton) {
        this.posButton = posButton;
    }

    public void setNegButton(String negButton) {
        this.negButton = negButton;
    }

    public void setPosListener(BRDialogView.BROnClickListener posListener) {
        this.posListener = posListener;
    }

    public void setNegListener(BRDialogView.BROnClickListener negListener) {
        this.negListener = negListener;
    }

    public void setDismissListener(DialogInterface.OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public static interface BROnClickListener {
        void onClick(BRDialogView brDialogView);
    }

    public void dismissWithAnimation() {
        BRDialogView.this.dismiss();

    }

//    public interface SpanClickListener {
//        void onClick();
//
//    }

}
