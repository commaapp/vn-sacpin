package com.vn.code;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.appevents.AppEventsLogger;
import com.vn.viewcustem.CircleWaveView;
import com.vn.viewcustem.CircleWaveViewListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.dmoral.prefs.Prefs;
import facebook.FacebookBanner;
import inter.OnErrorLoadAd;
import richadx.RichNativeAd;

public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getName();
    @BindView(R.id.tv_tip_optimize)
    TextView tvTipOptimize;
    Unbinder unbinder;

    private BroadcastReceiver broadcastReceiver;

    private CircleWaveView circleWaveView;

    private int optimizedColor, unoptimizedColor;

    private TextView btnOptimize;
    private View btnOptimizeBG, btnOptimizeDONE;

    private View root;


    private boolean isOptimizing = false;
    LinearLayout nativeAdContainer;
    View adView;

    private AppEventsLogger logger;
    private LinearLayout btnSetting;
    private LinearLayout btnMoreApp;
    private NativeAd nativeAd;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        logger = AppEventsLogger.newLogger(context);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.content_main, container, false);
//        btnSetting = (LinearLayout) root.findViewById(R.id.btnSetting);
//        btnSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                logger.logEvent("MainScreen_ButtonSetting_Clicked");
//                startSettingsFragment();
//            }
//        });

//        btnMoreApp = (LinearLayout) root.findViewById(R.id.btnMoreApp);
//        btnMoreApp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                logger.logEvent("MainScreen_ButtonMoreapp_Clicked");
//                loadCrossInterstitial();
//            }
//        });
        unbinder = ButterKnife.bind(this, root);

        btnOptimize = (TextView) root.findViewById(R.id.btn_optimize);
        btnOptimizeBG = root.findViewById(R.id.btn_optimize_bg);

        btnOptimizeBG.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
//                        showHintOptimize();
                    }
                });

        btnOptimizeDONE = root.findViewById(R.id.btn_optimize_done);
        circleWaveView = (CircleWaveView) root.findViewById(R.id.seekbar_point);
        optimizedColor = ResourcesCompat.getColor(getResources(), R.color.colorWaterOptimized, null);
        unoptimizedColor = ResourcesCompat.getColor(getResources(), R.color.colorWaterNormal, null);

        SharedPreferences sharedPref = getContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        if (MyApplication.isOptimized()) {
            btnOptimize.setText(R.string.optimize_done);
            btnOptimizeDONE.setVisibility(View.GONE);
            circleWaveView.setWaterColor(optimizedColor);
            tvTipOptimize.setText(getString(R.string.tip_done));
        }
        btnOptimizeBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doOptimize();
            }
        });


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
                    updateChargingProgress(intent);
                else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                    SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
                    if (!sharedPref.getBoolean(Config.TRIGGER_EXIT_ON_UNPLUG, false)) {
                        btnOptimizeBG.setClickable(true);
                        btnOptimize.setText(R.string.optimize_start);
                        btnOptimizeDONE.setVisibility(View.GONE);
                        btnOptimizeBG.setBackgroundResource(R.drawable.btn_optimize);
                        tvTipOptimize.setText(getString(R.string.tip_optimize));
                        circleWaveView.setWaterColor(unoptimizedColor);
                        circleWaveView.setTimeLeft("", "");
                    }
                }
            }
        };

        String action = getActivity().getIntent().getAction();
        if (action != null && action.equals(Intent.ACTION_POWER_CONNECTED)) { // Co loi null xuat hien
            getActivity().setIntent(new Intent(Intent.ACTION_SYNC));
            btnOptimizeBG.callOnClick();
        }

//        MyApplication.showNotification();
        MyApplication.showNotificationOptimize();

        return root;
    }

    public void doOptimize() {
        if (MyApplication.isOptimized()) {
            logger.logEvent("Main_ButtonOptimizeDone_Clicked");
            startActivity(new Intent(getActivity(), DoneActivity.class));

        } else {
            Prefs.with(getActivity()).write("optimize", "");
            if (canWriteSetting(MyApplication.getAppContext())) {
                logger.logEvent("Main_ButtonOptimizeStart_Clicked");

                btnOptimizeBG.setClickable(false);
                btnOptimize.setText(R.string.optimize_running);
                tvTipOptimize.setText(getString(R.string.tip_optimizting));
                btnOptimizeBG.setBackgroundResource(R.drawable.btn_optimizing);
                isOptimizing = true;

                clearMem();
            } else
                askWriteSettings();
        }
    }

    public void startSettingsFragment() {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAdx();
//        showNativeAd();
    }

    RichNativeAd nativeRich;
    FacebookBanner facebookBanner;

    private void loadAdx() {
        ViewHolder viewHolder = new ViewHolder(root);
        nativeRich = new RichNativeAd(getContext(), viewHolder.frameAds, "/112517806/519401517413776");
        facebookBanner = new FacebookBanner(getContext(), viewHolder.frameAds, "1631427560285640_1672969492798113");
        facebookBanner.setOnErrorLoadAd(new OnErrorLoadAd() {
            @Override
            public void onMyError() {
                nativeRich.show();
            }
        });
        facebookBanner.show();


    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        Intent batteryStatus = getActivity().registerReceiver(broadcastReceiver, intentFilter);
        updateChargingProgress(batteryStatus);
        onWindowHasFocus();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateChargingProgress(Intent intent) {
        float batteryPct;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryPct = (level * 100) / (float) scale;
        circleWaveView.setProgress((int) batteryPct);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        if (!isCharging)
            circleWaveView.setAmplitude(30);
        else
            circleWaveView.setAmplitude(2);
        SharedPreferences preferences = getContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        float speed = preferences.getFloat(Config.CHARGING_SPEED, -1);
        int chargingSpeedIndex = preferences.getInt(Config.CHARGING_SPEED_INDEX, -1);
        if (speed > 0 && chargingSpeedIndex > 0) {
            int remainLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) - level;
            long timeRemain = (long) (remainLevel * speed);
            long minutes = (timeRemain / 60000) % 60;
            long hours = (timeRemain / 3600000) % 24;

            if (isCharging)
                circleWaveView.setTimeLeft(getString(R.string.time_left_main), getString(R.string.time_left_value,
                        Long.toString(hours), Long.toString(minutes)));
            else
                circleWaveView.setTimeLeft("", "");
        }
    }

    private void clearMem() {
        SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        boolean clearMem = sharedPref.getBoolean(Config.ENABLE_CLEAR_MEM, true);
        if (clearMem)
            ChargeUtils.clearMem(MyApplication.getAppContext());
        circleWaveView.setProgressAngle(-1);
        circleWaveView.setDrawNeon(true);
        circleWaveView.updateIndicatorIconPosition(72, new CircleWaveViewListener() {
            @Override
            public void OnNeonReachedTarget(float target) {
                offInternet();
            }
        });
    }

    private void offInternet() {
        SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        boolean internetOff = sharedPref.getBoolean(Config.ENABLE_INTERNET_OFF, false);
        if (internetOff)
            ChargeUtils.offInternet(MyApplication.getAppContext());
        circleWaveView.updateIndicatorIconPosition(144, new CircleWaveViewListener() {
            @Override
            public void OnNeonReachedTarget(float target) {
                reduceBrightness();
            }
        });
    }

    private void reduceBrightness() {
        SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        boolean brightnessMin = sharedPref.getBoolean(Config.ENABLE_BRIGHTNESS_MIN, true);
        if (brightnessMin)
            ChargeUtils.reduceBrightness(MyApplication.getAppContext());
        circleWaveView.updateIndicatorIconPosition(216, new CircleWaveViewListener() {
            @Override
            public void OnNeonReachedTarget(float target) {
                offBluetooth();
            }
        });
    }

    private void offBluetooth() {
        SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        boolean bluetoothOff = sharedPref.getBoolean(Config.ENABLE_BLUETOOTH_OFF, true);
        if (bluetoothOff)
            ChargeUtils.offBluetooth(MyApplication.getAppContext());
        circleWaveView.updateIndicatorIconPosition(288, new CircleWaveViewListener() {
            @Override
            public void OnNeonReachedTarget(float target) {
                offRotate();
            }
        });
    }

    private void offRotate() {
        SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        boolean rotateOff = sharedPref.getBoolean(Config.ENABLE_ROTATE_OFF, true);
        if (rotateOff)
            ChargeUtils.offRotate(MyApplication.getAppContext());
        circleWaveView.updateIndicatorIconPosition(360, new CircleWaveViewListener() {
            @Override
            public void OnNeonReachedTarget(float target) {
                circleWaveView.setWaterColor(optimizedColor);
                circleWaveView.setDrawNeon(false);

                btnOptimize.post(new Runnable() {
                    @Override
                    public void run() {
                        btnOptimize.setText(R.string.optimize_done);
                        btnOptimizeDONE.setVisibility(View.GONE);
                        tvTipOptimize.setText(getString(R.string.tip_done));
                        btnOptimizeBG.setBackgroundResource(R.drawable.btn_optimize);
                        btnOptimizeBG.setClickable(true);
                    }
                });
                isOptimizing = false;

//                MyApplication.showNotification();
                MyApplication.showNotificationOptimize();
            }
        });
    }


    private boolean canWriteSetting(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(Config.ENABLE_BRIGHTNESS_MIN, true) || sharedPref.getBoolean(Config.ENABLE_ROTATE_OFF, true))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(MyApplication.getAppContext())) {
                return false;
            }
        return true;
    }

    private void askWriteSettings() {
        Toast.makeText(getContext(), R.string.ask_permission, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onWindowHasFocus() {
        if (!MyApplication.isOptimized()) {
            if (!isOptimizing) {
                btnOptimize.setText(R.string.optimize_start);
                btnOptimizeDONE.setVisibility(View.GONE);
                tvTipOptimize.setText(getString(R.string.tip_optimize));
                btnOptimizeBG.setBackgroundResource(R.drawable.btn_optimize);
                btnOptimizeBG.setClickable(true);
                circleWaveView.setWaterColor(unoptimizedColor);
            }
        } else {
            btnOptimize.setText(R.string.optimize_done);
            btnOptimizeDONE.setVisibility(View.GONE);
            tvTipOptimize.setText(getString(R.string.tip_done));
            btnOptimizeBG.setClickable(true);
            circleWaveView.setWaterColor(optimizedColor);
        }
//        MyApplication.showNotification();
        MyApplication.showNotificationOptimize();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    public static class buttonListener extends BroadcastReceiver {
        MainFragment mainFragment = new MainFragment();

        @Override
        public void onReceive(Context context, Intent intent) {
            mainFragment.doOptimize();
        }

    }


    static class ViewHolder {

        @BindView(R.id.frame_ads)
        LinearLayout frameAds;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
