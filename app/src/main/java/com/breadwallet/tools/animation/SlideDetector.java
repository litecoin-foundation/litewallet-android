package com.breadwallet.tools.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;

public class SlideDetector implements View.OnTouchListener {

    private static final String TAG = SlideDetector.class.getName();

    private Context context;
    private View _root;
    float origY;
    float dY;

    public SlideDetector(Context context, final View view) {
        this.context = context;
        _root = view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                origY = _root.getY();
                dY = _root.getY() - event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getRawY() + dY > origY)
                    _root.animate()
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                break;
            case MotionEvent.ACTION_UP:
                if (_root.getY() > origY + _root.getHeight() / 5) {
                    _root.animate()
                            .y(_root.getHeight() * 2)
                            .setDuration(200)
                            .setInterpolator(new OvershootInterpolator(0.5f))
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    removeCurrentView();
                                }
                            })
                            .start();
                } else {
                    _root.animate()
                            .y(origY)
                            .setDuration(100)
                            .setInterpolator(new OvershootInterpolator(0.5f))
                            .start();
                }

                break;
            default:
                return false;
        }
        return true;
    }

    private void removeCurrentView() {
        ((Activity) context).getFragmentManager().popBackStack();
    }
}
