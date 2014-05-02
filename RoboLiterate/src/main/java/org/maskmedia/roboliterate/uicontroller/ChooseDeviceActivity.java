/**
 * Copyright (C) 2013 Geoffrey Falk
 *
 *  Adapted from MINDdroid software project: Copyright 2010 Guenther Hoelzl, Shawn Brown
 *  https://github.com/NXT/LEGO-MINDSTORMS-MINDdroid
 *
 * (original work is) Copyright (C) 2009 The Android Open Source Project
 *
 **/

package org.maskmedia.roboliterate.uicontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.maskmedia.roboliterate.R;
import org.maskmedia.roboliterate.robotcomms.RobotConnectorService;

import java.util.Set;

/**
 * Activity for user to select a paired device as well as search for new, unpaired devices
 */

public class ChooseDeviceActivity extends Activity {

    private static final String PAIRING = "pairing";
    public static String EXTRA_DEVICE_NAME= "device_name";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBtAdapter;
    private String mCallingActivity;

    private ChooseDeviceArrayAdapter mNewDevicesArrayAdapter;

    private Button mScanButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        if (Build.VERSION.SDK_INT >= 11) {
            // on older android devices activity will not be closed on touch outside.
        setFinishOnTouchOutside(false);
        }
        setContentView(R.layout.device_list);
        mCallingActivity = getCallingActivity().getClassName();






        mScanButton = (Button)findViewById(R.id.button_scan);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscovery();
                view.setVisibility(View.GONE);
            }
        });



        ChooseDeviceArrayAdapter mPairedDevicesArrayAdapter = new ChooseDeviceArrayAdapter(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ChooseDeviceArrayAdapter(this, R.layout.device_name);

        // Find and set up the ListView for paired devices

        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices

        Set<BluetoothDevice> pairedDevices;
        pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter

        boolean legoDevicesFound = false;
        assert pairedDevices != null;
        if (pairedDevices.size() > 0) {
       //     findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);

            // only add LEGO devices

            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().startsWith(RobotConnectorService.OUI_LEGO)) {
                    legoDevicesFound = true;
                    mPairedDevicesArrayAdapter.add(new String[]{device.getName(), device.getAddress()});
                }
            }
        }
        if (!legoDevicesFound) {
//            String noDevices = getResources().getText(R.string.none_paired).toString();
//            mPairedDevicesArrayAdapter.add(new String[]{noDevices, ""});
            setTitle(R.string.none_paired);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners

        this.unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {


        // Set result CANCELED incase the user backs out


        if (mCallingActivity.equals(RemoteControlActivity.class.getName())) {
            Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED, intent);
            finish();

        } else {

        Intent intent = new Intent(getApplicationContext(), StoryBuilderActivity.class);
        startActivity(intent);
            finish();
        }
        }


    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices

      //  findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter



        mBtAdapter.startDiscovery();


    }


    private OnItemClickListener mDeviceClickListener;

    {
        mDeviceClickListener = new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
                String device_name = ((TextView) v.findViewById(R.id.device_name)).getText().toString();

//                if (device_name.startsWith("EV3")) {
//                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_EV3), Toast.LENGTH_SHORT);
//                    return;
//                }

                String device_address;
                device_address = ((TextView) v.findViewById(R.id.device_address)).getText().toString();

                // Cancel discovery because it's costly and we're about to connect

                mBtAdapter.cancelDiscovery();

                // Create the result Intent and include the infos

                Intent intent = new Intent();
                Bundle data = new Bundle();
                data.putString(EXTRA_DEVICE_NAME, device_name);
                data.putString(EXTRA_DEVICE_ADDRESS, device_address);
                data.putBoolean(PAIRING, av.getId() == R.id.new_devices);
                intent.putExtras(data);

                // Set result and finish this Activity

                setResult(RESULT_OK, intent);
                finish();

            }
        };
    }


    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // If it's already paired, skip it, because it's been listed already

                if (device != null) {
                    if ((device.getBondState() != BluetoothDevice.BOND_BONDED) && (device.getAddress().startsWith(RobotConnectorService.OUI_LEGO))) {
                        mNewDevicesArrayAdapter.add(new String[]{device.getName(),device.getAddress()});
                    }
                }

                // When discovery is finished, change the Activity title

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
//                    String noDevices = getResources().getText(R.string.none_found).toString();
//                    mNewDevicesArrayAdapter.add(new String[]{noDevices,""});
                    setTitle(R.string.no_more_devices);
                    mScanButton.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    static class ViewHolder {
        public TextView title;
        public TextView subTitle;
        public ImageView image;
    }


    private class ChooseDeviceArrayAdapter extends ArrayAdapter<String[]> {

        public final Activity mContext;

        public ChooseDeviceArrayAdapter(Activity context, int resource) {
            super(context, resource);
            mContext = context;

        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = mContext.getLayoutInflater();
                rowView = inflater.inflate(R.layout.device_name, null);
                ViewHolder viewHolder = new ViewHolder();
               if (rowView!=null) {
                viewHolder.title = (TextView) rowView.findViewById(R.id.device_name);
                viewHolder.subTitle = (TextView) rowView.findViewById(R.id.device_address);
                viewHolder.image = (ImageView) rowView
                        .findViewById(R.id.icon);
                rowView.setTag(viewHolder);
            }
            }
            if (rowView!=null) {
            ViewHolder holder = (ViewHolder) rowView.getTag();
            String[] s = getItem(position);
            if (s!=null) {
            holder.title.setText(s[0]);
            holder.subTitle.setText(s[1]);
                if (s[0]!=null) {
            if (s[0].startsWith("EV3")) {
                holder.image.setImageResource(R.drawable.ev3_icon);
            } else if (s[0].startsWith("NXT")){
                holder.image.setImageResource(R.drawable.nxt_icon);
            } else {
                holder.image.setImageResource(R.drawable.logo);
            }
                } else {
                    holder.image.setImageResource(R.drawable.logo);
                }
            }

            return rowView;
            }
            return null;
        }



    }


}







