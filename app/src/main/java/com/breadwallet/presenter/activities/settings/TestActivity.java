package com.breadwallet.presenter.activities.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.util.BRActivity;

public class TestActivity extends BRActivity {
    private static final String TAG = TestActivity.class.getName();


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // https://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
        // No call for super(). Bug on API Level > 11. 
        // Removed super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}
