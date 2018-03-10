package com.vn.code;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import facebook.FacebookInterstitialFragment;
import inter.OnErrorLoadAd;
import richadx.RichInterstialAdFragment;

/**
 * Created by d on 1/24/2018.
 */

public class SplashActivity extends AppCompatActivity {
    FacebookInterstitialFragment facebookInterstitialFragment;
    RichInterstialAdFragment richInterstialAdFragment;
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        richInterstialAdFragment = new RichInterstialAdFragment();
        facebookInterstitialFragment = new FacebookInterstitialFragment();
                try {
                    richInterstialAdFragment.setOnErrorLoadAd(new OnErrorLoadAd() {

                        @Override
                        public void onMyError() {
                            finish();
//                            facebookInterstitialFragment.setOnErrorLoadAd(new OnErrorLoadAd() {
//                                @Override
//                                public void onError() {
//
//                                }
//                            });
//                            getSupportFragmentManager().beginTransaction().replace(R.id.frame_ad, facebookInterstitialFragment).commitAllowingStateLoss();
                        }
                    });
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_ad, richInterstialAdFragment).commitAllowingStateLoss();
                } catch (Exception e) {
                    finish();
                }



    }

}
