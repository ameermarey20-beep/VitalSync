package com.vitalsync.vitalsync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// استيراد Firebase الأساسي
import com.google.firebase.auth.FirebaseAuth;
import com.vitalsync.vitalsync.LoginActivity;
import com.vitalsync.vitalsync.R;

public class ProfileFragment extends Fragment {

    private EditText etAge, etBloodType, etWeight, etHeight, etConditions;
    private Switch swAthlete;
    private Button btnSaveProfile, btnLogout;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile, container, false);

        // ربط العناصر
        etAge = view.findViewById(R.id.etAge);
        etBloodType = view.findViewById(R.id.etBloodType);
        etWeight = view.findViewById(R.id.etWeight);
        etHeight = view.findViewById(R.id.etHeight);
        etConditions = view.findViewById(R.id.etConditions);
        swAthlete = view.findViewById(R.id.swAthlete);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        loadData();

        btnSaveProfile.setOnClickListener(v -> saveData());

        // زر تسجيل الخروج باستخدام FirebaseAuth مباشرة
        btnLogout.setOnClickListener(v -> {
            try {
                // تسجيل الخروج مباشرة
                FirebaseAuth.getInstance().signOut();

                // الانتقال لصفحة LoginActivity
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();

            } catch (Exception e) {
                Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("age", etAge.getText().toString());
        editor.putString("blood", etBloodType.getText().toString());
        editor.putString("weight", etWeight.getText().toString());
        editor.putString("height", etHeight.getText().toString());
        editor.putString("conditions", etConditions.getText().toString());
        editor.putBoolean("isAthlete", swAthlete.isChecked());
        editor.apply();
        Toast.makeText(getActivity(), "Profile Saved!", Toast.LENGTH_SHORT).show();
    }

    private void loadData() {
        etAge.setText(sharedPreferences.getString("age", ""));
        etBloodType.setText(sharedPreferences.getString("blood", ""));
        etWeight.setText(sharedPreferences.getString("weight", ""));
        etHeight.setText(sharedPreferences.getString("height", ""));
        etConditions.setText(sharedPreferences.getString("conditions", ""));
        swAthlete.setChecked(sharedPreferences.getBoolean("isAthlete", false));
    }
}