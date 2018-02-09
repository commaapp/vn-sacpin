package com.vn.code;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

public class LockActivity extends AppCompatActivity {
    Fragment settingsFragment;
    LockFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fabric code

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_lock);


        settingsFragment = new LockFragmentSettings();
        mainFragment = new LockFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, mainFragment).commitAllowingStateLoss();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mainFragment != null)
            mainFragment.onWindowHasFocus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mainFragment != null)
            mainFragment.onOptimized();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mainFragment != null)
            mainFragment.onOptimized();
    }

    public void showSettings() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.translate_right_in, R.anim.translate_left_out, R.anim.translate_left_in, R.anim.translate_right_out);
        ft.replace(R.id.fragment_container, settingsFragment).commitAllowingStateLoss();
        ft.addToBackStack(SettingsActivity.class.getName());
//        startActivity(new Intent(LockActivity.this, SettingsActivity.class));
    }
//    public void showMain() {
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.setCustomAnimations(R.anim.translate_left_in, R.anim.translate_right_out);
//        ft.replace(R.id.fragment_container, mainFragment).commit();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(Config.IS_LOCKING, false).commit();
    }
}
