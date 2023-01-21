package com.breadwallet.presenter.activities.settings;

import static com.breadwallet.R.layout.settings_list_item;
import static com.breadwallet.R.layout.settings_list_section;
import static com.breadwallet.R.layout.settings_list_switch;
import static com.breadwallet.presenter.entities.BRSettingsItem.SettingItemsType.ADDON;
import static com.breadwallet.presenter.entities.BRSettingsItem.SettingItemsType.SECTION;
import static com.breadwallet.presenter.entities.BRSettingsItem.SettingItemsType.SWITCH;

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
import androidx.appcompat.widget.SwitchCompat;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.presenter.entities.BRSettingsItem;
import com.breadwallet.tools.manager.BRSharedPrefs;

import java.util.ArrayList;
import java.util.List;

public class AdvancedActivity extends BRActivity {
    private static final String TAG = AdvancedActivity.class.getName();
    private ListView listView;
    public List<BRSettingsItem> items;
    public static boolean appVisible = false;
    private static AdvancedActivity app;

    public static AdvancedActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);

        listView = (ListView) findViewById(R.id.settings_list);
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
                    View switchView = v.findViewById(R.id.item_switch);
                    View desc = v.findViewById(R.id.item_desc);
                    if (switchView instanceof SwitchCompat) {
                        ((SwitchCompat) switchView).setChecked(item.isChecked);
                        switchView.setOnClickListener(view -> {
                            //relay click event to item.listener
                            item.listener.onClick(view);

                            if (desc instanceof TextView && view instanceof SwitchCompat) {
                                Boolean isChecked = ((SwitchCompat) view).isChecked();
                                ((TextView) desc).setText(isChecked ?
                                        getString(R.string.DeveloperMode_On) : getString(R.string.DeveloperMode_Off));
                            }
                        });
                        if (desc instanceof TextView) {
                            ((TextView) desc).setText(item.isChecked ?
                                    getString(R.string.DeveloperMode_On) : getString(R.string.DeveloperMode_Off));
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
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    private void populateItems() {

        items.add(new BRSettingsItem(SECTION, null, null, null, null));

        items.add(new BRSettingsItem(ADDON, getString(R.string.NodeSelector_title), "", null, v -> {
            Intent intent = new Intent(AdvancedActivity.this, NodesActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.empty_300);
        }));

        items.add(new BRSettingsItem(SWITCH, getString(R.string.DeveloperMode_title), "",
                BRSharedPrefs.isApiServerModeDev(getApplicationContext()), v -> {
            if (v instanceof SwitchCompat) {
                if (((SwitchCompat) v).isChecked()) {
                    BRSharedPrefs.setApiServerMode(v.getContext(), BRSharedPrefs.PrefsServerMode.DEV);
                } else {
                    BRSharedPrefs.setApiServerMode(v.getContext(), BRSharedPrefs.PrefsServerMode.PROD);
                }
            }
        }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }
}
