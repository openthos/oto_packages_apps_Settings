package com.android.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.app.AlertDialog;

public class ScreenDisplaySettings extends SettingsPreferenceFragment
                            implements Preference.OnPreferenceChangeListener {
    private static final String SYSTEM_DPI = "system_dpi";
    private static final String DPI_LOW = "dpi_low";
    private static final String DPI_MEDIUM = "dpi_medium";
    private static final String DPI_HIGH = "dpi_high";
    private static final int NUM_DPI_LOW = 120;
    private static final int NUM_DPI_MEDIUM = 160;
    private static final int NUM_DPI_HIGH = 240;
    private CheckBoxPreference mLDpi;
    private CheckBoxPreference mMDpi;
    private CheckBoxPreference mHDpi;
    private boolean mIsChange = false;

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

        int dpi = Settings.System.getInt(getActivity().getContentResolver(), SYSTEM_DPI, NUM_DPI_MEDIUM);
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
                showWarningDialog(NUM_DPI_LOW);
                break;
            case DPI_MEDIUM:
                showWarningDialog(NUM_DPI_MEDIUM);
                break;
            case DPI_HIGH:
                showWarningDialog(NUM_DPI_HIGH);
                break;
        }
        return false;
    }

    private boolean showWarningDialog(final int dpi) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.tips_dpi);
        builder.setMessage(R.string.warning_dialog_message);
        builder.setPositiveButton(android.R.string.ok,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (dpi) {
                        case NUM_DPI_LOW:
                            mLDpi.setChecked(true);
                            mMDpi.setChecked(false);
                            mHDpi.setChecked(false);
                            break;
                        case NUM_DPI_MEDIUM:
                            mMDpi.setChecked(true);
                            mHDpi.setChecked(false);
                            mLDpi.setChecked(false);
                            break;
                        case NUM_DPI_HIGH:
                            mHDpi.setChecked(true);
                            mLDpi.setChecked(false);
                            mMDpi.setChecked(false);
                            break;
                    }
                    Settings.System.putInt(getActivity().getContentResolver(), SYSTEM_DPI, dpi);
                    SystemProperties.set("sys.sf.lcd_density.recommend", String.valueOf(dpi));
                    mIsChange = true;
                }
        });

        builder.setNegativeButton(android.R.string.cancel,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mIsChange = false;
                }
        });
        builder.create().show();
        return mIsChange;
    }
}
