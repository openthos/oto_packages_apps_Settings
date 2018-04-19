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
    private Switch mSwitchEmail;
    private Switch mSwitchAppdata;
    private Switch mSwitchAppstore;
    private Switch mSwitchBrowser;
    private Switch mSwitchStartupmenu;

    private Button mButtonImport;
    private Button mButtonExport;

    private TextView mBrowserImport;
    private TextView mBrowserExport;
    private ListView mListView;

    private List<ResolveInfo> mExportBrowsers;
    private List<ResolveInfo> mImportBrowsers;
    private PackageManager mPackageManager;
    private ResolveAdapter mAdapter;
    private List<String> mSyncBrowsers;
    private boolean mIsImport = false;

    private ClickListener mClickListener;
    private CheckedChangeListener mCheckedChangeListener;

    private static final String SEAFILE_PATH = "/data/sea/data/";
    private static final String SEAFILE_PATH_BROWSER = "/UserConfig/browser/";

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
        mSwitchEmail = (Switch) v.findViewById(R.id.switch_email);
        mSwitchAppdata = (Switch) v.findViewById(R.id.switch_appdata);
        mSwitchAppstore = (Switch) v.findViewById(R.id.switch_appstore);
        mSwitchBrowser = (Switch) v.findViewById(R.id.switch_browser);
        mSwitchStartupmenu = (Switch) v.findViewById(R.id.switch_startmenu);
        mBrowserImport = (TextView) v.findViewById(R.id.tv_browser_import);
        mBrowserExport = (TextView) v.findViewById(R.id.tv_browser_export);
        mListView = (ListView) v.findViewById(R.id.lv_browser);

        if (mSwitchBrowser.isChecked()) {
            mBrowserImport.setEnabled(true);
            mBrowserExport.setEnabled(true);
        } else {
            mBrowserImport.setEnabled(false);
            mBrowserExport.setEnabled(false);
        }
        mButtonImport = (Button) v.findViewById(R.id.cloud_import);
        mButtonExport = (Button) v.findViewById(R.id.cloud_export);

        mClickListener = new ClickListener();
        mCheckedChangeListener = new CheckedChangeListener();
        mButtonImport.setOnClickListener(mClickListener);
        mButtonExport.setOnClickListener(mClickListener);
        mBrowserImport.setOnClickListener(mClickListener);
        mBrowserExport.setOnClickListener(mClickListener);
        mSwitchBrowser.setOnCheckedChangeListener(mCheckedChangeListener);

    }

    private void initData() {
        mSyncBrowsers = new ArrayList();
        mImportBrowsers = new ArrayList();
        mExportBrowsers = new ArrayList();
        mAdapter = new ResolveAdapter();
        mListView.setAdapter(mAdapter);
        mPackageManager = getActivity().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        Uri uri = Uri.parse("https://");
        intent.setData(uri);
        List<ResolveInfo> list = mPackageManager.queryIntentActivities(
                intent, PackageManager.GET_INTENT_FILTERS);
        mExportBrowsers = list;

        setListViewHeight();
    }

    private void setListViewHeight() {
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        if (mAdapter.getCount() > 0) {
            View item = mAdapter.getView(0, null, mListView);
            item.measure(0, 0);
            params.height = item.getMeasuredHeight() * mAdapter.getCount() +
                    mListView.getDividerHeight() * (mAdapter.getCount() - 1);
            mListView.setLayoutParams(params);
        } else {
            params.height = 0;
            mListView.setLayoutParams(params);
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
                    mSwitchEmail.isChecked(),
                    mSwitchAppdata.isChecked(),
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
                    mSwitchEmail.isChecked(),
                    mSwitchAppdata.isChecked(),
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
        private List<ResolveInfo> mBrowsersList = new ArrayList();

        @Override
        public int getCount() {
            if (mIsImport) {
                return mImportBrowsers.size();
            } else {
                return mExportBrowsers.size();
            }
        }

        @Override
        public Object getItem(int i) {
            if (mIsImport) {
                return mImportBrowsers.get(i);
            } else {
                return mExportBrowsers.get(i);
            }
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
            if (mIsImport) {
                mBrowsersList = mImportBrowsers;
            } else {
                mBrowsersList = mExportBrowsers;
            }
            holder = (ViewHolder) convertView.getTag();
            holder.text.setText(mBrowsersList.get(i).loadLabel(mPackageManager));
            holder.image.setImageDrawable(mBrowsersList.get(i).loadIcon(mPackageManager));
            holder.check.setTag(mBrowsersList.get(i).activityInfo.packageName);
            holder.check.setOnCheckedChangeListener(mCheckedChangeListener);
            try {
                if ((mPackageManager.getPackageInfo(mBrowsersList.get(i).activityInfo.packageName, 0).
                        applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                    holder.check.setChecked(true);
                    holder.check.setClickable(false);
                    mSyncBrowsers.add(mBrowsersList.get(i).activityInfo.packageName);
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
            switch (view.getId()) {
                case R.id.cloud_import:
                    if (mSwitchBrowser.isChecked() && !mIsImport) {
                        Toast.makeText(getActivity(),
                                R.string.warning_browser_export, Toast.LENGTH_LONG).show();
                        return;
                    }
                    importAllFiles();
                    break;
                case R.id.cloud_export:
                    if (mSwitchBrowser.isChecked() && mIsImport) {
                        Toast.makeText(getActivity(),
                                R.string.warning_browser_import, Toast.LENGTH_LONG).show();
                        return;
                    }
                    showExportConfirmDialog();
                    break;
                case R.id.tv_browser_import:
                    try {
                        mImportBrowsers.clear();
                        File file = new File(SEAFILE_PATH +
                                mISeafileService.getUserName() + SEAFILE_PATH_BROWSER);
                        if (file.exists()) {
                            File[] syncBrowsers = file.listFiles();
                            for (int i = 0; i < mExportBrowsers.size(); i++) {
                                for (int j = 0; j < syncBrowsers.length; j++) {
                                    if (mExportBrowsers.get(i).activityInfo.packageName.equals(
                                            syncBrowsers[j].getName().replace(".tar.gz", ""))) {
                                        mImportBrowsers.add(mExportBrowsers.get(i));
                                    }
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mIsImport = true;
                    mSyncBrowsers.clear();
                    mListView.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                    setListViewHeight();
                    mBrowserImport.setBackgroundResource(R.color.text_bg_color);
                    mBrowserExport.setBackgroundResource(R.color.circle_avatar_frame_pressed_color);
                    break;
                case R.id.tv_browser_export:
                    mIsImport = false;
                    mSyncBrowsers.clear();
                    mListView.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                    setListViewHeight();
                    mBrowserExport.setBackgroundResource(R.color.text_bg_color);
                    mBrowserImport.setBackgroundResource(R.color.circle_avatar_frame_pressed_color);
                    break;
            }
        }
    }

    private class CheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.switch_browser:
                    if (isChecked) {
                        mBrowserImport.setEnabled(true);
                        mBrowserExport.setEnabled(true);
                    } else {
                        mBrowserImport.setEnabled(false);
                        mBrowserExport.setEnabled(false);
                        mListView.setVisibility(View.GONE);
                    }
                    break;
                case R.id.cb_list_item:
                    if (isChecked) {
                        mSyncBrowsers.add((String) buttonView.getTag());
                    } else {
                        mSyncBrowsers.remove((String) buttonView.getTag());
                    }
                    break;
            }
        }
    }
}
