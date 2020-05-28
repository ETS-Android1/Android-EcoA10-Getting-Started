package com.ecopaynet.ecoa10_gettingstarted;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ecopaynet.ecoa10.Device;
import com.ecopaynet.ecoa10.DeviceBluetooth;
import com.ecopaynet.ecoa10.DeviceSerial;
import com.ecopaynet.ecoa10.DeviceTcpip;
import com.ecopaynet.ecoa10.EcoA10;
import com.ecopaynet.ecoa10.Environment;
import com.ecopaynet.ecoa10.Error;
import com.ecopaynet.ecoa10.Events;
import com.ecopaynet.ecoa10.Information;
import com.ecopaynet.ecoa10.LogLevel;
import com.ecopaynet.ecoa10.Status;

public class MainActivity extends AppCompatActivity
    implements Events.Initialization,
        Events.Log
{
    Button selectDeviceButton;
    TextView selectedDeviceInfoTextView;
    Button initializeEcoA10Button;
    Button saleButton;
    Button refundButton;
    Button terminateButton;
    TextView deviceInformationTextView;

    Device selectedDevice = null;

    ProgressDialog progressDialog;

    SharedPreferences sharedPreferences;
    String sharedPreferencesKey = "com.ecopaynet.ecoa10_gettingstarted_preferences";

    static final int DEVICE_SELECTION_REQUEST_CODE = 1;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check file write permissions
        isStoragePermissionGranted();

        sharedPreferences = this.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE);

        if (savedInstanceState == null)
        {
            EcoA10.addLogEventHandler(this);
        }

        TextView libraryVersionTextView = (TextView) findViewById(R.id.libraryVersionTextView);
        libraryVersionTextView.setText("ECOA10 Library v" + EcoA10.getLibraryVersion());

        selectedDeviceInfoTextView = (TextView) findViewById(R.id.selectedDeviceInfoTextView);

        deviceInformationTextView = (TextView) findViewById(R.id.deviceInformationTextView);

        selectDeviceButton = (Button) findViewById(R.id.selectDeviceButton);
        selectDeviceButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, DeviceSelectionActivity.class);
                startActivityForResult(intent, DEVICE_SELECTION_REQUEST_CODE);
            }
        });

        initializeEcoA10Button = (Button) findViewById(R.id.initializeEcoA10Button);
        initializeEcoA10Button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                progressDialog = ProgressDialog.show(MainActivity.this, "Initializing Device", "Please wait...");
                EcoA10.setEnvironment(Environment.TEST);
                EcoA10.initialize(selectedDevice, MainActivity.this, MainActivity.this);
            }
        });

        saleButton = (Button) findViewById(R.id.saleButton);
        saleButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, SaleActivity.class);
                startActivity(intent);
            }
        });

        refundButton = (Button) findViewById(R.id.refundButton);
        refundButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, RefundActivity.class);
                startActivity(intent);
            }
        });

        terminateButton = (Button) findViewById(R.id.terminateButton);
        terminateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EcoA10.terminate();
                setPhase2Buttons();
                deviceInformationTextView.setText("");
            }
        });

        this.setPhase1Buttons();
        this.loadSavedDevice();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        if(EcoA10.getStatus() == Status.READY)
        {
            setPhase3Buttons();
            setDeviceInformation();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case DEVICE_SELECTION_REQUEST_CODE:
            {
                if(resultCode == RESULT_OK)
                {
                    this.saveSelectedDevice(data);
                    this.loadSavedDevice();
                }
            }
            break;
        }
    }

    private void saveSelectedDevice(Intent intentData)
    {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();

        String deviceType = intentData.getStringExtra("DEVICE_TYPE");

        editor.putString("DEVICE_TYPE", deviceType);
        if(deviceType.equals("BLUETOOTH"))
        {
            editor.putString("DEVICE_NAME", intentData.getStringExtra("DEVICE_NAME"));
            editor.putString("DEVICE_ADDRESS", intentData.getStringExtra("DEVICE_ADDRESS"));
        }
        else if(deviceType.equals("TCPIP"))
        {
            editor.putString("DEVICE_NAME", intentData.getStringExtra("DEVICE_NAME"));
            editor.putString("DEVICE_IP_ADDRESS", intentData.getStringExtra("DEVICE_IP_ADDRESS"));
            editor.putInt("DEVICE_PORT", intentData.getIntExtra("DEVICE_PORT", 0));
        }
        editor.apply();
    }

    private void loadSavedDevice()
    {
        String deviceType = this.sharedPreferences.getString("DEVICE_TYPE", "BLUETOOTH");
        switch (deviceType)
        {
            case "BLUETOOTH":
            {
                String deviceName = this.sharedPreferences.getString("DEVICE_NAME", "");
                String deviceAddress = this.sharedPreferences.getString("DEVICE_ADDRESS", "");
                if(deviceName.length() > 0 && deviceAddress.length() > 0)
                {
                    this.selectedDevice = new DeviceBluetooth(deviceName, deviceAddress);
                    selectedDeviceInfoTextView.setText("Device: " + this.selectedDevice.getName());
                    this.setPhase2Buttons();
                }
            }
            break;
            case "SERIAL":
            {
                this.selectedDevice = new DeviceSerial();
                selectedDeviceInfoTextView.setText("Device: Serial Port");
                this.setPhase2Buttons();
            }
            break;
            case "TCPIP":
            {
                String deviceName = this.sharedPreferences.getString("DEVICE_NAME", "");
                String deviceIpAddress = this.sharedPreferences.getString("DEVICE_IP_ADDRESS", "");
                int devicePort = this.sharedPreferences.getInt("DEVICE_PORT", 0);
                if(deviceName.length() > 0 && deviceIpAddress.length() > 0 && devicePort > 0)
                {
                    this.selectedDevice = new DeviceTcpip(deviceName, deviceIpAddress, devicePort);
                    selectedDeviceInfoTextView.setText("Device: " + this.selectedDevice.getName());
                    this.setPhase2Buttons();
                }
            }
            break;
        }
    }

    private void setPhase1Buttons()
    {
        this.selectDeviceButton.setEnabled(true);
        this.initializeEcoA10Button.setEnabled(false);
        this.saleButton.setEnabled(false);
        this.refundButton.setEnabled(false);
        this.terminateButton.setEnabled(false);
    }

    private void setPhase2Buttons()
    {
        this.selectDeviceButton.setEnabled(true);
        this.initializeEcoA10Button.setEnabled(true);
        this.saleButton.setEnabled(false);
        this.refundButton.setEnabled(false);
        this.terminateButton.setEnabled(false);
    }

    private void setPhase3Buttons()
    {
        this.selectDeviceButton.setEnabled(false);
        this.initializeEcoA10Button.setEnabled(false);
        this.saleButton.setEnabled(true);
        this.refundButton.setEnabled(true);
        this.terminateButton.setEnabled(true);
    }

    private void setDeviceInformation()
    {
        Information information = EcoA10.getInformation();

        String deviceInformation = "";
        deviceInformation += "Environment: " + information.environment + "\r\n";
        deviceInformation += "Commerce name: " + information.commerceName + "\r\n";
        deviceInformation += "Commerce address: " + information.commerceAddress + "\r\n";
        deviceInformation += "Commerce number: " + information.commerceNumber + "\r\n";
        deviceInformation += "Currency: " + information.commerceCurrency.getAlpha() + "\r\n";

        deviceInformationTextView.setText(deviceInformation);
    }

    @Override
    public void onInitializationComplete()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                progressDialog.dismiss();
                setPhase3Buttons();
                setDeviceInformation();
            }
        });
    }

    @Override
    public void onInitializationError(final Error error)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                progressDialog.dismiss();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Initialization error:");
                alertDialogBuilder.setMessage(error.getCode() + " - " + error.getMessage());
                alertDialogBuilder.setPositiveButton("OK", null);
                alertDialogBuilder.show();

                deviceInformationTextView.setText("");
            }
        });
    }

    @Override
    public void onNewMessageLogged(LogLevel logLevel, String s)
    {
        Log.println(Log.DEBUG, "ECOA10_GettingStarted", s);
    }

    public  boolean isStoragePermissionGranted()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }
}
