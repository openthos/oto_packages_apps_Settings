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

import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

public class JabolSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "JabolSettings";

    private static final String KEY_JABOL_SETTINGS = "jabol_settings";
    private static final String KEY_TIETO_MULTIWINDOW = "tieto_multiwindow";

    private CheckBoxPreference mTietoMultiwindow;

    private PreferenceGroup mJabolSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();
        addPreferencesFromResource(R.xml.jabol_settings);

        mTietoMultiwindow = (CheckBoxPreference) findPreference(KEY_TIETO_MULTIWINDOW);
        mTietoMultiwindow.setPersistent(false);
        mTietoMultiwindow.setChecked(Settings.System.getInt(resolver,
                Settings.System.TIETO_MULTIWINDOW_DISABLED, 0) != 0);

        mJabolSettings = (PreferenceGroup) findPreference(KEY_JABOL_SETTINGS);
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
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Log.d(TAG, "Jabol setting has been changed");
        if (preference == mTietoMultiwindow) {
            Settings.System.putInt(getContentResolver(), Settings.System.TIETO_MULTIWINDOW_DISABLED,
                    mTietoMultiwindow.isChecked() ? 1 : 0);
            Log.d(TAG, "Preference has been changed");
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
}
