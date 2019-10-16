package com.example.e_float;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
* <h1>Beacon fragment</h1>
* This fragment handles the displaying of informationg relating to the selected beacon.
* It also handles to UI elements for the displayed fragment
*	
* @author David Fitzsimmons
* @version 1.0
* @since 2019-10-5
*/

public class BeaconFragment extends Fragment {
	//Page information
    private String pageTitle;
    private int pageNum;

	//UI containers
    private TextView mDeviceName;
    private TextView mDeviceAddress;
    private TextView mConnectionState;
    private ImageView mConnectionImage;
    private Button mDeployButton;

	//place holder values
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private String capturedData = "asdubadsvfasd";

	//Main activity instance
    MainActivity activity;

	/**
	* default constructor
    */
	public BeaconFragment() {
        //Required empty public constructor
    }
	
	/**
	* This method will create a new instance of the fragment and pass information backwards
	*
	* @param pageNum, this is the number of the page being displayed
	* @param pageTitle, this is the title of the page being displayed
	* @return BeaconFragment this returns a copy of itself with an intent containing pageNum and pageTitle
	*/
    public static BeaconFragment newInstance(int pageNum, String pageTitle) {
        BeaconFragment beaconFragment = new BeaconFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pageNumber", pageNum);
        bundle.putString("pageTitle", pageTitle);
        beaconFragment.setArguments(bundle);
        return beaconFragment;
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

		//validating the options menu
        setHasOptionsMenu(true);

		//allocating a copy of the activity containing this fragment to access the parent methods
        activity = (MainActivity) getActivity();

		//Setting a listener for when the parent activity is trying to access these methods
        ((MainActivity) getActivity()).UpdateConnnectionAdapterNotification(new MainActivity.BeaconFragmentListener() {
            //changing what is displayed within the button
			@Override
            public void setButtonText(int resourceId) {
                SetButtonText(resourceId);
            }

			//changing the current visibility of the button
            @Override
            public void hideDeployButton(Boolean hide) {
                HideDeployButton(hide);
            }
			
			//changing the text field to display the current selected device
            @Override
            public void updateSelectedDeviceParams(BluetoothDevice device) {
                UpdateSelectedDeviceParams(device);
            }

			//changing the text to show the current connection state
            @Override
            public void updateConnectionState(int connection) {
                UpdateConnectionState(connection);
            }

			//changing the current image to correspond to the connection state
            @Override
            public void updateConnectionImage(int connection) {
                UpdateConnectionImage(connection);
            }
        });

        //Log.d("debugCon", "BeaconFragment onCreate");
    }


	/**
	* Creates and returns the view hierarchy assocated with the fragment
	*/
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
                    activity.DeployDevice();
                } else if (activity.mConnectStatus.equals(MainActivity.ConnectStatus.TETHERED)) {
                    activity.toastMaker("Tether dismissed");
                    activity.DismissTether();
                }
            }
        });

        mConnectionImage = view.findViewById(R.id.connection_status_image);

        return view;
    }

	/**
	* This methods changes what text is displayed on the button
	*
	* @param resourceId, the location of the text resource to change the button text to
	*/
    private void SetButtonText(final int resourceId) {
        mDeployButton.setText(resourceId);
    }

	/**
	* This method toggles whether to hide and deactivate the button or not
	*
	* @param hide, the boolean variable dictating whether to hide or not the button
	*/
    private void HideDeployButton(Boolean hide) {
        if (hide) {
            mDeployButton.setVisibility(View.INVISIBLE);
            mDeployButton.setEnabled(false);
        } else {
            mDeployButton.setVisibility(View.VISIBLE);
            mDeployButton.setEnabled(true);
        }
    }

	/**
	* This methods will update the text displaying the currently selected device 
	*
	* @param device, a copy of the selected device to extract information from
	*/
    private void UpdateSelectedDeviceParams(BluetoothDevice device) {
        if (device != null) {
            mDeviceName.setText(device.getName());
            mDeviceAddress.setText(device.getAddress());
        } else {
            mDeviceName.setText(R.string.nil);
            mDeviceAddress.setText(R.string.nil);
        }
    }

	/**
	* This method will update the text displaying the current connection status
	*
	* @param resourceId, the location of the text resource to change the displayed text to
	*/
    private void UpdateConnectionState(final int resourceId) {
        mConnectionState.setText(resourceId);
    }

	/**
	* This method will update the displayed image corresponding to the connection status
	*
	* @param resourceId, the location of the image resource to change the displayed image to
	*/
    private void UpdateConnectionImage(final int resourceId) {
        mConnectionImage.setImageResource(resourceId);
    }
}