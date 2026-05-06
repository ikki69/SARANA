package com.example.asetpeminjaman;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.content.SharedPreferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private String userRole = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // Cek Role
        SharedPreferences pref = getSharedPreferences("USER_DATA", MODE_PRIVATE);
        userRole = pref.getString("role", "user");

        if (userRole.equals("admin")) {
            bottomNavigationView.setVisibility(View.GONE);
            loadFragment(new AdminDashboardFragment());
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
            loadFragment(new HomeFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_inventory) {
                fragment = new InventoryFragment();
            } else if (itemId == R.id.nav_history) {
                fragment = new HistoryFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Helper method to switch tab from fragments
     */
    public void switchToTab(int navItemId) {
        bottomNavigationView.setSelectedItemId(navItemId);
    }
}
