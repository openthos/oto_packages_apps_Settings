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
import android.widget.Switch;
import com.android.settings.applications.*;
import com.android.settings.applications.ApplicationsState.AppEntry;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;

import java.util.ArrayList;

public class FireWallSettings extends Fragment implements ApplicationsState.Callbacks {
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
        View mRootView = inflater.inflate(R.layout.fire_wall_application, container, false);
        mListContainer = mRootView.findViewById(R.id.list_container);
        if (mListContainer != null) {
            // Create adapter and list view here
            View emptyView = mListContainer.findViewById(com.android.internal.R.id.empty);
            ListView lv = (ListView) mListContainer.findViewById(R.id.lv_firewall);
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
            mHolder = AppViewHolder.fireWallHolder(inflater, view);
            view = mHolder.rootView;
            // Bind the data efficiently with the mHolder
            ApplicationsState.AppEntry entry = mEntries.get(i);
            mHolder.entry = entry;
            if (entry.label != null) {
                mHolder.appName.setText(entry.label);
            }
            mApplicationsState.ensureIcon(entry);
            if (entry.icon != null) {
                mHolder.appIcon.setImageDrawable(entry.icon);
            }
            mHolder.inNetLicenseSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mEntries.get(i).inNetLicenseChecked = isChecked;
                }
            });
            mHolder.outNetLicenseSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mEntries.get(i).outNetLicenseChecked = isChecked;
                }
            });

            if (!entry.inNetLicenseChecked) {
                mHolder.inNetLicenseSwitch.setChecked(false);
            } else {
                mHolder.inNetLicenseSwitch.setChecked(true);
            }
            if (!entry.outNetLicenseChecked) {
                mHolder.outNetLicenseSwitch.setChecked(false);
            } else {
                mHolder.outNetLicenseSwitch.setChecked(true);
            }
            return view;
        }
    }
}
