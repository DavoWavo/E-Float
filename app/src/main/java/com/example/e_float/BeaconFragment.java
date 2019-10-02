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
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
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
    //private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private TextView mDeviceName;
    private TextView mDeviceAddress;
    private TextView mConnectionState;
    private ImageView mConnectionImage;
    private Button mDeployButton;

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
            public void updateConnectionState(int connection) {
                UpdateConnectionState(connection);
            }

            @Override
            public void updateConnectionImage(int connection) {
                UpdateConnectionImage(connection);
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
        mConnectionState = view.findViewById(R.id.connection_state);

        mDeployButton = (Button) view.findViewById(R.id.deploy_button);
        mDeployButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (activity.mConnectStatus.equals(MainActivity.ConnectStatus.CONNECTED)) {
                    activity.toastMaker("Device deployed");
                    activity.CurrentConnectionStatus(MainActivity.ConnectStatus.DEPLOYED);
                }
            }
        });

        mConnectionImage = view.findViewById(R.id.connection_status_image);

        return view;
    }


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

    private void UpdateConnectionImage(final int resourceId) {
        mConnectionImage.setImageResource(resourceId);
    }
}