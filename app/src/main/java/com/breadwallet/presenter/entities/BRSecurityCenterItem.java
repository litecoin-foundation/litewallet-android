package com.breadwallet.presenter.entities;

import android.view.View;
import android.widget.ImageView;

import static android.R.attr.checkMark;

public class BRSecurityCenterItem {

    public String title;
    public String text;
    public int checkMarkResId;
    public View.OnClickListener listener;

    public BRSecurityCenterItem(String title, String text, int checkMarkResId, View.OnClickListener listener) {
        this.title = title;
        this.text = text;
        this.checkMarkResId = checkMarkResId;
        this.listener = listener;
    }

}