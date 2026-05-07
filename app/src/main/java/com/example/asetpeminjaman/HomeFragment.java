package com.example.asetpeminjaman;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvTotalAset, tvTotalDipinjam, tvTotalKembali, tvTotalTerlambat, tvWelcome;
    private LinearLayout layoutPeminjamanAktif;
    private LinearLayout cardTotalAset, cardTotalDipinjam, cardTotalKembali, cardTotalTerlambat;
    private TextView tvLihatSemua, tvProgressTitle, tvProgressPercent, tvProgressFraction;
    private android.widget.ProgressBar pbKapasitas;
    private Button btnQuickPinjam;
    private ImageView btnLogout;
    private DataManager dataManager;
    private DataManager.DataChangeListener dataListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        dataManager = DataManager.getInstance();
        
        // Setup listener untuk update otomatis
        dataListener = () -> {
            if (isAdded()) {
                updateStatistik();
                tampilkanPeminjamanAktif();
            }
        };
        dataManager.addListener(dataListener);
        
        tvTotalAset = view.findViewById(R.id.tvTotalAset);
        tvTotalDipinjam = view.findViewById(R.id.tvTotalDipinjam);
        tvTotalKembali = view.findViewById(R.id.tvTotalKembali);
        tvTotalTerlambat = view.findViewById(R.id.tvTotalTerlambat);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        layoutPeminjamanAktif = view.findViewById(R.id.layoutPeminjamanAktif);
        tvLihatSemua = view.findViewById(R.id.tvLihatSemua);
        btnQuickPinjam = view.findViewById(R.id.btnQuickPinjam);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Progress Card Views
        tvProgressTitle = view.findViewById(R.id.tvProgressTitle);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        tvProgressFraction = view.findViewById(R.id.tvProgressFraction);
        pbKapasitas = view.findViewById(R.id.pbKapasitas);

        // Inisialisasi Card Layouts
        cardTotalAset = view.findViewById(R.id.cardTotalAset);
        cardTotalDipinjam = view.findViewById(R.id.cardTotalDipinjam);
        cardTotalKembali = view.findViewById(R.id.cardTotalKembali);
        cardTotalTerlambat = view.findViewById(R.id.cardTotalTerlambat);

        // Klik pada Card Total Aset
        cardTotalAset.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_inventory);
            }
        });

        // Klik pada Card Dipinjam, Kembali & Terlambat arahkan ke tab Riwayat dengan filter
        cardTotalDipinjam.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_history, "DIPINJAM");
            }
        });
        cardTotalKembali.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_history, "KEMBALI");
            }
        });
        cardTotalTerlambat.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_history, "TERLAMBAT");
            }
        });

        btnLogout.setOnClickListener(v -> {
            if (getActivity() != null) {
                SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
                pref.edit().clear().apply();
                
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
                Toast.makeText(getActivity(), R.string.logout_success, Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences pref = getActivity() != null ? getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE) : null;
        String username = pref != null ? pref.getString("username", "Pengguna") : "Pengguna";
        tvWelcome.setText(getString(R.string.welcome_user, username));

        btnQuickPinjam.setOnClickListener(v -> startActivity(new Intent(getActivity(), FormPeminjamanActivity.class)));

        tvLihatSemua.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_history);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatistik();
        tampilkanPeminjamanAktif();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataManager != null && dataListener != null) {
            dataManager.removeListener(dataListener);
        }
    }

    private void updateStatistik() {
        if (!isAdded()) return;
        
        SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        String username = pref.getString("username", "");
        String role = pref.getString("role", "user");
        boolean isAdmin = "admin".equals(role);

        int totalAset = dataManager.getTotalAset();
        int totalDipinjam = dataManager.getTotalDipinjam(username, isAdmin);
        int totalKembali = dataManager.getTotalDikembalikan(username, isAdmin);
        int totalTerlambat = dataManager.getTotalTerlambat(username, isAdmin);
        int itemDipinjam = dataManager.getTotalItemDipinjam(username, isAdmin);

        tvTotalAset.setText(String.valueOf(totalAset));
        tvTotalDipinjam.setText(String.valueOf(totalDipinjam));
        tvTotalKembali.setText(String.valueOf(totalKembali));
        tvTotalTerlambat.setText(String.valueOf(totalTerlambat));

        // Update Progress Card
        if (isAdmin) {
            tvProgressTitle.setText(getString(R.string.item_dipinjam_format, itemDipinjam, totalAset));
        } else {
            tvProgressTitle.setText(getString(R.string.user_pinjam_format, itemDipinjam));
        }

        tvProgressFraction.setText(getString(R.string.progress_fraction, itemDipinjam, totalAset));
        
        int percent = (totalAset > 0) ? (itemDipinjam * 100) / totalAset : 0;
        tvProgressPercent.setText(getString(R.string.progress_percent, percent));
        pbKapasitas.setProgress(percent);
    }

    private void tampilkanPeminjamanAktif() {
        layoutPeminjamanAktif.removeAllViews();
        
        SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        String username = pref.getString("username", "");
        
        List<DataPeminjaman> aktif = dataManager.getPeminjamanAktifByUser(username);

        if (aktif.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText(R.string.peminjaman_aktif_empty);
            emptyText.setPadding(0, 40, 0, 40);
            emptyText.setGravity(android.view.Gravity.CENTER);
            layoutPeminjamanAktif.addView(emptyText);
        } else {
            int maxTampil = Math.min(aktif.size(), 3);
            for (int i = aktif.size() - 1; i >= aktif.size() - maxTampil; i--) {
                final DataPeminjaman peminjaman = aktif.get(i);
                View itemView = getLayoutInflater().inflate(R.layout.item_peminjaman, layoutPeminjamanAktif, false);

                TextView tvNama = itemView.findViewById(R.id.tvItemNama);
                TextView tvNim = itemView.findViewById(R.id.tvItemNim);
                LinearLayout containerItems = itemView.findViewById(R.id.containerItemPeminjaman);
                TextView tvTanggal = itemView.findViewById(R.id.tvItemTanggal);
                TextView tvStatus = itemView.findViewById(R.id.tvItemStatus);

                tvNama.setText(peminjaman.getNama());
                tvNim.setText(peminjaman.getNim());
                
                containerItems.removeAllViews();
                for (ItemPinjam item : peminjaman.getItems()) {
                    View subItem = getLayoutInflater().inflate(R.layout.item_peminjaman_subitem, containerItems, false);
                    ((TextView)subItem.findViewById(R.id.tvSubItemNama)).setText(item.getNamaAset());
                    ((TextView)subItem.findViewById(R.id.tvSubItemJumlah)).setText("x" + item.getJumlah());
                    containerItems.addView(subItem);
                }

                tvTanggal.setText(getString(R.string.pinjam_kembali_format, peminjaman.getTanggalPinjam(), peminjaman.getTanggalRencanaKembali()));
                tvStatus.setText(peminjaman.getStatus());

                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), DetailPeminjamanActivity.class);
                    intent.putExtra("PEMINJAMAN_ID", peminjaman.getId());
                    startActivity(intent);
                });

                layoutPeminjamanAktif.addView(itemView);
            }
        }
    }
}
