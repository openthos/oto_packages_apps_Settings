/*
 * Copyright (C) 2018 The Android-x86 Open Source Project
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

import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.SettingsPreferenceFragment;

public class AndroidX86DashboardFragment extends SettingsPreferenceFragment {

    private SwitchPreference mNativeBridgePreference;

    private static final String KEY_TOGGLE_NB = "toggle_nb";
    private static final String PROPERTY_NATIVEBRIDGE = "persist.sys.nativebridge";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.android_x86_options);
        mNativeBridgePreference = (SwitchPreference) findPreference(KEY_TOGGLE_NB);
        mNativeBridgePreference.setChecked(SystemProperties.getBoolean(PROPERTY_NATIVEBRIDGE, false));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mNativeBridgePreference) {
            SystemProperties.set(PROPERTY_NATIVEBRIDGE, mNativeBridgePreference.isChecked() ? "1" : "0");
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.APPLICATION;
    }
}
