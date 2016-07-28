/*
 * Copyright (C) 2010 The Android Open Source Project
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

/*
 * developed by libing@gmail.com
 */

package com.android.settings.proxy;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.preference.CheckBoxPreference;

import java.util.ArrayList;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.widget.SwitchBar;
import com.android.settings.SettingsActivity;
import android.widget.Switch;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;

public class ProxySettings extends SettingsPreferenceFragment{
    private ProxyEnabler mProxyEnabler;
    private View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.proxy_settings,container, false);
        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mProxyEnabler = createProxyEnabler();
    }

    @Override
    public void onResume() {
        final Activity activity = getActivity();
        super.onResume();
        if (mProxyEnabler != null) {
            mProxyEnabler.resume(activity);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mProxyEnabler != null) {
            mProxyEnabler.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mProxyEnabler != null) {
            mProxyEnabler.teardownSwitchBar();
        }
    }

    /* package */
    ProxyEnabler createProxyEnabler() {
        final SettingsActivity activity = (SettingsActivity) getActivity();
        return new ProxyEnabler(activity, activity.getSwitchBar(), mRootView);
    }

}
