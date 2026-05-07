package com.example.asetpeminjaman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboardFragment extends Fragment {

    private TextView tvKpiTotalAset, tvKpiJenisAset, tvKpiPendingPinjam, tvKpiPendingKembali;
    private LinearLayout containerKapasitasAdmin;
    private DataManager dataManager;
    private DataManager.DataChangeListener dataListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        dataManager = DataManager.getInstance();
        
        // Listener untuk update data otomatis dari Firestore
        dataListener = this::refreshData;
        dataManager.addListener(dataListener);

        // Bind Views
        tvKpiTotalAset = view.findViewById(R.id.tvKpiTotalAset);
        tvKpiJenisAset = view.findViewById(R.id.tvKpiJenisAset);
        tvKpiPendingPinjam = view.findViewById(R.id.tvKpiPendingPinjam);
        tvKpiPendingKembali = view.findViewById(R.id.tvKpiPendingKembali);
        containerKapasitasAdmin = view.findViewById(R.id.containerKapasitasAdmin);

        // Click Listeners untuk navigasi fitur Admin
        view.findViewById(R.id.btnAdminBack).setOnClickListener(v -> logout());
        
        view.findViewById(R.id.tvAdminLihatRiwayat).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_admin_schedule);
            }
        });

        // Navigasi Filter Riwayat dari Card
        view.findViewById(R.id.cardKpiPendingPinjam).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_admin_approve);
            }
        });

        view.findViewById(R.id.cardKpiPendingKembali).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_admin_approve);
            }
        });

        view.findViewById(R.id.cardKpiTotalAset).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_admin_inventory);
            }
        });

        refreshData();

        return view;
    }

    private void logout() {
        if (getActivity() != null) {
            SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
            pref.edit().clear().apply();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataManager != null && dataListener != null) {
            dataManager.removeListener(dataListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        if (!isAdded()) return;

        // 1. Update KPI Stats dari DataManager
        tvKpiTotalAset.setText(String.valueOf(dataManager.getTotalAset()));
        tvKpiJenisAset.setText(String.valueOf(dataManager.getTotalJenisAset()));
        tvKpiPendingPinjam.setText(String.valueOf(dataManager.getCountPendingPersetujuan()));
        tvKpiPendingKembali.setText(String.valueOf(dataManager.getCountPendingPengembalian()));

        // 2. Update Kapasitas Per Kategori (Dinamis sesuai database)
        updateCapacities();
    }

    private void updateCapacities() {
        if (containerKapasitasAdmin == null) return;
        containerKapasitasAdmin.removeAllViews();

        Map<String, int[]> categoryStats = new HashMap<>(); // [total, dipinjam]

        for (DataAset aset : dataManager.getAllAset()) {
            String kat = aset.getKategori();
            if (!categoryStats.containsKey(kat)) {
                categoryStats.put(kat, new int[]{0, 0});
            }
            int[] stats = categoryStats.get(kat);
            if (stats != null) {
                stats[0] += aset.getStokTotal();
                stats[1] += aset.getStokDipinjam();
            }
        }

        int[] drawables = {
            R.drawable.progress_custom_jaringan,
            R.drawable.progress_custom_hardware,
            R.drawable.progress_custom_software
        };
        int categoryCount = 0;

        for (Map.Entry<String, int[]> entry : categoryStats.entrySet()) {
            String kat = entry.getKey();
            int[] stats = entry.getValue();
            if (stats == null) continue;
            
            int total = stats[0];
            int dipinjam = stats[1];

            View itemView = getLayoutInflater().inflate(R.layout.item_kapasitas_kategori, containerKapasitasAdmin, false);
            TextView tvNama = itemView.findViewById(R.id.tvKatNama);
            TextView tvPersen = itemView.findViewById(R.id.tvKatPersen);
            ProgressBar pb = itemView.findViewById(R.id.pbKatProgress);

            tvNama.setText(kat);
            
            int tersedia = total - dipinjam;
            int percent = (total > 0) ? (tersedia * 100) / total : 0;
            
            String percentText = percent + "%";
            tvPersen.setText(percentText);
            pb.setProgress(percent);
            pb.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), drawables[categoryCount % drawables.length], null));
            
            containerKapasitasAdmin.addView(itemView);
            categoryCount++;
        }
    }
}
