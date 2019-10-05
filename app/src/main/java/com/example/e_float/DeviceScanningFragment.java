package com.example.e_float;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceScanningFragment extends Fragment {
    private String pageTitle;
    private int pageNum;
    private MainActivity activity;

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    RecyclerView rvDevices;

    BluetoothDevice mSelectedDevice;
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

        activity = (MainActivity) getActivity();
        mDevices = new ArrayList<>();

        if (activity != null) {
            mDevices = activity.mDevices;
        }

        //listening for changes from the main activity
        ((MainActivity) getActivity()).UpdateScanningAdapterNotification(new MainActivity.DeviceScanningUpdateAdapterListener() {
            @Override
            public void RefreshAdapter() {
                Log.d("debugMode", "RecyclerView adapter refreshed");
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void AddDevice(BluetoothDevice device) {
                if (device.getName() != null) {
                    Log.d("debugMode", device.getName());
                    mDevices.add(device);
                }
            }
        });

        initializeRecyclerViewUI(view);

        return view;
    }

    public void initializeRecyclerViewUI(View view) {
        //RecyclerView layout manager
        rvDevices = (RecyclerView) view.findViewById(R.id.rv_Devices);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        rvDevices.setLayoutManager(mLayoutManager);

        //RecyclerView Adapter
        mAdapter = new RVAdapter(mDevices);
        rvDevices.setAdapter(mAdapter);
    }

    public void SelectDevice(int position) {
        if (activity != null)
            activity.SelectDevice(mDevices.get(position));
    }

    public void UnselectDevice() {
        if (activity != null)
            activity.UnselectDevice();
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
        public void onBindViewHolder(@NonNull final DeviceScanningFragment.RVAdapter.ViewHolder holder, final int position) {
            BluetoothDevice mDevice = mDevices.get(position);

            holder.mName.setText(mDevice.getName());

            //ensuring you can only connect to eFloats
            if (mDevice.getName().equals("eFloat")) {
                holder.mConnectButton.setVisibility(View.VISIBLE);
                holder.mConnectButton.setEnabled(true);
            } else {
                holder.mConnectButton.setVisibility(View.INVISIBLE);
                holder.mConnectButton.setEnabled(false);
            }

            holder.mConnectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //changing the text within the buttons
                    if (activity.mConnectStatus.equals(MainActivity.ConnectStatus.EMPTY)) {
                        SelectDevice(position);
                        holder.mConnectButton.setText(R.string.unselect);
                        notifyDataSetChanged();
                    } else {
                        UnselectDevice();
                        holder.mConnectButton.setText(R.string.select);
                        notifyDataSetChanged();
                    }
                }
            });
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
            public Button mConnectButton;

            public ViewHolder(View view) {
                super(view);
                mName = view.findViewById(R.id.device_name_tv);
                mConnectButton = view.findViewById(R.id.connect_button);
            }
        }
    }
}
