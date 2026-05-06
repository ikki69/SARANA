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

    private TextView tvTotalAset, tvTotalDipinjam, tvTotalKembali, tvWelcome;
    private LinearLayout layoutPeminjamanAktif;
    private TextView tvLihatSemua;
    private Button btnQuickPinjam;
    private ImageView btnLogout;
    private DataManager dataManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        dataManager = DataManager.getInstance();
        
        tvTotalAset = view.findViewById(R.id.tvTotalAset);
        tvTotalDipinjam = view.findViewById(R.id.tvTotalDipinjam);
        tvTotalKembali = view.findViewById(R.id.tvTotalKembali);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        layoutPeminjamanAktif = view.findViewById(R.id.layoutPeminjamanAktif);
        tvLihatSemua = view.findViewById(R.id.tvLihatSemua);
        btnQuickPinjam = view.findViewById(R.id.btnQuickPinjam);
        btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            if (getActivity() != null) {
                SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
                pref.edit().clear().apply();
                
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
                Toast.makeText(getActivity(), "Berhasil keluar", Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        String username = pref.getString("username", "Pengguna");
        tvWelcome.setText("Halo, " + username + "!");

        btnQuickPinjam.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), FormPeminjamanActivity.class));
        });

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

    private void updateStatistik() {
        tvTotalAset.setText(String.valueOf(dataManager.getTotalAset()));
        tvTotalDipinjam.setText(String.valueOf(dataManager.getTotalDipinjam()));
        tvTotalKembali.setText(String.valueOf(dataManager.getTotalDikembalikan()));
    }

    private void tampilkanPeminjamanAktif() {
        layoutPeminjamanAktif.removeAllViews();
        
        SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        String username = pref.getString("username", "");
        
        List<DataPeminjaman> aktif = dataManager.getPeminjamanAktifByUser(username);

        if (aktif.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("Belum ada peminjaman aktif");
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

                tvTanggal.setText("Pinjam: " + peminjaman.getTanggalPinjam() + " | Kembali: " + peminjaman.getTanggalRencanaKembali());
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
