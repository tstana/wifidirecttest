package com.example.wifidirecttest;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity
{
    private TextView textDebug;
    private TextView textCount;
    private int count;

    private static WifiP2pManager manager;
    private static WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private static List peers = new ArrayList();
    private IntentFilter intentFilter;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Set up the debug text view */
        textDebug = (TextView) findViewById(R.id.text_debug);
        textDebug.setMovementMethod(new ScrollingMovementMethod());

        textCount = (TextView) findViewById(R.id.text_count);
        this.count = 0;

        /* Set up the intent filter for the BroadcastReceiver */
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        /* Create service intent object */
        serviceIntent = new Intent(getApplicationContext(), PeerConnectIntentService.class);

        /* Initialize WiFiP2pManager and start peer discovery */
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                /**
                 *  onSuccess() only tells us that peer discovery was successfully _initiated_.
                 *  WiFiDirectBroadcastReceiver will get the list of peers with requestPeers().
                 *  Peers will then be returned to this activity to peerListListener below.
                 */
            }

            @Override
            public void onFailure(int reason)
            {
                switch (reason) {
                    case WifiP2pManager.BUSY:
                        setTextDebug("Failed to initiate device connection, P2P framework busy");
                        break;
                    case WifiP2pManager.ERROR:
                        setTextDebug("Failed to initiate device connection, P2P framework error");
                        break;
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        setTextDebug("Failed to initiate device connection, P2P unsupported");
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener()
    {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList)
        {
            peers.clear();
            peers.addAll(peerList.getDeviceList());

            if (peers.size() == 0) {
                setTextDebug("No peers discovered!");
            } else {
                setTextCount();
                serviceIntent.setData(null);
                getApplicationContext().startService(serviceIntent);
            }
        }
    };

    public static class PeerConnectIntentService extends IntentService
    {
        public PeerConnectIntentService()
        {
            super("PeerConnectIntentService");
        }

        @Override
        protected void onHandleIntent(Intent workIntent)
        {
            WifiP2pDevice device = (WifiP2pDevice) peers.get(0);
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess()
                {
                        /* The BroadcastReceiver will inform us on the connection state */
                }

                @Override
                public void onFailure(int reason)
                {
                    switch (reason) {
                        case WifiP2pManager.BUSY:
//                            setTextDebug("Failed to initiate device connection, P2P framework busy");
                            break;
                        case WifiP2pManager.ERROR:
//                            setTextDebug("Failed to initiate device connection, P2P framework error");
                            break;
                        case WifiP2pManager.P2P_UNSUPPORTED:
//                            setTextDebug("Failed to initiate device connection, P2P unsupported");
                            break;
                    }
                }
            });
        }
    }

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info)
        {
            if (info.groupFormed) {
                if (info.isGroupOwner) {
                    setTextDebug("We're group owner!");
                    setTextDebug("owner IP: " + info.groupOwnerAddress.getHostAddress());
                    new ServerTask(textDebug).execute();
                } else {
                    setTextDebug("Group formed, but we're not the owner.");
                    setTextDebug("owner IP: " + info.groupOwnerAddress.getHostAddress());
                    new ClientTask(textDebug, info.groupOwnerAddress).execute();
                }
            }
        }
    };


    public void setTextDebug(String text)
    {
        String tmp = String.valueOf(textDebug.getText());
        tmp += text;
        tmp += "\n";
        textDebug.setText(tmp);
    }

    public void setTextCount()
    {
        count++;
        textCount.setText(String.valueOf(count));
    }
}