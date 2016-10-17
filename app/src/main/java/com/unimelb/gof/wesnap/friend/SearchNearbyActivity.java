package com.unimelb.gof.wesnap.friend;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.chat.ChatStarter;
import com.unimelb.gof.wesnap.models.Chat;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


/**
 * Created by Karl on 7/10/2016.
 */

public class SearchNearbyActivity extends BaseActivity {

    public final static String EXTRA_USERNAME = "text_username";
    private final static String TAG = "SearchNearby";
    private final static String FRIEND_ADDING_MESSAGE = "Please check your added me.";
    private final static int REQUEST_DISCOVERABLE_DURATION = 1000;
    private final static int MESSAGE_READ = 1;

    private boolean isEnableBluetooth = false;
    private String mUsername;
    private String friendUsername;
    LinearLayout ll1, ll2;
    TextView tv1;
    ListView pairedListView, discoverListView;
    BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter<String> pairedAdapter, discoverAdapter;
    ArrayList<BluetoothDevice> pairedDeviceList;
    ArrayList<BluetoothDevice> discoverDeviceList;
    ArrayList<String> pairedDevicesNameList;
    DiscoverItemClicked discoverItemClicked;
    PairedItemClicked pairedItemClicked;
    BluetoothDevice BTDevice;
    AcceptThread mAcceptThread;
    ConnectedThread mConnectedThread;
    ConnectThread mConnectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mUsername = intent.getStringExtra(EXTRA_USERNAME);

        setContentView(R.layout.activity_search_nearby);
        ll1 = (LinearLayout)findViewById(R.id.ll1);
        ll2 = (LinearLayout)findViewById(R.id.ll2);
        tv1 = (TextView)findViewById(R.id.tv_tile_connected);
        pairedListView = (ListView)findViewById(R.id.lv_paired_devices);
        discoverListView = (ListView) findViewById(R.id.lv_discover_devices);
        pairedItemClicked = new PairedItemClicked();
        discoverItemClicked = new DiscoverItemClicked();
        pairedDeviceList = new ArrayList<BluetoothDevice>();
        discoverDeviceList = new ArrayList<BluetoothDevice>();
        pairedDevicesNameList = new ArrayList<String>();
        pairedAdapter= new ArrayAdapter<String>(
                SearchNearbyActivity.this,
                android.R.layout.simple_list_item_1,
                pairedDevicesNameList
                );
        discoverAdapter= new ArrayAdapter<String>(
                SearchNearbyActivity.this,
                android.R.layout.simple_list_item_single_choice
                );
        pairedListView.setAdapter(pairedAdapter);
        discoverListView.setAdapter(discoverAdapter);
        discoverAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        discoverListView.setOnItemClickListener(discoverItemClicked);
        pairedListView.setOnItemClickListener(pairedItemClicked);
        setBluetooth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == REQUEST_DISCOVERABLE_DURATION) {
            Log.d(TAG, "Bluetooth is enable");
            getPairedDevices();
            startSearching();
            isEnableBluetooth = true;
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        else {
            Log.e(TAG, "Bluetooth is not enable");
            Toast.makeText(SearchNearbyActivity.this,
                    "Error: Bluetooth is not enable.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setBluetooth(){

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){
            // null value, error out
            Log.e(TAG, "Device does not support bluetooth");
            Toast.makeText(SearchNearbyActivity.this,
                    "Error: Device does not support bluetooth.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!mBluetoothAdapter.isEnabled()){
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                    REQUEST_DISCOVERABLE_DURATION);
            startActivityForResult(discoverableIntent, 1);
        }

        else {
            Log.d(TAG, "Bluetooth is already enable.");
            getPairedDevices();
            startSearching();
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    private synchronized void getPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size()>0){
            for (BluetoothDevice device: pairedDevices){
                pairedDevicesNameList.add(device.getName());
                pairedDeviceList.add(device);
            }
        }
        pairedAdapter.notifyDataSetChanged();
    }

    class PairedItemClicked implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mAcceptThread.cancel();
            BTDevice = pairedDeviceList.get(position);
            connect(BTDevice);
        }
    }

    class DiscoverItemClicked implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
            BTDevice = discoverDeviceList.get(position);
            Log.i("Log", "The device : " + BTDevice.toString());
            connect(BTDevice);
        }
    }


    private void connect(BluetoothDevice BTDevice){
        Log.d(TAG, "connecting...");
        ll1.setVisibility(View.GONE);
        ll2.setVisibility(View.GONE);
        tv1.setVisibility(View.VISIBLE);
        mConnectThread = new ConnectThread(BTDevice);
        mConnectThread.start();
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "on recieve");
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // this checks if the size of bluetooth device is 0,then add the
                // device to the arraylist.
                if(discoverDeviceList.size()<1){
                    Log.d(TAG, "device adding.");
                    if (device.getName() != null){
                        discoverAdapter.add(device.getName());
                        discoverDeviceList.add(device);
                        discoverAdapter.notifyDataSetChanged();
                        Log.d(TAG, "device added");
                    }
                }
                else {
                    boolean flag = true;    // flag to indicate that particular device is already in the arlist or not
                    for(int i = 0; i<discoverDeviceList.size();i++) {
                        if(device.getAddress().equals(discoverDeviceList.get(i).getAddress())) {
                            flag = false;
                        }
                    }
                    if(flag) {
                        if (device.getName() != null){
                            discoverAdapter.add(device.getName());
                            discoverDeviceList.add(device);
                            discoverAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
            Log.d(TAG, "end onrecieve");
        }
    };

    private void startSearching() {
        Log.i("Log", "in the start searching method");
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        SearchNearbyActivity.this.registerReceiver(myReceiver, intentFilter);
        mBluetoothAdapter.startDiscovery();
    }

    private void manageConnectedSocket(BluetoothSocket socket){
        Log.i("Log", "in the manageConnectedSocket method");
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isEnableBluetooth){
            unregisterReceiver(myReceiver);
            pairedDevicesNameList.clear();
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    Log.d("friend username:", "main thread recieve");
                    byte[] readBuf = (byte[])msg.obj;
                    friendUsername = new String (readBuf, 0, msg.arg1);
                    addFriend();
                    break;
                default:
                    break;
            }
        }
    };

    private void addFriend(){
        Log.d(TAG, "adding friends");
        Log.d("my username", mUsername);
        Log.d("friend username", friendUsername);
        FirebaseUtil.getUsernamesRef().child(friendUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = (String) dataSnapshot.getValue();
                FriendHandler.sendFriendRequestNoFeedback(uid);
                ll1.setVisibility(View.GONE);
                ll2.setVisibility(View.GONE);
                tv1.setVisibility(View.VISIBLE);
                tv1.setText(FRIEND_ADDING_MESSAGE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        AppParams.APP_NAME,
                        UUID.fromString(AppParams.MY_UUID)
                        );
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "ACCEPTING");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);

                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(AppParams.MY_UUID));
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "CONNECTING");
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d(TAG, "CONNECTED MANAGEMENT");
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            write(mUsername.getBytes());
            Log.d(TAG, "write and send mUsername");
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    Log.d(TAG, "listening and reading");
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    } catch (IOException e) {
                        break;
                    }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return true;
    }
}
