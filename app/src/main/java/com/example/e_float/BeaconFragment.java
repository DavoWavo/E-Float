package com.example.e_float;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BeaconFragment extends Fragment {
    private String pageTitle;
    private int pageNum;
    private BluetoothDevice mSelectedDevice;
    private ExpandableListView mGattServicesList;
    //private BluetoothLeService mBluetoothBLEService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private TextView mDeviceName;
    private TextView mDeviceAddress;

    private TextView mConnectionState;
    private TextView mDataField;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private String capturedData = "asdubadsvfasd";

    MainActivity activity;

    public BeaconFragment() {
        //Required empty public constructor
    }

    public static BeaconFragment newInstance(int pageNum, String pageTitle) {
        BeaconFragment beaconFragment = new BeaconFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pageNumber", pageNum);
        bundle.putString("pageTitle", pageTitle);
        beaconFragment.setArguments(bundle);
        return beaconFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.pageNum = getArguments().getInt("pageNumber", 0);
        this.pageTitle = getArguments().getString("pageTitle");

        setHasOptionsMenu(true);

        activity = (MainActivity) getActivity();

        ((MainActivity) getActivity()).UpdateConnnectionAdapterNotification(new MainActivity.BeaconFragmentListener() {
            //probably not needed now
            @Override
            public void updateDeviceParams(BluetoothDevice device) {
                UpdateDeviceParams(device);
            }

            @Override
            public void displayGattServices(List<BluetoothGattService> gattService) {
                DisplayGattServices(gattService);
            }

            @Override
            public  void displayData(String data) {
                DisplayData(data);
            }

            @Override
            public void clearUi() {
                ClearUi();
            }

            @Override
            public void updateConnectionState(int connection) {
                UpdateConnectionState(connection);
            }
        });


        Log.d("debugCon", "BeaconFragment onCreate");
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beacon, container, false);

        Log.d("debugCon", "BeaconFragment onCreateView");

        if (activity.mSelectedDevice != null) {
            //mDeviceName.setText(activity.mSelectedDevice.getName());
            mDeviceAddress.setText(activity.mSelectedDevice.getAddress());
        }

        mDeviceName = view.findViewById(R.id.device_name);
        mDeviceAddress = view.findViewById(R.id.device_address);
        mGattServicesList = view.findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(serviceListClickListener);
        mConnectionState = view.findViewById(R.id.connection_state);
        mDataField = view.findViewById(R.id.data_value);

        return view;
    }

    private final ExpandableListView.OnChildClickListener serviceListClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
            if (mGattCharacteristics != null) {
                final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();

                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null) {
                        activity.mBluetoothBLEService.setCharacteristicNotification(mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    activity.mBluetoothBLEService.readCharacteristic(characteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mNotifyCharacteristic = characteristic;
                    activity.mBluetoothBLEService.setCharacteristicNotification(characteristic, true);
                }
                return true;
            }
            return false;
        }
    };

    private void UpdateDeviceParams(BluetoothDevice device) {
        mSelectedDevice = device;
        if (device != null) {
            mDeviceName.setText(device.getName());
            mDeviceAddress.setText(device.getAddress());
        }
    }

    private void UpdateConnectionState(final int resourceId) {
        mConnectionState.setText(resourceId);
    }

    private void ClearUi() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    private void DisplayData(String data) {
        if (data != null & data != capturedData) {
            capturedData = data;
            Log.d("debugMode", "Data = " + data);
            mDataField.setText(data);
        }
        /*
        if (data != null) {
            Log.d("debugMode", "Data = " + data);
            mDataField.setText(data);
        }
        */
    }

    private void DisplayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                getContext(),
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }
}