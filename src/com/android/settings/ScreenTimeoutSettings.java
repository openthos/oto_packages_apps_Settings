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

    public static final int SECOND_VALUE = 1000;
    public static final int DEFAULT_VALUE_INDEX_CHARGING = 4;
    public static final int DEFAULT_VALUE_INDEX_NO_CHARGING = 2;

    public static final String CONFIG_FILE_NAME = "charge";
    public static final String BATTERY_CHARGING = "battery charging";
    public static final String BATTERY_NO_CHARGING = "battery no charging";
    public static final String AUTO_SLEEP_CHARGING_KEY = "screen_timeout_charging";
    public static final String AUTO_SLEEP_NO_CHARGING_KEY = "screen_timeout_notcharging";

    public static final String mStrArrayTimes[] = {"15 seconds", "1 minutes", "5 minutes",
                                                "10 minutes", "30 minutes", "1 hours", " never"};
    // Use Integer.MAX_VALUE time replace 'never sleep'.
    public static final int mTimes[] = {15 * SECOND_VALUE, 60 * SECOND_VALUE, 300 * SECOND_VALUE,
                                         600 * SECOND_VALUE, 1800 * SECOND_VALUE,
                                         3600 * SECOND_VALUE, Integer.MAX_VALUE};
    public SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.screen_timeout_settings);

        mSharedPreferences = getActivity().getSharedPreferences(CONFIG_FILE_NAME,
                Context.MODE_PRIVATE);
        mCharging = (ListPreference)findPreference(AUTO_SLEEP_CHARGING_KEY);
        mNotCharging = (ListPreference)findPreference(AUTO_SLEEP_NO_CHARGING_KEY);

        int chargeIndex = mSharedPreferences.getInt(
                            BATTERY_CHARGING, DEFAULT_VALUE_INDEX_CHARGING);
        int noChargeIndex = mSharedPreferences.getInt(
                            BATTERY_NO_CHARGING, DEFAULT_VALUE_INDEX_NO_CHARGING);
        if (isCharging()) {
            Settings.System.putInt(getContentResolver(),
                                   android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
                                   mTimes[chargeIndex]);
        } else {
            Settings.System.putInt(getContentResolver(),
                                   android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
                                   mTimes[noChargeIndex]);
        }
        /**
         * Init default choose item.
         */
        mCharging.setSummary(mStrArrayTimes[chargeIndex]);
        mCharging.setValueIndex(chargeIndex);
        mNotCharging.setSummary(mStrArrayTimes[noChargeIndex]);
        mNotCharging.setValueIndex(noChargeIndex);

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
                    Settings.System.putInt(getContentResolver(),
                                      android.provider.Settings.System.SCREEN_OFF_TIMEOUT_CHARGING,
                                      mTimes[index]);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putInt(BATTERY_CHARGING, index);
                    editor.commit();
                    mCharging.setSummary(mStrArrayTimes[index]);
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
                    Settings.System.putInt(getContentResolver(),
                                      android.provider.Settings.System.SCREEN_OFF_TIMEOUT_UNCHARGE,
                                      mTimes[index]);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putInt(BATTERY_NO_CHARGING, index);
                    editor.commit();
                    mNotCharging.setSummary(mStrArrayTimes[index]);
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
}
