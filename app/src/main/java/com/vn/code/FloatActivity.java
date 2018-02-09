package com.vn.code;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;


import com.facebook.appevents.AppEventsLogger;

import facebook.FacebookNativeAdFragment;
import inter.OnErrorLoadAd;
import richadx.NativeAdFragment;

import static com.vn.code.Config.*;


public class FloatActivity extends AppCompatActivity implements View.OnClickListener {

    private BroadcastReceiver broadcastReceiver;
    private AppEventsLogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        logger = AppEventsLogger.newLogger(this);
        logger.logEvent("DialogAsk_Show");

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_float);

        findViewById(R.id.btn_no).setOnClickListener(this);
        findViewById(R.id.btn_yes).setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, filter);
        loadAdx();
    }

    NativeAdFragment nativeRich;
    FacebookNativeAdFragment nativeFB;

    private void loadAdx() {
        nativeRich = new NativeAdFragment();
        nativeRich.setIdAd("/112517806/519401517414007");

        nativeFB = new FacebookNativeAdFragment();
        nativeFB.setIdAd("1631427560285640_1672977469463982");

        nativeFB.setOnErrorLoadAd(new OnErrorLoadAd() {
            @Override
            public void onError() {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_ads, nativeRich).commitAllowingStateLoss();
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_ads, nativeFB).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_no:
                logger.logEvent("DialogAsk_ButtonNo_Clicked");

                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                break;
            case R.id.btn_yes:
                logger.logEvent("DialogAsk_ButtonYes_Clicked");

                SharedPreferences sharedPref = getSharedPreferences(SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
                if (sharedPref.getBoolean(TRIGGER_SHOW_STATE_CHARGING, true) && sharedPref.getBoolean(IS_LOCKING, false)) {
                    ChargeUtils.doOptimize(view.getContext());
                    Intent lockIntent = new Intent(this, LockActivity.class);
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(lockIntent);
                } else {
                    KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                    if (myKM.inKeyguardRestrictedInputMode() && sharedPref.getBoolean(TRIGGER_SHOW_STATE_CHARGING, true)) {
                        Intent lockIntent = new Intent(this, LockActivity.class);
                        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(lockIntent);
                    } else {
                        Intent intent = new Intent();
                        intent.setClass(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(Intent.ACTION_POWER_CONNECTED);

                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                }
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        SharedPreferences sharedPref = getSharedPreferences(SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(IS_ASKING, false).commit();
    }

    LinearLayout nativeAdContainer;
    View adView;


}
