package com.breadwallet.presenter.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.BreadActivity;
import com.breadwallet.presenter.interfaces.BRAuthCompletion;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.animation.DecelerateOvershootInterpolator;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.security.AuthManager;
import com.breadwallet.tools.security.FingerprintUiHelper;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.Utils;

import timber.log.Timber;

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
public class FragmentFingerprint extends Fragment
        implements FingerprintUiHelper.Callback {
    public static final String TAG = FragmentFingerprint.class.getName();

    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintUiHelper mFingerprintUiHelper;
    private BRAuthCompletion completion;
    private TextView title;
    private TextView message;
    private LinearLayout fingerPrintLayout;
    private RelativeLayout fingerprintBackground;
    private boolean authSucceeded;
    public static final int ANIMATION_DURATION = 300;
    private String customTitle;
    private String customMessage;

    FingerprintUiHelper.FingerprintUiHelperBuilder mFingerprintUiHelperBuilder;

    public FragmentFingerprint() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View authModalView = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        message = (TextView) authModalView.findViewById(R.id.fingerprint_description);
        title = (TextView) authModalView.findViewById(R.id.fingerprint_title);
        fingerPrintLayout = (LinearLayout) authModalView.findViewById(R.id.fingerprint_layout);
        fingerprintBackground = (RelativeLayout) authModalView.findViewById(R.id.fingerprint_background);
        Bundle bundle = getArguments();
        String titleString = bundle.getString("title");
        String messageString = bundle.getString("message");
        if (!Utils.isNullOrEmpty(titleString)) {
            customTitle = titleString;
            title.setText(customTitle);
        }
        if (!Utils.isNullOrEmpty(messageString)) {
            customMessage = messageString;
            message.setText(customMessage);
        }
        FingerprintManager mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Activity.FINGERPRINT_SERVICE);
        mFingerprintUiHelperBuilder = new FingerprintUiHelper.FingerprintUiHelperBuilder(mFingerprintManager);
        mFingerprintUiHelper = mFingerprintUiHelperBuilder.build((ImageView) authModalView.findViewById(R.id.fingerprint_icon),
                (TextView) authModalView.findViewById(R.id.fingerprint_status), this, getContext());
        View mFingerprintContent = authModalView.findViewById(R.id.fingerprint_container);

        Button mCancelButton = (Button) authModalView.findViewById(R.id.cancel_button);
        Button mSecondDialogButton = (Button) authModalView.findViewById(R.id.second_dialog_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BRAnimator.isClickAllowed()) return;
                closeMe();
            }
        });
        mCancelButton.setText(R.string.Button_cancel);
        mSecondDialogButton.setText(getString(R.string.Prompts_TouchId_usePin_android));
        mFingerprintContent.setVisibility(View.VISIBLE);
        mSecondDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BRAnimator.isClickAllowed()) return;
                closeMe();
                goToBackup();
            }
        });

        return authModalView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ViewTreeObserver observer = fingerPrintLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                animateBackgroundDim(false);
                animateSignalSlide(false);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
//        animateBackgroundDim(true);
//        animateSignalSlide(true);
        if (!authSucceeded)
            completion.onCancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFingerprintUiHelper.startListening(mCryptoObject);
        authSucceeded = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    /**
     * Switches to backup (password) screen. This either can happen when fingerprint is not
     * available or the user chooses to use the password authentication method by pressing the
     * button. This can also happen when the user had too many fingerprint attempts.
     */
    private void goToBackup() {
        final Context app = getContext();
        closeMe();

        if (mFingerprintUiHelper != null)
            mFingerprintUiHelper.stopListening();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AuthManager.getInstance().authPrompt(app, customTitle, customMessage, true, false, completion);
            }
        }, ANIMATION_DURATION + 100);
    }

    @Override
    public void onAuthenticated() {
        final Activity app = getActivity();
        authSucceeded = true;

        if (completion != null) completion.onComplete();
        BRAnimator.killAllFragments(app);
        BRAnimator.startBreadIfNotStarted(app);

        closeMe();

    }

    public void setCompletion(BRAuthCompletion completion) {
        this.completion = completion;
    }

    @Override
    public void onError() {
        goToBackup();
    }

    private void animateBackgroundDim(boolean reverse) {
        int transColor = reverse ? R.color.black_trans : android.R.color.transparent;
        int blackTransColor = reverse ? android.R.color.transparent : R.color.black_trans;

        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(transColor, blackTransColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                fingerprintBackground.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        anim.setDuration(ANIMATION_DURATION);
        anim.start();
    }

    private void animateSignalSlide(final boolean reverse) {
        float layoutTY = fingerPrintLayout.getTranslationY();
        if (!reverse) {
            fingerPrintLayout.setTranslationY(layoutTY + BreadActivity.screenParametersPoint.y);
            fingerPrintLayout.animate()
                    .translationY(layoutTY)
                    .setDuration(ANIMATION_DURATION + 200)
                    .setInterpolator(new DecelerateOvershootInterpolator(2.0f, 1f))
                    .withLayer();
        } else {
            fingerPrintLayout.animate()
                    .translationY(1500)
                    .setDuration(ANIMATION_DURATION)
                    .withLayer()
                    .setInterpolator(new AnticipateInterpolator(2.0f))
                    .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (getActivity() != null) {
                        fingerPrintLayout.clearAnimation();
                        AnalyticsManager.logCustomEvent(BRConstants._20230131_NENR);
                    }
                }
            });

        }

    }

    private void closeMe() {
        animateBackgroundDim(true);
        animateSignalSlide(true);
    }

}