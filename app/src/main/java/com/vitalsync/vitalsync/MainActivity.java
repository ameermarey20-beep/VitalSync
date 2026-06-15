package com.vitalsync.vitalsync;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private boolean isBluetoothConnected = false;
    private int lastBpm = 0;

    public void setBluetoothConnected(boolean connected) {
        this.isBluetoothConnected = connected;
    }

    public boolean isBluetoothConnected() {
        return isBluetoothConnected;
    }

    public void setLastBpm(int bpm) {
        this.lastBpm = bpm;
    }

    public int getLastBpm() {
        return lastBpm;
    }

    // 1. استقبال الـ MAC Address وتمريره مباشرة للـ HomeFragment
    private final ActivityResultLauncher<Intent> selectDeviceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String address = result.getData().getStringExtra("deviceAddress");

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (currentFragment instanceof HomeFragment) {
                        ((HomeFragment) currentFragment).connectToRobot(address);
                    }
                }
            }
    );

    // لانشر مخصص لطلب الأذونات من المستخدم برمجياً
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (!allGranted) {
                    Toast.makeText(this, "الرجاء الموافقة على صلاحيات الأجهزة القريبة ليعمل البلوتوث بنجاح", Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // تشغيل فحص وطلب الصلاحيات تلقائياً فور تشغيل التطبيق تفادياً للكراش على الريدمي
        checkAndRequestBluetoothPermissions();

        // فتح صفحة الـ Home تلقائياً عند التشغيل الأول
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // برمجة التنقل بين الـ Fragments المتبقية
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.nav_stats) {
                    selectedFragment = new StatsFragment();
                } else if (id == R.id.nav_history) {
                    selectedFragment = new HistoryFragment();
                } else if (id == R.id.nav_rescue) {
                    selectedFragment = new RescueGuideFragment();
                } else if (id == R.id.nav_profile) {
                    selectedFragment = new Fragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            }
        });
    }

    private void showLockedFeatureMessage() {
        Toast.makeText(this, "Please connect to the Vital Sync robot via Bluetooth first to unlock stats and history!", Toast.LENGTH_LONG).show();
    }

    // دالة فحص الصلاحيات البرمجية المتوافقة مع أجهزة الأندرويد الحديثة (شاومي وريدمي)
    private void checkAndRequestBluetoothPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // أندرويد 12 فما فوق
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else { // أندرويد 11 وأقل يحتاج صلاحية الموقع للوصول للبلوتوث
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissionsLauncher.launch(permissionsNeeded.toArray(new String[0]));
        }
    }

    // 2. الدالة المخصصة لاستدعائها من داخل الـ HomeFragment لفتح شاشة اختيار جهاز البلوتوث
    public void openBluetoothDeviceList() {
        Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
        selectDeviceLauncher.launch(intent);
    }

    public void navigateToRescue() {
        bottomNav.setSelectedItemId(R.id.nav_rescue);
    }
}