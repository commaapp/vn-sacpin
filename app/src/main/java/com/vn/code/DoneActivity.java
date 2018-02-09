package com.vn.code;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.facebook.appevents.AppEventsLogger;

import facebook.FacebookNativeAdFragment;
import inter.OnErrorLoadAd;
import richadx.NativeAdFragment;


public class DoneActivity extends AppCompatActivity {
    private Button btnDone;
    AppEventsLogger logger;
    LinearLayout nativeAdContainer;
    View adView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);
        logger = AppEventsLogger.newLogger(DoneActivity.this);

        btnDone = (Button) findViewById(R.id.btn_ok);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        loadAdx();


    }
    NativeAdFragment nativeRich;
    FacebookNativeAdFragment nativeFB;

    private void loadAdx() {
        nativeRich = new NativeAdFragment();
        nativeRich.setIdAd("/112517806/519401517413938");

        nativeFB = new FacebookNativeAdFragment();
        nativeFB.setIdAd("1631427560285640_1672975596130836");

        nativeFB.setOnErrorLoadAd(new OnErrorLoadAd() {
            @Override
            public void onError() {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_ads, nativeRich).commitAllowingStateLoss();
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_ads, nativeFB).commitAllowingStateLoss();
    }



}
