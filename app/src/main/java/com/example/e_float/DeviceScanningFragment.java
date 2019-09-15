package com.example.e_float;

import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceScanningFragment extends Fragment {
    private String pageTitle;
    private int pageNum;

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    RecyclerView rvDevices;

    ArrayList<BluetoothDevice> mDevices;

    public DeviceScanningFragment() {
        //Required empty public constructor
    }

    public static DeviceScanningFragment newInstance(int pageNum, String pageTitle) {
        DeviceScanningFragment deviceScanningFragment = new DeviceScanningFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pageNumber", pageNum);
        bundle.putString("pageTitle", pageTitle);
        deviceScanningFragment.setArguments(bundle);
        return deviceScanningFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.pageNum = getArguments().getInt("pageNumber", 0);
        this.pageTitle = getArguments().getString("pageTitle");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedsInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragement_device_scanning, container, false);

        MainActivity activity = (MainActivity) getActivity();
        mDevices = new ArrayList<>();

        if (activity != null) {
            mDevices = activity.mDevices;
        }

        //listening for changes from the main activity
        ((MainActivity) getActivity()).passUpdateAdapterNotification(new MainActivity.DeviceScanningUpdateAdapterListener() {
            @Override
            public void RefreshAdapter() {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void AddDevice(BluetoothDevice device) {
                Log.d("debugMode", device.getName());
                mDevices.add(device);
                mAdapter.notifyDataSetChanged();
            }
        });

        initializeRecyclerViewUI(view);

        return view;
    }

    public void initializeRecyclerViewUI(View view) {
        //RecyclerView layout manager
        rvDevices = (RecyclerView) view.findViewById(R.id.rvDevices);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        rvDevices.setLayoutManager(mLayoutManager);

        //RecyclerView Adapter
        mAdapter = new RVAdapter(mDevices);
        rvDevices.setAdapter(mAdapter);
    }

    public class RVAdapter extends RecyclerView.Adapter<DeviceScanningFragment.RVAdapter.ViewHolder> {
        ArrayList<BluetoothDevice> mDevices;

        public RVAdapter(ArrayList<BluetoothDevice> devices) {
            this.mDevices = devices;
        }

        @NonNull
        @Override
        public DeviceScanningFragment.RVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.ble_device_tile, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceScanningFragment.RVAdapter.ViewHolder holder, int position) {
            BluetoothDevice mDevice = mDevices.get(position);
            holder.mName.setText(mDevice.getName());
            holder.mAddress.setText(mDevice.getAddress());
        }

        @Override
        public int getItemCount() {
            if (!mDevices.isEmpty())
                return mDevices.size();
            else
                return 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mName;
            public TextView mAddress;

            public ViewHolder(View view) {
                super(view);
                mName = view.findViewById(R.id.device_name);
                mAddress = view.findViewById(R.id.device_address);
            }
        }
    }
}
