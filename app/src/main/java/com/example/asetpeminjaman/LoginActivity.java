package com.example.asetpeminjaman;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.SharedPreferences;
import java.util.Objects;

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

        btnLogin.setOnClickListener(v -> {
            Editable userEditable = etUsername.getText();
            Editable passEditable = etPassword.getText();
            
            String user = userEditable != null ? userEditable.toString().trim() : "";
            String pass = passEditable != null ? passEditable.toString().trim() : "";

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(user, pass);
        });
    }

    private void loginUser(String user, String pass) {
        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.please_wait);

        // 1. Cek Akun Admin Spesial (Hardcoded) - Bisa masuk tanpa perlu ada di Firestore
        if (user.equalsIgnoreCase("admin") && Objects.equals(pass, "12345")) {
            SharedPreferences pref = getSharedPreferences("USER_DATA", MODE_PRIVATE);
            pref.edit()
                    .putString("username", "Admin")
                    .putString("role", "admin")
                    .apply();

            Toast.makeText(LoginActivity.this, "Login Admin Berhasil!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        // 2. Jika bukan admin, cek ke Firestore untuk User biasa
        db.collection("users")
                .whereEqualTo("username", user)
                .whereEqualTo("password", pass)
                .get()
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.login_title);

                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Simpan data User biasa ke SharedPreferences
                        SharedPreferences pref = getSharedPreferences("USER_DATA", MODE_PRIVATE);
                        pref.edit()
                                .putString("username", user)
                                .putString("role", "user")
                                .apply();

                        Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Login Gagal
                        String errorMsg = "Username atau Password salah";
                        if (!task.isSuccessful()) {
                            errorMsg = "Koneksi database bermasalah";
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
