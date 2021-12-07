package com.breadwallet.presenter.entities;

import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

public class BRMenuItem {

    public String text;
    public int resId;
    public View.OnClickListener listener;

    public BRMenuItem(String text, int resId, View.OnClickListener listener) {
        this.text = text;
        this.resId = resId;
        this.listener = listener;
    }

}