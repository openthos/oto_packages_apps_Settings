package com.android.settings;

import android.app.Fragment;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import com.android.settings.widget.SwitchBar;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.text.TextUtils;
import android.net.Uri;
import android.database.Cursor;
import android.app.WallpaperManager;
import android.util.Log;
import com.android.otosoft.tools.FileUtils;
import com.android.otosoft.tools.ChangeBuildPropTools;
import android.database.sqlite.SQLiteDatabase;
import android.content.pm.PackageInfo;
import android.content.Intent;
import java.util.List;
import java.util.ArrayList;
import org.openthos.seafile.ISeafileService;

public class CloudServiceFragment extends Fragment {
    private Switch mSwitchWallpaper;
    private Switch mSwitchWifi;
    private Switch mSwitchAppdata;
    private Switch mSwitchAppstore;
    private Switch mSwitchBrowser;
    private Switch mSwitchStartupmenu;

    private Button mButtonImport;
    private Button mButtonExport;

    private TextView mBrowserImport;
    private TextView mBrowserExport;
    private TextView mAppdataImport;
    private TextView mAppdataExport;
    private ListView mListViewBrowser;
    private ListView mListViewAppdata;

    private List<ResolveInfo> mExportBrowsers = new ArrayList();
    private List<ResolveInfo> mImportBrowsers = new ArrayList();
    private List<ResolveInfo> mExportAppdata = new ArrayList();
    private List<ResolveInfo> mImportAppdata = new ArrayList();
    private List<String> mSyncBrowsers = new ArrayList();
    private List<String> mSyncAppdata = new ArrayList();
    private ResolveAdapter mBrowsersAdapter;
    private ResolveAdapter mAppdataAdapter;
    private PackageManager mPackageManager;
    private int mTag = -1;

    private ClickListener mClickListener;
    private CheckedChangeListener mCheckedChangeListener;

    private File mCloudFolder;
    private ISeafileService mISeafileService;
    private SeafileServiceConnection mSeafileServiceConnection;

    private static final String TAG = "CloudServiceFragment";
    private static final String ROOT_COMMOND = "chmod -R 777 ";
    private static final int BUF_SIZE = 1024;
    private boolean DEBUG = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View mRootView = inflater.inflate(R.layout.fragment_cloud_service, container, false);
        mSeafileServiceConnection = new SeafileServiceConnection();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("org.openthos.seafile",
                    "org.openthos.seafile.SeafileService"));
        getActivity().bindService(intent, mSeafileServiceConnection, Context.BIND_AUTO_CREATE);
        initView(mRootView);
        initData();

        mCloudFolder = new File(getActivity().getResources()
                                             .getString(R.string.cloudfolder_path));
        return mRootView;
    }

    private void initView(View v) {
        mSwitchWallpaper = (Switch) v.findViewById(R.id.switch_wallpaper);
        mSwitchWifi = (Switch) v.findViewById(R.id.switch_wifi);
        mSwitchAppdata = (Switch) v.findViewById(R.id.switch_appdata);
        mSwitchAppstore = (Switch) v.findViewById(R.id.switch_appstore);
        mSwitchBrowser = (Switch) v.findViewById(R.id.switch_browser);
        mSwitchStartupmenu = (Switch) v.findViewById(R.id.switch_startmenu);
        mBrowserImport = (TextView) v.findViewById(R.id.tv_browser_import);
        mBrowserExport = (TextView) v.findViewById(R.id.tv_browser_export);
        mListViewBrowser = (ListView) v.findViewById(R.id.lv_browser);
        mAppdataImport = (TextView) v.findViewById(R.id.tv_appdata_import);
        mAppdataExport = (TextView) v.findViewById(R.id.tv_appdata_export);
        mListViewAppdata = (ListView) v.findViewById(R.id.lv_appdata);
        mButtonImport = (Button) v.findViewById(R.id.cloud_import);
        mButtonExport = (Button) v.findViewById(R.id.cloud_export);

        if (mSwitchBrowser.isChecked()) {
            mBrowserImport.setEnabled(true);
            mBrowserExport.setEnabled(true);
        } else {
            mBrowserImport.setEnabled(false);
            mBrowserExport.setEnabled(false);
        }

        if (mSwitchAppdata.isChecked()) {
            mAppdataImport.setEnabled(true);
            mAppdataExport.setEnabled(true);
        } else {
            mAppdataImport.setEnabled(false);
            mAppdataExport.setEnabled(false);
        }

        mClickListener = new ClickListener();
        mCheckedChangeListener = new CheckedChangeListener();
        mButtonImport.setOnClickListener(mClickListener);
        mButtonExport.setOnClickListener(mClickListener);
        mBrowserImport.setOnClickListener(mClickListener);
        mBrowserExport.setOnClickListener(mClickListener);
        mSwitchBrowser.setOnCheckedChangeListener(mCheckedChangeListener);
        mAppdataImport.setOnClickListener(mClickListener);
        mAppdataExport.setOnClickListener(mClickListener);
        mSwitchAppdata.setOnCheckedChangeListener(mCheckedChangeListener);
        mSwitchStartupmenu.setOnCheckedChangeListener(mCheckedChangeListener);
    }

    private void initData() {
        mPackageManager = getActivity().getPackageManager();
        mBrowsersAdapter = new ResolveAdapter();
        mListViewBrowser.setAdapter(mBrowsersAdapter);
        mAppdataAdapter = new ResolveAdapter();
        mListViewAppdata.setAdapter(mAppdataAdapter);
    }

    private void setListViewHeight(ListView listView, ResolveAdapter adapter) {
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        if (adapter.getCount() > 0) {
            View item = adapter.getView(0, null, listView);
            item.measure(0, 0);
            params.height = item.getMeasuredHeight() * adapter.getCount() +
                    listView.getDividerHeight() * (adapter.getCount() - 1);
            listView.setLayoutParams(params);
        } else {
            params.height = 0;
            listView.setLayoutParams(params);
        }
    }

    private void showExportConfirmDialog() {
        AlertDialog.Builder builder=new Builder(getActivity());
        builder.setMessage(getActivity().getResources().
                           getString(R.string.export_confirm_dialog_info));
        builder.setPositiveButton(getActivity().
                                  getResources().getString(R.string.cloud_service_dialog_confirm),
                                  new android.content.DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int which) {
                                          exportAllFiles();
                                          dialog.dismiss();
                                      }
                                  });
        builder.setNegativeButton(getActivity().
                                  getResources().getString(R.string.cloud_service_dialog_cancel),
                                  new android.content.DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int which) {
                                          dialog.dismiss();
                                      }
                                  });
        builder.create().show();
    }

    private void importAllFiles() {
        try {
            mISeafileService.restoreSettings(mSwitchWallpaper.isChecked(),
                    mSwitchWifi.isChecked(),
                    mSwitchAppdata.isChecked(),
                    mSyncAppdata,
                    mSwitchStartupmenu.isChecked(),
                    mSwitchBrowser.isChecked(),
                    mSyncBrowsers,
                    mSwitchAppstore.isChecked());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void exportAllFiles() {
        try {
            mISeafileService.saveSettings(mSwitchWallpaper.isChecked(),
                    mSwitchWifi.isChecked(),
                    mSwitchAppdata.isChecked(),
                    mSyncAppdata,
                    mSwitchStartupmenu.isChecked(),
                    mSwitchBrowser.isChecked(),
                    mSyncBrowsers,
                    mSwitchAppstore.isChecked());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class SeafileServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mISeafileService = ISeafileService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private class ResolveAdapter extends BaseAdapter {
        private List<ResolveInfo> allList = new ArrayList();
        private List<String> syncList = new ArrayList();

        private void setList(List<ResolveInfo> allList, List<String> syncList) {
            this.allList = allList;
            this.syncList = syncList;
        }

        @Override
        public int getCount() {
            return allList.size();
        }

        @Override
        public Object getItem(int i) {
            return allList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            View convertView = view;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).
                        inflate(R.layout.list_item, viewGroup, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();
            holder.text.setText(allList.get(i).loadLabel(mPackageManager));
            holder.image.setImageDrawable(allList.get(i).loadIcon(mPackageManager));
            holder.check.setTag(R.id.tag_list, syncList);
            holder.check.setTag(R.id.tag_package, allList.get(i).activityInfo.packageName);
            holder.check.setOnCheckedChangeListener(mCheckedChangeListener);
            try {
                if ((mPackageManager.getPackageInfo(allList.get(i).activityInfo.packageName, 0).
                        applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                    holder.check.setChecked(false);
                    holder.check.setChecked(true);
                    holder.check.setClickable(false);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    private class ViewHolder {
        public TextView text;
        public ImageView image;
        public CheckBox check;

        public ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.tv_list_item);
            image = (ImageView) view.findViewById(R.id.iv_list_item);
            check = (CheckBox) view.findViewById(R.id.cb_list_item);
        }
    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            try {
                switch (view.getId()) {
                    case R.id.cloud_import:
                        if ((mSwitchBrowser.isChecked()
                                && mTag == mISeafileService.getTagBrowserExport())
                            || (mSwitchAppdata.isChecked()
                                && mTag == mISeafileService.getTagBrowserExport())) {
                            Toast.makeText(getActivity(),
                                    R.string.warning_browser_export, Toast.LENGTH_LONG).show();
                            return;
                        }
                        importAllFiles();
                        break;
                    case R.id.cloud_export:
                        if ((mSwitchBrowser.isChecked()
                                && mTag == mISeafileService.getTagBrowserImport())
                            || (mSwitchAppdata.isChecked()
                                && mTag == mISeafileService.getTagAppdataImport())) {
                            Toast.makeText(getActivity(),
                                    R.string.warning_browser_import, Toast.LENGTH_LONG).show();
                            return;
                        }
                        showExportConfirmDialog();
                        break;
                    case R.id.tv_browser_import:
                        operateClick(mImportBrowsers, mISeafileService.getTagBrowserImport(),
                                    mBrowsersAdapter, mSyncBrowsers, mListViewBrowser,
                                    mBrowserImport, mBrowserExport);
                        break;
                    case R.id.tv_browser_export:
                        operateClick(mExportBrowsers, mISeafileService.getTagBrowserExport(),
                                    mBrowsersAdapter, mSyncBrowsers, mListViewBrowser,
                                    mBrowserExport, mBrowserImport);
                        break;
                    case R.id.tv_appdata_import:
                        operateClick(mImportAppdata, mISeafileService.getTagAppdataImport(),
                                    mAppdataAdapter, mSyncAppdata, mListViewAppdata,
                                    mAppdataImport, mAppdataExport);
                        break;
                    case R.id.tv_appdata_export:
                        operateClick(mExportAppdata, mISeafileService.getTagAppdataExport(),
                                    mAppdataAdapter, mSyncAppdata, mListViewAppdata,
                                    mAppdataExport, mAppdataImport);
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void operateClick(List<ResolveInfo> appList, int tag, ResolveAdapter adapter,
            List<String> syncList, ListView listView, TextView light, TextView dark) {
        try {
            appList = mISeafileService.getAppsInfo(tag);
            syncList.clear();
            adapter.setList(appList, syncList);
            adapter.notifyDataSetChanged();
            setListViewHeight(listView, adapter);
            listView.setVisibility(View.VISIBLE);
            mTag = tag;
            light.setBackgroundResource(R.color.text_bg_color);
            dark.setBackgroundResource(R.color.circle_avatar_frame_pressed_color);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class CheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.switch_startmenu:
                    if (isChecked) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(getString(R.string.warn_restore_startupmenu));
                        builder.setPositiveButton(R.string.okay, null);
                        builder.create().show();
                    }
                    break;
                case R.id.switch_appdata:
                    if (isChecked) {
                        mAppdataExport.performClick();
                        mAppdataImport.setEnabled(true);
                        mAppdataExport.setEnabled(true);
                    } else {
                        mAppdataImport.setEnabled(false);
                        mAppdataExport.setEnabled(false);
                        mListViewAppdata.setVisibility(View.GONE);
                    }
                    break;
                case R.id.switch_browser:
                    if (isChecked) {
                        mBrowserExport.performClick();
                        mBrowserImport.setEnabled(true);
                        mBrowserExport.setEnabled(true);
                    } else {
                        mBrowserImport.setEnabled(false);
                        mBrowserExport.setEnabled(false);
                        mListViewBrowser.setVisibility(View.GONE);
                    }
                    break;
                case R.id.cb_list_item:
                    if (isChecked) {
                        ((List<String>) buttonView.getTag(R.id.tag_list)).
                                add((String) buttonView.getTag(R.id.tag_package));
                    } else {
                        ((List<String>) buttonView.getTag(R.id.tag_list)).
                                remove((String) buttonView.getTag(R.id.tag_package));
                    }
                    break;
            }
        }
    }
}
