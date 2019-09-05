package com.example.e_float;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends ListActivity {
    private ArrayList<Device> devices = new ArrayList<Device>();
    private LayoutInflater mInflator;
    private ProgressDialog progressDialog;
    private Context context;
    ProgressBar progressBar;

    //Called when the activity is first created
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void initializeUI(){
        mInflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class RowIconAdapter extends ArrayAdapter<Device> {
        private ArrayList<Device> devices;

        //view lookup cache
        public final class ViewHolder {
            TextView deviceText;
            TextView deviceStrength;
        }

        //movie params cache for asyncTask
        public final class DeviceParams {
            String deviceName;
            int deviceStrength;

            DeviceParams(String deviceName, int deviceStrength) {
                this.deviceName = deviceName;
                this.deviceStrength = deviceStrength;
            }
        }

        public RowIconAdapter(Context c, int rowResourceId, int textViewResourceId, ArrayList<Device> items) {
            super(c, rowResourceId, textViewResourceId, items);
            devices = items;
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            //get the data item for this position
            final Device currDevice = devices.get(pos);

            //check if an existing view is being reused, otherwise inflate the view
            final DeviceParams deviceParams;
            ViewHolder viewHolder;

            if (convertView == null) {
                //if there is no reusable view, inflate a brand new view for the row
                viewHolder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.device_tile, parent, false);

                viewHolder.deviceText = (TextView) convertView.findViewById(R.id.device_name_tv);
                viewHolder.deviceStrength = (TextView) convertView.findViewById(R.id.device_strength_tv);

                convertView.setTag(viewHolder);
            } else {
                //View is being recycled, retrieve the viewHolder object from tag
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //putting the data into the view
            if (currDevice != null) {
                //populate the data from the data object via the viewHolder object in the template
                viewHolder.deviceText.setText(currDevice.getName());
                viewHolder.deviceStrength.setText(currDevice.getStrength());

                //pass into the asyncTask both the name and rating via a wrapper
                deviceParams = new DeviceParams(currDevice.getName(), currDevice.getStrength());
            }
            return convertView;
        }
    }

    private class LoadingDevices extends AsyncTask<Integer, Integer, ArrayList<Device>> {
        @Override
        protected ArrayList<Device> doInBackground(Integer... voids) {
            ArrayList<Device> devicesLoaded = new ArrayList<Device>();

            //Put the bluetooth device retrival methods here
            return devicesLoaded;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Loading...");
            progressDialog.show();
        }

        //update the progress of the dialog
        @Override
        protected void onProgressUpdate(Integer... values) {
            setProgress(values[0]);
        }

        //removing the dialog box from the view and allocate devices the result of the loading.
        @Override
        protected void onPostExecute(ArrayList<Device> devicesLoaded) {
            devices = devicesLoaded;
            setListAdapter(new RowIconAdapter(context, R.layout.device_tile, R.id.device_name_tv, devices));
            progressDialog.dismiss();
        }
    }
}
