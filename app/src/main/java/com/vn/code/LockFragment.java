package com.vn.code;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.appevents.AppEventsLogger;
import com.vn.viewcustem.BatteryView;
import com.vn.viewcustem.CircleWaveView;
import com.vn.viewcustem.SlidePanelCustem;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.dmoral.prefs.Prefs;

import static com.facebook.FacebookSdk.getApplicationContext;

public class LockFragment extends Fragment {
    private static final String TAG = LockFragment.class.getName();
    @BindView(R.id.layout_lock_sliding)
    SlidePanelCustem layoutLockSliding;
    Unbinder unbinder;
    @BindView(R.id.tv_progess_battery)
    TextView tvProgessBattery;
    @BindView(R.id.battery_view)
    BatteryView batteryView;

    private BroadcastReceiver dateChanged, batteryChanged;

    private ImageView iconFlash;

    private CircleWaveView progress;

    private int optimizedColor, normalColor;

    private View root;

    private TextView timeRemainText, timeRemainValue;

    private AppEventsLogger logger;
    private TextView slide_2_unlock_txt;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        logger = AppEventsLogger.newLogger(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Fabric code
        root = inflater.inflate(R.layout.content_lock, container, false);
        timeRemainText = (TextView) root.findViewById(R.id.tv_time_remain);
        timeRemainValue = (TextView) root.findViewById(R.id.tv_remain_value);
        progress = (CircleWaveView) root.findViewById(R.id.progress_circle);

        dateChanged = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
                    ((TextView) root.findViewById(R.id.tv_date)).setText(DateFormat.format("E, d MMM", new Date()));
                } else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                    Bundle animation = ActivityOptions.makeCustomAnimation(context,
                            R.anim.fade_in,
                            R.anim.fade_out)
                            .toBundle();

                    SharedPreferences sharedPref = getActivity().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
                    String onPlugMode = sharedPref.getString(Config.TRIGGER_ON_PLUG, Config.TRIGGER_ASK_2_RUN);
                    if (onPlugMode.equals(Config.TRIGGER_ASK_2_RUN)) {
                        Intent i = new Intent(context, FloatActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i, animation);
                    }
                }
            }
        };

        ((TextView) root.findViewById(R.id.tv_date)).setText(DateFormat.format("E, d MMM", new Date()));

        batteryChanged = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateProgress(intent);

                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
                startBlink(isCharging);

                if (!isCharging)
                    progress.setAmplitude(Float.MAX_VALUE);
                else
                    progress.setAmplitude(1.5f);
            }
        };

        iconFlash = (ImageView) root.findViewById(R.id.thunder_icon);

        root.findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.logEvent("Lock_IconMenuSetting_Clicked");

                ((LockActivity) getActivity()).showSettings();
            }
        });

        optimizedColor = ResourcesCompat.getColor(getResources(), R.color.colorWaterOptimized, null);
        normalColor = ResourcesCompat.getColor(getResources(), R.color.colorWaterNormal, null);

        unbinder = ButterKnife.bind(this, root);
        layoutLockSliding.setSliderFadeColor(0);
        layoutLockSliding.closePane();
        layoutLockSliding.setPanelSlideListener(new SlidePanelCustem.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (layoutLockSliding.isOpen())
                    unLock();

            }

            @Override
            public void onPanelOpened(View panel) {

            }

            @Override
            public void onPanelClosed(View panel) {

            }
        });
        return root;
    }

    private void unLock() {
        logger.logEvent("Lock_IconSlideTo_Unlock");
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    private boolean isOptimized(Context context) {
        SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);

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

    @Override
    public void onResume() {
        super.onResume();
        progress.invalidate();
        batteryView.invalidate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);

        getActivity().registerReceiver(dateChanged, intentFilter);

        Intent intent = getActivity().registerReceiver(batteryChanged, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        updateProgress(intent);

        onOptimized();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(dateChanged);
        getActivity().unregisterReceiver(batteryChanged);
    }

    public void onOptimized() {
        if (MyApplication.isOptimized())
            progress.setWaterColor(optimizedColor);
    }

    private void startBlink(boolean isCharging) {
        if (isCharging) {
            final Animation animation = new AlphaAnimation(1, 0);
            animation.setDuration(600);
            animation.setInterpolator(new LinearInterpolator());
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.REVERSE);
            iconFlash.startAnimation(animation);
        } else
            iconFlash.clearAnimation();
    }

    private void updateProgress(Intent intent) {
        float batteryPct;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryPct = (level * 100) / (float) scale;
        progress.setProgress((int) batteryPct);
        batteryView.setBatteryLevel((int) batteryPct);
        tvProgessBattery.setText((int) batteryPct + "% ");
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        float speed = sharedPreferences.getFloat(Config.CHARGING_SPEED, -1);
        int chargingSpeedIndex = sharedPreferences.getInt(Config.CHARGING_SPEED_INDEX, -1);
        if (speed > 0 && chargingSpeedIndex > 0) {
            int remainLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) - level;

            long timeRemain = (long) (remainLevel * speed);
            long minutes = (timeRemain / 60000) % 60;
            long hours = (timeRemain / 3600000) % 24;

            timeRemainValue.setText(getString(R.string.time_left_value
//                    ,
//                    Long.toString(hours), Long.toString(minutes)
            ));
            timeRemainValue.setVisibility(View.GONE);

//            boolean isFull = Prefs.with(getApplicationContext()).readBoolean(Config.TRIGGER_FULL_BATTERY, true);
//            if (isFull) {
            Log.e("abcd", "" + level);
            if (level >= 100) {
                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.ting);
                mediaPlayer.start();
                timeRemainText.setText(R.string.battery_full);
            } else timeRemainText.setText(R.string.time_left);

//            }

        }
    }


    public void onWindowHasFocus() {
        if (!isOptimized(MyApplication.getAppContext())) {
            progress.setWaterColor(normalColor);
        } else {
            progress.setWaterColor(optimizedColor);
        }
//        MyApplication.showNotification();
        MyApplication.showNotificationOptimize();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
