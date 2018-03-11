package com.vn.code;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;

import es.dmoral.prefs.Prefs;

import static com.facebook.FacebookSdk.getApplicationContext;

public class PowerConnectionReceiver extends BroadcastReceiver {
    boolean screenOff;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
//        try {
//            Bundle animation = ActivityOptions.makeCustomAnimation(context,
//                    R.anim.fade_in,
//                    R.anim.fade_out)
//                    .toBundle();
//        } catch (Exception e) {
//
//        }
        if (intent.getAction().equals("android.intent.action.ACTION_POWER_CONNECTED")) {
            SharedPreferences sharedPref = context.getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            Intent i = new Intent(context, FloatActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            editor.putBoolean(Config.IS_ASKING, true).apply();
            context.startActivity(i);
            notiFullBattery();
            if (((screenOff = !isScreenOn(context)) || !ChargeUtils.isAppOnForeground(context))) {
                if (screenOff) {
//                    if (sharedPref.getBoolean(Config.TRIGGER_SHOW_STATE_CHARGING, true)) {
                    Intent lockIntent = new Intent(context, LockActivity.class);
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    editor.putBoolean(Config.IS_LOCKING, true).apply();
                    context.startActivity(lockIntent);
//                    }
                }

//                String onPlugMode = sharedPref.getString(Config.TRIGGER_ON_PLUG, Config.TRIGGER_ASK_2_RUN);
//                if (onPlugMode.equals(Config.TRIGGER_ASK_2_RUN)) {
//                    Intent i = new Intent(context, FloatActivity.class);
//                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    editor.putBoolean(Config.IS_ASKING, true).apply();
//                    context.startActivity(i, animation);
//                } else if (onPlugMode.equals(Config.TRIGGER_AUTO_RUN)) {
//                    Intent i = new Intent(context, MainActivity.class);
//                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    i.setAction(Intent.ACTION_POWER_CONNECTED);
//                    context.startActivity(i, animation);
//                }
            }

            editor.putBoolean(Config.IS_CHARGER_CONNECTED, true);
            editor.commit();
//            MyApplication.showNotification();
            MyApplication.showNotificationOptimize();
        } else if (intent.getAction().equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
            SharedPreferences sharedPref = context.getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
            boolean restoreOnUnPlug = sharedPref.getBoolean(Config.TRIGGER_RESTORE_STATE, true);
            if (restoreOnUnPlug && MyApplication.isOptimized())
                restoreState(sharedPref, context);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Config.IS_CHARGER_CONNECTED, false)
                    .putInt(Config.BATTERY_LEVEL, -1)
                    .putFloat(Config.CHARGING_SPEED, -1)
                    .putInt(Config.CHARGING_SPEED_INDEX, -1)
                    .apply();
        }

    }

    private void notiFullBattery() {
        boolean isFull = Prefs.with(getApplicationContext()).readBoolean(Config.TRIGGER_FULL_BATTERY, true);
        if (isFull) {
            if (getBatteryPercentage() >= 100) {
                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.ting);
                mediaPlayer.start();
            }
        }
    }

    private Intent getBatteryStatusIntent() {
        IntentFilter batFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        return getApplicationContext().registerReceiver(null, batFilter);
    }

    private int getBatteryPercentage() {
        int percentage = 0;
        Intent batteryStatus = getBatteryStatusIntent();
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            percentage = (int) ((level / (float) scale) * 100);
        }

        return percentage;
    }

    private void restoreState(SharedPreferences sharedPref, Context context) {
        if (sharedPref.getBoolean(Config.ENABLE_BRIGHTNESS_MIN, true))
            if (sharedPref.getInt(Config.STATE_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(context)) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    int br = sharedPref.getInt(Config.STATE_BRIGHTNESS, 0);

                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, br);
                }
            }
        if (sharedPref.getBoolean(Config.ENABLE_BLUETOOTH_OFF, true))
            if (sharedPref.getBoolean(Config.STATE_BLUETOOTH, false)) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                }
            }

        if (sharedPref.getBoolean(Config.ENABLE_ROTATE_OFF, true))
            if (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(context))
                Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, sharedPref.getInt(Config.STATE_ROTATE, 0));

        if (sharedPref.getBoolean(Config.ENABLE_INTERNET_OFF, false))
            if (sharedPref.getBoolean(Config.STATE_WIFI, false)) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
            }

        if (sharedPref.getBoolean(Config.ENABLE_INTERNET_OFF, false))
            if (sharedPref.getBoolean(Config.STATE_MOBILE_DATA, false)) {
                ChargeUtils.setConnection(true, context);
            }
    }

    public boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm.isScreenOn();
        }
    }
}
