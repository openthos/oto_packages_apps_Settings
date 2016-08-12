package com.android.settings.applications;

import com.android.settings.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Switch;

// View Holder used when displaying views
public class AppViewHolder {
    public ApplicationsState.AppEntry entry;
    public View rootView;
    public TextView appName;
    public ImageView appIcon, appSwitch;
    public TextView appSize;
    public TextView disabled;
    public CheckBox checkBox;
    public Switch inNetLicenseSwitch;
    public Switch outNetLicenseSwitch;
    public RadioButton buttonAuto;
    public RadioButton buttonPhone;
    public RadioButton buttonDesktop;

    static public AppViewHolder createOrRecycle(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.manage_applications_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            AppViewHolder holder = new AppViewHolder();
            holder.rootView = convertView;
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appSize = (TextView) convertView.findViewById(R.id.app_size);
            holder.disabled = (TextView) convertView.findViewById(R.id.app_disabled);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.app_on_sdcard);
            convertView.setTag(holder);
            return holder;
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            return (AppViewHolder)convertView.getTag();
        }
    }

    public static AppViewHolder autoStartHolder(LayoutInflater inflater,View convertView){
         if (convertView == null) {
            convertView = inflater.inflate(R.layout.autostart_applications_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            AppViewHolder holder = new AppViewHolder();
            holder.rootView = convertView;
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appSize = (TextView) convertView.findViewById(R.id.app_size);
            holder.disabled = (TextView) convertView.findViewById(R.id.app_disabled);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.app_on_sdcard);
            holder.appSwitch = (ImageView) convertView.findViewById(R.id.app_switch);
            convertView.setTag(holder);
            return holder;
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            return (AppViewHolder)convertView.getTag();
        }
    }

    public static AppViewHolder runModeHolder(LayoutInflater inflater,View convertView) {
         if (convertView == null) {
            convertView = inflater.inflate(R.layout.runmode_applications_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            AppViewHolder holder = new AppViewHolder();
            holder.rootView = convertView;
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appSize = (TextView) convertView.findViewById(R.id.app_size);
            holder.disabled = (TextView) convertView.findViewById(R.id.app_disabled);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.app_on_sdcard);
            holder.buttonAuto = (RadioButton) convertView.findViewById(R.id.btn_auto);
            holder.buttonPhone = (RadioButton) convertView.findViewById(R.id.btn_phone);
            holder.buttonDesktop = (RadioButton) convertView.findViewById(R.id.btn_desktop);
            convertView.setTag(holder);
            return holder;
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            return (AppViewHolder)convertView.getTag();
        }

    }

    static public AppViewHolder fireWallHolder(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.firewall_applications_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            AppViewHolder holder = new AppViewHolder();
            holder.rootView = convertView;
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appSize = (TextView) convertView.findViewById(R.id.app_size);
            holder.disabled = (TextView) convertView.findViewById(R.id.app_disabled);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.app_on_sdcard);
            holder.inNetLicenseSwitch = (Switch) convertView.findViewById(R.id.app_in_net_license);
            holder.outNetLicenseSwitch = (Switch) convertView.findViewById(
                                                                  R.id.app_out_net_license);
            convertView.setTag(holder);
            return holder;
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            return (AppViewHolder)convertView.getTag();
        }
    }

    void updateSizeText(CharSequence invalidSizeStr, int whichSize) {
        if (ManageApplications.DEBUG) Log.i(ManageApplications.TAG, "updateSizeText of " + entry.label + " " + entry
                + ": " + entry.sizeStr);
        if (entry.sizeStr != null) {
            switch (whichSize) {
                case ManageApplications.SIZE_INTERNAL:
                    appSize.setText(entry.internalSizeStr);
                    break;
                case ManageApplications.SIZE_EXTERNAL:
                    appSize.setText(entry.externalSizeStr);
                    break;
                default:
                    appSize.setText(entry.sizeStr);
                    break;
            }
        } else if (entry.size == ApplicationsState.SIZE_INVALID) {
            appSize.setText(invalidSizeStr);
        }
    }
}
