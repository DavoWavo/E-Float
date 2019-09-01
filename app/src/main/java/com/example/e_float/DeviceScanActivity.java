package com.example.e_float;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

//Activity for scanning and displaying avaliable BLE devices
public class DeviceScanActivity extends ListActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private BLEDeviceListAdapter mBLEDeviceListAdapter;
    private boolean mScanning;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //BLE not supported on device
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            //Bluetooth not supported on device
            finish();
            return;
        }
    }


    private BluetoothAdapter.LeScanCallback mBLEScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBLEDeviceListAdapter.addDevice(device);
                    mBLEDeviceListAdapter.notifyDataSetChange();
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            //BLE device is not avaliable
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanBLEDevice(false);
        mBLEDeviceListAdapter.clear();
    }

    private void scanBLEDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mBLEScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.stopLeScan(mBLEScanCallback);
        }
    }

    private class BLEDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mBLEDevices;
        private LayoutInflater mLayoutInflater;

        public BLEDeviceListAdapter() {
            super();
            mBLEDevices = new ArrayList<BluetoothDevice>();
            mLayoutInflater = DeviceScanActivity.this.getLayoutInflater();
        }

        public boolean addDevice(BluetoothDevice device) {
            if (!mBLEDevices.contains(device)){
                mBLEDevices.add(device);
                return true;
            } else {
                return false;
            }
        }

        public boolean clearDevice(BluetoothDevice device) {
            if (mBLEDevices.contains(device)) {
                mBLEDevices.remove(device);
                return true;
            } else {
                return false;
            }
        }

        public boolean clearList() {
            mBLEDevices.clear();
            if (mBLEDevices.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }

        public BluetoothDevice getDevice(int i) {
            return mBLEDevices.get(i);
        }

        @Override
        public int getCount() {
            return mBLEDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mBLEDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;

            if (view == null) {
                view = mLayoutInflater.inflate()
            }
        }
    }
}
