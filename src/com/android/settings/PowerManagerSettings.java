/*
 * Copyright (C) 2014 Tieto Poland Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.view.View;
import android.provider.SearchIndexableResource;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import android.content.Context;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
import android.preference.SwitchPreference;
import android.os.BatteryManager;

public class PowerManagerSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener , Preference.OnPreferenceClickListener,Indexable{

    private static final String TAG = "PowerManagerManagerSettings";
    private static final String KEY_ENERGY_SAVING_MODE = "energy_saving_mode";
    private static final String ENERGY_SAVING_MODE_BALANCE = "energy_saving_mode_balance";
    private static final String ENERGY_SAVING_MODE_SAVING = "energy_saving_mode_saving";
    private static final String ENERGY_SAVING_MODE_EFFICIENT = "energy_saving_mode_efficient";

    private Preference mScreenEnergySavingModePreference;
    private AlertDialog mDialog = null;
    private int cpuCount = 1;

    private static final String KEEP_SCREEN_ON = "keep_screen_on";
    private SwitchPreference mKeepScreenOn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.power_manager_settings);

        mScreenEnergySavingModePreference = (PreferenceScreen) findPreference(KEY_ENERGY_SAVING_MODE);
        mScreenEnergySavingModePreference.setOnPreferenceClickListener(this);

        mKeepScreenOn = (SwitchPreference)findPreference(KEEP_SCREEN_ON);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        if (pref ==  mScreenEnergySavingModePreference) {
            showEnergeySavingModeDialog();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mKeepScreenOn) {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    mKeepScreenOn.isChecked() ?
                    (BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB) : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.power_manager_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };

    private void showEnergeySavingModeDialog(){
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        String energySavingMode = Settings.Global.getString(
                          getActivity().getContentResolver(), KEY_ENERGY_SAVING_MODE);
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final View energySavingModeDialog = layoutInflater
                .inflate(R.layout.energy_saving_mode_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.energy_save_mode);
        builder.setView(energySavingModeDialog);
        builder.setCancelable(true);

        RadioGroup rg = (RadioGroup) energySavingModeDialog.findViewById(
                                                                R.id.energy_save_mode_group);
        final RadioButton rbBalance = (RadioButton) energySavingModeDialog.findViewById(
                                                                R.id.energy_save_mode_balance);
        final RadioButton rbSaving = (RadioButton) energySavingModeDialog.findViewById(
                                                                R.id.energy_save_mode_saving);
        final RadioButton rbEfficient = (RadioButton) energySavingModeDialog.findViewById(
                                                                R.id.energy_save_mode_efficient);

        String cpuInfo = exec("cat /proc/cpuinfo| grep processor|wc -l");
        if (cpuInfo != "") {
            cpuCount = Integer.parseInt(cpuInfo);
        }

        if (ENERGY_SAVING_MODE_BALANCE.equals(energySavingMode)) {
            rbBalance.setChecked(true);
        } else if (ENERGY_SAVING_MODE_SAVING.equals(energySavingMode)) {
            rbSaving.setChecked(true);
        } else if (ENERGY_SAVING_MODE_EFFICIENT.equals(energySavingMode)) {
            rbEfficient.setChecked(true);
        } else {
            rbBalance.setChecked(true);
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rbBalance.getId()) {
                    rbBalance.setChecked(true);
                } else if (checkedId == rbSaving.getId()) {
                    rbSaving.setChecked(true);
                } else if (checkedId == rbEfficient.getId()) {
                    rbEfficient.setChecked(true);
                }
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (rbBalance.isChecked()) {
                    energySavingModeSetting("powersave",cpuCount);
                    Settings.Global.putString(getActivity().getContentResolver(),
                                   KEY_ENERGY_SAVING_MODE, ENERGY_SAVING_MODE_BALANCE);
                } else if (rbSaving.isChecked()) {
                    energySavingModeSetting("powersave",cpuCount);
                    Settings.Global.putString(getActivity().getContentResolver(),
                                   KEY_ENERGY_SAVING_MODE, ENERGY_SAVING_MODE_SAVING);
                } else if (rbEfficient.isChecked()) {
                    energySavingModeSetting("performance",cpuCount);
                    Settings.Global.putString(getActivity().getContentResolver(),
                                   KEY_ENERGY_SAVING_MODE, ENERGY_SAVING_MODE_EFFICIENT);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
            }
        });
        mDialog = builder.create();
        mDialog.show();
    }

    private void energySavingModeSetting(String mode , int count) {
        for (int i = 0; i < count; i++) {
            exec("echo \""+mode+"\" > /sys/devices/system/cpu/"
                 + "cpu" + i + "/cpufreq/scaling_governor");
        }
    }

    public String exec(String cmd) {
        try {
            String[] cmdA = { "/system/bin/su", "-c", cmd};
            Process process = Runtime.getRuntime().exec(cmdA);
            LineNumberReader br = new LineNumberReader(new InputStreamReader(
                    process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
