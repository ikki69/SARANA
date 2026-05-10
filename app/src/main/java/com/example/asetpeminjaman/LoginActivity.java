package com.example.asetpeminjaman;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Pemicu inisialisasi database otomatis
        DataManager.getInstance();

        db = FirebaseFirestore.getInstance();
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

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

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lupa Password");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);
        builder.setView(view);

        EditText etUser = view.findViewById(R.id.etForgotUser);
        EditText etLastChars = view.findViewById(R.id.etLastThreeChars);
        Button btnVerify = view.findViewById(R.id.btnVerifyHistory);

        AlertDialog dialog = builder.create();

        btnVerify.setOnClickListener(v -> {
            String username = etUser.getText().toString().trim();
            String inputChars = etLastChars.getText().toString().trim();

            if (username.isEmpty() || inputChars.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(username).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    List<String> history = (List<String>) task.getResult().get("passwordHistory");
                    if (history != null && history.size() >= 3) {
                        // Gabungkan riwayat menjadi satu string kunci
                        String secretKey = history.get(0) + history.get(1) + history.get(2);
                        
                        if (secretKey.equals(inputChars)) {
                            dialog.dismiss();
                            showChangePasswordDialog(username);
                        } else {
                            Toast.makeText(this, "3 Karakter terakhir salah!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Data verifikasi belum siap, silakan coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Username tidak ditemukan!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showChangePasswordDialog(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ubah Password Baru");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        builder.setView(view);

        EditText etNewPass = view.findViewById(R.id.etNewPass);
        Button btnSave = view.findViewById(R.id.btnSaveNewPass);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String newPass = etNewPass.getText().toString().trim();
            if (newPass.length() < 5) {
                Toast.makeText(this, "Password minimal 5 karakter!", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(username).update("password", newPass)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Password berhasil diubah!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
        });

        dialog.show();
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
