package com.breadwallet.presenter.entities;

import android.view.View;

public class BRSettingsItem {

    public enum SettingItemsType {
        SECTION, ADDON, SWITCH
    }

    public SettingItemsType type;
    public String title;
    public String addonText;
    public Boolean isChecked;
    public View.OnClickListener listener;

    public BRSettingsItem(SettingItemsType type, String title, String addonText, Boolean isChecked, View.OnClickListener listener) {
        this.type = type;
        this.title = title;
        this.addonText = addonText;
        this.isChecked = isChecked;
        this.listener = listener;
    }
}