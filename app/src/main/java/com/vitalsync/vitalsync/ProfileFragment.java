package com.vitalsync.vitalsync;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ImageView ivProfilePic;
    private FloatingActionButton fabEditPic;
    private TextInputEditText etAge, etBloodType, etWeight, etHeight, etConditions;
    private Button btnSaveProfile, btnLogout;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivProfilePic.setImageURI(imageUri);
                    uploadImageToFirebase();
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        fabEditPic = view.findViewById(R.id.fabEditPic);
        etAge = view.findViewById(R.id.etAge);
        etBloodType = view.findViewById(R.id.etBloodType);
        etWeight = view.findViewById(R.id.etWeight);
        etHeight = view.findViewById(R.id.etHeight);
        etConditions = view.findViewById(R.id.etConditions);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadUserData();

        fabEditPic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSaveProfile.setOnClickListener(v -> saveHealthData());

        btnLogout.setOnClickListener(v -> {
            FireBaseServices.getInstance().getAuth().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = FireBaseServices.getInstance().getAuth().getCurrentUser();
        if (user != null) {
            FireBaseServices.getInstance().getFirestore().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            etAge.setText(documentSnapshot.getString("age"));
                            etBloodType.setText(documentSnapshot.getString("bloodType"));
                            etWeight.setText(documentSnapshot.getString("weight"));
                            etHeight.setText(documentSnapshot.getString("height"));
                            etConditions.setText(documentSnapshot.getString("conditions"));
                            String photoUrl = documentSnapshot.getString("profilePicUrl");
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                Picasso.get().load(photoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(ivProfilePic);
                            }
                        }
                    });
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            FirebaseUser user = FireBaseServices.getInstance().getAuth().getCurrentUser();
            if (user == null) return;

            StorageReference fileRef = FireBaseServices.getInstance().getStorage()
                    .getReference("profile_pictures/" + user.getUid() + ".jpg");

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> 
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    FireBaseServices.getInstance().getFirestore().collection("users")
                            .document(user.getUid())
                            .update("profilePicUrl", downloadUrl)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show());
                })
            ).addOnFailureListener(e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void saveHealthData() {
        FirebaseUser user = FireBaseServices.getInstance().getAuth().getCurrentUser();
        if (user == null) return;

        Map<String, Object> healthData = new HashMap<>();
        healthData.put("age", etAge.getText().toString());
        healthData.put("bloodType", etBloodType.getText().toString());
        healthData.put("weight", etWeight.getText().toString());
        healthData.put("height", etHeight.getText().toString());
        healthData.put("conditions", etConditions.getText().toString());

        FireBaseServices.getInstance().getFirestore().collection("users")
                .document(user.getUid())
                .set(healthData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Health data saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
