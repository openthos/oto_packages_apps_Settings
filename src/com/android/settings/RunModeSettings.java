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

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import com.android.settings.applications.ApplicationsState.*;
import com.android.settings.applications.*;
import com.android.settings.applications.ManageApplications.*;
import android.content.Context;
import android.widget.ImageView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.util.Log;
import android.content.pm.ApplicationInfo;
import com.android.settings.applications.ApplicationsState.AppEntry;

public class RunModeSettings extends Fragment implements ApplicationsState.Callbacks {
    private ApplicationsState mApplicationsState;
    private ApplicationsState.Session mSession;
    private AppViewHolder mHolder;
    private View mLoadingContainer;
    private View mListContainer;
    private ListView mListView;
    private HashMap<String,Integer> map;
    private ArrayList<ApplicationsState.AppEntry> mBaseEntries;
    private ArrayList<ApplicationsState.AppEntry> mEntries;
    private ImageView mImageView;
    private ContentResolver mResolver;

    CharSequence mCurFilterPrefix;

    @Override
    public void onRunningStateChanged(boolean running){};
    @Override
    public void onPackageListChanged(){};
    @Override
    public void onRebuildComplete(ArrayList<AppEntry> apps){};
    @Override
    public void onPackageIconChanged(){};
    @Override
    public void onPackageSizeChanged(String packageName){};
    @Override
    public void onAllSizesComputed(){};

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mResolver = getActivity().getContentResolver();
        Uri uriQuery = Uri.parse("content: //com.otosoft.tools.myprovider/selectState");
        Cursor cursor = mResolver.query(uriQuery, null, null, null, null);
        map = new HashMap();
        if(cursor != null){
            while(cursor.moveToNext()) {
                map.put(cursor.getString(cursor.getColumnIndex("appPackage")),
                        cursor.getInt(cursor.getColumnIndex("state")));
            }
        }

        View mRootView = inflater.inflate(R.layout.runmode_applications_apps, container, false);
        mListContainer = mRootView.findViewById(R.id.list_container);
        if (mListContainer != null) {
            // Create adapter and list view here
            View emptyView = mListContainer.findViewById(com.android.internal.R.id.empty);
            ListView lv = (ListView) mListContainer.findViewById(android.R.id.list);
            if (emptyView != null) {
                lv.setEmptyView(emptyView);
            }
            mListView = lv;
            mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
            mSession=mApplicationsState.newSession(this);
            mSession.resume();
            ArrayList<ApplicationsState.AppEntry> entries
                                     = mSession.rebuild(ApplicationsState.ALL_ENABLED_FILTER,
                                                        ApplicationsState.SIZE_COMPARATOR);
            mBaseEntries = entries;
            if (mBaseEntries != null) {
                mEntries = applyPrefixFilter(mCurFilterPrefix, mBaseEntries);
            } else {
                mEntries = null;
            }
            RunModeAdapter adapter=new RunModeAdapter(getActivity());
            mListView.setAdapter(adapter);
        }
        return mRootView;
    }

    ArrayList<ApplicationsState.AppEntry> applyPrefixFilter(CharSequence prefix,
                                               ArrayList<ApplicationsState.AppEntry> origEntries) {
        if (prefix == null || prefix.length() == 0) {
            return origEntries;
        } else {
            String prefixStr = ApplicationsState.normalize(prefix.toString());
            final String spacePrefixStr = " " + prefixStr;
            ArrayList<ApplicationsState.AppEntry> newEntries
                    = new ArrayList<ApplicationsState.AppEntry>();
            for (int i=0; i<origEntries.size(); i++) {
                ApplicationsState.AppEntry entry = origEntries.get(i);
                // repair
                String nlabel = entry.getNormalizedLabel();
                if (nlabel.startsWith(prefixStr) || nlabel.indexOf(spacePrefixStr) != -1) {
                    newEntries.add(entry);
                }
            }
            return newEntries;
        }
    }

    class RunModeAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        public RunModeAdapter(Context context){
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mEntries != null ? mEntries.size() : 0;
        }

        @Override
        public Object getItem(int i) {
            return mEntries.get(i);
        }

        @Override
        public long getItemId(int i) {
            return mEntries.get(i).id;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            mHolder = AppViewHolder.runModeHolder(inflater, view);
            view = mHolder.rootView;

            // Bind the data efficiently with the mHolder
            final  ApplicationsState.AppEntry entry = mEntries.get(i);
            mHolder.entry = entry;

            if (entry.label != null) {
                mHolder.appName.setText(entry.label);
            }
            mApplicationsState.ensureIcon(entry);
            if (entry.icon != null) {
                mHolder.appIcon.setImageDrawable(entry.icon);
            }
            //estimate whether the button state changed !!
            if (map.get(entry.appPackage) == null) {
                Uri uriInsert = Uri.parse("content: //com.otosoft.tools.myprovider/selectState");
                ContentValues values = new ContentValues();
                values.put("appPackage", entry.appPackage);
                values.put("state", 0);
                mResolver.insert(uriInsert, values);

                map.put(entry.appPackage, 0);
                mHolder.buttonAuto.setChecked(true);
            } else {
                int buttonSelected = (Integer) map.get(entry.appPackage);
                switch (buttonSelected){
                    case 0:
                        mHolder.buttonAuto.setChecked(true);
                        break;
                    case 1:
                        mHolder.buttonPhone.setChecked(true);
                        break;
                    case 2:
                        mHolder.buttonDesktop.setChecked(true);
                        break;
                    default:
                        break;
                }
            }

            mHolder.buttonAuto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uriUpdate = Uri.parse("content://com.otosoft.tools.myprovider/selectState");
                    map.put(entry.appPackage, 0);
                    ContentValues values_0 = new ContentValues();
                    values_0.put("state", 0);
                    mResolver.update(uriUpdate, values_0, "appPackage = ?",
                              new String[] { entry.appPackage });
                }
            });

            mHolder.buttonPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uriUpdate = Uri.parse("content://com.otosoft.tools.myprovider/selectState");
                    map.put(entry.appPackage, 1);
                    ContentValues values_1 = new ContentValues();
                    values_1.put("state", 1);
                    mResolver.update(uriUpdate, values_1, "appPackage = ?",
                              new String[] { entry.appPackage });
                }
            });

            mHolder.buttonDesktop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uriUpdate = Uri.parse("content://com.otosoft.tools.myprovider/selectState");
                    map.put(entry.appPackage, 2);
                    ContentValues values_2 = new ContentValues();
                    values_2.put("state", 2);
                    mResolver.update(uriUpdate, values_2, "appPackage = ?",
                              new String[] { entry.appPackage });
                }
            });
            return view;
        }
    }
}
