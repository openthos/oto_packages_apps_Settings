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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
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

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoStartSettings extends Fragment implements ApplicationsState.Callbacks {
    private AppViewHolder mHolder;
    private View mLoadingContainer;
    private View mListContainer;
    private ListView mListView;
    private PackageManager mPackageManager;
    private Intent intent;
    private ArrayList<HashMap<String, Object>> appList;
    private List<ResolveInfo> allowInfoList;
    private List<ResolveInfo> forbidInfoList;

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
        mPackageManager = getActivity().getPackageManager();
        if (mListContainer != null) {
            // Create adapter and list view here
            View emptyView = mListContainer.findViewById(com.android.internal.R.id.empty);
            mListView = (ListView) mListContainer.findViewById(android.R.id.list);
            if (emptyView != null) {
                mListView.setEmptyView(emptyView);
            }
            appList = new ArrayList<HashMap<String, Object>>();
            updateAppList();
            /*
             * if (mBaseEntries != null) { mEntries =
             * applyPrefixFilter(mCurFilterPrefix, mBaseEntries); } else {
             * mEntries = null; }
             */
            MyAdapter adapter = new MyAdapter(getActivity());
            mListView.setAdapter(adapter);
        }
        return mRootView;
    }

    public void updateAppList() {
        intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        allowInfoList = mPackageManager.queryBroadcastReceivers(intent,
                                                                PackageManager.GET_RECEIVERS);
        int k = 0;
        while (k < allowInfoList.size()) {
            if (((allowInfoList.get(k).activityInfo.applicationInfo.flags
                                                             & ApplicationInfo.FLAG_SYSTEM) == 1)
                || ((allowInfoList.get(k).activityInfo.applicationInfo.flags
                                               & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1)) {
                allowInfoList.remove(k);
            } else {
                k++;
            }
        }
        String appName = null;
        String packageReceiver = null;
        Object icon = null;
        if (allowInfoList.size() > 0) {
            appName = mPackageManager.getApplicationLabel(allowInfoList.get(0)
                                                        .activityInfo.applicationInfo).toString();
            packageReceiver = allowInfoList.get(0).activityInfo.packageName + "/"
                                + allowInfoList.get(0).activityInfo.name;
            icon = mPackageManager.getApplicationIcon(allowInfoList
                                                          .get(0).activityInfo.applicationInfo);
            for (int i = 1; i < allowInfoList.size(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                if (appName.equals(mPackageManager.getApplicationLabel(allowInfoList
                                            .get(i).activityInfo.applicationInfo).toString())) {
                    packageReceiver = packageReceiver + ";"
                                      + allowInfoList.get(i).activityInfo.packageName
                                      + "/" + allowInfoList.get(i).activityInfo.name;
                } else {
                    map.put("icon", icon);
                    map.put("appName", appName);
                    map.put("packageReceiver", packageReceiver);
                    map.put("isChecked", true);
                    appList.add(map);
                    packageReceiver = allowInfoList.get(i).activityInfo.packageName + "/"
                            + allowInfoList.get(i).activityInfo.name;
                    appName = mPackageManager.getApplicationLabel(allowInfoList.get(i)
                                                        .activityInfo.applicationInfo).toString();
                    icon = mPackageManager.getApplicationIcon(allowInfoList.
                                                             get(i).activityInfo.applicationInfo);
                }
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("icon", icon);
            map.put("appName", appName);
            map.put("packageReceiver", packageReceiver);
            map.put("isChecked", true);
            appList.add(map);
        }

        forbidInfoList = mPackageManager.queryBroadcastReceivers(intent,
                                                          PackageManager.GET_DISABLED_COMPONENTS);
        k = 0;
        while (k < forbidInfoList.size()) {
            if (((forbidInfoList.get(k).activityInfo.applicationInfo.flags
                                                         & ApplicationInfo.FLAG_SYSTEM) == 1)
                || ((forbidInfoList.get(k).activityInfo.applicationInfo.flags
                                                & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1)) {
                forbidInfoList.remove(k);
            } else {
                k++;
            }
        }

        k = 0;
        while (k < forbidInfoList.size()) {
            ComponentName mComponentName = new ComponentName(forbidInfoList
                                                                .get(k).activityInfo.packageName,
                                                         forbidInfoList.get(k).activityInfo.name);
            if (mPackageManager.getComponentEnabledSetting(mComponentName) != 2) {
               forbidInfoList.remove(k);
            } else {
                k++;
            }
        }

        if (forbidInfoList.size() > 0) {
            appName = mPackageManager.getApplicationLabel(forbidInfoList
                                          .get(0).activityInfo.applicationInfo).toString();
            packageReceiver = forbidInfoList.get(0).activityInfo.packageName + "/"
                              + forbidInfoList.get(0).activityInfo.name;
            icon = mPackageManager.getApplicationIcon(forbidInfoList
                                                          .get(0).activityInfo.applicationInfo);
            for (int i = 1; i < forbidInfoList.size(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                if (appName.equals(mPackageManager.getApplicationLabel(forbidInfoList.get(i)
                                                      .activityInfo.applicationInfo).toString())) {
                    packageReceiver = packageReceiver + ";"
                                      + forbidInfoList.get(i).activityInfo.packageName + "/"
                                      + forbidInfoList.get(i).activityInfo.name;
                } else {
                    map.put("icon", icon);
                    map.put("appName", appName);
                    map.put("packageReceiver", packageReceiver);
                    map.put("isChecked", false);
                    appList.add(map);
                    packageReceiver = forbidInfoList.get(i).activityInfo.packageName + "/"
                            + forbidInfoList.get(i).activityInfo.name;
                    appName = mPackageManager.getApplicationLabel(forbidInfoList.get(i)
                                                  .activityInfo.applicationInfo).toString();
                    icon = mPackageManager.getApplicationIcon(forbidInfoList
                                                  .get(i).activityInfo.applicationInfo);
                }
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("icon", icon);
            map.put("appName", appName);
            map.put("packageReceiver", packageReceiver);
            map.put("isChecked", false);
            appList.add(map);
        }

    }

    class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public MyAdapter(Context context){
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return appList != null ? appList.size() : 0;
        }

        @Override
        public Object getItem(int i) {
            return appList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            mHolder = AppViewHolder.autoStartHolder(inflater, view);
            view = mHolder.rootView;
            mHolder.appName.setText(appList.get(i).get("appName").toString());
            mHolder.appIcon.setImageDrawable((Drawable) appList.get(i).get("icon"));
            if (!(appList.get(i).get("isChecked") == true)) {
                mHolder.appSwitch.setSelected(false);
            } else if (appList.get(i).get("isChecked") == true) {
                mHolder.appSwitch.setSelected(true);
            }
            mHolder.appSwitch.setOnClickListener(new android.view.View.OnClickListener() {
                public void onClick(View v) {
                    v.setSelected(!v.isSelected());
                    appList.get(i).put("isChecked", !(Boolean) (appList.get(i).get("isChecked")));
                    changeAutoStartState(appList.get(i));
                }
            });
            return view;
        }

        public void changeAutoStartState(final HashMap<String, Object> hashMap) {
            new Thread() {
                public void run() {
                    String cmd;
                    if (!(Boolean) hashMap.get("isChecked")) {
                         String[] packageReceiverList = hashMap.get("packageReceiver")
                                                                    .toString().split(";");
                         for (int j = 0; j < packageReceiverList.length; j++) {
                             cmd = "pm disable " + packageReceiverList[j];
                             cmd = cmd.replace("$", "\"" + "$" + "\"");
                             execCmd(cmd);
                         }
                    } else {
                         String[] packageReceiverList = hashMap.get("packageReceiver")
                                                                    .toString().split(";");
                         for (int j = 0; j < packageReceiverList.length; j++) {
                             cmd = "pm enable " + packageReceiverList[j];
                             cmd = cmd.replace("$", "\"" + "$" + "\"");
                             execCmd(cmd);
                         }
                    }
                }
            }.start();
        }
    }

    public static boolean execCmd(String cmd) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }
}
