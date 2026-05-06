package com.example.asetpeminjaman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardFragment extends Fragment {

    private ListView listViewPending;
    private TextView tvAdminPendingPinjam, tvAdminPendingKembali, tvAdminTotalAset;
    private DataManager dataManager;
    private List<DataPeminjaman> listPending;
    private AdminAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        dataManager = DataManager.getInstance();
        listViewPending = view.findViewById(R.id.listViewPending);
        tvAdminPendingPinjam = view.findViewById(R.id.tvAdminPendingPinjam);
        tvAdminPendingKembali = view.findViewById(R.id.tvAdminPendingKembali);
        tvAdminTotalAset = view.findViewById(R.id.tvAdminTotalAset);
        ImageView btnLogout = view.findViewById(R.id.btnLogoutAdmin);

        listPending = new ArrayList<>();
        adapter = new AdminAdapter(listPending);
        listViewPending.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> logout());

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
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        listPending.clear();
        listPending.addAll(dataManager.getPeminjamanPending());
        adapter.notifyDataSetChanged();
        
        // Update Stats
        tvAdminPendingPinjam.setText(String.valueOf(dataManager.getCountPendingPersetujuan()));
        tvAdminPendingKembali.setText(String.valueOf(dataManager.getCountPendingPengembalian()));
        tvAdminTotalAset.setText(String.valueOf(dataManager.getTotalAset()));
    }

    private class AdminAdapter extends ArrayAdapter<DataPeminjaman> {
        public AdminAdapter(List<DataPeminjaman> data) {
            super(getActivity(), R.layout.item_peminjaman_admin, data);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_peminjaman_admin, parent, false);
            }
            DataPeminjaman p = getItem(position);
            if (p != null) {
                TextView tvNama = convertView.findViewById(R.id.tvAdminNama);
                TextView tvStatusBadge = convertView.findViewById(R.id.tvAdminStatusBadge);
                
                // Menampilkan Nama Peminjam + Username Akun
                tvNama.setText(p.getNama() + " (@" + p.getAccountUsername() + ")");
                
                // Handle Badge
                if (p.getStatus().equals("Menunggu Persetujuan")) {
                    tvStatusBadge.setText("PINJAM");
                    tvStatusBadge.setBackground(getResources().getDrawable(R.drawable.bg_button_primary));
                } else {
                    tvStatusBadge.setText("KEMBALI");
                    tvStatusBadge.setBackground(getResources().getDrawable(R.drawable.bg_button_secondary));
                    tvStatusBadge.getBackground().setTint(getResources().getColor(R.color.accent_orange));
                }

                ((TextView)convertView.findViewById(R.id.tvAdminKeperluan)).setText("Keperluan: " + p.getKeperluan());

                LinearLayout container = convertView.findViewById(R.id.containerAdminItems);
                container.removeAllViews();
                for (ItemPinjam item : p.getItems()) {
                    View subItem = getLayoutInflater().inflate(R.layout.item_peminjaman_subitem, container, false);
                    ((TextView)subItem.findViewById(R.id.tvSubItemNama)).setText(item.getNamaAset());
                    ((TextView)subItem.findViewById(R.id.tvSubItemJumlah)).setText("x" + item.getJumlah());
                    container.addView(subItem);
                }

                Button btnApprove = convertView.findViewById(R.id.btnApprove);
                Button btnReject = convertView.findViewById(R.id.btnReject);

                btnApprove.setOnClickListener(v -> {
                    String targetStatus = p.getStatus().equals("Menunggu Persetujuan") ? "Dipinjam" : "Dikembalikan";
                    dataManager.setStatusPeminjaman(p.getId(), targetStatus);
                    Toast.makeText(getContext(), "Persetujuan Berhasil", Toast.LENGTH_SHORT).show();
                    refreshData();
                });

                btnReject.setOnClickListener(v -> {
                    String targetStatus = p.getStatus().equals("Menunggu Persetujuan") ? "Ditolak" : "Dipinjam";
                    dataManager.setStatusPeminjaman(p.getId(), targetStatus);
                    Toast.makeText(getContext(), "Pengajuan Ditolak", Toast.LENGTH_SHORT).show();
                    refreshData();
                });
            }
            return convertView;
        }
    }
}
