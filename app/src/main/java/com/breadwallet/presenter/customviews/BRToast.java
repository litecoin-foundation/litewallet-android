package com.breadwallet.presenter.customviews;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.breadwallet.BreadApp;
import com.breadwallet.R;

public class BRToast {
    private static boolean customToastAvailable = true;
    private static String oldMessage;
    private static Toast toast;

    /**
     * Shows a custom toast using the given string as a paramater,
     *
     * @param message the message to be shown in the custom toast
     */
    public static void showCustomToast(Context app, String message, int yOffSet, int duration, int layoutDrawable) {
        if (app == null) return;
        if (!(app instanceof Activity)) app = BreadApp.getBreadContext();
        if (app == null) return;
        if (toast == null) toast = new Toast(app);
        if (!BreadApp.isAppInBackground(app)) return;

        if (customToastAvailable || !oldMessage.equals(message)) {
            oldMessage = message;
            customToastAvailable = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    customToastAvailable = true;
                }
            }, 1000);
            LayoutInflater inflater = ((Activity) app).getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast, (ViewGroup) ((Activity) app).findViewById(R.id.toast_layout_root));
            layout.setBackgroundResource(layoutDrawable);
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText(message);
            toast.setGravity(Gravity.TOP, 0, yOffSet);
            toast.setDuration(duration);
            toast.setView(layout);
            toast.show();
        }
    }

    public static void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    public static boolean isToastShown() {
        return toast != null && toast.getView() != null && toast.getView().isShown();
    }
}
