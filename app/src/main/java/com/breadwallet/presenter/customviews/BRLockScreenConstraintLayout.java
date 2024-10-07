package com.breadwallet.presenter.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;

import com.breadwallet.R;

public class BRLockScreenConstraintLayout extends ConstraintLayout {
    public static final String TAG = BRLockScreenConstraintLayout.class.getName();
    private int width;
    private int height;
    private boolean created;

    public BRLockScreenConstraintLayout(Context context) {
        super(context);
        init();
    }

    public BRLockScreenConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BRLockScreenConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        long start = System.currentTimeMillis();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        // Correct any translations set before the measure was set
    }
}
