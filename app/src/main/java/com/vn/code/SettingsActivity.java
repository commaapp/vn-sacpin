package com.vn.code;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.prefs.Prefs;
import facebook.FacebookBanner;
import inter.OnErrorLoadAd;
import richadx.RichNativeAd;

public class SettingsActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {
    @BindView(R.id.frame_ads)
    LinearLayout frameAds;
    private ImageView btnBack;
    private AppEventsLogger logger;
    private ImageView btnRate;
    private LinearLayout btnDownload;
    List<ApplicationInfo> packages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_settings);
        ButterKnife.bind(this);
        initView();
        loadAdx();
    }

    RichNativeAd nativeRich;
    FacebookBanner facebookBanner;

    private void loadAdx() {


        nativeRich = new RichNativeAd(this, frameAds, "/112517806/519401517413692");
        facebookBanner = new FacebookBanner(this, frameAds, "1631427560285640_1672977689463960");
        facebookBanner.setOnErrorLoadAd(new OnErrorLoadAd() {
            @Override
            public void onMyError() {
                nativeRich.show();
            }
        });
        facebookBanner.show();


    }

    @SuppressLint("RestrictedApi")
    private void initView() {
        logger = AppEventsLogger.newLogger(this);
        btnRate = (ImageView) findViewById(R.id.btnRate);
        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingsActivity.this, getString(R.string.sms_thank_you_rate), Toast.LENGTH_SHORT).show();
                Intent i = new Intent("android.intent.action.VIEW");
                i.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                startActivity(i);
            }
        });
        updateSetting();
        btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        CheckBox internetControl = (CheckBox) findViewById(R.id.off_internet);

        ((CheckBox) findViewById(R.id.clear_ram)).setOnCheckedChangeListener(this);
        internetControl.setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.min_brightness)).setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.off_bluetooth)).setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.off_rotate)).setOnCheckedChangeListener(this);

        ((RadioGroup) findViewById(R.id.trigger_on_plug)).setOnCheckedChangeListener(this);

        ((CheckBox) findViewById(R.id.exit_on_unplug)).setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.state_on_lock)).setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.restore_on_exit)).setOnCheckedChangeListener(this);
        ((CheckBox) findViewById(R.id.full_battery_on_exit)).setOnCheckedChangeListener(this);

        if (ChargeUtils.isDeviceRooted())
            internetControl.setText(R.string.function_off_internet);
        else
            internetControl.setText(R.string.function_off_wifi);

        PackageManager pm = getPackageManager();
        packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
    }


    private void updateSetting() {
        SharedPreferences sharedPref = getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        ((CheckBox) findViewById(R.id.clear_ram)).setChecked(sharedPref.getBoolean(Config.ENABLE_CLEAR_MEM, true));
        ((CheckBox) findViewById(R.id.off_internet)).setChecked(sharedPref.getBoolean(Config.ENABLE_INTERNET_OFF, false));
        ((CheckBox) findViewById(R.id.min_brightness)).setChecked(sharedPref.getBoolean(Config.ENABLE_BRIGHTNESS_MIN, true));
        ((CheckBox) findViewById(R.id.off_bluetooth)).setChecked(sharedPref.getBoolean(Config.ENABLE_BLUETOOTH_OFF, true));
        ((CheckBox) findViewById(R.id.off_rotate)).setChecked(sharedPref.getBoolean(Config.ENABLE_ROTATE_OFF, true));

        String triggerOnPlug = sharedPref.getString(Config.TRIGGER_ON_PLUG, Config.TRIGGER_ASK_2_RUN);
        if (triggerOnPlug.equals(Config.TRIGGER_AUTO_RUN))
            ((RadioGroup) findViewById(R.id.trigger_on_plug)).check(R.id.auto_run);
        else if (triggerOnPlug.equals(Config.TRIGGER_ASK_2_RUN))
            ((RadioGroup) findViewById(R.id.trigger_on_plug)).check(R.id.ask_run);
        else if (triggerOnPlug.equals(Config.TRIGGER_NO_RUN))
            ((RadioGroup) findViewById(R.id.trigger_on_plug)).check(R.id.no_run);

        ((CheckBox) findViewById(R.id.exit_on_unplug)).setChecked(sharedPref.getBoolean(Config.TRIGGER_EXIT_ON_UNPLUG, true));
        ((CheckBox) findViewById(R.id.state_on_lock)).setChecked(sharedPref.getBoolean(Config.TRIGGER_SHOW_STATE_CHARGING, true));
        ((CheckBox) findViewById(R.id.restore_on_exit)).setChecked(sharedPref.getBoolean(Config.TRIGGER_RESTORE_STATE, true));
        ((CheckBox) findViewById(R.id.full_battery_on_exit)).setChecked(Prefs.with(getApplicationContext()).readBoolean(Config.TRIGGER_FULL_BATTERY, true));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        SharedPreferences sharedPref = getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (compoundButton.getId()) {
            case R.id.clear_ram:
                if (checked)
                    logger.logEvent("Setting_ClearRam_Checked");
                else
                    logger.logEvent("Setting_ClearRam_UnChecked");
                editor.putBoolean(Config.ENABLE_CLEAR_MEM, checked);
                break;
            case R.id.off_internet:
                if (checked)
                    logger.logEvent("Setting_InternetOff_Checked");
                else
                    logger.logEvent("Setting_InternetOff_UnChecked");
                editor.putBoolean(Config.ENABLE_INTERNET_OFF, checked);
                break;
            case R.id.min_brightness:
                if (checked)
                    logger.logEvent("Setting_BrightnessMin_Checked");
                else
                    logger.logEvent("Setting_BrightnessMin_UnChecked");
                editor.putBoolean(Config.ENABLE_BRIGHTNESS_MIN, checked);
                break;
            case R.id.off_bluetooth:
                if (checked)
                    logger.logEvent("Setting_BluetoothOff_Checked");
                else
                    logger.logEvent("SettingScreen_BluetoothOff_UnChecked");
                editor.putBoolean(Config.ENABLE_BLUETOOTH_OFF, checked);
                break;
            case R.id.off_rotate:
                if (checked)
                    logger.logEvent("Setting_RotateOff_Checked");
                else
                    logger.logEvent("Setting_RotateOff_UnChecked");
                editor.putBoolean(Config.ENABLE_ROTATE_OFF, checked);
                break;
            case R.id.exit_on_unplug:
                if (checked)
                    logger.logEvent("Setting_AutoExitApp_Checked");
                else
                    logger.logEvent("Setting_AutoExitApp_UnChecked");
                editor.putBoolean(Config.TRIGGER_EXIT_ON_UNPLUG, checked);
                break;
            case R.id.state_on_lock:
                if (checked)
                    logger.logEvent("Setting_ShowStateCharging_Checked");
                else
                    logger.logEvent("Setting_ShowStateCharging_UnChecked");
                editor.putBoolean(Config.TRIGGER_SHOW_STATE_CHARGING, checked);
                break;
            case R.id.restore_on_exit:
                if (checked)
                    logger.logEvent("Setting_AutoRestoreState_Checked");
                else
                    logger.logEvent("Setting_AutoRestoreState_UnChecked");
                editor.putBoolean(Config.TRIGGER_RESTORE_STATE, checked);
                break;
            case R.id.full_battery_on_exit:
                Prefs.with(getApplicationContext()).writeBoolean(Config.TRIGGER_FULL_BATTERY, checked);
                if (checked) {
                    logger.logEvent("Setting_Alertbatteryfull_Checked");
                    Intent intentPowerService = new Intent(this, PowerService.class);
                    startService(intentPowerService);
                } else {
                    logger.logEvent("Setting_Alertbatteryfull_Unchecked");
                }

                break;
        }
        editor.commit();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
        SharedPreferences sharedPref = getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (i) {
            case R.id.no_run:
                logger.logEvent("Setting_DoNotRun_Selected");
                editor.putString(Config.TRIGGER_ON_PLUG, Config.TRIGGER_NO_RUN);
                break;
            case R.id.ask_run:
                logger.logEvent("Setting_ShowAskDialog_Selected");
                editor.putString(Config.TRIGGER_ON_PLUG, Config.TRIGGER_ASK_2_RUN);
                break;
            case R.id.auto_run:
                logger.logEvent("Setting_AutoRunApp_Selected");
                editor.putString(Config.TRIGGER_ON_PLUG, Config.TRIGGER_AUTO_RUN);
                break;
        }
        editor.commit();
    }


}
