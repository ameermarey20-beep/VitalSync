package com.vitalsync.vitalsync;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class HomeFragment extends Fragment {

    private TextView tvWelcome, tvHeartRate, tvCondition, tvConnectionStatus;
    private ProgressBar pulseProgressBar;
    private View viewStatusIndicator;
    private Button btnConnectBluetooth;

    // متغيرات البلوتوث لقراءة بيانات الروبوت
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private boolean isConnected = false;
    private Thread receiveThread;

    // معرف البلوتوث القياسي لقطعة الـ HC-05
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // ربط كلاس الجافا بملف التصميم fragment_home.xml
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // تعريف العناصر من الواجهة الحالية المرفقة
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvHeartRate = view.findViewById(R.id.tvHeartRate);
        tvCondition = view.findViewById(R.id.tvCondition);
        tvConnectionStatus = view.findViewById(R.id.tvConnectionStatus);
        pulseProgressBar = view.findViewById(R.id.pulseProgressBar);
        viewStatusIndicator = view.findViewById(R.id.viewStatusIndicator);

        // ربط زر البلوتوث المضاف للـ XML
        btnConnectBluetooth = view.findViewById(R.id.btnConnectBluetooth);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // استقبال اسم المستخدم الممرر من صفحة الـ SignUp وعرضه
        if (getActivity() != null && getActivity().getIntent() != null) {
            String username = getActivity().getIntent().getStringExtra("USER_NAME");
            if (username != null) {
                tvWelcome.setText("Welcome, " + username + "!");
            } else if (FireBaseServices.getInstance().getAuth().getCurrentUser() != null) {
                String email = FireBaseServices.getInstance().getAuth().getCurrentUser().getEmail();
                tvWelcome.setText("Welcome, " + email + "!");
            }
        }

        // عند الضغط على زر البلوتوث، يتم استدعاء دالة فتح الأجهزة من الـ MainActivity
        if (btnConnectBluetooth != null) {
            btnConnectBluetooth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isConnected) {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).openBluetoothDeviceList();
                        }
                    } else {
                        disconnectDevice();
                    }
                }
            });
        }

        return view;
    }

    // دالة الاتصال بالروبوت (يتم استدعاؤها من الـ MainActivity بعد اختيار الـ HC-05)
    @SuppressLint("MissingPermission")
    public void connectToRobot(String macAddress) {
        if (tvConnectionStatus != null) {
            tvConnectionStatus.setText("Connecting...");
            tvConnectionStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800")));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(BT_UUID);
                    bluetoothAdapter.cancelDiscovery();
                    bluetoothSocket.connect();

                    inputStream = bluetoothSocket.getInputStream();
                    isConnected = true;

                    // تحديث واجهة المستخدم بعد نجاح الاتصال مباشرة
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (btnConnectBluetooth != null) btnConnectBluetooth.setText("Disconnect");
                            startListeningForBpm(); // البدء بالاستماع للنبض القادم من الروبوت
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    try { if (bluetoothSocket != null) bluetoothSocket.close(); } catch (IOException ignored) {}

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (tvConnectionStatus != null) {
                                tvConnectionStatus.setText("Failed");
                                tvConnectionStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
                            }
                        }
                    });
                }
            }
        }).start();
    }

    // قراءة البث المستمر القادم من الأردوينو وتفكيك قيم الـ BPM
    private void startListeningForBpm() {
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;
                StringBuilder sb = new StringBuilder();

                while (isConnected && bluetoothSocket != null && bluetoothSocket.isConnected() && !Thread.currentThread().isInterrupted()) {
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            String incomingData = new String(buffer, 0, bytes);
                            sb.append(incomingData);

                            int endOfMessageIndex = sb.indexOf(";");
                            if (endOfMessageIndex >= 0) {
                                String completeMessage = sb.substring(0, endOfMessageIndex);
                                sb.delete(0, endOfMessageIndex + 1);

                                if (completeMessage.contains("BPM:")) {
                                    final String bpmString = completeMessage.replace("BPM:", "").trim();

                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                int bpmValue = Integer.parseInt(bpmString);

                                                if (bpmValue > 0) {
                                                    updateHeartRateUI(bpmValue); // تحديث الألوان الحية والمؤشرات بناءً على قراءتك الأصلية
                                                } else {
                                                    if (tvHeartRate != null) tvHeartRate.setText("--");
                                                    if (tvCondition != null) {
                                                        tvCondition.setText("Place your finger");
                                                        tvCondition.setTextColor(Color.parseColor("#7F8C8D"));
                                                    }
                                                }
                                            } catch (NumberFormatException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        receiveThread.start();
    }

    // دالة تحديث المربعات والألوان والمؤشرات حسب النبض (دالتك الحالية مدمجة بالكامل)
    public void updateHeartRateUI(int bpm) {
        if (tvHeartRate != null) {
            tvHeartRate.setText(String.valueOf(bpm));
            pulseProgressBar.setProgress(bpm);

            tvConnectionStatus.setText("Connected");
            tvConnectionStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));

            if (bpm >= 60 && bpm <= 100) {
                tvCondition.setText("Normal Pulse");
                tvCondition.setTextColor(Color.parseColor("#4CAF50"));
                viewStatusIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            } else if (bpm > 100) {
                tvCondition.setText("High Pulse");
                tvCondition.setTextColor(Color.parseColor("#E53935"));
                viewStatusIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
            } else {
                tvCondition.setText("Low Pulse");
                tvCondition.setTextColor(Color.parseColor("#1E88E5"));
                viewStatusIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E88E5")));
            }
        }
    }

    // قطع الاتصال وإعادة تهيئة العناصر
    private void disconnectDevice() {
        isConnected = false;
        try {
            if (receiveThread != null) receiveThread.interrupt();
            if (inputStream != null) inputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (btnConnectBluetooth != null) btnConnectBluetooth.setText("Connect Bluetooth");
        if (tvHeartRate != null) tvHeartRate.setText("--");
        if (tvCondition != null) {
            tvCondition.setText("No Data");
            tvCondition.setTextColor(Color.parseColor("#3F4850"));
        }
        if (tvConnectionStatus != null) {
            tvConnectionStatus.setText("Waiting for Robot...");
            tvConnectionStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800")));
        }
        if (viewStatusIndicator != null) {
            viewStatusIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectDevice();
    }
}