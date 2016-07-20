package com.android.settings;

import java.io.File;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 *     name:system reset
 *     author:wang zhixu
 *     date:2016-07-20
 */
public class SystemResetSetting extends Fragment implements OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.system_reset_setting, container, false);
        LinearLayout systemReset = (LinearLayout) (mRootView.findViewById(R.id.system_reset));
        systemReset.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.system_reset:
            showDialog();
            break;
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder=new Builder(getActivity());
        builder.setMessage(getActivity().getResources().
                           getString(R.string.system_reset_dialog_info));
        builder.setPositiveButton(getActivity().
                                  getResources().getString(R.string.system_reset_dialog_continue),
                                  new android.content.DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int which) {
                                          try {
                                              File doc=new File("/data");
                                              File file =new File(doc, "rec_reset");
                                              file.createNewFile();
                                              reset();
                                          } catch (Exception e) {
                                              // TODO: handle exception
                                          }
                                      }
                                  });
        builder.setNegativeButton(getActivity().
                                  getResources().getString(R.string.system_reset_dialog_cancel),
                                  new android.content.DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int which) {
                                          dialog.cancel();
                                      }
                                  });
        builder.show();
    }

    private void reset() {
        //Intent iReboot = new Intent(Intent.ACTION_REBOOT);
        //iReboot.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //getActivity().startActivity(iReboot);
        PowerManager pm = (PowerManager)getActivity().getApplicationContext().
                                                 getSystemService(Context.POWER_SERVICE);
        pm.reboot(null);
    }
}
