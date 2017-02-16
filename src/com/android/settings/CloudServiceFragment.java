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

    private String mWallpaperOldPath = "data/system/users/0/wallpaper";
    private String mWallpaperNewPath = "data/sea/data/sdcard/cloudFolder/wallpaper";

    private File mCloudFolder;

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
                importWallpaperFiles();
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
                break;
        }
    }

    private void importWallpaperFiles() {
        if (mSwitchWallpaper.isChecked()) {
            InputStream wallpaperFile = null;
            if (mCloudFolder.exists()){
                try {
                    wallpaperFile = new FileInputStream(mWallpaperNewPath);
                    if (wallpaperFile != null) {
                        WallpaperManager manager =
                                         WallpaperManager.getInstance(getActivity());
                        manager.setStream(wallpaperFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (wallpaperFile !=null) {
                            wallpaperFile.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void exportWallpaperFiles() {
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int byteRead = 0;
            File wallpaperOldFile = new File(mWallpaperOldPath);
            if (wallpaperOldFile.exists()) {
                inStream = new FileInputStream(mWallpaperOldPath);
                fs = new FileOutputStream(mWallpaperNewPath);
                byte[] buffer = new byte[1024];
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
