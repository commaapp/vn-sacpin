package com.vn.code;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ChargeUtils {
    public static void doOptimize(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        boolean clearMem = sharedPref.getBoolean(Config.ENABLE_CLEAR_MEM, true);
        if (clearMem)
            ChargeUtils.clearMem(context);
        boolean internetOff = sharedPref.getBoolean(Config.ENABLE_INTERNET_OFF, false);
        if (internetOff)
            ChargeUtils.offInternet(context);
        boolean brightnessMin = sharedPref.getBoolean(Config.ENABLE_BRIGHTNESS_MIN, true);
        if (brightnessMin)
            ChargeUtils.reduceBrightness(context);
        boolean bluetoothOff = sharedPref.getBoolean(Config.ENABLE_BLUETOOTH_OFF, true);
        if (bluetoothOff)
            ChargeUtils.offBluetooth(context);
        boolean rotateOff = sharedPref.getBoolean(Config.ENABLE_ROTATE_OFF, true);
        if (rotateOff)
            ChargeUtils.offRotate(context);
    }

    public static void clearMem(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                ActivityManager.RunningAppProcessInfo apinfo = list.get(i);
                String[] pkgList = apinfo.pkgList;
                if ((!apinfo.processName.startsWith("com.sec")) && ((apinfo.importance > 150) || (apinfo.processName.contains("google")))) {
                    for (int j = 0; j < pkgList.length; j++) {
                        activityManager.killBackgroundProcesses(pkgList[j]);
                    }
                }
            }
        }
    }

    public static void offInternet(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            saveState(Config.STATE_WIFI, wifiManager.isWifiEnabled(), context);
            wifiManager.setWifiEnabled(false);
        }

        saveConnection(context);
        setConnection(false, context);
    }

    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    private final static String COMMAND_L_ON = "svc data enable\n ";
    private final static String COMMAND_L_OFF = "svc data disable\n ";
    private final static String COMMAND_SU = "su";

    public static void saveConnection(Context context) {
        boolean mobileYN = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mobileYN = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 1) == 1;
        } else {
            mobileYN = Settings.Secure.getInt(context.getContentResolver(), "mobile_data", 1) == 1;
        }
        saveState(Config.STATE_MOBILE_DATA, mobileYN, context);
    }

    public static void setConnection(boolean enable, Context context) {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            String command;
            if (enable)
                command = COMMAND_L_ON;
            else
                command = COMMAND_L_OFF;

            try {
                Process su = Runtime.getRuntime().exec(COMMAND_SU);
                DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

                outputStream.writeBytes(command);
                outputStream.flush();

                outputStream.writeBytes("exit\n");
                outputStream.flush();
                try {
                    su.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ConnectivityManager conman = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                @SuppressWarnings("rawtypes") final Class conmanClass = Class.forName(conman.getClass().getName());
                final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
                iConnectivityManagerField.setAccessible(true);
                final Object iConnectivityManager = iConnectivityManagerField.get(conman);
                @SuppressWarnings("rawtypes") final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
                try {
                    @SuppressWarnings("unchecked") final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                    setMobileDataEnabledMethod.setAccessible(true);
                    setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
                } catch (NoSuchMethodException e) {
                    Class[] cArg = new Class[2];
                    cArg[0] = String.class;
                    cArg[1] = Boolean.TYPE;
                    Method setMobileDataEnabledMethod;

                    setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", cArg);

                    Object[] pArg = new Object[2];
                    pArg[0] = context.getPackageName();
                    pArg[1] = enable;
                    setMobileDataEnabledMethod.setAccessible(true);
                    setMobileDataEnabledMethod.invoke(iConnectivityManager, pArg);
                }
            } catch (Exception e) {

            }
        }

    }

    public static void setMobileDataState(boolean mobileDataEnabled, Context context) {
        try {
            boolean mobileYN = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mobileYN = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 1) == 1;
            } else {
                mobileYN = Settings.Secure.getInt(context.getContentResolver(), "mobile_data", 1) == 1;
            }

            saveState(Config.STATE_MOBILE_DATA, mobileYN, context);

            context.enforceCallingOrSelfPermission(android.Manifest.permission.MODIFY_PHONE_STATE, null);

            TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);

            if (null != setMobileDataEnabledMethod) {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
        } catch (Exception ex) {
            Log.e(Config.class.getName(), "Error setting mobile data state", ex);
        }
    }

    public static void offRotate(Context context) {
        if (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(context)) {
            try {
                saveState(Config.STATE_ROTATE, Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION), context);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        }
    }

    public static boolean isRotateOff(Context context) {
        if (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(context)) {
            try {
                return Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 0;
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void offBluetooth(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null)
            saveState(Config.STATE_BLUETOOTH, bluetoothAdapter.isEnabled(), context);
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    public static boolean isBluetoothEnabled(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null)
            return bluetoothAdapter.isEnabled();
        return false;
    }

    public static void reduceBrightness(Context context) {
        if (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(context)) {
            try {
                int mode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
                saveState(Config.STATE_BRIGHTNESS_MODE, mode, context);
                int oldBr = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                saveState(Config.STATE_BRIGHTNESS, oldBr, context);

                int br = 0;

                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, br);

//                float brightness = br / (float) 255;
//                WindowManager.LayoutParams lp = getWindow().getAttributes();
//                lp.screenBrightness = brightness;
//                getWindow().setAttributes(lp);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isBrightnessMin(Context context) {
        if (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(context)) {
            try {
                int mode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
                int currentBr = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                return mode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL && currentBr == 0;
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private static void saveState(String tag, int value, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(tag, value);
        editor.commit();
    }

    private static void saveState(String tag, boolean value, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(tag, value);
        editor.commit();
    }

    public static boolean isDeviceRooted() {
//        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
        return false;
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> task = manager.getRunningAppProcesses();
        if (task.get(0).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            return true;
        return false;
    }
}
