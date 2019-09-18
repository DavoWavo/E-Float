package com.example.e_float;

import com.google.android.material.tabs.TabLayout;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

//Activity for scanning and displaying avaliable BLE devices
public class MainActivity extends AppCompatActivity {
    ArrayList<BluetoothDevice> mDevices;
    BluetoothDevice mSelectedDevice;
    BluetoothAdapter mBluetoothAdapter;
    Boolean mScanning;
    Handler mHandler;

    CustomPagerAdapter customPagerAdapter;

    //interfaces to fragments
    DeviceScanningUpdateAdapterListener deviceScanningCommander;
    public interface DeviceScanningUpdateAdapterListener {
        public void RefreshAdapter();
        public void AddDevice(BluetoothDevice device);
    }

    public void passUpdateAdapterNotification(DeviceScanningUpdateAdapterListener activityCommander) {
        this.deviceScanningCommander = activityCommander;
    }

    public void ConnectDevice(BluetoothDevice device) {
        //Log.d("debugCon", device.getName());
        //Log.d("debugCon", device.getAddress());
        mSelectedDevice = device;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScanning = false;
        mDevices = new ArrayList<>();

        InitializeUI();

        InitializeBLE();
    }

    private void InitializeUI() {
        Log.d("debugMode", "Initializing user interface");

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //PageAdapter
        customPagerAdapter = new CustomPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(customPagerAdapter);

        //PageAdapter tabs
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private void InitializeBLE() {
        Log.d("debugMode", "MainActivity InitializingBLE");
        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Log.d("debugMode", "MainActivity onCreateOptionsMenu entered");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                Log.d("debugMode", "MainActivity onOptionsItemSelected scanning");
                scanBLEDevice(true);
                break;
            case R.id.menu_stop:
                Log.d("debugMode", "MainActivity onOptionsItemSelected stopping");
                scanBLEDevice(false);
                break;
        }
        return true;
    }

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
                    }
                }, 10000);

                mScanning = true;
                mBluetoothAdapter.startLeScan(mBLEScanCallBack);
            }
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mBLEScanCallBack);
        }
    }
}