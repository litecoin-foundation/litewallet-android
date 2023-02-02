package com.breadwallet.tools.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class SlideDetector implements View.OnTouchListener {

    public interface SlideDetectorEvent {
        void onClosed();
    }

    private static final String TAG = SlideDetector.class.getName();
    private View _root;
    float origY;
    float dY;
    private SlideDetectorEvent slideDetectorEvent;

    public SlideDetector(final View view, SlideDetectorEvent slideDetectorEvent) {
        this.slideDetectorEvent = slideDetectorEvent;
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
        if (slideDetectorEvent != null) {
            slideDetectorEvent.onClosed();
        }
    }
}
