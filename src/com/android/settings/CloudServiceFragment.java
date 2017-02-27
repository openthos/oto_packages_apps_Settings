package com.android.settings;

import android.app.Fragment;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
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
    private static final String TAG = "CloudServiceFragment";
    private static final String ROOT_COMMOND = "chmod -R 777 ";
    private static final int BUF_SIZE = 1024;
    private boolean DEBUG = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View mRootView = inflater.inflate(R.layout.fragment_cloud_service, container, false);
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
                if (mCloudFolder.exists()) {
                    importAllFiles();
                    Toast.makeText(getActivity(),getActivity().getResources().
                         getString(R.string.import_reboot_info_warn),Toast.LENGTH_SHORT).show();
                }
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
        if (mSwitchWallpaper.isChecked()) {
            importWallpaperFiles();
        }
        if (mSwitchEmail.isChecked()) {
            importEmailFiles();
        }
        if (mSwitchStartupmenu.isChecked()) {
            importStatusBarFiles();
        }
        if (mSwitchWifi.isChecked()) {
            importWifiFiles();
        }
        if (mSwitchBrowser.isChecked()) {
            importBrowserFiles();
        }
        if (mSwitchAppstore.isChecked()) {
            getActivity().sendBroadcast(
                          new Intent(Intent.ACTION_APPSTORE_SEAFILE));
        }
    }

    private void importWallpaperFiles() {
        if (mSwitchWallpaper.isChecked()) {
            InputStream wallpaperFile = null;
            try {
                wallpaperFile = new FileInputStream(IMAGE_WALLPAPER_SEAFILE_PATH);
                if (wallpaperFile != null) {
                    WallpaperManager.getInstance(getActivity()).setStream(wallpaperFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (wallpaperFile != null) {
                        wallpaperFile.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void importEmailFiles() {
        File emailFiles = new File(EMAIL_SEAFILE_PATH);
        if (emailFiles.exists()){
            File emailNewDevicePrefs = new File(PREFS_PATH);
            if (emailNewDevicePrefs.exists()) {
                ChangeBuildPropTools.exec(ROOT_COMMOND + PREFS_PATH);
                FileUtils.deleteGeneralFile(PREFS_PATH);
            }
            ChangeBuildPropTools.exec(ROOT_COMMOND + EMAIL_FILE_PATH);
            ChangeBuildPropTools.exec(ROOT_COMMOND + PREFS_SEAFILE_PATH);
            if (FileUtils.copyGeneralFile(PREFS_SEAFILE_PATH, EMAIL_FILE_PATH)) {
                if (DEBUG) Log.i(TAG,"seafile email sync to new device sucessful!");
            } else {
                if (DEBUG) Log.i(TAG,"seafile email sync to new device fail!");
            }
        }
    }

    private void importStatusBarFiles() {
        File statusbarDbFiles = new File(STATUSBAR_DB_SEAFILE_PATH);
        if (statusbarDbFiles.exists()) {
            SQLiteDatabase statusbarDb = SQLiteDatabase.openDatabase(
                          STATUSBAR_DB_SEAFILE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            Cursor cursor = statusbarDb.rawQuery("select * from status_bar_tb", null);
            if (cursor != null) {
                List<PackageInfo> pkgInfos = getActivity()
                                        .getPackageManager().getInstalledPackages(0);
                ArrayList<String> pkgNameLists = new ArrayList();
                while(cursor.moveToNext()) {
                    String pkgName = cursor.getString(cursor.getColumnIndex("pkgname"));
                    for (PackageInfo pkgInfo : pkgInfos) {
                        if (pkgInfo.packageName.equals(pkgName)) {
                            pkgNameLists.add(pkgName);
                        }
                    }
                }
                Intent intent = new Intent(Intent.STATUS_BAR_SEAFILE);
                intent.putStringArrayListExtra("pkgname", pkgNameLists);
                getActivity().sendBroadcast(intent);
            }
        }
    }

    private void importWifiFiles() {
        ChangeBuildPropTools.exec(ROOT_COMMOND + WIFI_INFO_SEAFILE_CONTENT);
        ChangeBuildPropTools.exec(ROOT_COMMOND + WIFI_INFO_FILE_PATH);
        ChangeBuildPropTools.exec("cp -f " + WIFI_INFO_SEAFILE_CONTENT + " " + WIFI_INFO_FILE_PATH);
    }

    private void importBrowserFiles() {
        ChangeBuildPropTools.exec(ROOT_COMMOND + BROWSER_INFO_FILE_PATH);
        ChangeBuildPropTools.exec(ROOT_COMMOND + BROWSER_INFO_SEAFILE_CONTENT);
        ChangeBuildPropTools.exec("cp -rf " + BROWSER_INFO_SEAFILE_CONTENT +" "
                                  + BROWSER_INFO_FILE_PATH);
    }

    private void exportAllFiles() {
        if (!mCloudFolder.exists()) {
            mCloudFolder.mkdirs();
        }
        if (mSwitchWallpaper.isChecked()) {
            File wallpaperSeafile = new File(WALLPAPER_SEAFILE_PATH);
            if (!wallpaperSeafile.exists()) {
                wallpaperSeafile.mkdirs();
            }
            exportWallpaperFiles();
        }
        if (mSwitchStartupmenu.isChecked()) {
            File startupMenuFile = new File (STARTUPMENU_SEAFILE_PATH);
            if (startupMenuFile.exists()) {
                FileUtils.deleteGeneralFile(STARTUPMENU_SEAFILE_PATH);
            }
            startupMenuFile.mkdirs();
            exportStartupmenuFiles();
        }
        if (mSwitchEmail.isChecked()) {
            File emailFile = new File (EMAIL_SEAFILE_PATH);
            if (emailFile.exists()) {
                FileUtils.deleteGeneralFile(EMAIL_SEAFILE_PATH);
            }
            emailFile.mkdirs();
            exportEmailFiles();
        }
        if (mSwitchWifi.isChecked()) {
            File wifiInfoSeafile = new File(WIFI_INFO_SEAFILE_PATH);
            if (!wifiInfoSeafile.exists()) {
                wifiInfoSeafile.mkdirs();
            }
            exportWifiFiles();
        }
        if (mSwitchBrowser.isChecked()) {
            File browserInfoSeafile = new File(BROWSER_INFO_SEAFILE_PATH);
            if (!browserInfoSeafile.exists()) {
                browserInfoSeafile.mkdirs();
            }
            exportBrowserFiles();
        }
        if (mSwitchAppstore.isChecked()) {
            File appstoreDirSeafile = new File(APPSTORE_SEAFILE_PATH);
            if (!appstoreDirSeafile.exists()) {
                appstoreDirSeafile.mkdirs();
            }
            File appstoreSeafile = new File(APPSTORE_PKGNAME_SEAFILE_PATH);
            if (!appstoreSeafile.exists()) {
                try {
                    appstoreSeafile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            exportAppstoreFiles();
        }
    }

    private void exportWallpaperFiles() {
        ChangeBuildPropTools.exec(ROOT_COMMOND + WALLPAPER_PATH);
        ChangeBuildPropTools.exec(ROOT_COMMOND + WALLPAPER_SEAFILE_PATH);
        ChangeBuildPropTools.exec("cp -f " + WALLPAPER_PATH + " " + WALLPAPER_SEAFILE_PATH);
    }

    private void exportStartupmenuFiles() {
        File statusbarDb = new File(STATUSBAR_DB_PATH);
        if (statusbarDb.exists()) {
            ChangeBuildPropTools.exec(ROOT_COMMOND + STATUSBAR_DB_PATH);
            if (FileUtils.copyGeneralFile(STATUSBAR_DB_PATH, STARTUPMENU_SEAFILE_PATH)) {
                if (DEBUG) Log.i(TAG,"statusbar sync to seafile sucessful!");
            } else {
                if (DEBUG) Log.i(TAG,"statusbar sync to seafile fail!");
            }
        }
    }

    private void exportEmailFiles() {
        File emailSharedPrefs = new File(PREFS_PATH);
        if (emailSharedPrefs.exists()) {
            ChangeBuildPropTools.exec(ROOT_COMMOND + PREFS_PATH);
            ChangeBuildPropTools.exec(ROOT_COMMOND + EMAIL_SEAFILE_PATH);
            if (FileUtils.copyGeneralFile(PREFS_PATH, EMAIL_SEAFILE_PATH)) {
                if (DEBUG) Log.i(TAG,"email sync to seafile sucessful!");
            } else {
                if (DEBUG) Log.i(TAG,"email sync to seafile fail!");
            }
        }
    }

    private void exportWifiFiles() {
        ChangeBuildPropTools.exec(ROOT_COMMOND + WIFI_INFO_FILE_CONTENT);
        ChangeBuildPropTools.exec(ROOT_COMMOND + WIFI_INFO_SEAFILE_PATH);
        ChangeBuildPropTools.exec("cp -f " + WIFI_INFO_FILE_CONTENT + " " + WIFI_INFO_SEAFILE_PATH);
    }

    private void exportBrowserFiles() {
        ChangeBuildPropTools.exec(ROOT_COMMOND + BROWSER_INFO_FILE_CONTENT);
        ChangeBuildPropTools.exec(ROOT_COMMOND + BROWSER_INFO_SEAFILE_PATH);
        ChangeBuildPropTools.exec("cp -rf "+BROWSER_INFO_FILE_CONTENT + " "
                                   + BROWSER_INFO_SEAFILE_PATH);
    }

    private void exportAppstoreFiles() {
        List<PackageInfo> pkgInfos = getActivity()
                               .getPackageManager().getInstalledPackages(0);
        try {
            BufferedWriter appWriter = new BufferedWriter(
                                       new FileWriter(APPSTORE_PKGNAME_SEAFILE_PATH));
            for (PackageInfo pkgInfo : pkgInfos) {
                appWriter.write(pkgInfo.packageName);
                appWriter.newLine();
                appWriter.flush();
            }
            appWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
