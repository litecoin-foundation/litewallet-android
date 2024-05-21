
package com.breadwallet.presenter.activities.intro;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.breadwallet.presenter.activities.AnnounceUpdatesViewActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.breadwallet.entities.IntroLanguageResource;
import com.breadwallet.tools.adapter.CountryLanguageAdapter;
import com.breadwallet.tools.util.LocaleHelper;
import com.google.android.material.snackbar.Snackbar;
//import com.breadwallet.BuildConfig;
import com.breadwallet.R;
import com.breadwallet.presenter.activities.BreadActivity;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.security.BRKeyStore;
import com.breadwallet.tools.security.PostAuth;
import com.breadwallet.tools.security.SmartValidator;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.BRWalletManager;
import com.google.android.play.core.ktx.BuildConfig;
import com.platform.APIClient;
import java.io.Serializable;
import java.util.Objects;
import timber.log.Timber;

public class IntroActivity extends BRActivity implements Serializable {
    public Button newWalletButton;
    public Button recoverWalletButton;
    public static IntroActivity introActivity;
    public static boolean appVisible = false;
    private static IntroActivity app;
    public CountryLanguageAdapter countryLanguageAdapter;
    public RecyclerView listLangRecyclerView;
    public IntroLanguageResource introLanguageResource = new IntroLanguageResource();
    public static boolean isNewWallet = false;
    public static IntroActivity getApp() {
        return app;
    }

    public static final Point screenParametersPoint = new Point();
    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Log.i("Some", getPackageName());
        newWalletButton = (Button) findViewById(R.id.button_new_wallet);
        recoverWalletButton = (Button) findViewById(R.id.button_recover_wallet);
        TextView description = findViewById(R.id.textView);

        TextView versionText = findViewById(R.id.version_text);
        listLangRecyclerView = findViewById(R.id.language_list);
        View parentLayout = findViewById(android.R.id.content);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        countryLanguageAdapter = new CountryLanguageAdapter(this, introLanguageResource.loadResources());
        listLangRecyclerView.setAdapter(countryLanguageAdapter);

        listLangRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    int centerPosition = (lastVisibleItemPosition - firstVisibleItemPosition) / 2 + firstVisibleItemPosition;
                    if (centerPosition != RecyclerView.NO_POSITION) {
                        countryLanguageAdapter.updateCenterPosition(centerPosition);
                        description.setText(countryLanguageAdapter.selectedDesc());
                        showDialogForItem(countryLanguageAdapter.selectedMessage());
                    }
                }
            }

        });

        listLangRecyclerView.setLayoutManager(layoutManager);

        setListeners();
        updateBundles();
        if (!BuildConfig.DEBUG && BRKeyStore.AUTH_DURATION_SEC != 300) {
            RuntimeException ex = new RuntimeException("onCreate: AUTH_DURATION_SEC should be 300");
            Timber.e(ex);
            throw ex;
        }
        introActivity = this;

        getWindowManager().getDefaultDisplay().getSize(screenParametersPoint);
        versionText.setText(BRConstants.APP_VERSION_NAME_CODE);

        if (Utils.isEmulatorOrDebug(this))
            Utils.printPhoneSpecs();

        byte[] masterPubKey = BRKeyStore.getMasterPublicKey(this);
        boolean isFirstAddressCorrect = false;
        if (masterPubKey != null && masterPubKey.length != 0) {
            Timber.d("timber: masterPubkey exists");

            isFirstAddressCorrect = SmartValidator.checkFirstAddress(this, masterPubKey);
        }
        if (!isFirstAddressCorrect) {
            Timber.d("timber: Calling wipeWalletButKeyStore");
            BRWalletManager.getInstance().wipeWalletButKeystore(this);
        }

        if (BuildConfig.VERSION_NAME == "v2.8.4") {
            Snackbar.make(parentLayout,
                            R.string.release_notes,
                            Snackbar.LENGTH_INDEFINITE).setAction(R.string.Webview_dismiss, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    })
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                    .show();
        }

        PostAuth.getInstance().onCanaryCheck(this, false);
    }

    private void showDialogForItem(String title) {
        Dialog dialog = new Dialog(IntroActivity.this);
        dialog.setContentView(R.layout.pop_up_language_intro);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_pop_up_intro);
        Button btnYes = dialog.findViewById(R.id.button_yes);
        Button btnNo = dialog.findViewById(R.id.button_no);
        TextView txtTitle = dialog.findViewById(R.id.dialog_message);
        txtTitle.setText(title);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LocaleHelper.Companion.getInstance().setLocaleIfNeeded(countryLanguageAdapter.selectedLang())) {
                    dialog.dismiss();
                    recreate();
                }else {
                    dialog.dismiss();
                }
            }
        });


        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void updateBundles() {
        BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("updateBundle");
                final long startTime = System.currentTimeMillis();
                APIClient apiClient = APIClient.getInstance(IntroActivity.this);
                long endTime = System.currentTimeMillis();
                Timber.d("timber: updateBundle DONE in %sms",endTime - startTime);
            }
        });
    }

    private void setListeners() {
        newWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BreadActivity bApp = BreadActivity.getApp();
                if (bApp != null) bApp.finish();
                isNewWallet = true;
                Intent intentEmailNewWallet = new Intent(IntroActivity.this, AnnounceUpdatesViewActivity.class);
                intentEmailNewWallet.putExtra("isNewWallet", true);
                startActivity(intentEmailNewWallet);
            }
        });

        recoverWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BreadActivity bApp = BreadActivity.getApp();
                if (bApp != null) bApp.finish();
                isNewWallet = false;
                Intent intentEmailRecover = new Intent(IntroActivity.this, AnnounceUpdatesViewActivity.class);
                intentEmailRecover.putExtra("isNewWallet", false);
                startActivity(intentEmailRecover);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;

    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}
