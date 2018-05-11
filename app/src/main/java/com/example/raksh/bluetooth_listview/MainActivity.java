package com.example.raksh.bluetooth_listview;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private static final int REQUEST_ENABLE_BT = 123;
    private static final int REQUEST_PICK_FILE = 124;
    Switch toggleSwitch;
    ListView lv;
    BluetoothAdapter bluetoothAdapter;

    BroadcastReceiver bluetoothDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(device.getName());
                final ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1, devices);

                lv.setAdapter(adapter);
                Log.d(getLocalClassName(), "Device found: " + device.getAddress());
            }
        }
    };
    ArrayList devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = findViewById(R.id.listview);

        devices = new ArrayList<>();

        toggleSwitch = findViewById(R.id.toggle);

        toggleSwitch.setOnCheckedChangeListener(this);

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_PICK_FILE);
            }
        });

        bluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "No Bluetooth adapter detected", Toast.LENGTH_SHORT).show();
            return;
        }

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        registerReceiver(bluetoothDeviceReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothDeviceReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toggleSwitch.isChecked() != bluetoothAdapter.isEnabled()) {

            toggleSwitch.setOnCheckedChangeListener(null);
            toggleSwitch.toggle();
            toggleSwitch.setOnCheckedChangeListener(this);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            toggleSwitch.setText("Bluetooth_on");
        } else {

            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.disable();
            devices.clear();
            final ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1, devices);
            lv.setAdapter(adapter);
            toggleSwitch.setText("Bluetooth_off");

            Log.d(getLocalClassName(), "Disabled Bluetooth");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            Log.d(getLocalClassName(), String.valueOf(bluetoothAdapter.startDiscovery()));
        } else if (requestCode == REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            Uri uri = null;
            uri = data.getData();
            Log.i(getLocalClassName(), "Uri: " + uri.toString());
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(shareIntent);
        }
    }
}