package com.breadwallet.tools.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.UpdatePinActivity;
import com.breadwallet.presenter.activities.intro.WriteDownActivity;
import com.breadwallet.presenter.activities.settings.ShareDataActivity;
import com.breadwallet.tools.security.BRKeyStore;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.wallet.BRPeerManager;

import static com.breadwallet.tools.manager.PromptManager.PromptItem.PAPER_KEY;
import static com.breadwallet.tools.manager.PromptManager.PromptItem.RECOMMEND_RESCAN;
import static com.breadwallet.tools.manager.PromptManager.PromptItem.SHARE_DATA;
import static com.breadwallet.tools.manager.PromptManager.PromptItem.UPGRADE_PIN;

public class PromptManager {

    private PromptManager() {
    }

    private static PromptManager instance;

    public static PromptManager getInstance() {
        if (instance == null) instance = new PromptManager();
        return instance;
    }

    public enum PromptItem {
        SYNCING,
        FINGER_PRINT,
        PAPER_KEY,
        UPGRADE_PIN,
        RECOMMEND_RESCAN,
        NO_PASSCODE,
        SHARE_DATA
    }

    public class PromptInfo {
        public String title;
        public String description;
        public View.OnClickListener listener;

        public PromptInfo(String title, String description, View.OnClickListener listener) {
            assert (title != null);
            assert (description != null);
            assert (listener != null);
            this.title = title;
            this.description = description;
            this.listener = listener;
        }
    }

    public boolean shouldPrompt(Context app, PromptItem item) {
        assert (app != null);
        switch (item) {
            case PAPER_KEY:
                return !BRSharedPrefs.getPhraseWroteDown(app);
            case UPGRADE_PIN:
                return BRKeyStore.getPinCode(app).length() != 6;
            case RECOMMEND_RESCAN:
                return BRSharedPrefs.getScanRecommended(app);
            case SHARE_DATA:
                return !BRSharedPrefs.getShareData(app) && !BRSharedPrefs.getShareDataDismissed(app);

        }
        return false;
    }

    public PromptItem nextPrompt(Context app) {
        if (shouldPrompt(app, RECOMMEND_RESCAN)) return RECOMMEND_RESCAN;
        if (shouldPrompt(app, UPGRADE_PIN)) return UPGRADE_PIN;
        if (shouldPrompt(app, PAPER_KEY)) return PAPER_KEY;
        if (shouldPrompt(app, SHARE_DATA)) return SHARE_DATA;
        return null;
    }

    public PromptInfo promptInfo(final Activity app, PromptItem item) {
        switch (item) {
            case PAPER_KEY:
                return new PromptInfo(app.getString(R.string.Prompts_PaperKey_title), app.getString(R.string.Prompts_PaperKey_body), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(app, WriteDownActivity.class);
                        app.startActivity(intent);
                        app.overridePendingTransition(R.anim.enter_from_bottom, R.anim.fade_down);
                    }
                });
            case UPGRADE_PIN:
                return new PromptInfo(app.getString(R.string.Prompts_UpgradePin_title), app.getString(R.string.Prompts_UpgradePin_body), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(app, UpdatePinActivity.class);
                        app.startActivity(intent);
                        app.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                    }
                });
            case RECOMMEND_RESCAN:
                return new PromptInfo(app.getString(R.string.Prompts_RecommendRescan_title), app.getString(R.string.Prompts_RecommendRescan_body), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                            @Override
                            public void run() {
                                BRSharedPrefs.putStartHeight(app, 0);
                                BRPeerManager.getInstance().rescan();
                                BRSharedPrefs.putScanRecommended(app, false);
                            }
                        });
                    }
                });
            case SHARE_DATA:
                return new PromptInfo(app.getString(R.string.Prompts_ShareData_title), app.getString(R.string.Prompts_ShareData_body), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(app, ShareDataActivity.class);
                                app.startActivity(intent);
                                app.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                            }
                        });
                    }
                });

        }
        return null;
    }

    /**
     * touchIdPrompt - Shown to the user to enable biometric authentication for purchases under a certain amount.
     * paperKeyPrompt - Shown to the user if they have not yet written down their paper key. This is a persistent prompt and shows up until the user has gone through the paper key flow.
     * upgradePinPrompt - Shown to recommend to the user they should upgrade their PIN from 4 digits to 6. Only shown once. If the user dismisses do not show again.
     * recommendRescanPrompt - Shown when the user should rescan the blockchain
     * noPasscodePrompt - Shown when the user does not have a passcode set up for their device.
     * shareDataPrompt - Shown when asking the user if they wish to share anonymous data. Lowest priority prompt. Only show once and if they dismiss do not show again.
     */
    public String getPromptName(PromptItem prompt) {
        switch (prompt) {
            case PAPER_KEY:
                return "paperKeyPrompt";
            case UPGRADE_PIN:
                return "upgradePinPrompt";
            case RECOMMEND_RESCAN:
                return "recommendRescanPrompt";
            case NO_PASSCODE:
                return "noPasscodePrompt";
            case SHARE_DATA:
                return "shareDataPrompt";

        }
        return null;
    }

}
