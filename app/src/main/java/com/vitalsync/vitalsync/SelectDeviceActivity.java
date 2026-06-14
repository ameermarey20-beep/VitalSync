package com.vitalsync.vitalsync;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Set;

public class SelectDeviceActivity extends AppCompatActivity {
    private ListView deviceList;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> deviceNameList = new ArrayList<>();
    private ArrayList<String> deviceAddressList = new ArrayList<>();

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // إنشاء قائمة برمجياً دون الحاجة لتعديل ملفات الـ XML المعقدة لديك
        deviceList = new ListView(this);
        setContentView(deviceList);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "البلوتوث غير مدعوم", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceNameList.add(device.getName() + "\n" + device.getAddress());
                deviceAddressList.add(device.getAddress());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNameList);
        deviceList.setAdapter(adapter);

        deviceList.setOnItemClickListener((parent, view, position, id) -> {
            String macAddress = deviceAddressList.get(position);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("deviceAddress", macAddress);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });
    }
}
