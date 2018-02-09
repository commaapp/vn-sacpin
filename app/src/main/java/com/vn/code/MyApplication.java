package com.vn.code;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import io.fabric.sdk.android.Fabric;

import static com.vn.code.R.id.btnOptimize;

public class MyApplication extends Application {
    private static final int NOTIFICATION_ID = 0;
    private static Context context;

    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        context = getApplicationContext();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
//        if (getHintStatus() == 1) {
//            setHintStatus(0);
//        } else if (getHintStatus() == 3) {
//            setHintStatus(2);
//        }
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static void showLowBatteryNotification() {

//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(context)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setContentTitle(context.getString(R.string.nof_title_low))
//                        .setContentText(context.getString(R.string.nof_fast_charge));
//        // Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(context, Config.class);
//        resultIntent.setAction(Intent.ACTION_MAIN);
//
//        // The stack builder object will contain an artificial back stack for the
//        // started Activity.
//        // This ensures that navigating backward from the Activity leads out of
//        // your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(Config.class);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
//        NotificationManager mNotificationManager =
//                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        // NOTIFICATION_ID allows you to update the notification later on.
//        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static void showNotificationOptimize() {
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
//                R.layout.layout_notification_optimize);
//        Intent intent = new Intent(context, MainActivity.buttonListener.class);
//        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
//        int strTitle;
//        int strText;
//        SharedPreferences sharedPref = context.getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
//        strTitle = sharedPref.getBoolean(Config.IS_CHARGER_CONNECTED, false) ? R.string.nof_title_charging : R.string.nof_title_not_charging;
//        strText = isOptimized() ? R.string.nof_fast_charge_lite_optimized : R.string.nof_fast_charge_lite_unoptimized;
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setAutoCancel(true)
//                .setOngoing(true)
//                .setContent(remoteViews);
//
//        remoteViews.setTextViewText(R.id.tvNotificationTitle, context.getString(strTitle));
////        remoteViews.setTextViewText(R.id.tvNotificationDes, context.getString(strText));
//        remoteViews.setOnClickPendingIntent(btnOptimize, PendingIntent.getBroadcast(context, 4, intent, 0));
//
//        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
//        notificationmanager.notify(1, builder.build());
    }

    public static void showNotification() {
        int strTitle;
        int strText;
        SharedPreferences sharedPref = context.getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        strTitle = sharedPref.getBoolean(Config.IS_CHARGER_CONNECTED, false) ? R.string.nof_title_charging : R.string.nof_title_not_charging;
        strText = isOptimized() ? R.string.nof_fast_charge_lite_optimized : R.string.nof_fast_charge_lite_unoptimized;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(strTitle))
                        .setContentText(context.getString(strText));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, Config.class);
        resultIntent.setAction(Intent.ACTION_MAIN);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(Config.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // NOTIFICATION_ID allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    public static boolean isOptimized() {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);

        boolean internetOffEnabled = sharedPref.getBoolean(Config.ENABLE_INTERNET_OFF, false);
        if (internetOffEnabled && ChargeUtils.isWifiEnabled(context)) {
            return false;
        }
        boolean brightnessMinEnabled = sharedPref.getBoolean(Config.ENABLE_BRIGHTNESS_MIN, true);
        if (brightnessMinEnabled && !ChargeUtils.isBrightnessMin(context)) {
            return false;
        }
        boolean bluetoothOffEnabled = sharedPref.getBoolean(Config.ENABLE_BLUETOOTH_OFF, true);
        if (bluetoothOffEnabled && ChargeUtils.isBluetoothEnabled(context)) {
            return false;
        }
        boolean rotateOffEnabled = sharedPref.getBoolean(Config.ENABLE_ROTATE_OFF, true);
        if (rotateOffEnabled && !ChargeUtils.isRotateOff(context)) {
            return false;
        }
        return true;
    }
}
