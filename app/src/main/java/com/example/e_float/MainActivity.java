package com.example.e_float;


import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;

//Activity for scanning and displaying avaliable BLE devices
public class MainActivity extends AppCompatActivity {
    private DeviceScanActivity mDeviceScanActivity;
    private LayoutInflater mInflater;
    private Context context;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitializeUI();

        InitializeBLE();

        context = this;

        mDeviceScanActivity = new DeviceScanActivity(mHandler, mBluetoothAdapter, mInflater);
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

    private void InitializeUI() {
        Log.d("debugMode", "Initializing user interface");

        mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("debugMode", "MainActivity onCreateOptionsMenu entered");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        if (!mDeviceScanActivity.checkScanning()) {
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
                mDeviceScanActivity.scanBLEDevice(true);
                break;
            case R.id.menu_stop:
                Log.d("debugMode", "MainActivity onOptionsItemSelected stopping");
                mDeviceScanActivity.scanBLEDevice(false);
                break;
        }
        return true;
    }

    public class DeviceScanActivity extends ListActivity {
        private BLEDeviceListAdapter mBLEDeviceListAdapter;
        private BluetoothAdapter mBluetoothAdapter;
        private boolean mScanning;
        private Handler mHandler;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Log.d("debugMode", "DeviceScanActivity onCreate entered");
        }

        public DeviceScanActivity(Handler mHandler, BluetoothAdapter mBluetoothAdapter, LayoutInflater mInflater) {
            this.mHandler = mHandler;
            this.mBluetoothAdapter = mBluetoothAdapter;

            mBLEDeviceListAdapter = new BLEDeviceListAdapter(mInflater);
            mBLEDeviceListAdapter.notifyDataSetChanged();
        }

        public boolean checkScanning() {
            return this.mScanning;
        }

        private void scanBLEDevice(final boolean enable) {
            Log.d("debugMode", "DeviceScanActivity scanBLEDevice entered");
            if (enable) {
                Log.d("debugMode", "DeviceScanActivity scanBLEDevice is enabled");
                if (mHandler != null) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("debugMode", "DeviceScanActivity scanBLEDevice running scan");
                            mScanning = false;
                            mBluetoothAdapter.stopLeScan(mBLEScanCallBack);

                            //invalidateOptionsMenu();
                        }
                    }, 10000);

                    mScanning = true;
                    mBluetoothAdapter.startLeScan(mBLEScanCallBack);
                    Log.d("debugMode", "DeviceScanActivity scanBLEDevice scan complete");
                } else {
                    Log.d("debugMode", "DeviceScanActivity scanBLEDevice mHandler  is null");
                }
            } else {
                Log.d("debugMode", "DeviceScanActivity scanBLEDevice is disabled");
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mBLEScanCallBack);
            }
            //invalidateOptionsMenu();
            Log.d("debugMode", "DeviceScanActivity scanBLEDevice scan process finished");
        }

        private class BLEDeviceListAdapter extends BaseAdapter {
            private ArrayList<BluetoothDevice> mBLEDevices;
            private LayoutInflater mInflater;

            public BLEDeviceListAdapter(LayoutInflater mInflater) {
                super();
                mBLEDevices = new ArrayList<BluetoothDevice>();
                this.mInflater = mInflater;
            }

            public void addDevice(BluetoothDevice device) {
                if (!mBLEDevices.contains(device)){
                    Log.d("debugMode", "DeviceScanActivity device added");
                    Log.d("debugMode", device.getName());
                    this.mBLEDevices.add(device);
                    this.notifyDataSetChanged();
                }
            }

            public void clearList() {
                if (!mBLEDevices.isEmpty())
                    this.mBLEDevices.clear();
            }

            public BluetoothDevice getDevice(int i) {
                return this.mBLEDevices.get(i);
            }

            @Override
            public int getCount() {
                return this.mBLEDevices.size();
            }

            @Override
            public Object getItem(int i) {
                return this.mBLEDevices.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;

                Log.d("debugMode", "DeviceScanActivity view updated");

                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.ble_device_tile, null);

                    viewHolder = new ViewHolder();

                    viewHolder.deviceAddress = findViewById(R.id.device_address);
                    viewHolder.deviceName = findViewById(R.id.device_name);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                BluetoothDevice currDevice = this.mBLEDevices.get(pos);
                if (currDevice != null) {
                    final String deviceName = currDevice.getName();
                    if (deviceName != null && deviceName.length() > 0){
                        viewHolder.deviceName.setText(deviceName);
                    } else {
                        viewHolder.deviceName.setText("unknown device");
                    }

                    viewHolder.deviceAddress.setText(currDevice.getAddress());
                }

                return convertView;
            }
        }

        private BluetoothAdapter.LeScanCallback mBLEScanCallBack = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (device != null) {
                            mBLEDeviceListAdapter.addDevice(device);
                            mBLEDeviceListAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        };

        private final class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
        }
    };
}