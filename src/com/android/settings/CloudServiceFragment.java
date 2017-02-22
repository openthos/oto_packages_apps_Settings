package com.android.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

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

    private static final String WALLPAPER_OLD_PATH = "data/system/users/0/wallpaper";
    private static final String WALLPAPER_NEW_PATH = "data/sea/data/sdcard/cloudFolder/wallpaper";

    private static final String EMAIL_FILE_PATH = "data/data/com.android.email";
    private static final String EMAIL_SEAFILE_PATH = "data/sea/data/sdcard/cloudFolder/email";
    private static final String PREFS_PATH = EMAIL_FILE_PATH + "/shared_prefs";
    private static final String PREFS_SEAFILE_PATH = EMAIL_SEAFILE_PATH + "/shared_prefs";


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
                    if (mSwitchWallpaper.isChecked()) {
                        importWallpaperFiles();
                    }
                    if (mSwitchEmail.isChecked()) {
                        importEmailFiles();
                    }
                }
                break;
            case R.id.cloud_export:
                if (!mCloudFolder.exists()) {
                    mCloudFolder.mkdirs();
                }
                if (mSwitchWallpaper.isChecked()) {
                    exportWallpaperFiles();
                }
                if (mSwitchStartupmenu.isChecked()) {
                    exportStartupmenuFiles();
                }
                if (mSwitchEmail.isChecked()) {
                    File emailFile = new File (EMAIL_SEAFILE_PATH);
                    if (!emailFile.exists()) {
                        emailFile.mkdirs();
                    } else {
                        FileUtils.deleteGeneralFile(EMAIL_SEAFILE_PATH);
                        emailFile.mkdirs();
                    }
                    exportEmailFiles();
                }
                break;
        }
    }

    private void importWallpaperFiles() {
        if (mSwitchWallpaper.isChecked()) {
            InputStream wallpaperFile = null;
            try {
                wallpaperFile = new FileInputStream(WALLPAPER_NEW_PATH);
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

    public void importEmailFiles() {
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

    public void exportWallpaperFiles() {
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int byteRead = 0;
            File wallpaperOldFile = new File(WALLPAPER_OLD_PATH);
            if (wallpaperOldFile.exists()) {
                inStream = new FileInputStream(WALLPAPER_OLD_PATH);
                fs = new FileOutputStream(WALLPAPER_NEW_PATH);
                byte[] buffer = new byte[BUF_SIZE];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportStartupmenuFiles() {
        Uri uriQuery = Uri.parse(
               "content://com.android.systemui.util.StatusBarContentProvider/status_bar_tb");
        if (uriQuery != null) {
            Cursor cursor = getActivity().getContentResolver().
                                       query(uriQuery, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                writeFile("data/data/cloudFolder/CloudStartMenu.xml",
                               cursor.getString(cursor.getColumnIndex("pkgname")) + "\n", true);
                cursor.close();
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

    public static boolean writeFile(String fileName, String content, boolean append) {
        FileWriter fileWriter = null;
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                 file.createNewFile();
            }
        } catch (IOException e) {
            return false;
        }
        try {
            fileWriter = new FileWriter(fileName, append);
            fileWriter.write(content);
            fileWriter.flush();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
