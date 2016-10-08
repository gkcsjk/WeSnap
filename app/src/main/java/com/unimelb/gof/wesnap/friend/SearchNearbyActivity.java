package com.unimelb.gof.wesnap.friend;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Karl on 7/10/2016.
 */

public class SearchNearbyActivity extends BaseActivity {

    public final static String EXTRA_USERNAME = "text_username";
    private final static String TAG = "SearchNearby";
    private final static int REQUEST_DISCOVERABLE_DURATION = 300;
    private String mUsername;
    ListView pairedListView, discoverListView;
    BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter<String> pairedAdapter, discoverAdapter;
    ArrayList<BluetoothDevice> pairedDeviceList;
    ArrayList<String> pairedDevicesNameList;
    Set<BluetoothDevice> pairedDevices;
    DiscoverItemClicked discoverItemClicked;
    PairedItemClicked pairedItemClicked;
    BluetoothDevice BTDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setupBluetooth();
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(EXTRA_USERNAME);

        setContentView(R.layout.activity_search_nearby);
        pairedListView = (ListView)findViewById(R.id.lv_paired_devices);
        discoverListView = (ListView) findViewById(R.id.lv_discover_devices);
        pairedAdapter= new ArrayAdapter<String>(
                SearchNearbyActivity.this,
                R.layout.item_device,
                pairedDevicesNameList
                );
        discoverAdapter= new ArrayAdapter<String>(
                SearchNearbyActivity.this,
                R.layout.item_device
                );
        pairedListView.setAdapter(pairedAdapter);
        discoverListView.setAdapter(discoverAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        getPairedDevices();
        discoverListView.setOnItemClickListener(discoverItemClicked);
        pairedListView.setOnItemClickListener(pairedItemClicked);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DISCOVERABLE_DURATION && resultCode == RESULT_OK) {
            Log.d(TAG, "Bluetooth is enable");
        }
        else {
            Log.e(TAG, "Bluetooth is not enable");
            Toast.makeText(SearchNearbyActivity.this,
                    "Error: Bluetooth is not enable.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupBluetooth(){
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
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void getPairedDevices(){
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size()>0){
            for (BluetoothDevice device: pairedDevices){
                pairedDevicesNameList.add(device.getName());
                pairedDeviceList.add(device);
            }
        }
        pairedAdapter.notifyDataSetChanged();
    }

    class PairedItemClicked implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            BTDevice = .get(position);
            //bdClass = arrayListBluetoothDevices.get(position);
            Log.i("Log", "The dvice : "+bdDevice.toString());
            /*
             * here below we can do pairing without calling the callthread(), we can directly call the
             * connect(). but for the safer side we must usethe threading object.
             */
            //callThread();
            //connect(bdDevice);
            Boolean isBonded = false;
            try {
                isBonded = createBond(bdDevice);
                if(isBonded)
                {
                    //arrayListpaired.add(bdDevice.getName()+"\n"+bdDevice.getAddress());
                    //adapter.notifyDataSetChanged();
                    getPairedDevices();
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }//connect(bdDevice);
            Log.i("Log", "The bond is created: "+isBonded);
        }
    }
    class DiscoverItemClicked implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
            bdDevice = arrayListPairedBluetoothDevices.get(position);
            try {
                Boolean removeBonding = removeBond(bdDevice);
                if(removeBonding)
                {
                    arrayListpaired.remove(position);
                    adapter.notifyDataSetChanged();
                }


                Log.i("Log", "Removed"+removeBonding);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }



}
