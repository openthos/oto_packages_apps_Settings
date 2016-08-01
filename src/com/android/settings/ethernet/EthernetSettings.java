/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * developed by hclydao@gmail.com
 */

package com.android.settings.ethernet;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.preference.CheckBoxPreference;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Iterator;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;import android.net.LinkAddress;
import com.android.settings.widget.SwitchBar;
import com.android.settings.SettingsActivity;
import android.widget.Switch;
import android.app.Activity;
import android.app.ActivityManager;
import android.net.EthernetManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkProperties;
import android.net.StaticIpConfiguration;
import android.text.TextUtils;
import java.net.Inet4Address;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.provider.Settings;
import android.util.Slog;
import android.widget.Toast;
import android.os.Looper;

public class EthernetSettings extends SettingsPreferenceFragment {
    //private static final String TAG = "EthernetSettings";
    private final String TAG = "EthConfDialog";
    private EthernetEnabler mEthEnabler;
    //private static final String KEY_CONF_ETH = "ETHERNET_CONFIG";
    private EthernetDialog mEthDialog = null;
    //private Preference mEthConfigPref;
    private ConnectivityManager mCM;
    //add new by wanglifeng
    private RadioButton mDhcpButton,mStaticIpButton;
    private EditText mIpAddress,mNetWork,mDnsAddress;
    private TextView mDiscard,mSave;
    private StaticIpConfiguration mStaticIpConfiguration = null;
    private EthernetManager mEthManager = null;
    private IpAssignment mIpAssignment = IpAssignment.DHCP;
    private Context mContext;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ethernet_settings,container,false);
        view = rootView.findViewById(R.id.ethernet_linearl);
        mDhcpButton = (RadioButton) rootView.findViewById(R.id.btn_dhcp);
        mStaticIpButton = (RadioButton) rootView.findViewById(R.id.btn_staticip);
        mIpAddress = (EditText) rootView.findViewById(R.id.ed_ipaddress);
        mNetWork = (EditText) rootView.findViewById(R.id.ed_network);
        mDnsAddress = (EditText) rootView.findViewById(R.id.ed_dns_address);
        mDiscard = (TextView) rootView.findViewById(R.id.tv_discard);
        mSave = (TextView) rootView.findViewById(R.id.tv_save);
        mDhcpButton.setChecked(true);
        mStaticIpButton.setChecked(false);
        mIpAddress.setEnabled(false);
        mNetWork.setEnabled(false);
        mDnsAddress.setEnabled(false);
        mCM = (ConnectivityManager)getActivity().getSystemService( Context.CONNECTIVITY_SERVICE);
        mEthManager = (EthernetManager)getActivity().getSystemService(Context.ETHERNET_SERVICE);
        mContext = getActivity();
        buildDialogContent();
        return rootView;
    }

    public int buildDialogContent(){
        mStaticIpButton.setOnClickListener(new RadioButton.OnClickListener() {
            public void onClick(View v) {
                mIpAddress.setEnabled(true);
                mNetWork.setEnabled(true);
                //mGw.setEnabled(true);
                //mMask.setEnabled(true);
                mDnsAddress.setEnabled(true);
                mIpAssignment = IpAssignment.STATIC;
                if(TextUtils.isEmpty(mIpAddress.getText().toString()))
                    mIpAddress.setText("192.168.1.15");
                if(TextUtils.isEmpty(mDnsAddress.getText().toString()))
                    mDnsAddress.setText("192.168.1.1");
                //if(TextUtils.isEmpty(mGw.getText().toString()))
                    //mGw.setText("192.168.1.1");
                if(TextUtils.isEmpty(mNetWork.getText().toString()))
                    mNetWork.setText("24");
            }
        });

        mDhcpButton.setOnClickListener(new RadioButton.OnClickListener() {
            public void onClick(View v) {
                mIpAddress.setEnabled(false);
                mDnsAddress.setEnabled(false);
                //mGw.setEnabled(false);
                //mMask.setEnabled(false);
                mNetWork.setEnabled(false);
                mIpAssignment = IpAssignment.DHCP;
                mDnsAddress.setText("");
                //mGw.setText("");
                mNetWork.setText("");
                mIpAddress.setText("");
            }
        });

        mDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handle_saveconf();
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        UpdateInfo();
        return 0;
    }

    public void UpdateInfo() {
        int enable = Settings.Global.getInt(mContext.getContentResolver(),
                                            Settings.Global.ETHERNET_ON,0);
        if(enable == EthernetManager.ETH_STATE_ENABLED) {
            //if(mEthManager.isAvailable()) {
            IpConfiguration ipinfo = mEthManager.getConfiguration();
            if(ipinfo != null) {
                if(ipinfo.ipAssignment == IpAssignment.DHCP) {
                    mDhcpButton.setChecked(true);
                    mIpAddress.setEnabled(false);
                    mDnsAddress.setEnabled(false);
                    //mGw.setEnabled(false);
                    //mMask.setEnabled(true);
                    mNetWork.setEnabled(false);
                    mDnsAddress.setText("");
                    //mGw.setText("");
                    mNetWork.setText("");
                    mIpAddress.setText("");
                    if(mCM != null) {
                        LinkProperties lp  = mCM.getLinkProperties(
                                                     ConnectivityManager.TYPE_ETHERNET);
                        if(lp != null) {
                            mIpAddress.setText(formatIpAddresses(lp));
                        }
                    }
                } else {
                    mStaticIpButton.setChecked(true);
                    mIpAddress.setEnabled(true);
                    mDnsAddress.setEnabled(true);
                    //mGw.setEnabled(true);
                    //mMask.setEnabled(true);
                    mNetWork.setEnabled(true);
                    StaticIpConfiguration staticConfig = ipinfo.getStaticIpConfiguration();
                    if (staticConfig != null) {
                        if (staticConfig.ipAddress != null) {
                            mIpAddress.setText(
                                         staticConfig.ipAddress.getAddress().getHostAddress());
                            mNetWork.setText(Integer.toString(
                                                 staticConfig.ipAddress.getNetworkPrefixLength()));
                        }
                        if (staticConfig.gateway != null) {
                            //mGw.setText(staticConfig.gateway.getHostAddress());
                        }
                        Iterator<InetAddress> dnsIterator = staticConfig.dnsServers.iterator();
                        if (dnsIterator.hasNext()) {
                            mDnsAddress.setText(dnsIterator.next().getHostAddress());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // On/off switch is hidden for Setup Wizard (returns null)
        mEthEnabler = createEthernetEnabler();
    }

    @Override
    public void onResume() {
        final Activity activity = getActivity();
        super.onResume();
        if (mEthEnabler != null) {
            mEthEnabler.resume(activity);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mEthEnabler != null) {
            mEthEnabler.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mEthEnabler != null) {
            mEthEnabler.teardownSwitchBar();
        }
    }

    /**
     * @return new WifiEnabler or null (as overridden by WifiSettingsForSetupWizard)
     */
    /* package */
    EthernetEnabler createEthernetEnabler() {
        final SettingsActivity activity = (SettingsActivity) getActivity();
        return new EthernetEnabler(activity, activity.getSwitchBar(),
                                   (EthernetManager)getSystemService(Context.ETHERNET_SERVICE), view);
    }

    private String formatIpAddresses(LinkProperties prop) {
        if (prop == null) return null;
        Iterator<InetAddress> iter = prop.getAllAddresses().iterator();
        // If there are no entries, return null
        if (!iter.hasNext()) return null;
        // Concatenate all available addresses, comma separated
        String addresses = "";
        while (iter.hasNext()) {
            addresses += iter.next().getHostAddress();
            if (iter.hasNext()) addresses += "\n";
        }
        return addresses;
    }

    private void handle_saveconf() {
        if (mDhcpButton.isChecked()) {
            Slog.i(TAG,"mode dhcp");
            mEthManager.setConfiguration(new IpConfiguration(mIpAssignment, ProxySettings.NONE,
                    null, null));
        } else {
            Slog.i(TAG,"mode static ip");
            if(isIpAddress(mIpAddress.getText().toString())
                    && isIpAddress(mDnsAddress.getText().toString())) {

                if(TextUtils.isEmpty(mIpAddress.getText().toString())
                        || TextUtils.isEmpty(mNetWork.getText().toString())
                        || TextUtils.isEmpty(mDnsAddress.getText().toString())) {
                    Toast.makeText(mContext, R.string.eth_settings_empty, Toast.LENGTH_LONG).show();
                    return ;
                }

                mStaticIpConfiguration = new StaticIpConfiguration();
                int result = validateIpConfigFields(mStaticIpConfiguration);
                if (result != 0) {
                    Toast.makeText(mContext, " error id is " + result, Toast.LENGTH_LONG).show();
                    return ;
                } else {
                    mEthManager.setConfiguration( new IpConfiguration(mIpAssignment, ProxySettings.NONE,
                            mStaticIpConfiguration, null));
                }
            } else {
                Toast.makeText(mContext, R.string.eth_settings_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    private int validateIpConfigFields(StaticIpConfiguration staticIpConfiguration) {

        String ipAddr = mIpAddress.getText().toString();

        Inet4Address inetAddr = getIPv4Address(ipAddr);
        if (inetAddr == null) {
            return 2;
        }
        /*
        String netmask = mMask.getText().toString();
        if (TextUtils.isEmpty(netmask))
            return 11;
        Inet4Address netmas = getIPv4Address(netmask);
        if (netmas == null) {
            return 12;
        }
        int nmask = NetworkUtils.inetAddressToInt(netmas);
        int prefixlength = NetworkUtils.netmaskIntToPrefixLength(nmask);
        */
        int networkPrefixLength = -1;
        try {
            networkPrefixLength = Integer.parseInt(mNetWork.getText().toString());
            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                return 3;
            }
            staticIpConfiguration.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);
        } catch (NumberFormatException e) {
            // Set the hint as default after user types in ip address
        }

        /* String gateway = mGw.getText().toString();

        InetAddress gatewayAddr = getIPv4Address(gateway);
        if (gatewayAddr == null) {
            return 4;
        }
        staticIpConfiguration.gateway = gatewayAddr;*/

        String dns = mDnsAddress.getText().toString();
        InetAddress dnsAddr = null;

        dnsAddr = getIPv4Address(dns);
        if (dnsAddr == null) {
            return 5;
        }

        staticIpConfiguration.dnsServers.add(dnsAddr);

        return 0;
    }

    private Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException|ClassCastException e) {
            return null;
        }
    }

    private boolean isIpAddress(String value) {
        int start = 0;
        int end = value.indexOf('.');
        int numBlocks = 0;

        while (start < value.length()) {
            if (end == -1) {
                end = value.length();
            }

            try {
                int block = Integer.parseInt(value.substring(start, end));
                if ((block > 255) || (block < 0)) {
                        return false;
                }
            } catch (NumberFormatException e) {
                    return false;
            }

            numBlocks++;

            start = end + 1;
            end = value.indexOf('.', start);
        }
        return numBlocks == 4;
    }
}
