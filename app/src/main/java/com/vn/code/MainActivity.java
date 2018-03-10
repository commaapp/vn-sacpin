package com.vn.code;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.AdSettings;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.AdRequest;

import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.prefs.Prefs;

import static com.vn.code.Config.SETTINGS_PREFERENCE;
import static com.vn.code.Config.TRIGGER_EXIT_ON_UNPLUG;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {


    private static final String TAG = MainActivity.class.getName();

    private BroadcastReceiver broadcastReceiver;

    private FragmentManager.OnBackStackChangedListener
            mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            syncActionBarArrowState();
        }
    };

    //    private FragmentDrawer drawerFragment;
    private MainFragment mainFragment;

    private AppEventsLogger logger;
//    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = AppEventsLogger.newLogger(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AdSettings.addTestDevice("6571e1f14a42b3ca80108430a8209c7e");
//        AdRequest.Builder.addTestDevice("5EBD7399381255C67F1FD11B3792F569");
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowHomeEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
//        drawerFragment = (FragmentDrawer)
//                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
//        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
//        drawerFragment.setDrawerListener(this);

        if (!getIntent().getAction().equals(Intent.ACTION_POWER_CONNECTED))
            startActivity(new Intent(this, SplashActivity.class));
        Intent intentPowerService = new Intent();
        intentPowerService.setClass(this, PowerService.class);
        startService(intentPowerService);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                    SharedPreferences sharedPref = getSharedPreferences(SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
                    if (sharedPref.getBoolean(TRIGGER_EXIT_ON_UNPLUG, true))
                        finish();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(broadcastReceiver, intentFilter);

        mainFragment = new MainFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, mainFragment).commitAllowingStateLoss();

        askWriteSetting();
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mainFragment != null)
            mainFragment.onWindowHasFocus();
    }

    private void askWriteSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast.makeText(this, R.string.ask_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void syncActionBarArrowState() {
        invalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        boolean action = MyCache.getBooleanValueByName(MainActivity.this, Config.LOG_APP, "action");
        if (!action) {
            if (!MyCache.getBooleanValueByName(MainActivity.this, Config.LOG_APP, "rate")) {
                showDialogRateApp();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void showDialogRateApp() {
        final Dialog dialog1 = new Dialog(MainActivity.this);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        dialog1.setContentView(layoutInflater.inflate(R.layout.custom_dialog_rate, null));
        dialog1.setCancelable(true);
        final TextView btnRate = dialog1.findViewById(R.id.btnRate);
        TextView btnLater = dialog1.findViewById(R.id.btnLater);


        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyCache.getBooleanValueByName(MainActivity.this, Config.LOG_APP, "change")) {
                    MyCache.putBooleanValueByName(MainActivity.this, Config.LOG_APP, "rate", true);
                    dialog1.dismiss();
                    finish();
                }

            }
        });
        btnLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCache.putBooleanValueByName(MainActivity.this, Config.LOG_APP, "rate", false);
                dialog1.dismiss();
                finish();
            }
        });
        RatingBar mRatingBar = dialog1.findViewById(R.id.mRatingBar);
        LayerDrawable stars = (LayerDrawable) mRatingBar.getProgressDrawable();
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.colorLater), PorterDuff.Mode.SRC_ATOP);

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating > 3) {
                    Toast.makeText(MainActivity.this, getString(R.string.sms_thank_you_rate), Toast.LENGTH_SHORT).show();
                    MyCache.putBooleanValueByName(MainActivity.this, Config.LOG_APP, "rate", true);
                    Intent i = new Intent("android.intent.action.VIEW");
                    i.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                    startActivity(i);
                    finish();
                } else if (rating <= 3) {
                    MyCache.putBooleanValueByName(MainActivity.this, Config.LOG_APP, "change", true);
                } else {
                }
            }
        });

        dialog1.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        boolean isShowMain = getSupportFragmentManager().getBackStackEntryCount() == 0;
        MenuItem settings = menu.findItem(R.id.action_settings);
        settings.setVisible(isShowMain);
//        menu.findItem(R.id.action_love).setVisible(isShowMain);
        if (isShowMain) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(R.string.app_name);
        } else {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(R.string.action_settings);
        }

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_love:
                logger.logEvent("MainScreen_IconMoreapp_Clicked");
//                loadCrossInterstitial();
                rateApp();
                break;
            case R.id.action_settings:
                logger.logEvent("Main_IconMenuSetting_Clicked");
                startSettingsFragment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void rateApp() {
        final String appPackageName = getPackageName();

        Toast.makeText(this, R.string.rate_five_star, Toast.LENGTH_LONG).show();

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public void startSettingsFragment() {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        overridePendingTransition(R.anim.slide_up_info, R.anim.no_change);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                logger.logEvent("SlideMenu_IconRate5star_Clicked");

                rateApp();
                break;
            case 1:
                logger.logEvent("SlideMenu_IconShareApp_Clicked");

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case 2:
                logger.logEvent("SlideMenu_IconMoreApp_Clicked");

//                loadCrossInterstitial();
                break;
            case 3:
                logger.logEvent("SlideMenu_IconSetting_Clicked");
                startSettingsFragment();
                break;
            default:
                break;
        }
    }

    @OnClick({R.id.imv_rate_main, R.id.imv_setting_main})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imv_rate_main:
                logger.logEvent("MainScreen_IconMoreapp_Clicked");
//                loadCrossInterstitial();
                rateApp();
                break;
            case R.id.imv_setting_main:
                logger.logEvent("Main_IconMenuSetting_Clicked");
                startSettingsFragment();
                break;
        }
    }

    public static class buttonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            intent.setClass(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_POWER_CONNECTED);
            context.startActivity(intent);
        }

    }
}
