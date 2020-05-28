package com.ecopaynet.ecoa10_gettingstarted;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ecopaynet.ecoa10.Device;
import com.ecopaynet.ecoa10.DeviceBluetooth;
import com.ecopaynet.ecoa10.DeviceSerial;
import com.ecopaynet.ecoa10.DeviceTcpip;
import com.ecopaynet.ecoa10.EcoA10;
import com.ecopaynet.ecoa10_gettingstarted.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceSelectionActivity extends AppCompatActivity
    implements AdapterView.OnItemClickListener
{
    ProgressDialog progressDialog;
    List<Device> availableDevicesList;
    ListView devicesListView;
    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_selection);

        devicesListView = (ListView) findViewById( R.id.availableDevicesListView);
        devicesListView.setOnItemClickListener(this);

        this.progressDialog = ProgressDialog.show(this, "Loading Devices", "Please wait...");

        loadDevices();

        showDevices();
    }

    private void loadDevices()
    {
        this.availableDevicesList = new ArrayList<>();

        //Force add tcpip device
        this.availableDevicesList.add(new DeviceTcpip("Local Device", "127.0.0.1", 5556));

        //Force add serial device
        this.availableDevicesList.add(new DeviceSerial());

        //Load bluetooth devices
        this.availableDevicesList.addAll(EcoA10.getBluetoothPairedDevices());

    }

    private void showDevices()
    {
        ArrayList<String> deviceNamesList = new ArrayList<>();
        for (int index = 0; index < availableDevicesList.size(); index++)
        {
            Device device = availableDevicesList.get(index);

            String deviceName = "";
            switch (device.getType())
            {
                case BLUETOOTH:
                    {
                        deviceName = "BT: " + device.getName();
                    }
                    break;
                case SERIAL:
                    {
                        deviceName = "Serial Port Device";
                    }
                    break;
                case TCPIP:
                {
                    deviceName = device.getName();
                }
                break;
            }
            deviceNamesList.add(deviceName);
        }
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNamesList);
        devicesListView.setAdapter( listAdapter );

        this.progressDialog.hide();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if(parent.getId() == R.id.availableDevicesListView)
        {
            Device selectedDevice = this.availableDevicesList.get((int)id);

            Intent intent=new Intent();
            intent.putExtra("DEVICE_TYPE", selectedDevice.getType().toString());
            switch (selectedDevice.getType())
            {
                case BLUETOOTH:
                {
                    intent.putExtra("DEVICE_NAME", selectedDevice.getName());
                    intent.putExtra("DEVICE_ADDRESS", ((DeviceBluetooth)selectedDevice).getAddress());
                }
                break;
                case SERIAL:
                {
                    //
                }
                break;
                case TCPIP:
                {
                    intent.putExtra("DEVICE_NAME", selectedDevice.getName());
                    intent.putExtra("DEVICE_IP_ADDRESS", ((DeviceTcpip)selectedDevice).getIpAddress());
                    intent.putExtra("DEVICE_PORT", ((DeviceTcpip)selectedDevice).getPort());
                }
                break;
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}
