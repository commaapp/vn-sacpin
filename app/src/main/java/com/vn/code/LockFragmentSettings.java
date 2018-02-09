package com.vn.code;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.facebook.appevents.AppEventsLogger;


public class LockFragmentSettings extends Fragment {
//    private AdView adViewBanner;
    private AppEventsLogger logger;
    private View root;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        logger = AppEventsLogger.newLogger(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.content_lock_setting, container, false);


        CheckBox showState = (CheckBox) root.findViewById(R.id.toggle_state_charging);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        showState.setChecked(sharedPref.getBoolean(Config.TRIGGER_SHOW_STATE_CHARGING, true));

        showState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    logger.logEvent("LockSetting_ButtonSwitch_On");
                else
                    logger.logEvent("LockSetting_ButtonSwitch_Off");

                SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(Config.TRIGGER_SHOW_STATE_CHARGING, checked);
                editor.commit();
            }
        });

        root.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.logEvent("LockSetting_IconBack_Clicked");

                SharedPreferences sharedPref = MyApplication.getAppContext().getSharedPreferences(Config.SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
                if (sharedPref.getBoolean(Config.TRIGGER_SHOW_STATE_CHARGING, true)) {
                    getActivity().onBackPressed();
                } else {
                    getActivity().finish();
                }
            }
        });


        return root;
    }






}
