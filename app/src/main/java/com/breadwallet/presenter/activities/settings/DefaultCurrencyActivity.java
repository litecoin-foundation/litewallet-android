package com.breadwallet.presenter.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.RestoreActivity;
import com.breadwallet.presenter.activities.util.ActivityUTILS;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.presenter.entities.CurrencyEntity;
import com.breadwallet.tools.adapter.CurrencyListAdapter;
import com.breadwallet.tools.manager.SharedPreferencesManager;
import com.breadwallet.tools.sqlite.CurrencyDataSource;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.BRCurrency;

import java.math.BigDecimal;

import static com.platform.HTTPServer.URL_SUPPORT;


public class DefaultCurrencyActivity extends BRActivity {
    private static final String TAG = DefaultCurrencyActivity.class.getName();
    private TextView exchangeText;
    private ListView listView;
    private CurrencyListAdapter adapter;
    //    private String ISO;
//    private float rate;
    public static boolean appVisible = false;
    private static DefaultCurrencyActivity app;
    private Button leftButton;
    private Button rightButton;

    public static DefaultCurrencyActivity getApp() {
        return app;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_currency);

        ImageButton faq = (ImageButton) findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity app = DefaultCurrencyActivity.this;
                Intent intent = new Intent(app, WebViewActivity.class);
                intent.putExtra("url", URL_SUPPORT);
                intent.putExtra("articleId", BRConstants.defaultCurrency);
                app.startActivity(intent);
                app.overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });

        exchangeText = (TextView) findViewById(R.id.exchange_text);
        listView = (ListView) findViewById(R.id.currency_list_view);
        adapter = new CurrencyListAdapter(this);
        adapter.addAll(CurrencyDataSource.getInstance(this).getAllCurrencies());
        leftButton = (Button) findViewById(R.id.left_button);
        rightButton = (Button) findViewById(R.id.right_button);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButton(true);
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButton(false);
            }
        });

        int unit = SharedPreferencesManager.getCurrencyUnit(this);
        if (unit == BRConstants.CURRENT_UNIT_BITS) {
            setButton(true);
        } else {
            setButton(false);
        }
        updateExchangeRate();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView currencyItemText = (TextView) view.findViewById(R.id.currency_item_text);
                final String selectedCurrency = currencyItemText.getText().toString();
                String iso = selectedCurrency.substring(0, 3);
                SharedPreferencesManager.putIso(DefaultCurrencyActivity.this, iso);
                SharedPreferencesManager.putCurrencyListPosition(DefaultCurrencyActivity.this, position);

                updateExchangeRate();

            }

        });
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void updateExchangeRate() {
        //set the rate from the last saved
        String iso = SharedPreferencesManager.getIso(this);
        CurrencyEntity entity = CurrencyDataSource.getInstance(this).getCurrencyByIso(iso);
        if (entity != null) {
            String finalExchangeRate = BRCurrency.getFormattedCurrencyString(DefaultCurrencyActivity.this, SharedPreferencesManager.getIso(this), new BigDecimal(entity.rate));
            boolean bits = SharedPreferencesManager.getCurrencyUnit(this) == BRConstants.CURRENT_UNIT_BITS;
            exchangeText.setText(BRCurrency.getFormattedCurrencyString(this, "BTC", new BigDecimal(bits ? 1000000 : 1)) + " = " + finalExchangeRate);
        }
        adapter.notifyDataSetChanged();
    }

    private void setButton(boolean left) {
        if (left) {
            SharedPreferencesManager.putCurrencyUnit(this, BRConstants.CURRENT_UNIT_BITS);
            leftButton.setTextColor(getColor(R.color.white));
            leftButton.setBackground(getDrawable(R.drawable.b_half_left_blue));
            rightButton.setTextColor(getColor(R.color.dark_blue));
            rightButton.setBackground(getDrawable(R.drawable.b_half_right_blue_stroke));
        } else {
            SharedPreferencesManager.putCurrencyUnit(this, BRConstants.CURRENT_UNIT_BITCOINS);
            leftButton.setTextColor(getColor(R.color.dark_blue));
            leftButton.setBackground(getDrawable(R.drawable.b_half_left_blue_stroke));
            rightButton.setTextColor(getColor(R.color.white));
            rightButton.setBackground(getDrawable(R.drawable.b_half_right_blue));
        }
        updateExchangeRate();

    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
        ActivityUTILS.init(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }


}
