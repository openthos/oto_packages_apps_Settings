package com.android.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.applications.*;
import android.widget.Switch;
import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import android.widget.CompoundButton;
import com.android.settings.widget.SwitchBar;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.util.Log;
import android.widget.Toast;

public class CloudServiceFragment extends Fragment implements OnCheckedChangeListener {
    private Switch mswitch_wallpaper;
    private Switch mswitch_wifi;
    private Switch mswitch_email;
    //private Switch mswitch_photo;
    private Switch mswitch_appdata;
    private Switch mswitch_appstore;
    private Switch mswitch_browser;
    private Switch mswitch_startmenu;
    //private Switch mswitch_file;

    private Button mbutton_import;
    private Button mbutton_export;

    private String oldPath;
    private String newPath;

    private Intent intent;
    private cloudReceiver mCloudReceiver;
    public static final String BROADCAST_ACTION = "sendBroadcast";
    public static final String BROADCAST_WALLPAPER = "sendWallpaperBroadcast";
    public static final String BROADCAST_WIFI = "sendWifiBroadcast";
    public static final String BROADCAST_EMAIL = "sendEmailBroadcast";
    //public static final String BROADCAST_PHOTO = "sendPhotoBroadcasT";
    public static final String BROADCAST_STARTMENU = "sendStartmenuBroadcast";
    public static final String BROADCAST_BROWSER = "sendbrowserBroadcast";
    public static final String BROADCAST_APPSTORE= "sendAppstoreBroadcast";
    public static final String BROADCAST_APPDATA = "sendAppdataBroadcast";
    //public static final String BROADCAST_FILE = "sendFileBroadcast";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View mRootView = inflater.inflate(R.layout.fragment_cloud_service, container, false);
        initView(mRootView);

        mCloudReceiver = new cloudReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_WALLPAPER);
        intentFilter.addAction(BROADCAST_WIFI);
        intentFilter.addAction(BROADCAST_STARTMENU);
        getActivity().registerReceiver(mCloudReceiver, intentFilter);

        File dirFolder = new File("data/data/cloudFolder");
        if (!dirFolder.exists()) {
            dirFolder.mkdirs();
        }
        //File fileWallpaperName = new File("data/data/cloudFolder/CloudWallpaper.xml");
        File fileWifiName = new File("data/data/cloudFolder/CloudWifi.xml");
        File fileEmailName = new File("data/data/cloudFolder/CloudEmail.xml");
        File fileAppdataName = new File("data/data/cloudFolder/CloudAppdata.xml");
        //File filePictureName = new File("data/data/cloudFolder/CloudPicture.xml");
        File fileStartmenuName = new File("data/data/cloudFolder/CloudStartMenu.xml");
        File fileAppStoreName = new File("data/data/cloudFolder/CloudAppStore.xml");
        File fileBrowserName = new File("data/data/cloudFolder/CloudBrowser.xml");
        //File fileFileName = new File("data/data/cloudFolder/CloudFile.xml");

        try {
            //if (!fileWallpaperName.exists()) {
            //    fileWallpaperName.createNewFile();
            //}
            if (!fileWifiName.exists()) {
                fileWifiName.createNewFile();
            }
            if (!fileEmailName.exists()) {
                fileEmailName.createNewFile();
            }
            if (!fileAppdataName.exists()) {
                fileAppdataName.createNewFile();
            }
            //if (!filePictureName.exists()) {
            //    filePictureName.createNewFile();
            //}
            if (!fileStartmenuName.exists()) {
                fileStartmenuName.createNewFile();
            }
            if (!fileAppStoreName.exists()) {
                fileAppStoreName.createNewFile();
            }
            if (!fileBrowserName.exists()) {
                fileBrowserName.createNewFile();
            }
        } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }
        return mRootView;
    }

    private void initView(View v) {
        mswitch_wallpaper = (Switch) v.findViewById(R.id.switch_wallpaper);
        mswitch_wifi = (Switch) v.findViewById(R.id.switch_wifi);
        mswitch_email = (Switch) v.findViewById(R.id.switch_email);
        //mswitch_photo = (Switch) v.findViewById(R.id.switch_photo);
        mswitch_appdata = (Switch) v.findViewById(R.id.switch_appdata);
        mswitch_appstore = (Switch) v.findViewById(R.id.switch_appstore);
        mswitch_browser = (Switch) v.findViewById(R.id.switch_browser);
        mswitch_startmenu = (Switch) v.findViewById(R.id.switch_startmenu);
        //mswitch_file = (Switch) v.findViewById(R.id.switch_file);

        mbutton_import = (Button) v.findViewById(R.id.cloud_import);
        mbutton_export = (Button) v.findViewById(R.id.cloud_export);

        mswitch_wallpaper.setOnCheckedChangeListener(this);
        mswitch_wifi.setOnCheckedChangeListener(this);
        mswitch_email.setOnCheckedChangeListener(this);
        //mswitch_photo.setOnCheckedChangeListener(this);
        mswitch_appdata.setOnCheckedChangeListener(this);
        mswitch_appstore.setOnCheckedChangeListener(this);
        mswitch_browser.setOnCheckedChangeListener(this);
        mswitch_startmenu.setOnCheckedChangeListener(this);
        //mswitch_file.setOnCheckedChangeListener(this);

        //mbutton_import.setOnClickListener(this);
        //mbutton_export.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
        // TODO Auto-generated method stub
        intent = new Intent();
        intent.setAction(BROADCAST_ACTION);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        // TODO Auto-generated method stub
        switch (buttonView.getId()) {
            case R.id.switch_wallpaper:
                intent.setAction(BROADCAST_WALLPAPER);
                break;
            case R.id.switch_wifi:
                intent.setAction(BROADCAST_WIFI);
                break;
            case R.id.switch_email:
                intent.setAction(BROADCAST_EMAIL);
                break;
            //case R.id.switch_photo:
                //intent.setAction(BROADCAST_PHOTO);
                //break;
            case R.id.switch_appdata:
                intent.setAction(BROADCAST_APPDATA);
                break;
            case R.id.switch_appstore:
                intent.setAction(BROADCAST_APPSTORE);
                break;
            case R.id.switch_browser:
                intent.setAction(BROADCAST_BROWSER);
                break;
            case R.id.switch_startmenu:
                intent.setAction(BROADCAST_STARTMENU);
                break;
            //case R.id.switch_file:
                //intent.setAction(BROADCAST_FILE);
                //break;
        }
        getActivity().sendBroadcast(intent);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
    }

    public static boolean copyFile(String oldPath, String newPath) {
        boolean iscopy = false;
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                inStream = new FileInputStream(oldPath);
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                iscopy = true;
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
        return iscopy;
    }

    public class cloudReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BROADCAST_WALLPAPER:
                    oldPath = "data/system/users/0/wallpaper";
                    newPath = "data/data/cloudFolder/wallpaper";
                    copyFile (oldPath,newPath);
                    break;
            }
        }
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(mCloudReceiver);
        super.onDestroy();
    }
}
