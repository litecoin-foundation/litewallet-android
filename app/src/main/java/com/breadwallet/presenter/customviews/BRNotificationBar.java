package com.breadwallet.presenter.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.BreadActivity;
import com.breadwallet.tools.util.Utils;

public class BRNotificationBar extends androidx.appcompat.widget.Toolbar {

    public BRNotificationBar(Context context) {
        super(context);
        init();
    }

    public BRNotificationBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BRNotificationBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.notification_gradient);
        setElevation(Utils.getPixelsFromDps(getContext(), 8));
        setContentInsetsRelative(0, 0);

        inflate(getContext(), R.layout.notification_bar, this);
        TextView description = findViewById(R.id.description);
        BRButton close = findViewById(R.id.cancel_button);

        description.setText("No internet connection found.\nCheck your connection and try again.");

        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof BreadActivity) {
                    ((BreadActivity) getContext()).removeNotificationBar();
                }
            }
        });
    }
}