package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by zised on 2016-06-29.
 */
public class MBatteryReceiver extends BroadcastReceiver {
    private static final String TAG = "MBatteryReceiver";
    public static final Integer mTimes[] = {15000, 30000, 60000, 120000, 300000, 600000, 1800000};
    SharedPreferences mSharedPreferences;
    @Override
    public void onReceive(Context context, Intent intent) {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING)
                             || (status == BatteryManager.BATTERY_STATUS_FULL);
        mSharedPreferences = context.getSharedPreferences("charge", Context.MODE_PRIVATE);
        if (isCharging) {
            int index = mSharedPreferences.getInt("charging",0);
            Settings.System.putInt(context.getContentResolver(),
                                   android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
                                   mTimes[index]);
        } else {
            int no_index = mSharedPreferences.getInt("nocharging",0);
            Settings.System.putInt(context.getContentResolver(),
                                   android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
                                   mTimes[no_index]);
        }
    }
}
