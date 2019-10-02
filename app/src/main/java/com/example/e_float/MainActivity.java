package com.example.e_float;

import com.google.android.material.tabs.TabLayout;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

//Activity for scanning and displaying avaliable BLE devices
public class MainActivity extends AppCompatActivity {
    enum ConnectStatus {
        CONNECTED,
        DISCONNECTED,
        DEPLOYED,
        TETHERED
    }

    ArrayList<BluetoothDevice> mDevices;
    BluetoothDevice mSelectedDevice;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeService mBluetoothBLEService;
    Intent enableBtIntent;
    Boolean mScanning;
    Boolean mConnected = false;
    Handler mHandler;
    ConnectStatus mConnectStatus;

    ViewPager viewPager;
    FragmentPagerAdapter adapterViewPager;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    //interfaces to device connection fragment
    BeaconFragmentListener beaconFragmentCommander;
    public interface BeaconFragmentListener {
        public void updateDeviceParams(BluetoothDevice device);
        public void updateConnectionState(int connection);
        public void updateConnectionImage(int connection);
    }
    public void UpdateConnnectionAdapterNotification(BeaconFragmentListener activityCommander) {
        this.beaconFragmentCommander = activityCommander;
    }

    //interfaces to device scanning fragment
    DeviceScanningUpdateAdapterListener deviceScanningCommander;
    public interface DeviceScanningUpdateAdapterListener {
        public void RefreshAdapter();
        public void AddDevice(BluetoothDevice device);
    }

    public void UpdateScanningAdapterNotification(DeviceScanningUpdateAdapterListener activityCommander) {
        this.deviceScanningCommander = activityCommander;
    }


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d("debugFlow", "onServiceConnected");
            mBluetoothBLEService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothBLEService.initialize()) {
                Log.d("debugCon", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (mSelectedDevice != null)
                mBluetoothBLEService.connect(mSelectedDevice.getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothBLEService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("debugFlow", "onReceive");
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                beaconFragmentCommander.updateDeviceParams(mSelectedDevice);
                //beaconFragmentCommander.updateConnectionState(R.string.connected);
                CurrentConnectionStatus(ConnectStatus.CONNECTED);
                Log.d("debugCon", "BLE service GATT connected");
                invalidateOptionsMenu();
                toastMaker("BLE service connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                if (mConnectStatus.equals(ConnectStatus.DEPLOYED))
                    CurrentConnectionStatus(ConnectStatus.TETHERED);
                else
                    CurrentConnectionStatus(ConnectStatus.DISCONNECTED);

                //beaconFragmentCommander.updateConnectionState(R.string.disconnected);
                Log.d("debugCon", "BLE service GATT disconnected");
                invalidateOptionsMenu();
                toastMaker("BLE service disconnected");
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checking application permissions - course location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission is not granted
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }

        //checking application permissions - fine location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission is not granted
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }

        mScanning = false;
        mDevices = new ArrayList<>();

        InitializeUI();

        InitializeBLE();

        //initializing the BLE gatt service
        Log.d("debugCon", "Binding service 1");
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d("debugCon", "Binding service 2");
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.d("debugCon", "Binding service 3");
    }

    private void InitializeUI() {
        Log.d("debugMode", "Initializing user interface");

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //PageAdapter
        viewPager = findViewById(R.id.view_pager);
        adapterViewPager = new CustomPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        //PageAdapter tabs
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private void InitializeBLE() {
        Log.d("debugMode", "MainActivity InitializingBLE");
        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toastMaker("BLUE is not supported");
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    @Override
    protected void onResume() {
        Log.d("debugFlow", "onResume");
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothBLEService != null) {
            final boolean result = mBluetoothBLEService.connect(mSelectedDevice.getAddress());
        } else {
            Log.d("debugCon", "onResume Unable to find service");
        }
    }

    @Override
    protected void onPause() {
        Log.d("debugFlow", "onPause");
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.d("debugFlow", "onDestroy");
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothBLEService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        if (viewPager.getCurrentItem() == 0) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            if (!mScanning) {
                menu.findItem(R.id.menu_stop).setVisible(false);
                menu.findItem(R.id.menu_scan).setVisible(true);
            } else {
                menu.findItem(R.id.menu_scan).setVisible(false);
                menu.findItem(R.id.menu_stop).setVisible(true);
            }
        } else if (viewPager.getCurrentItem() == 1) {
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_stop).setVisible(false);
            if (!mConnected) {
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
            } else {
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mDevices.clear();
                deviceScanningCommander.RefreshAdapter();
                Log.d("debugMode", "MainActivity onOptionsItemSelected scanning");
                scanBLEDevice(true);
                break;
            case R.id.menu_stop:
                Log.d("debugMode", "MainActivity onOptionsItemSelected stopping");
                scanBLEDevice(false);
                break;
            case R.id.menu_connect:
                if (mSelectedDevice != null)
                    mBluetoothBLEService.connect(mSelectedDevice.getAddress());
                else
                    toastMaker("BLE device not selected");
                break;
            case R.id.menu_disconnect:
                mBluetoothBLEService.disconnect();
                break;
        }
        return true;
    }

    //This method adds the devices to the displayed discovered device list on the Scanning fragment
    private BluetoothAdapter.LeScanCallback mBLEScanCallBack = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device != null) {
                        if (!mDevices.contains(device)) {
                            deviceScanningCommander.AddDevice(device);
                            deviceScanningCommander.RefreshAdapter();
                        }
                    }
                }
            });
        }
    };

    private void scanBLEDevice(final boolean enable) {
        if (enable) {
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mBLEScanCallBack);
                        invalidateOptionsMenu();
                    }
                }, 10000);

                mScanning = true;
                mBluetoothAdapter.startLeScan(mBLEScanCallBack);
            }
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mBLEScanCallBack);
        }
        invalidateOptionsMenu();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        Log.d("debugFlow", "makeGattUpdateIntentFilter");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.EXTRA_DATA);
        return intentFilter;
    }

    //Called from the Device scanning fragment, this will attempt to connect to the BLE device
    public void ConnectDevice(BluetoothDevice device) {
        Log.d("debugCon", "Attempting to bind service");
        mSelectedDevice = device;
        if (mBluetoothBLEService != null) {
            final boolean result = mBluetoothBLEService.connect(device.getAddress());
            beaconFragmentCommander.updateDeviceParams(mSelectedDevice);
            Log.d("debugCon", "Connect request result=" + result);
        } else {
            Log.d("debugCon", "No BLE Service avaliable");
        }
    }

    public void toastMaker(String condiment) {
        Toast.makeText(this, condiment, Toast.LENGTH_SHORT).show();
    }

    public void CurrentConnectionStatus(ConnectStatus status) {
        //if the status has changed, then update the status
        if (status != mConnectStatus) {
            mConnectStatus = status;
            switch (mConnectStatus) {
                case CONNECTED:
                    beaconFragmentCommander.updateConnectionState(R.string.connected);
                    beaconFragmentCommander.updateConnectionImage(R.drawable.connected);
                    break;
                case DISCONNECTED:
                    beaconFragmentCommander.updateConnectionState(R.string.disconnected);
                    beaconFragmentCommander.updateConnectionImage(R.drawable.disconnected);
                    break;
                case DEPLOYED:
                    beaconFragmentCommander.updateConnectionState(R.string.deployed);
                    beaconFragmentCommander.updateConnectionImage(R.drawable.deploy);
                    break;
                case TETHERED:
                    beaconFragmentCommander.updateConnectionState(R.string.tethered);
                    beaconFragmentCommander.updateConnectionImage(R.drawable.tether);
                    break;
            }
            invalidateOptionsMenu();
        }

    }
}