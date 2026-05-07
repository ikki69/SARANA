package com.example.asetpeminjaman;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Cek apakah user sudah login
                SharedPreferences pref = getSharedPreferences("USER_DATA", MODE_PRIVATE);
                String username = pref.getString("username", null);

                Intent intent;
                if (username != null) {
                    // Jika sudah login, langsung ke MainActivity
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    // Jika belum login, ke LoginActivity
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
        }, SPLASH_DURATION);
    }
}
