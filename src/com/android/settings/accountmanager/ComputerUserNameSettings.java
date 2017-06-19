/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.accountmanager;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.SpellCheckerSubtype;
import android.view.textservice.TextServicesManager;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextUtils;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.android.otosoft.tools.ChangeBuildPropTools;
import com.android.internal.widget.LockPatternUtils;
import android.content.SharedPreferences;

public class ComputerUserNameSettings extends SettingsPreferenceFragment
        implements OnPreferenceClickListener {
    private static final String TAG = ComputerUserNameSettings.class.getSimpleName();
    private static final boolean DBG = false;

    private static final String KEY_COMPUTER_USERNAME = "computer_username";
    private static final String KEY_SCREEN_PASSWORD = "screen_password";
    //private static final String RO_PROPERTY_USER = "ro.build.user";

    private Preference mComputerUserNamePref;
    private Preference mScreenPasswordPref;
    private AlertDialog mDialog = null;
    private TextServicesManager mTsm;
    private String userName;
    private EditText mOldPassword;
    private EditText mNewPassword;
    private EditText mAgainnewPassword;
    private LockPatternUtils mLockPatternUtils;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.computer_username_prefs);
        mComputerUserNamePref = findPreference(KEY_COMPUTER_USERNAME);
        mComputerUserNamePref.setOnPreferenceClickListener(this);
        mScreenPasswordPref = findPreference(KEY_SCREEN_PASSWORD);
        mScreenPasswordPref.setOnPreferenceClickListener(this);

        //userName = ChangeBuildPropTools.getPropertyValue(RO_PROPERTY_USER);
        userName = Settings.System.getString(getActivity().getContentResolver(),
                                                   Settings.System.SYS_PROPERTY_USER);
        mLockPatternUtils = new LockPatternUtils(getActivity());
        mComputerUserNamePref.setSummary(userName);
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
    public boolean onPreferenceClick(final Preference pref) {
        if (pref == mComputerUserNamePref ) {
            showChangeComputerUserNameDialog();
            return true;
        } else if (pref == mScreenPasswordPref) {
            showChangeScreenPasswordDialog();
        }
        return false;
    }

    private static int convertSubtypeIndexToDialogItemId(final int index) { return index + 1; }
    private static int convertDialogItemIdToSubtypeIndex(final int item) { return item - 1; }

    private void showChangeComputerUserNameDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.change_computer_username);
        final EditText editTextPref = new EditText(getActivity());
        editTextPref.setWidth(200);
        builder.setView(editTextPref);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                String newUserName = editTextPref.getText().toString().trim();
                //grant permission
                /*ChangeBuildPropTools.exec("chmod -R 777  /system/build.prop");
                ChangeBuildPropTools.setPropertyName(
                               ChangeBuildPropTools.getPropertyName(RO_PROPERTY_USER,newUserName));
                ChangeBuildPropTools.exec("chmod -R 644  /system/build.prop");*/
                Settings.System.putString(getActivity().getContentResolver(),
                                          Settings.System.SYS_PROPERTY_USER, newUserName);
                updateComputerUserName(newUserName);
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

    private void showChangeScreenPasswordDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final View screenPasswordDialog = layoutInflater
                .inflate(R.layout.screen_password_dialog, null);
        mOldPassword = (EditText) screenPasswordDialog.findViewById(R.id.old_password);
        mNewPassword = (EditText) screenPasswordDialog.findViewById(R.id.new_password);
        mAgainnewPassword = (EditText) screenPasswordDialog.findViewById(R.id.again_new_password);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.change_screen_password);
        builder.setView(screenPasswordDialog);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                String mOldPasswordpin = mOldPassword.getText().toString();
                if (mLockPatternUtils.checkPassword(mOldPasswordpin)) {
                    savePassword();
                } else {
                    Toast.makeText(getActivity(),getText(R.string.check_password),
                    Toast.LENGTH_SHORT).show();
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

    private void savePassword() {
        String newpasswordpin=mNewPassword.getText().toString();
        String againpasswordpin=mAgainnewPassword.getText().toString();
        if (!TextUtils.isEmpty(newpasswordpin) && !TextUtils.isEmpty(againpasswordpin)
                                               && newpasswordpin.equals(againpasswordpin)) {
            mLockPatternUtils.saveLockPassword(newpasswordpin,
                                               DevicePolicyManager.PASSWORD_QUALITY_NUMERIC, false);
        } else {
            Toast.makeText(getActivity(), getText(R.string.wrong_password),
                           Toast.LENGTH_SHORT).show();
        }
    }

    private void updateComputerUserName(String name){
        mComputerUserNamePref.setSummary(name);
    }

}
