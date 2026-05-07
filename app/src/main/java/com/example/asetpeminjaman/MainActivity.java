package com.example.asetpeminjaman;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.content.SharedPreferences;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_admin);
            bottomNavigationView.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
            loadFragment(new AdminDashboardFragment());
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
            loadFragment(new HomeFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Animasi pop-up untuk icon yang ditekan
            View itemView = findViewById(item.getItemId());
            if (itemView != null) {
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.nav_pop_up);
                itemView.startAnimation(anim);
            }

            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_inventory || itemId == R.id.nav_admin_inventory) {
                fragment = new InventoryFragment();
            } else if (itemId == R.id.nav_history) {
                fragment = new HistoryFragment();
            } else if (itemId == R.id.nav_admin_approve) {
                fragment = new ApproveFragment();
            } else if (itemId == R.id.nav_admin_reports) {
                fragment = new AdminDashboardFragment();
            } else if (itemId == R.id.nav_admin_schedule) {
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
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Helper method to switch tab from fragments
     */
    public void switchToTab(int navItemId) {
        switchToTab(navItemId, null);
    }

    public void switchToTab(int navItemId, String filter) {
        if (filter != null) {
            Fragment fragment = null;
            if (navItemId == R.id.nav_history || navItemId == R.id.nav_admin_schedule) {
                fragment = new HistoryFragment();
                Bundle bundle = new Bundle();
                bundle.putString("FILTER_TYPE", filter);
                fragment.setArguments(bundle);
            }
            
            if (fragment != null) {
                loadFragment(fragment);
                bottomNavigationView.getMenu().findItem(navItemId).setChecked(true);
                return;
            }
        }
        bottomNavigationView.setSelectedItemId(navItemId);
    }
}
