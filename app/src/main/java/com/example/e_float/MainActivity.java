package com.example.e_float;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//Activity for scanning and displaying avaliable BLE devices
public class MainActivity extends AppCompatActivity {
    //RecyclerView.LayoutManager mLayoutManager;
    //RecyclerView.Adapter mAdapter;
    //RecyclerView rvDevices;
    ArrayList<BluetoothDevice> mDevices;
    BluetoothAdapter mBluetoothAdapter;
    Boolean mScanning;
    Handler mHandler;

    //CustomPagerAdapter pagerAdapter;
    FragmentPagerAdapter adapterViewPager;

    DeviceScanningUpdateAdapterListener deviceScanningCommander;

    public interface DeviceScanningUpdateAdapterListener {
        public void RefreshAdapter();
        public void AddDevice(BluetoothDevice device);
    }

    public void passUpdateAdapterNotification(DeviceScanningUpdateAdapterListener activityCommander) {
        this.deviceScanningCommander = activityCommander;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //PageAdapter
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        adapterViewPager = new CustomPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        //RecyclerView layout manager
        //rvDevices = findViewById(R.id.rvDevices);
        //mLayoutManager = new LinearLayoutManager(this);
        //rvDevices.setLayoutManager(mLayoutManager);

        //RecyclerView Adapter
        //mAdapter = new RVAdapter(mDevices);
        //rvDevices.setAdapter(mAdapter);
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
        inflater.inflate(R.menu.toolbar_menu, menu);
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
                            //Log.d("debugMode", device.getName());
                            //mDevices.add(device);
                            deviceScanningCommander.AddDevice(device);
                            //deviceScanningCommander.RefreshAdapter();
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