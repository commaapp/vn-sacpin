package com.vn.code;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by d on 11/15/2017.
 */

public class MyCache {
    private static SharedPreferences.Editor editor;
    private static SharedPreferences preferences;

    private static void openLog(Context context, String logName) {
        preferences = context.getSharedPreferences(logName, MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static void putStringValueByName(Context context, String logName, String name, String value) {
        openLog(context, logName);
        editor.putString(name, value);
        editor.commit();
    }

    public static String getStringValueByName(Context context, String logName, String name) {
        openLog(context, logName);
        String values = preferences.getString(name, "");
        return values;
    }

    public static void putBooleanValueByName(Context context, String logName, String name, boolean value) {
        openLog(context, logName);
        editor.putBoolean(name, value);
        editor.commit();
    }

    public static boolean getBooleanValueByName(Context context, String logName, String name) {
        openLog(context, logName);
        return preferences.getBoolean(name, false);
    }

    public static void removeAll() {
        editor.clear();
        editor.commit();
    }

    public static boolean getBooleanValueByName(Context context, String logName, String name, boolean b) {
        openLog(context, logName);
        return preferences.getBoolean(name, b);
    }
}
