package com.android.settings;

import android.app.Fragment;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

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
import com.openthos.seafile.ISeafileService;

public class CloudServiceFragment extends Fragment implements OnClickListener {
    private Switch mSwitchWallpaper;
    private Switch mSwitchWifi;
    private Switch mSwitchEmail;
    private Switch mSwitchAppdata;
    private Switch mSwitchAppstore;
    private Switch mSwitchBrowser;
    private Switch mSwitchStartupmenu;

    private Button mButtonImport;
    private Button mButtonExport;

    private static final String WALLPAPER_PATH = "data/system/users/0/wallpaper";
    private static final String WALLPAPER_SEAFILE_PATH =
                                        "data/sea/data/sdcard/cloudFolder/wallpaper";
    private static final String IMAGE_WALLPAPER_SEAFILE_PATH =
                                        WALLPAPER_SEAFILE_PATH + "/wallpaper";
    private static final String EMAIL_FILE_PATH = "data/data/com.android.email";
    private static final String EMAIL_SEAFILE_PATH = "data/sea/data/sdcard/cloudFolder/email";
    private static final String PREFS_PATH = EMAIL_FILE_PATH + "/shared_prefs";
    private static final String PREFS_SEAFILE_PATH = EMAIL_SEAFILE_PATH + "/shared_prefs";
    private static final String STATUSBAR_DB_PATH =
                         "data/data/com.android.systemui/databases/Status_bar_database.db";
    private static final String STARTUPMENU_SEAFILE_PATH =
                         "data/sea/data/sdcard/cloudFolder/startupmenu";
    private static final String STATUSBAR_DB_SEAFILE_PATH =
                         STARTUPMENU_SEAFILE_PATH + "/Status_bar_database.db";
    private static final String WIFI_INFO_FILE_PATH = "data/misc/wifi";
    private static final String WIFI_INFO_FILE_CONTENT = "data/misc/wifi/wpa_supplicant.conf";
    private static final String WIFI_INFO_SEAFILE_PATH = "data/sea/data/sdcard/cloudFolder/wifi";
    private static final String WIFI_INFO_SEAFILE_CONTENT =
                                        "data/sea/data/sdcard/cloudFolder/wifi/wpa_supplicant.conf";
    private static final String BROWSER_INFO_FILE_PATH = "data/data/org.mozilla.fennec_root/files";
    private static final String BROWSER_INFO_FILE_CONTENT =
                                                 "data/data/org.mozilla.fennec_root/files/mozilla";
    private static final String BROWSER_INFO_SEAFILE_PATH =
                                                 "data/sea/data/sdcard/cloudFolder/browser";
    private static final String BROWSER_INFO_SEAFILE_CONTENT =
                                                "data/sea/data/sdcard/cloudFolder/browser/mozilla";
    private static final String APPSTORE_SEAFILE_PATH =
                                        "data/sea/data/sdcard/cloudFolder/appstore";
    private static final String APPSTORE_PKGNAME_SEAFILE_PATH =
                                        APPSTORE_SEAFILE_PATH + "/appPkgNames.txt";
    private static final String APPSTORE_PATH = "data/data/com.openthos.appstore/";

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
        intent.setComponent(new ComponentName("com.openthos.seafile",
                    "com.openthos.seafile.SeafileService"));
        getActivity().bindService(intent, mSeafileServiceConnection, Context.BIND_AUTO_CREATE);
        initView(mRootView);

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

        mButtonImport = (Button) v.findViewById(R.id.cloud_import);
        mButtonExport = (Button) v.findViewById(R.id.cloud_export);
        mButtonImport.setOnClickListener(this);
        mButtonExport.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cloud_import:
                importAllFiles();
                break;
            case R.id.cloud_export:
                showExportConfirmDialog();
                break;
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
}
