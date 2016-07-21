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
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.android.settings.applications.*;
import com.android.settings.applications.ApplicationsState.*;
import com.android.settings.applications.ApplicationsState.AppEntry;
import com.android.settings.applications.ManageApplications.*;

import java.util.ArrayList;

public class AutoStartSettings extends Fragment implements ApplicationsState.Callbacks {
    private ApplicationsState mApplicationsState;
    private ApplicationsState.Session mSession;
    private AppViewHolder mHolder;
    private View mLoadingContainer;
    private View mListContainer;
    private ListView mListView;
    private ArrayList<ApplicationsState.AppEntry> mBaseEntries;
    private ArrayList<ApplicationsState.AppEntry> mEntries;
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
       View mRootView = inflater.inflate(R.layout.autostart_applications_apps, container, false);
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
           MyAdapter adapter=new MyAdapter(getActivity());
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
                //repair
                String nlabel = entry.getNormalizedLabel();
                if (nlabel.startsWith(prefixStr) || nlabel.indexOf(spacePrefixStr) != -1) {
                    newEntries.add(entry);
                }
            }
            return newEntries;
        }
    }

    class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        public MyAdapter(Context context){
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
            mHolder = AppViewHolder.autoStartHolder(inflater, view);
            view = mHolder.rootView;
            // Bind the data efficiently with the mHolder
            ApplicationsState.AppEntry entry = mEntries.get(i);
            Log.i("1542",""+mEntries.get(i).label);
            mHolder.entry = entry;
            if (entry.label != null) {
                mHolder.appName.setText(entry.label);
            }
            mApplicationsState.ensureIcon(entry);
            if (entry.icon != null) {
            mHolder.appIcon.setImageDrawable(entry.icon);
            mHolder.appSwitch.setOnClickListener(new android.view.View.OnClickListener() {
                  public void onClick(View v) {
                          v.setSelected(!v.isSelected());
                          mEntries.get(i).isChecked = !mEntries.get(i).isChecked;
                      }
                  });
            }

            if (!entry.isChecked) {
                mHolder.appSwitch.setSelected(false);
            } else if (entry.isChecked) {
                mHolder.appSwitch.setSelected(true);
            }
            return view;
        }
    }
}
