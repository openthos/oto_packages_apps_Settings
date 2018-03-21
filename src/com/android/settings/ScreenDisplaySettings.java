package com.android.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;

public class ScreenDisplaySettings extends SettingsPreferenceFragment
                            implements Preference.OnPreferenceChangeListener {
    private static final String SYSTEM_DPI = "system_dpi";
    private static final String DPI_INIT_CONFIG = "sys.sf.lcd_density.recommend";
    private static final String DPI_LOW = "dpi_low";
    private static final String DPI_MEDIUM = "dpi_medium";
    private static final String DPI_HIGH = "dpi_high";
    private static final int NUM_DPI_LOW = 120;
    private static final int NUM_DPI_MEDIUM = 160;
    private static final int NUM_DPI_HIGH = 240;
    private CheckBoxPreference mLDpi;
    private CheckBoxPreference mHDpi;
    private CheckBoxPreference mMDpi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.screen_display_settings);
        init();
    }

    private void init() {
        mLDpi = (CheckBoxPreference) findPreference(DPI_LOW);
        mMDpi = (CheckBoxPreference) findPreference(DPI_MEDIUM);
        mHDpi = (CheckBoxPreference) findPreference(DPI_HIGH);
        if (getActivity().getWallpaperDesiredMinimumHeight() <= 768
                    && getActivity().getWallpaperDesiredMinimumWidth() <= 1366) {
            mHDpi.setEnabled(false);
        }

        int dpi = Settings.System.getInt(
                getActivity().getContentResolver(), SYSTEM_DPI, NUM_DPI_MEDIUM);
        switch (dpi) {
            case NUM_DPI_LOW:
                mLDpi.setChecked(true);
                break;
            case NUM_DPI_MEDIUM:
                mMDpi.setChecked(true);
                break;
            case NUM_DPI_HIGH:
                mHDpi.setChecked(true);
                break;
        }

        mLDpi.setOnPreferenceChangeListener(this);
        mMDpi.setOnPreferenceChangeListener(this);
        mHDpi.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        switch (preference.getKey()) {
            case DPI_LOW:
                mLDpi.setChecked(true);
                mMDpi.setChecked(false);
                mHDpi.setChecked(false);
                Settings.System.putInt(getActivity().getContentResolver(), SYSTEM_DPI, NUM_DPI_LOW);
                SystemProperties.set(DPI_INIT_CONFIG, String.valueOf(NUM_DPI_LOW));
                break;
            case DPI_MEDIUM:
                mMDpi.setChecked(true);
                mHDpi.setChecked(false);
                mLDpi.setChecked(false);
                Settings.System.putInt(
                        getActivity().getContentResolver(), SYSTEM_DPI, NUM_DPI_MEDIUM);
                SystemProperties.set(DPI_INIT_CONFIG, String.valueOf(NUM_DPI_MEDIUM));
                break;
            case DPI_HIGH:
                mHDpi.setChecked(true);
                mLDpi.setChecked(false);
                mMDpi.setChecked(false);
                Settings.System.putInt(
                        getActivity().getContentResolver(), SYSTEM_DPI, NUM_DPI_HIGH);
                SystemProperties.set(DPI_INIT_CONFIG, String.valueOf(NUM_DPI_HIGH));
                break;
        }
        return false;
    }
}
