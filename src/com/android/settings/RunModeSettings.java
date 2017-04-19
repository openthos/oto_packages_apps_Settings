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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;
import com.android.settings.applications.ApplicationsState.*;
import com.android.settings.applications.*;
import com.android.settings.applications.ManageApplications.*;
import android.content.Context;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ApplicationInfo;

public class RunModeSettings extends Fragment implements ApplicationsState.Callbacks {
    private AppViewHolder mHolder;
    private View mListContainer;
    private List<ResolveInfo> mResolveInfos;
    private PackageManager mPackageManager;

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
        mPackageManager = getActivity().getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mResolveInfos = mPackageManager.queryIntentActivities(mainIntent, 0);

        View mRootView = inflater.inflate(R.layout.runmode_applications_apps, container, false);
        mListContainer = mRootView.findViewById(R.id.list_container);
        if (mListContainer != null) {
            // Create adapter and list view here
            View emptyView = mListContainer.findViewById(com.android.internal.R.id.empty);
            ListView lv = (ListView) mListContainer.findViewById(android.R.id.list);
            if (emptyView != null) {
                lv.setEmptyView(emptyView);
            }
            RunModeAdapter adapter=new RunModeAdapter(getActivity());
            lv.setAdapter(adapter);
        }
        return mRootView;
    }

    class RunModeAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        public RunModeAdapter(Context context){
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mResolveInfos != null ? mResolveInfos.size() : 0;
        }

        @Override
        public Object getItem(int i) {
            return mResolveInfos.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            mHolder = AppViewHolder.runModeHolder(inflater, view);
            view = mHolder.rootView;

            final ResolveInfo info = mResolveInfos.get(i);

            mHolder.appName.setText(info.loadLabel(mPackageManager));
            mHolder.appIcon.setImageDrawable(info.loadIcon(mPackageManager));
            int buttonSelected = android.provider.Settings.Global.getInt(
                     getActivity().getContentResolver(), info.activityInfo.packageName, 0);
            switch (buttonSelected){
                case ApplicationInfo.AUTO_STARTUP_MODE:
                    mHolder.buttonAuto.setChecked(true);
                    break;
                case ApplicationInfo.PHONE_STARTUP_MODE:
                    mHolder.buttonPhone.setChecked(true);
                    break;
                case ApplicationInfo.DESKTOP_STARTUP_MODE:
                    mHolder.buttonDesktop.setChecked(true);
                    break;
                default:
                    break;
            }

            mHolder.buttonAuto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    android.provider.Settings.Global.putInt(
                        getActivity().getContentResolver(), info.activityInfo.packageName, 0);
                }
            });

            mHolder.buttonPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    android.provider.Settings.Global.putInt(
                        getActivity().getContentResolver(), info.activityInfo.packageName, 1);
                }
            });

            mHolder.buttonDesktop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    android.provider.Settings.Global.putInt(
                        getActivity().getContentResolver(), info.activityInfo.packageName, 2);
                }
            });
            return view;
        }
    }
}
