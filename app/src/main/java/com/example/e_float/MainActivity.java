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
import android.nfc.Tag;
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

/**
* <h1>Main action</h1>
* This class is the main portion of the software and handles all the back end work of the mobile system
*	
* @author David Fitzsimmons
* @version 1.2
* @since 2019-10-5
*/

//Activity for scanning and displaying avaliable BLE devices
public class MainActivity extends AppCompatActivity {
	//enum for different connection states
    enum ConnectStatus {
        EMPTY,
        SELECTED,
        CONNECTED,
        DISCONNECTED,
        DEPLOYED,
        TETHERED
    }

	//current connection status
    ConnectStatus mConnectStatus = ConnectStatus.EMPTY;

	//Bluetooth related variables
    ArrayList<BluetoothDevice> mDevices;
    BluetoothDevice mSelectedDevice;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeService mBluetoothBLEService;
    Intent enableBtIntent;
    Boolean mScanning;
    Boolean mConnect = false;
    Handler mHandler;

	//fragment handler
    ViewPager viewPager;
    FragmentPagerAdapter adapterViewPager;

	//list for ble characteristics, not used but remain for future functionality
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    /**
	* interfaces to device connection fragment
    */
	BeaconFragmentListener beaconFragmentCommander;
    public interface BeaconFragmentListener {
        public void setButtonText(int resourceId);
        public void hideDeployButton(Boolean hide);
        public void updateSelectedDeviceParams(BluetoothDevice device);
        public void updateConnectionState(int connection);
        public void updateConnectionImage(int connection);
    }
    public void UpdateConnnectionAdapterNotification(BeaconFragmentListener activityCommander) {
        this.beaconFragmentCommander = activityCommander;
    }

    /**
	* interfaces to device scanning fragment
    */
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
            Log.d("debugCon", "Service disconnected");
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("debugFlow", "onReceive");
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnect = true;
                beaconFragmentCommander.updateSelectedDeviceParams(mSelectedDevice);
                if (!mConnectStatus.equals(ConnectStatus.TETHERED))
                    CurrentConnectionStatus(ConnectStatus.CONNECTED);
                invalidateOptionsMenu();
                toastMaker("BLE service connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnect = false;
                if (mConnectStatus.equals(ConnectStatus.DEPLOYED)) {
                    TetherDevice();
                    toastMaker("Device tethered to fish");
                } else {
                    DisconnectDevice();
                    toastMaker("BLE service disconnected");
                }
            }
        }
    };

	/**
	* Called to do initial creation of the activity
	*/
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
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

	/**
	* This method initializes the UI elements
	*/
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

	/**
	* This method initializes the BLE elements
	*/
    private void InitializeBLE() {
        Log.d("debugMode", "MainActivity InitializingBLE");
        mHandler = new Handler();

		//checking the system for BLE support
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

	/**
	* Makes the activity begin interacting with the user after the activity was paused
	*/
    @Override
    protected void onResume() {
        super.onResume();

        //create an intent to display a display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

	/**
	* Used for unregistering the reciever when the activity is paused
	*/
    @Override
    protected void onPause() {
        super.onPause();

        scanBLEDevice(false);
        try {
            if (mGattUpdateReceiver != null) {
                this.unregisterReceiver(mGattUpdateReceiver);
            }
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }


    }

	/**
	* Unbinding the service when the activity is destroyed
	*/
    @Override
    protected void onDestroy() {
        Log.d("debugFlow", "onDestroy");
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothBLEService = null;
    }
	
	/**
	* creates an instance of the options menu and changing what is displayed 
	* depending on the connection state and current visible fragment
	*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        if (viewPager.getCurrentItem() == 0) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            menu.findItem(R.id.menu_undeploy).setVisible(false);
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
            if (mConnectStatus.equals(ConnectStatus.DISCONNECTED) || mConnectStatus.equals(ConnectStatus.SELECTED)) {
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_undeploy).setVisible(false);
            } else if (mConnectStatus.equals(ConnectStatus.CONNECTED) || mConnectStatus.equals(ConnectStatus.TETHERED)) {
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(true);
                menu.findItem(R.id.menu_undeploy).setVisible(false);
            } else if (mConnectStatus.equals(ConnectStatus.DEPLOYED)) {
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_undeploy).setVisible(true);
            } else {
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_undeploy).setVisible(false);
            }
        }
        return true;
    }

	/**
	* This method handles the options being selected within the action bar
	*/
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
                    ConnectDevice();
                else
                    toastMaker("BLE device not selected");
                break;
            case R.id.menu_disconnect:
                DropDevice();
                break;
            case R.id.menu_undeploy:
                UndeployDevice();
                break;
        }
        return true;
    }

    /**
	* Device scan callback.
	* This method adds the devices to the displayed discovered device list on the Scanning fragment
    */
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

	/**
	* This method will scan for avaliable BLE devices for 10000ms.
	*
	* @param, enable trigger when to begin scanning
	*/
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

	/**
	* Adding intents to the gattServiceIntent
	*/
    private static IntentFilter makeGattUpdateIntentFilter() {
        Log.d("debugFlow", "makeGattUpdateIntentFilter");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    /**
	* Called from the Device scanning fragment, this will attempt to connect to the BLE device
	* Then updates the connection status to CONNECTED
    */
	public void ConnectDevice() {
        if (mBluetoothBLEService != null) {
            if (mSelectedDevice != null) {
                final boolean result = mBluetoothBLEService.connect(mSelectedDevice.getAddress());
                beaconFragmentCommander.updateSelectedDeviceParams(mSelectedDevice);
                Log.d("debugCon", "connectiong request result = " + result);
                CurrentConnectionStatus(ConnectStatus.CONNECTED);
            }
        } else {
            toastMaker("No BLE Service available");
            Log.d("debugCon", "No BLE Service available");
        }
    }

	/**
	* Setting the current connection status to DISCONNECTED
	*/
    public void DisconnectDevice() {
        //checking to make sure there isn't nothing to disconnect from
        CurrentConnectionStatus(ConnectStatus.DISCONNECTED);
    }

    /**
	* This method will drop the device connection and update the connection status to DISCONNECTED
    */
	public void DropDevice() {
        //checking to make sure there isn't nothing to disconnect from
        if (mSelectedDevice != null) {
            //checking that device isn't connected already
            if (!mConnectStatus.equals(ConnectStatus.DISCONNECTED)) {
                mBluetoothBLEService.disconnect();
                CurrentConnectionStatus(ConnectStatus.DISCONNECTED);
            } else {
                //not connected to any devices
                Log.d("debugCon", "No devices connected");
                toastMaker("No devices connected");
            }
        }
    }

	/**
	* This method will update the selected device, update the beacon fragment selected device text field
	* and update the connection status to SELECTED
	*/
    public void SelectDevice(BluetoothDevice device) {
        mSelectedDevice = device;
        beaconFragmentCommander.updateSelectedDeviceParams(mSelectedDevice);
        CurrentConnectionStatus(ConnectStatus.SELECTED);
    }

	/**
	* This method will unselect the device and ensure the device is disconnected before unselecting
	* also update the connection status to EMPTY
	*/
    public void UnselectDevice() {
        //make sure the device isn't already connected, otherwise disconnect
        if (!mConnectStatus.equals(ConnectStatus.SELECTED))
            mBluetoothBLEService.disconnect();

		//clearing the selectedDevice
        mSelectedDevice = null;
        beaconFragmentCommander.updateSelectedDeviceParams(mSelectedDevice);
        CurrentConnectionStatus(ConnectStatus.EMPTY);
    }

	/**
	* Updating the connection status to DEPLOYED
	*/
    public void DeployDevice() {
        CurrentConnectionStatus(ConnectStatus.DEPLOYED);
    }

	/**
	* Updating the connection status to CONNECTED
	*/ 
    public void UndeployDevice() {
        CurrentConnectionStatus(ConnectStatus.CONNECTED);
    }

	/**
	* Updating the connection statis to TETHERED
	*/
    public void TetherDevice() {
        CurrentConnectionStatus(ConnectStatus.TETHERED);
    }

	/**
	* Dismissing the connection status
	* if device connected, then change to DEPLOYED otherwise DISCONNECTED
	*/
    public void DismissTether() {
        if (mConnect)
            CurrentConnectionStatus(ConnectStatus.DEPLOYED);
        else
            CurrentConnectionStatus(ConnectStatus.DISCONNECTED);
    }

	/**
	* Factory toast maker
	*
	* @param condiment, a string with the toast message
	*/
    public void toastMaker(String condiment) {
        Toast.makeText(this, condiment, Toast.LENGTH_SHORT).show();
    }

	/**
	* This method updates the connection status, and updates the string and images for the beacon fragment
	* depending on what the connection status was updated to.
	*
	* @param status, the current status for the machine to be updated to
	*/
    public void CurrentConnectionStatus(ConnectStatus status) {
        String state = "";

        //if the status has changed, then update the status
        if (status != mConnectStatus) {
            mConnectStatus = status;
            beaconFragmentCommander.hideDeployButton(true);
            switch (mConnectStatus) {
                case EMPTY:
                    state = "empty";
                    beaconFragmentCommander.updateConnectionState(R.string.select_device);
                    beaconFragmentCommander.updateConnectionImage(R.drawable.disconnected);
                    break;
                case SELECTED:
                    state  = "selected";
                    beaconFragmentCommander.updateConnectionState(R.string.disconnected);
                    beaconFragmentCommander.updateConnectionImage(R.drawable.disconnected);
                    break;
                case CONNECTED:
                    beaconFragmentCommander.updateConnectionState(R.string.connected);
                    beaconFragmentCommander.updateConnectionImage(R.drawable.connected);
                    beaconFragmentCommander.setButtonText(R.string.deploy);
                    beaconFragmentCommander.hideDeployButton(false);
                    state = "connected";
                    break;
                case DISCONNECTED:
                    beaconFragmentCommander.updateConnectionState(R.string.disconnected);
                    beaconFragmentCommander.updateConnectionImage(R.drawable.disconnected);
                    state = "disconnected";
                    break;
                case DEPLOYED:
                    beaconFragmentCommander.updateConnectionState(R.string.deployed);
                    beaconFragmentCommander.updateConnectionImage(R.drawable.deploy);
                    state = "deployed";
                    break;
                case TETHERED:
                    beaconFragmentCommander.updateConnectionState(R.string.tethered);
                    beaconFragmentCommander.updateConnectionImage(R.drawable.tether);
                    beaconFragmentCommander.setButtonText(R.string.dismiss);
                    beaconFragmentCommander.hideDeployButton(false);
                    state = "tethered";
                    break;
            }
            invalidateOptionsMenu();
            Log.d("debugState", state);
        }
    }
}