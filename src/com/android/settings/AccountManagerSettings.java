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
import android.content.Context;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.settings.accountmanager.OpenthosIDSettings;
import com.android.settings.accountmanager.ComputerUserNameSettings;
import com.android.otosoft.tools.ChangeBuildPropTools;
import org.openthos.seafile.ISeafileService;

import android.content.ContentResolver;
import android.net.Uri;
import android.database.Cursor;

public class AccountManagerSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener , Preference.OnPreferenceClickListener{

    private static final String TAG = "AccountManagerSettings";

    private static final String KEY_ACCOUNT_MANAGER_SETTINGS = "account_manager_settings";
    private static final String KEY_OPENTHOS_ID_EMAIL = "openthos_id_email";
    private static final String KEY_OPENTHOS_URL = "openthos_url";
    private static final String KEY_COMPUTER_USERNAME = "computer_username";
    private static final String KEY_COMPUTER_NAME = "computer_name";
    //private static final String RO_PROPERTY_HOST = "ro.build.host";
    //private static final String RO_PROPERTY_USER = "ro.build.user";
    private PreferenceScreen mOpenthosID;
    private PreferenceScreen mOpenthosUrl;
    private PreferenceScreen mComputerUserName;
    private PreferenceScreen mComputerName;
    private AlertDialog mDialog = null;
    private String defaultComputerName;
    private String computerName;

    private PreferenceGroup mAccountManagerSettings;

    private ISeafileService mISeafileService;
    private SeafileServiceConnection mSeafileServiceConnection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();
        addPreferencesFromResource(R.xml.account_manager_settings);
        final Activity activity = getActivity();

        mOpenthosID = (PreferenceScreen) findPreference(KEY_OPENTHOS_ID_EMAIL);
        mOpenthosID.setPersistent(true);
        mOpenthosUrl = (PreferenceScreen) findPreference(KEY_OPENTHOS_URL);
        mOpenthosUrl.setPersistent(true);
        mComputerUserName = (PreferenceScreen) findPreference(KEY_COMPUTER_USERNAME);
        mComputerUserName.setPersistent(true);
        mComputerName = (PreferenceScreen) findPreference(KEY_COMPUTER_NAME);
        mComputerName.setPersistent(true);

        //defaultComputerName = ChangeBuildPropTools.getPropertyValue(RO_PROPERTY_HOST);
        defaultComputerName = Settings.System.getString(getActivity().getContentResolver(),
                                                          Settings.System.SYS_PROPERTY_HOST);
        mComputerName.setSummary(defaultComputerName);

        mComputerName.setOnPreferenceClickListener(this);
        mOpenthosUrl.setOnPreferenceClickListener(this);

        mAccountManagerSettings = (PreferenceGroup) findPreference(KEY_ACCOUNT_MANAGER_SETTINGS);
        if (mAccountManagerSettings != null) {
            // Note: KEY_SPELL_CHECKERS preference is marked as persistent="false" in XML.
            //InputMethodAndSubtypeUtil.removeUnnecessaryNonPersistentPreference(spellChecker);
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClass(activity, SubSettings.class);
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT,
                    OpenthosIDSettings.class.getName());
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID,
                    R.string.openthos_id_settings_title);
            mOpenthosID.setIntent(intent);
            Log.d("accountIntent", "----------itme-------click");

            final Intent intentComputer = new Intent(Intent.ACTION_MAIN);
            intentComputer.setClass(activity, SubSettings.class);
            intentComputer.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT,
                    ComputerUserNameSettings.class.getName());
            intentComputer.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID,
                    R.string.computer_username_settings_title);
            mComputerUserName.setIntent(intentComputer);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mOpenthosID.setSummary(getText(R.string.email_address_summary).toString());
        mSeafileServiceConnection = new SeafileServiceConnection();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("org.openthos.seafile",
                    "org.openthos.seafile.SeafileService"));
        getActivity().bindService(intent, mSeafileServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private class SeafileServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mISeafileService = ISeafileService.Stub.asInterface(service);
            try {
                String id = mISeafileService.getUserName();
                if (!TextUtils.isEmpty(id)) {
                    mOpenthosID.setSummary(id);
                    mOpenthosUrl.setSummary(mISeafileService.getOpenthosUrl());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        if (pref == mComputerName) {
            showChangeDialog(R.string.change_computer_name);
            return true;
        } else if (pref == mOpenthosUrl) {
            showChangeDialog(R.string.change_openthos_url);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Log.d(TAG, "Account manager setting has been click");
        //if (preference == mOpenthosID) {
             return  false;
        //}
        //return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    private void showChangeDialog(final int title) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        final EditText editTextPref = new EditText(getActivity());
        if (title == R.string.change_openthos_url) {
            editTextPref.setText("http://");
            editTextPref.setSelection(editTextPref.getText().length());
        }
        builder.setView(editTextPref);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                String input = editTextPref.getText().toString();
                //grant permission
                /*ChangeBuildPropTools.exec("chmod -R 777  /system/build.prop");
                ChangeBuildPropTools.setPropertyName(
                           ChangeBuildPropTools.getPropertyName(RO_PROPERTY_HOST,newComputerName));
                ChangeBuildPropTools.exec("chmod -R 644  /system/build.prop");*/
                if (title == R.string.change_computer_name) {
                    Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.SYS_PROPERTY_HOST, input);
                    updateComputerName(input);
                } else if (title == R.string.change_openthos_url) {
                    updateOpenthosUrl(input);
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

    private void updateOpenthosUrl(String url) {
        mOpenthosUrl.setSummary(url);
        try {
            mISeafileService.setOpenthosUrl(url);
            if (!TextUtils.isEmpty(mISeafileService.getUserName())) {
                mISeafileService.stopAccount();
                mOpenthosID.setSummary(getText(R.string.email_address_summary).toString());
            }
            Toast.makeText(getActivity(),
                    getText(R.string.toast_relogin), Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateComputerName(String name){
        mComputerName.setSummary(name);
    }
}
