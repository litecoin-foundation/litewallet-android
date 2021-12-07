package com.breadwallet.tools.animation;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;

import com.breadwallet.R;

public class SpringAnimator {

    public static void showExpandCameraGuide(final View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
        ScaleAnimation trans = new ScaleAnimation(0.0f, 1f, 0.0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        trans.setDuration(800);
        trans.setInterpolator(new DecelerateOvershootInterpolator(1.5f, 2.5f));
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            view.startAnimation(trans);
        }
    }

    /**
     * Shows the springy animation on views
     */
    public static void springView(final View view) {
        if (view == null) return;
        ScaleAnimation trans = new ScaleAnimation(0.8f, 1f, 0.8f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        trans.setDuration(1000);
        trans.setInterpolator(new DecelerateOvershootInterpolator(0.5f, 1f));
        view.setVisibility(View.VISIBLE);
        view.startAnimation(trans);

    }

    public static void failShakeAnimation(Activity context, View view) {
        if (view == null) return;
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(shake);
    }
}