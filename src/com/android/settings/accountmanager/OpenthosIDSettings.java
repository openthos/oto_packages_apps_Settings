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
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.SpellCheckerSubtype;
import android.view.textservice.TextServicesManager;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;

public class OpenthosIDSettings extends SettingsPreferenceFragment
        implements  OnPreferenceClickListener {
    private static final String TAG = OpenthosIDSettings.class.getSimpleName();
    private static final boolean DBG = false;

    private static final String KEY_OPENTHOS_ID = "openthos_id";

    private Preference mOpenthosIDPref;
    private AlertDialog mDialog = null;
    private TextServicesManager mTsm;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.i("fragmentWhich","--------------openthosID");
        addPreferencesFromResource(R.xml.openthos_id_prefs);
        mOpenthosIDPref = findPreference(KEY_OPENTHOS_ID);
        mOpenthosIDPref.setOnPreferenceClickListener(this);

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
        if (pref == mOpenthosIDPref) {
           // showChooseLanguageDialog();
            return true;
        }
        return false;
    }

    private static int convertSubtypeIndexToDialogItemId(final int index) { return index + 1; }
    private static int convertDialogItemIdToSubtypeIndex(final int item) { return item - 1; }

//    private void showChooseLanguageDialog() {
//        if (mDialog != null && mDialog.isShowing()) {
//            mDialog.dismiss();
//        }
//        final SpellCheckerInfo currentSci = mTsm.getCurrentSpellChecker();
//        final SpellCheckerSubtype currentScs = mTsm.getCurrentSpellCheckerSubtype(
//                false /* allowImplicitlySelectedSubtype */);
//        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(R.string.phone_language);
//        final int subtypeCount = currentSci.getSubtypeCount();
//        final CharSequence[] items = new CharSequence[subtypeCount + 1 /* default */ ];
//        items[ITEM_ID_USE_SYSTEM_LANGUAGE] = getSpellCheckerSubtypeLabel(currentSci, null);
//        int checkedItemId = ITEM_ID_USE_SYSTEM_LANGUAGE;
//        for (int index = 0; index < subtypeCount; ++index) {
//            final SpellCheckerSubtype subtype = currentSci.getSubtypeAt(index);
//            final int itemId = convertSubtypeIndexToDialogItemId(index);
//            items[itemId] = getSpellCheckerSubtypeLabel(currentSci, subtype);
//            if (subtype.equals(currentScs)) {
//                checkedItemId = itemId;
//            }
//        }
//        builder.setSingleChoiceItems(items, checkedItemId, new AlertDialog.OnClickListener() {
//            @Override
//            public void onClick(final DialogInterface dialog, final int item) {
//                if (item == ITEM_ID_USE_SYSTEM_LANGUAGE) {
//                    mTsm.setSpellCheckerSubtype(null);
//                } else {
//                    final int index = convertDialogItemIdToSubtypeIndex(item);
//                    mTsm.setSpellCheckerSubtype(currentSci.getSubtypeAt(index));
//                }
//                if (DBG) {
//                    final SpellCheckerSubtype subtype = mTsm.getCurrentSpellCheckerSubtype(
//                            true /* allowImplicitlySelectedSubtype */);
//                    Log.d(TAG, "Current spell check locale is "
//                            + subtype == null ? "null" : subtype.getLocale());
//                }
//                dialog.dismiss();
//                updatePreferenceScreen();
//            }
//        });
//        mDialog = builder.create();
//        mDialog.show();
//    }
//
//    private void showSecurityWarnDialog(final SpellCheckerPreference pref) {
//        if (mDialog != null && mDialog.isShowing()) {
//            mDialog.dismiss();
//        }
//        final SpellCheckerInfo sci = pref.getSpellCheckerInfo();
//        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(android.R.string.dialog_alert_title);
//        builder.setMessage(getString(R.string.spellchecker_security_warning, pref.getTitle()));
//        builder.setCancelable(true);
//        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(final DialogInterface dialog, final int which) {
//                changeCurrentSpellChecker(sci);
//            }
//        });
//        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(final DialogInterface dialog, final int which) {
//            }
//        });
//        mDialog = builder.create();
//        mDialog.show();
//    }

}
