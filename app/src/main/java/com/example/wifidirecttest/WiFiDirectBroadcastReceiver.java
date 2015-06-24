package com.example.wifidirecttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by thedi on 25.05.15.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver
{
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                    MainActivity activity)
    {
        super();
        this.manager  = manager;
        this.channel  = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setTextDebug("BR: wifi direct enabled");
            } else {
                activity.setTextDebug("BR: wifi direct disabled");
            }
        } else if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
            if (manager != null) {
                manager.requestPeers(channel, activity.peerListListener);
            }
        } else if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
            if (manager != null) {
                manager.requestConnectionInfo(channel, activity.connectionInfoListener);
            }
        }
    }
}
