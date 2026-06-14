package com.vitalsync.vitalsync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.vitalsync.vitalsync.R;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String name = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (!name.isEmpty() && !email.isEmpty() && pass.length() >= 6) {
                FireBaseServices fbs = FireBaseServices.getInstance();
                fbs.getAuth().createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                                // نمرر اسم المستخدم للصفحة الرئيسية لتظهر كلمة Welcome
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.putExtra("USER_NAME", name);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Please fill all fields (Password min 6 chars)", Toast.LENGTH_SHORT).show();
            }
        });
    }
}