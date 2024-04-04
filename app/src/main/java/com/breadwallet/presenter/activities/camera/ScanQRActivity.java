package com.breadwallet.presenter.activities.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.tools.animation.SpringAnimator;
import com.breadwallet.tools.qrcode.QRCodeReaderView;
import com.breadwallet.tools.security.BitcoinUrlHandler;

import timber.log.Timber;

public class ScanQRActivity extends BRActivity implements ActivityCompat.OnRequestPermissionsResultCallback, QRCodeReaderView.OnQRCodeReadListener {
    private ImageView cameraGuide;
    private TextView descriptionText;
    private long lastUpdated;
    private UIUpdateTask task;
    private boolean handlingCode;
    public static boolean appVisible = false;
    private static final int MY_PERMISSION_REQUEST_CAMERA = 56432;

    private QRCodeReaderView qrCodeReaderView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        cameraGuide = findViewById(R.id.scan_guide);
        descriptionText = findViewById(R.id.description_text);

        task = new UIUpdateTask();
        task.start();

        cameraGuide.setImageResource(R.drawable.cameraguide);
        cameraGuide.setVisibility(View.GONE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
            Timber.d("timber: onCreate: Permissions needed? HUH?");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraGuide.setVisibility(View.VISIBLE);
                SpringAnimator.showExpandCameraGuide(cameraGuide);
            }
        }, 400);
    }


    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        if (qrCodeReaderView != null) {
            qrCodeReaderView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
        }
        task.stopTask();
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.fade_down, 0);
        super.onBackPressed();
    }

    private class UIUpdateTask extends Thread {
        public boolean running = true;

        @Override
        public void run() {
            super.run();
            while (running) {
                if (System.currentTimeMillis() - lastUpdated > 300) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cameraGuide.setImageResource(R.drawable.cameraguide);
                            descriptionText.setText("");
                        }
                    });
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Timber.e(e);
                }
            }
        }

        public void stopTask() {
            running = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
            Timber.w("Camera permission request was denied.");
        }
    }

    @Override
    public void onQRCodeRead(final String text, PointF[] points) {
        if (handlingCode) return;
        if (BitcoinUrlHandler.isBitcoinUrl(text)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastUpdated = System.currentTimeMillis();
                    cameraGuide.setImageResource(R.drawable.cameraguide);
                    descriptionText.setText("");
                    handlingCode = true;
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", text);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();

                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastUpdated = System.currentTimeMillis();
                    cameraGuide.setImageResource(R.drawable.cameraguide_red);
                    descriptionText.setText(getString(R.string.Send_invalidAddressTitle));
                }
            });
        }
    }

    private void initQRCodeReaderView() {
        qrCodeReaderView = findViewById(R.id.qrdecoderview);
        qrCodeReaderView.setAutofocusInterval(500L);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setBackCamera();
        qrCodeReaderView.startCamera();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}