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
package com.android.settings.ethernet;

import android.content.ContentResolver;
import android.content.Context;
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
import android.preference.CheckBoxPreference;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.widget.SwitchBar;
import com.android.settings.SettingsActivity;
import android.widget.Switch;
import android.app.Activity;
import android.app.ActivityManager;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.StaticIpConfiguration;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Slog;
import android.widget.Toast;
import android.os.Looper;

public class EthernetSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener,
        EthernetDialog.OnIpConfigChangedCallback {
    private static final String TAG = "EthernetSettings";
    private EthernetEnabler mEthEnabler;
    private static final String KEY_CONF_ETH = "ETHERNET_CONFIG";
    private static final String KEY_IP_ETH = "ETHERNET_IP_ADDRESS";
    private EthernetDialog mEthDialog = null;
    private Preference mEthConfigPref;
    private Preference mEthIpPref;
    private ConnectivityManager mCM;
    private EthernetManager mEM;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ethernet_settings);
        mEthConfigPref = findPreference(KEY_CONF_ETH);
        mEthIpPref = findPreference(KEY_IP_ETH);
    }

    @Override
    public void onStart() {
        super.onStart();
        // On/off switch is hidden for Setup Wizard (returns null)
        mEthEnabler = createEthernetEnabler();
        mCM = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        mEM = (EthernetManager)getActivity().getSystemService(Context.ETHERNET_SERVICE);
        mEthDialog = new EthernetDialog(getActivity(), mEM, mCM);
        mEthDialog.setIpConfigChangedCallback(this);
        mEthEnabler.setConfigDialog(mEthDialog);
    }

    @Override
    public void onResume() {
        final Activity activity = getActivity();
        super.onResume();
        if (mEthEnabler != null) {
            mEthEnabler.resume(activity);
            mEthIpPref.setSummary(getEthernetIpAddress());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mEthEnabler != null) {
            mEthEnabler.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mEthEnabler != null) {
            mEthEnabler.teardownSwitchBar();
        }
    }

    @Override
    public void onIpConfigChanged(String ipAddress) {
        mEthIpPref.setSummary(ipAddress);
    }

    /**
     * @return new WifiEnabler or null (as overridden by WifiSettingsForSetupWizard)
     */
    /* package */
    EthernetEnabler createEthernetEnabler() {
        final SettingsActivity activity = (SettingsActivity) getActivity();
        return new EthernetEnabler(activity, activity.getSwitchBar(),
                        (EthernetManager) getSystemService(Context.ETHERNET_SERVICE));
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEthConfigPref) {
            final SettingsActivity activity = (SettingsActivity) getActivity();
            if (activity.getSwitchBar().isChecked()) {
                if (mEthDialog != null)
                    mEthDialog.show();
            } else {
                Toast.makeText(getActivity(), "please turn on ethernet", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private String getEthernetIpAddress() {
        IpConfiguration ipinfo = mEM.getConfiguration();
        if (ipinfo != null) {
            if (ipinfo.ipAssignment == IpAssignment.DHCP) {
                return SystemProperties.get("dhcp.eth0.ipaddress");
            } else {
                StaticIpConfiguration staticConfig = ipinfo.getStaticIpConfiguration();
                if (staticConfig != null) {
                    if (staticConfig.ipAddress != null) {
                        return staticConfig.ipAddress.getAddress().getHostAddress();
                    }
                }
            }
        }
        return "";
    }
}
