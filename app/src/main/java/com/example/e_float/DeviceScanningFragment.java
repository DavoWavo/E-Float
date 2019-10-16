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

/**
* <h1>Device Scanning Fragment</h1>
* This class handles the different fragments for displaying.
*	
* @author David Fitzsimmons
* @version 1.0
* @since 2019-10-5
*/

public class DeviceScanningFragment extends Fragment {
    //Page information
	private String pageTitle;
    private int pageNum;

	//UI containers
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    RecyclerView rvDevices;

	//BLE device containers
    BluetoothDevice mSelectedDevice;
    ArrayList<BluetoothDevice> mDevices;

	//Main activity instance
    private MainActivity activity;

	/**
	* Default constructor
	*/
    public DeviceScanningFragment() {
        //Required empty public constructor
    }

	/**
	* This method will create a new instance of the fragment and pass information backwards
	*
	* @param pageNum, this is the number of the page being displayed
	* @param pageTitle, this is the title of the page being displayed
	* @return BeaconFragment this returns a copy of itself with an intent containing pageNum and pageTitle
	*/
    public static DeviceScanningFragment newInstance(int pageNum, String pageTitle) {
        DeviceScanningFragment deviceScanningFragment = new DeviceScanningFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pageNumber", pageNum);
        bundle.putString("pageTitle", pageTitle);
        deviceScanningFragment.setArguments(bundle);
        return deviceScanningFragment;
    }

	/**
	* Called to do initial creation of the fragment
	*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//setting the page number and page title of this fragment
        this.pageNum = getArguments().getInt("pageNumber", 0);
        this.pageTitle = getArguments().getString("pageTitle");
    }

	/**
	* Creates and returns the view heirarchy assocated with the fragment
	*/ 
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
			//refresh the view
		    @Override
            public void RefreshAdapter() {
                Log.d("debugMode", "RecyclerView adapter refreshed");
                mAdapter.notifyDataSetChanged();
            }
			//adding a device to the list of devices
            @Override
            public void AddDevice(BluetoothDevice device) {
                if (device.getName() != null) {
                    Log.d("debugMode", device.getName());
                    mDevices.add(device);
                }
            }
        });

		//initializing the recyclerview elements
        initializeRecyclerViewUI(view);

        return view;
    }

	/**
	* This method is used to initialize the recyclerview elements
	*
	* @param view, a copy of the current view from onCreateView
	*/
    public void initializeRecyclerViewUI(View view) {
        //RecyclerView layout manager
        rvDevices = (RecyclerView) view.findViewById(R.id.rv_Devices);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        rvDevices.setLayoutManager(mLayoutManager);

        //RecyclerView Adapter
        mAdapter = new RVAdapter(mDevices);
        rvDevices.setAdapter(mAdapter);
    }

	/**
	* This method is used for setting the current selected device from the list of scanned devices
	*
	* @param position, the position within the list of scanned devices that was selected
	*/
    public void SelectDevice(int position) {
        if (activity != null)
            activity.SelectDevice(mDevices.get(position));
    }

	/**
	* This methods is used for unselecting the device
	*/
    public void UnselectDevice() {
        if (activity != null)
            activity.UnselectDevice();
    }

	/**
	* Recycler view adapter that extends the view holder for list functionality
    */
	public class RVAdapter extends RecyclerView.Adapter<DeviceScanningFragment.RVAdapter.ViewHolder> {
        //list of devices to populate list with
		ArrayList<BluetoothDevice> mDevices;

		//default constructor
        public RVAdapter(ArrayList<BluetoothDevice> devices) {
            this.mDevices = devices;
        }

		//creates a new RecyclerView.ViewHolder
        @NonNull
        @Override
        public DeviceScanningFragment.RVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.ble_device_tile, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

		//creates a new RecyclerView.ViewHolder
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

		//return number of items within list
        @Override
        public int getItemCount() {
            if (!mDevices.isEmpty())
                return mDevices.size();
            else
                return 0;
        }

		//View holder for field population
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
