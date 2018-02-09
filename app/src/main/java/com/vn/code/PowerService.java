package com.vn.code;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Date;

public class PowerService extends Service {

    private BroadcastReceiver broadcastReceiver;

    private long batteryLevelDate;

    @Override
    public void onCreate() {
        super.onCreate();
        addBroadcastReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addBroadcastReceiver();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        //restart on all task remove
        Intent restartServiceIntent = new Intent(getApplicationContext(), FloatActivity.class);
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);
    }

    private void addBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
//        filter.addAction(Intent.ACTION_BATTERY_LOW);
        registerReceiver(broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
              //  if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
//                    Intent intentPowerService = new Intent(context, PowerService.class);
////                    intentPowerService.setClass(context, PowerService.class);
//                    intentPowerService.setAction(Intent.ACTION_MAIN);
//                    context.startService(intentPowerService);
//                    MyApplication.showLowBatteryNotification();
//                } else
                    if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = context.registerReceiver(null, filter);
                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;

                    SharedPreferences sharedPref = getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
                    if (isCharging && !sharedPref.getBoolean(Config.IS_ASKING, false) && sharedPref.getBoolean(Config.TRIGGER_SHOW_STATE_CHARGING, true)) {
                        final Intent i = new Intent(context, LockActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    }
                } else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                    SharedPreferences sharedPref = getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    int batteryLevel = getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE)
                            .getInt(Config.BATTERY_LEVEL, -1);
                    if (batteryLevel != -1) {
                        try {
                            float speed = (new Date().getTime() - batteryLevelDate) / (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) - batteryLevel); // milliseconds/level
                            int chargingSpeedIndex = sharedPref.getInt(Config.CHARGING_SPEED_INDEX, -1);
                            editor.putFloat(Config.CHARGING_SPEED, speed)
                                    .putInt(Config.CHARGING_SPEED_INDEX, chargingSpeedIndex + 1)
                                    .apply();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    editor.putInt(Config.BATTERY_LEVEL, intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)).commit();
                    batteryLevelDate = new Date().getTime();
                }
            }
        }, filter);
    }

    public void setMobileDataState(boolean mobileDataEnabled) {
        try {
            enforceCallingOrSelfPermission(android.Manifest.permission.MODIFY_PHONE_STATE, null);

            TelephonyManager telephonyService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);

            if (null != setMobileDataEnabledMethod) {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
        } catch (Exception ex) {
            Log.e(Config.class.getName(), "Error setting mobile data state", ex);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
