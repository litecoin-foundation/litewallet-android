package com.breadwallet.presenter.activities.settings;

import static com.breadwallet.R.layout.settings_list_item;
import static com.breadwallet.R.layout.settings_list_section;
import static com.breadwallet.R.layout.settings_list_switch;
import static com.breadwallet.presenter.entities.BRSettingsItem.SettingItemsType.ADDON;
import static com.breadwallet.presenter.entities.BRSettingsItem.SettingItemsType.SECTION;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.presenter.entities.BRSettingsItem;
import com.breadwallet.presenter.interfaces.BRAuthCompletion;
import com.breadwallet.presenter.language.ChangeLanguageBottomSheet;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.security.AuthManager;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BRActivity {
    private ListView listView;
    public List<BRSettingsItem> items;
    public static boolean appVisible = false;
    private static SettingsActivity app;

    public static SettingsActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        listView = findViewById(R.id.settings_list);
    }

    public class SettingsListAdapter extends ArrayAdapter<String> {

        private List<BRSettingsItem> items;
        private Context mContext;

        public SettingsListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<BRSettingsItem> items) {
            super(context, resource);
            this.items = items;
            this.mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View v;
            BRSettingsItem item = items.get(position);
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();


            switch (item.type) {
                case SECTION:
                    v = inflater.inflate(settings_list_section, parent, false);
                    v.setOnClickListener(item.listener);
                    break;
                case SWITCH:
                    v = inflater.inflate(settings_list_switch, parent, false);
                    if (item.isChecked) {
                        View switchView = v.findViewById(R.id.button_switch);
                        if (switchView != null) {
                            switchView.setOnClickListener(item.listener);
                        }
                    }
                    break;
                default:
                    v = inflater.inflate(settings_list_item, parent, false);
                    TextView addon = (TextView) v.findViewById(R.id.item_addon);
                    addon.setText(item.addonText);
                    v.setOnClickListener(item.listener);
                    break;
            }

            TextView title = (TextView) v.findViewById(R.id.item_title);
            title.setText(item.title);
            return v;
        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
        if (items == null)
            items = new ArrayList<>();
        items.clear();

        populateItems();

        listView.setAdapter(new SettingsListAdapter(this, R.layout.settings_list_item, items));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
    }

    private void populateItems() {

        /*Wallet Title*/
        items.add(new BRSettingsItem(SECTION, getString(R.string.Settings_wallet), "", null, null));

        /*Import Title*/
        items.add(new BRSettingsItem(ADDON, getString(R.string.Settings_importTitle), "", null, v -> {
            Intent intent = new Intent(SettingsActivity.this, ImportActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);

        }));

        /*Wipe Start_Recover Wallet*/
        items.add(new BRSettingsItem(ADDON, getString(R.string.Settings_wipe), "", null, v -> {
            Intent intent = new Intent(SettingsActivity.this, WipeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
        }));

        /*Manage Title*/
        items.add(new BRSettingsItem(SECTION, getString(R.string.Settings_manage), "", null, null));

        /*Fingerprint Limits*/
        if (AuthManager.isFingerPrintAvailableAndSetup(this)) {
            items.add(new BRSettingsItem(ADDON, getString(R.string.Settings_touchIdLimit_android), "", null, v -> AuthManager.getInstance().authPrompt(SettingsActivity.this, null, getString(R.string.VerifyPin_continueBody), true, false, new BRAuthCompletion() {
                @Override
                public void onComplete() {
                    Intent intent = new Intent(SettingsActivity.this, SpendLimitActivity.class);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                    startActivity(intent);
                }

                @Override
                public void onCancel() {

                }
            })));
        }

        /*Languages*/
        items.add(new BRSettingsItem(ADDON, getString(R.string.Settings_languages), null, null, v -> {
            ChangeLanguageBottomSheet fragment = new ChangeLanguageBottomSheet();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        }));

        /*Display Currency*/
        items.add(new BRSettingsItem(ADDON, getString(R.string.Settings_currency), BRSharedPrefs.getIso(this), null, v -> {
            Intent intent = new Intent(SettingsActivity.this, DisplayCurrencyActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }));

        /*Sync Blockchain*/
        items.add(new BRSettingsItem(ADDON, getString(R.string.Settings_sync), "", null, v -> {
            Intent intent = new Intent(SettingsActivity.this, SyncBlockchainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }));

        /*SPACER*/
        items.add(new BRSettingsItem(SECTION, "", "", null, null));

        /*Share Anonymous Data*/
        items.add(new BRSettingsItem(ADDON, getString(R.string.Settings_shareData), "", null, v -> {
            Intent intent = new Intent(SettingsActivity.this, ShareDataActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }));

        /*About*/
        items.add(new BRSettingsItem(ADDON, getString(R.string.Settings_about), "", null, v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }));

        /*SPACER*/
        items.add(new BRSettingsItem(SECTION, "", "", null, null));

        /*Advanced Settings*/
        items.add(new BRSettingsItem(ADDON, getString(R.string.Settings_advancedTitle), "", null, v -> {
            Intent intent = new Intent(SettingsActivity.this, AdvancedActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }));

        /*SPACER*/
        items.add(new BRSettingsItem(SECTION, "", "", null, null));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }
}
