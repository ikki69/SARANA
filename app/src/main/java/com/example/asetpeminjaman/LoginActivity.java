package com.example.asetpeminjaman;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.content.SharedPreferences;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUsername.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(user, pass);
            }
        });
    }

    private void loginUser(String user, String pass) {
        btnLogin.setEnabled(false);
        btnLogin.setText("MOHON TUNGGU...");

        db.collection("users")
                .whereEqualTo("username", user)
                .whereEqualTo("password", pass)
                .get()
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("MASUK");
                    
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Simpan username ke SharedPreferences
                        SharedPreferences pref = getSharedPreferences("USER_DATA", MODE_PRIVATE);
                        
                        // Cek Admin Special User
                        if (user.equalsIgnoreCase("admin") && pass.equals("12345")) {
                            pref.edit().putString("username", "Admin").putString("role", "admin").apply();
                            Toast.makeText(LoginActivity.this, "Login Admin Berhasil!", Toast.LENGTH_SHORT).show();
                        } else {
                            pref.edit().putString("username", user).putString("role", "user").apply();
                            Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                        }

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Login Gagal
                        Toast.makeText(LoginActivity.this, "Username atau Password salah", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
