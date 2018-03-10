package com.vn.code;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.facebook.appevents.AppEventsLogger;

import butterknife.BindView;
import butterknife.ButterKnife;
import facebook.FacebookBanner;
import inter.OnErrorLoadAd;
import richadx.RichNativeAd;


public class DoneActivity extends AppCompatActivity {
    @BindView(R.id.frame_ads)
    LinearLayout frameAds;
    @BindView(R.id.native_ad_container_main)
    LinearLayout nativeAdContainerMain;
    private Button btnDone;
    AppEventsLogger logger;
    LinearLayout nativeAdContainer;
    View adView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);
        ButterKnife.bind(this);
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

    RichNativeAd nativeRich;
    FacebookBanner facebookBanner;

    private void loadAdx() {
        nativeRich = new RichNativeAd(this, frameAds, "/112517806/519401517413938");
        facebookBanner = new FacebookBanner(this, frameAds, "1631427560285640_1672975596130836");
        facebookBanner.setOnErrorLoadAd(new OnErrorLoadAd() {
            @Override
            public void onMyError() {
                nativeRich.show();
            }
        });

        facebookBanner.show();
    }


}
