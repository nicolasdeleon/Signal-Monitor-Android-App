package com.example.testing_bt_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";

    private final int INVALID = 255;

    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;

    GraphInterface heartGraph, oxyGraph, tempGraph;

    BluetoothConnectionService mBluetoothConnection;

    Button btnStartConnection;
    Button btnSend;

    EditText editText;

    TextView O2Text, HRText;

    private boolean plotData = false;
    private Thread plotThread;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvnewDevices;

    // ---------------- START BROADCAST RECEIVERS DEFINITION ---------------------

    // Create a BroadcastReceiver object from class BroadcastReceiver
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        // This drives a class from BroadcastReceiver where onReceive method must be implemented if not declared abstract
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }

    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        // This drives a class from BroadcastReceiver where onReceive method must be implemented if not declared abstract
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch(mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled. Able to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Unable to receive connections");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected");
                        break;
                }
            }
        }

    };

    /*
    * Broadcast Reciever for listing devices that are not yet paired
    * - Executed by btnDiscovery() method.
    * */
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvnewDevices.setAdapter(mDeviceListAdapter);
            }

        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 3 Cases
                // case1: bonded already
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED");
                    mBTDevice = mDevice;
                }
                // case2: creating a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING");
                }
                // case3: breaking a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE");
                }
            }
        }
    };

    /*
    * mReceiver is the broadcast receiver called when data is sent via bluetooth to the app
    * */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Receive data from broadcast
            ArrayList<Integer> values = intent.getIntegerArrayListExtra("btMessage");

            // Dismiss btDevices List
            if(lvnewDevices.isShown()) {
                lvnewDevices.setVisibility(View.GONE);
                heartGraph.mChart.setVisibility(View.VISIBLE);
                oxyGraph.mChart.setVisibility(View.VISIBLE);
                tempGraph.mChart.setVisibility(View.VISIBLE);
            }

            // Plot data
            if(plotData) {
                plotData = false;
                Log.d(TAG, "INCOMING MESSAGE: " + values.toString());
                Integer EXPECTED_BUFFER_SIZE = 6;
                if(values.size() == EXPECTED_BUFFER_SIZE) {
                    // values[0] -> RESERVED FOR SYNC. FLAG
                    if(values.get(1) != INVALID) {
                       heartGraph.addEntry(values.get(1));
                    }
                    if(values.get(2) != INVALID) oxyGraph.addEntry(values.get(2));
                    if(values.get(3) != INVALID) tempGraph.addEntry(values.get(3));
                    if(values.get(4) != INVALID) O2Text.setText("O2: " + values.get(4).toString());
                    if(values.get(5) != INVALID) HRText.setText("HR: " + values.get(5).toString() + " bpm");
                } else {
                    Log.d(TAG, "Incorrect Message Length: " + values.size());
                }
            }
        }
    };

    // ---------------- END BROADCAST RECEIVERS DEFINITION ---------------------

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        plotThread.interrupt();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ---- CHARTS CONFIG AND INIT ----
        heartGraph = new GraphInterface(
                findViewById(R.id.heartRateChart),
                "ECG",
                256f
        );

        oxyGraph = new GraphInterface(
                findViewById(R.id.OxiChart),
                "SPO2",
                256f
        );

        tempGraph = new GraphInterface(
                findViewById(R.id.TempChart),
                "Temperature",
                256f
        );
        // ---- END CHARTS CONFIG AND INIT ----

        // ---- BUTTONS DEFINITION ----
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);

        btnONOFF.setOnClickListener(view -> {
            Log.d(TAG, "onClick: enabling/disabling bluetooth.");
            enableDisableBT();
        });

        btnStartConnection.setOnClickListener(view -> startBTConnection());

        btnSend.setOnClickListener(view -> {
            byte[] bytes = editText.getText().toString().getBytes(Charset.defaultCharset());
            mBluetoothConnection.write(bytes);
            editText.setText("");
        });

        // ---- END BUTTONS DEFINITION ----

        // ---- EDIT TEXT DEFINITION ----

        editText = (EditText) findViewById(R.id.editText);
        O2Text = (TextView) findViewById(R.id.O2Text);
        HRText = (TextView) findViewById(R.id.HRText);

        lvnewDevices = (ListView) findViewById(R.id.lvNewDevices);
        lvnewDevices.setOnItemClickListener(MainActivity.this);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mReceiver, new IntentFilter("btIncomingMessage")
        );

        // Catch  BluetoothAdapter state change in mBroadcastReceiver1
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, BTIntent);


        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, intentFilter);

        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);

        // Broadcasts when band state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startPlot();
    }

    /*
    * Starts a thread that loops and every 10 miliseconds turns plotData into true
    * After each data entry, plotData turns to false and doesn't admit data. The refresh-rate is set
    * to be max of 10 ms that is the time it takes to the thread to turn the var into true
    * */
    private void startPlot() {
        if(plotThread != null) {
            plotThread.interrupt();
        }

        plotThread = new Thread(() -> {
            while(true) {
                plotData = true;
                try {
                    Thread.sleep(10);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        plotThread.start();
    }

    public void startBTConnection() {
        Set<BluetoothDevice> Devices = mBluetoothAdapter.getBondedDevices();
        if(mBTDevice == null) {
            Log.d(TAG, String.valueOf(Devices.size()));
            for (BluetoothDevice bt: Devices) {
                // Add all the available devices to the list
                Log.d(TAG, bt.getName());
                if(bt.getName().equals("HC-05")) {
                    mBTDevice = mBluetoothAdapter.getRemoteDevice(bt.getAddress());
                    try {
                        startBTConnection(mBTDevice, MY_UUID_INSECURE);
                    }catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            startBTConnection(mBTDevice, MY_UUID_INSECURE);
        }
    }

    /*
    * Starting chat service method
    * */
    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");
        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        mBluetoothConnection.startClient(device, uuid);
    }

    public void enableDisableBT() {
        if(mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities");
        }
        if(!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enablingBT");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT");
            mBluetoothAdapter.disable();

        }
    }

    public void setBtnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery");
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
        }
        if(!mBluetoothAdapter.isDiscovering()) {
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            if(permissionCheck != 0) {
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
                        , 1001);
            }
            else {
                Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP");
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "onItemClick: You Clicked on a device.");

        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();
        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        // create the bond
        // NOTE: Requires API > something
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(position).createBond();
            mBTDevice = mBTDevices.get(position);
        }
    }
}
