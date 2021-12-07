package com.breadwallet.presenter.entities;

import android.view.View;

public class BRSettingsItem {

    public boolean isSection;
    public String title;
    public String addonText;
    public View.OnClickListener listener;

    public BRSettingsItem(String title, String addonText, View.OnClickListener listener, boolean isSection) {
        this.title = title;
        this.addonText = addonText;
        this.listener = listener;
        this.isSection = isSection;
    }

}