package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class ScreenTimeoutSettings extends SettingsPreferenceFragment{
    private ListPreference  mCharging;
    private ListPreference  mNotCharging;
    public static final Integer mTimes[] = {15000, 30000, 60000, 120000, 300000, 600000, 1800000};
    public SharedPreferences mSharedPreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.screen_timeout_settings);
        mSharedPreferences = getActivity().getSharedPreferences("charge", Context.MODE_PRIVATE);
        mCharging = (ListPreference)findPreference("screen_timeout_charging");
        mNotCharging = (ListPreference)findPreference("screen_timeout_notcharging");
        if (isCharging()) {
            Settings.System.putInt(getContentResolver(),
                                   android.provider.Settings.System.SCREEN_OFF_TIMEOUT, 30000);
        } else {
            Settings.System.putInt(getContentResolver(),
                                   android.provider.Settings.System.SCREEN_OFF_TIMEOUT,15000);
        }
        mCharging.setSummary(getEnt(30000));
        mNotCharging.setSummary(getEnt(15000));

        mCharging.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference instanceof  ListPreference) {
                    ListPreference listPreference = (ListPreference)preference;
                    int index = listPreference.findIndexOfValue((String)newValue);
                    if (isCharging()) {
                        Settings.System.putInt(getContentResolver(),
                                               android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
                                               mTimes[index]);
                    }
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putInt("charging", index);
                    editor.commit();
                    mCharging.setSummary(getEnt(mTimes[index]));
                }
                return true;
            }
        });
        mNotCharging.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference instanceof  ListPreference){
                    ListPreference listPreference = (ListPreference)preference;
                    int index = listPreference.findIndexOfValue((String)newValue);
                    if (!isCharging()) {
                        Settings.System.putInt(getContentResolver(),
                                               android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
                                               mTimes[index]);
                    }
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putInt("nocharging", index);
                    editor.commit();
                    mNotCharging.setSummary(getEnt(mTimes[index]));
                }
                return true;
            }
        });
    }

    public boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getActivity().registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING)
                             || (status == BatteryManager.BATTERY_STATUS_FULL);
        return  isCharging;
    }

    public String getEnt(int value) {
        String s = null;
        switch(value) {
            case 15000:
                s = "15 seconds";
                break;
             case 30000:
                s = "30 seconds";
                break;
            case 60000:
                s = "1 minute";
                break;
            case 120000:
                s = "2 minutes";
                break;
             case 300000:
                s = "5 minutes";
                break;
             case 600000:
                s = "10 minutes";
                break;
             case 1800000:
                s = "30 minutes";
                break;
        }
         return s;
    }
}
