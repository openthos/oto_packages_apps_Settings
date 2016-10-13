package com.android.settings;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;

public class HotKeySettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "HotKeySettings";
    private static final String KEY_HOT_COPY = "hot_copy";
    private static final String KEY_HOT_CUT = "hot_cut";
    private static final String KEY_HOT_PASTE = "hot_paste";
    private static final String KEY_HOT_DELETE = "hot_delete";
    private static final String KEY_HOT_SHIFT_DELETE = "hot_shift_delete";
    private static final String KEY_HOT_RENAME = "hot_rename";
    private static final String KEY_HOT_REFRESH = "hot_refresh";
    private static final String KEY_HOT_CLOSE = "hot_close";
    private static final String KEY_HOT_SWITCH = "hot_switch";
    private static final String KEY_HOT_SELECTION = "hot_selection";
    private static final String KEY_HOT_CTRL_C = "hot_ctrl_c";
    private static final String KEY_HOT_CTRL_X = "hot_ctrl_x";
    private static final String KEY_HOT_CTRL_V = "hot_ctrl_v";
    private static final String KEY_HOT_CTRL_DELETE = "hot_ctrl_delete";
    private static final String KEY_HOT_CTRL_SHIFT_DELETE = "hot_ctrl_shift_delete";
    private static final String KEY_HOT_CTRL_RENAME = "hot_ctrl_rename";
    private static final String KEY_HOT_CTRL_REFRESH = "hot_ctrl_refresh";
    private static final String KEY_HOT_CTRL_CLOSE = "hot_ctrl_close";
    private static final String KEY_HOT_CTRL_SWITCH = "hot_ctrl_switch";
    private static final String KEY_HOT_CTRL_SELECTION = "hot_ctrl_selection";
    private PreferenceGroup mHotKeySettings;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.hot_key_settings);

        //mHotCopy = (PreferenceScreen) findPreference(KEY_HOT_COPY);
        //mHotCopy.setOnPreferenceClickListener(this);
        //mHotCut = (PreferenceScreen) findPreference(KEY_HOT_CUT);
        //mHotCut.setOnPreferenceClickListener(this);
        //mHotPaste = (PreferenceScreen) findPreference(KEY_HOT_PASTE);
        //mHotPaste.setOnPreferenceClickListener(this);
        //mHotDelete = (PreferenceScreen) findPreference(KEY_HOT_DELETE);
        //mHotDelete.setOnPreferenceClickListener(this);
        //mHotShiftDelete = (PreferenceScreen) findPreference(KEY_HOT_SHIFT_DELETE);
        //mHotShiftDelete.setOnPreferenceClickListener(this);
        //mHotRename = (PreferenceScreen) findPreference(KEY_HOT_RENAME);
        //mHotRename.setOnPreferenceClickListener(this);
        //mHotRefresh = (PreferenceScreen) findPreference(KEY_HOT_REFRESH);
        //mHotRefresh.setOnPreferenceClickListener(this);
        //mHotClose = (PreferenceScreen) findPreference(KEY_HOT_CLOSE);
        //mHotClose.setOnPreferenceClickListener(this);
        //mHotSwitch = (PreferenceScreen) findPreference(KEY_HOT_SWITCH);
        //mHotSwitch.setOnPreferenceClickListener(this);
        //mHotSelection = (PreferenceScreen) findPreference(KEY_HOT_SELECTION);
        //mHotSelection.setOnPreferenceClickListener(this);

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
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,Preference preference){
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    @Override
    public boolean onPreferenceChange(Preference preference,Object newValue) {
        return true;
    }

}
