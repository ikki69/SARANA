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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryFragment extends Fragment {

    private RecyclerView recyclerViewAset;
    private TextView tvTotalItem;
    private LinearLayout containerKapasitas;
    private FloatingActionButton fabAddAset;
    private DataManager dataManager;
    private List<DataAset> listAset;
    private List<DataAset> listAsetFilter;
    private AsetRecyclerAdapter adapter;
    private String userRole = "user";
    private DataManager.DataChangeListener dataListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        dataManager = DataManager.getInstance();

        dataListener = this::refreshData;
        dataManager.addListener(dataListener);

        recyclerViewAset = view.findViewById(R.id.recyclerViewAset);
        tvTotalItem = view.findViewById(R.id.tvTotalItem);
        containerKapasitas = view.findViewById(R.id.containerKapasitas);
        fabAddAset = view.findViewById(R.id.fabAddAset);

        // Cek Role
        SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        userRole = pref.getString("role", "user");

        if ("admin".equals(userRole)) {
            fabAddAset.setVisibility(View.VISIBLE);
            fabAddAset.setOnClickListener(v -> showAddAsetDialog());
        }

        listAset = dataManager.getAllAset();
        listAsetFilter = new ArrayList<>(listAset);
        
        tvTotalItem.setText(listAset.size() + " Item");

        adapter = new AsetRecyclerAdapter(listAsetFilter);
        recyclerViewAset.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewAset.setAdapter(adapter);

        updateCapacities();

        return view;
    }

    private void showAddAsetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tambah_aset, null);
        builder.setView(dialogView);

        EditText etNama = dialogView.findViewById(R.id.etAddNamaAset);
        Spinner spKategori = dialogView.findViewById(R.id.spAddKategori);
        EditText etStok = dialogView.findViewById(R.id.etAddStok);
        EditText etHarga = dialogView.findViewById(R.id.etAddHarga);
        Spinner spKondisi = dialogView.findViewById(R.id.spAddKondisi);

        // Setup Spinner Kategori
        String[] kategoriArr = {"Komputer", "Elektronik", "Mikrokomputer", "Mikrokontroler", "Aksesoris", "Alat Ukur", "Jaringan"};
        ArrayAdapter<String> katAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, kategoriArr);
        katAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKategori.setAdapter(katAdapter);

        // Setup Spinner Kondisi
        String[] kondisiArr = {"Baik", "Rusak Ringan", "Rusak Berat"};
        ArrayAdapter<String> konAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, kondisiArr);
        konAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKondisi.setAdapter(konAdapter);

        builder.setTitle("Tambah Aset Baru");
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String nama = etNama.getText().toString().trim();
            String kategori = spKategori.getSelectedItem().toString();
            String stokStr = etStok.getText().toString().trim();
            String hargaStr = etHarga.getText().toString().trim();
            String kondisi = spKondisi.getSelectedItem().toString();

            if (nama.isEmpty() || stokStr.isEmpty()) {
                Toast.makeText(getContext(), "Harap isi semua data", Toast.LENGTH_SHORT).show();
                return;
            }

            int stok = Integer.parseInt(stokStr);
            long harga = hargaStr.isEmpty() ? 0 : Long.parseLong(hargaStr);
            
            DataAset baru = new DataAset(0, nama, kategori, stok, kondisi);
            if (harga > 0) baru.setHarga(harga);

            dataManager.tambahAset(baru);
            Toast.makeText(getContext(), "Aset berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void showEditDeleteDialog(DataAset aset) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_detail_aset, null);
        builder.setView(dialogView);

        TextView tvIcon = dialogView.findViewById(R.id.tvDetAsetIcon);
        TextView tvNama = dialogView.findViewById(R.id.tvDetAsetNama);
        TextView tvKat = dialogView.findViewById(R.id.tvDetAsetKategori);
        TextView tvKon = dialogView.findViewById(R.id.tvDetAsetKondisi);
        TextView tvStokTotal = dialogView.findViewById(R.id.tvDetAsetStokTotal);
        TextView tvStokPinjam = dialogView.findViewById(R.id.tvDetAsetStokPinjam);
        TextView tvStokTersedia = dialogView.findViewById(R.id.tvDetAsetStokTersedia);
        Button btnHapus = dialogView.findViewById(R.id.btnDetHapus);
        Button btnTambahStok = dialogView.findViewById(R.id.btnDetTambahStok);

        tvNama.setText(aset.getNamaAset());
        tvKat.setText(aset.getKategori());
        tvKon.setText(aset.getKondisi());
        tvStokTotal.setText(aset.getStokTotal() + " Unit");
        tvStokPinjam.setText(aset.getStokDipinjam() + " Unit");
        tvStokTersedia.setText(aset.getStokTersedia() + " Unit");

        // Set Icon based on category
        tvIcon.setText(getIconForCategory(aset.getKategori()));

        if ("admin".equals(userRole)) {
            btnHapus.setVisibility(View.VISIBLE);
            btnTambahStok.setVisibility(View.VISIBLE);
            
            btnTambahStok.setOnClickListener(v -> {
                showAddStockQuantityDialog(aset);
            });
            
            btnHapus.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Hapus Aset")
                        .setMessage("Apakah Anda yakin ingin menghapus " + aset.getNamaAset() + "?")
                        .setPositiveButton("Ya, Hapus", (d, w) -> {
                            dataManager.hapusAset(aset.getId());
                            Toast.makeText(getContext(), "Aset dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            });
        }

        builder.show();
    }

    private void showAddStockQuantityDialog(DataAset aset) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tambah Stok: " + aset.getNamaAset());
        
        final EditText input = new EditText(requireContext());
        input.setHint("Masukkan jumlah unit tambahan");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setPadding(60, 40, 60, 40);
        
        builder.setView(input);
        
        builder.setPositiveButton("Tambah", (dialog, which) -> {
            String qtyStr = input.getText().toString().trim();
            if (!qtyStr.isEmpty()) {
                int extra = Integer.parseInt(qtyStr);
                int newTotal = aset.getStokTotal() + extra;
                dataManager.updateStokAset(aset.getId(), newTotal);
                Toast.makeText(getContext(), "Stok berhasil diperbarui", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private String getIconForCategory(String category) {
        switch (category) {
            case "Komputer": return "💻";
            case "Elektronik": return "🔌";
            case "Mikrokomputer": return "🍓";
            case "Mikrokontroler": return "🤖";
            case "Aksesoris": return "🔗";
            case "Alat Ukur": return "📏";
            case "Jaringan": return "🌐";
            default: return "📦";
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
        listAset = dataManager.getAllAset();
        listAsetFilter.clear();
        listAsetFilter.addAll(listAset);
        tvTotalItem.setText(listAset.size() + " Item");
        updateCapacities();
        adapter.notifyDataSetChanged();
    }

    private void updateCapacities() {
        if (containerKapasitas == null) return;
        containerKapasitas.removeAllViews();

        Map<String, int[]> categoryStats = new HashMap<>(); // [total, dipinjam]

        for (DataAset aset : listAset) {
            String kat = aset.getKategori();
            if (!categoryStats.containsKey(kat)) {
                categoryStats.put(kat, new int[]{0, 0});
            }
            int[] stats = categoryStats.get(kat);
            stats[0] += aset.getStokTotal();
            stats[1] += aset.getStokDipinjam();
        }

        int[] drawables = {
            R.drawable.progress_custom_jaringan,
            R.drawable.progress_custom_hardware,
            R.drawable.progress_custom_software
        };
        int count = 0;

        for (Map.Entry<String, int[]> entry : categoryStats.entrySet()) {
            String kat = entry.getKey();
            int[] stats = entry.getValue();
            if (stats == null) continue;
            
            int total = stats[0];
            int dipinjam = stats[1];

            View itemView = getLayoutInflater().inflate(R.layout.item_kapasitas_kategori, containerKapasitas, false);
            TextView tvNama = itemView.findViewById(R.id.tvKatNama);
            TextView tvPersen = itemView.findViewById(R.id.tvKatPersen);
            ProgressBar pb = itemView.findViewById(R.id.pbKatProgress);

            tvNama.setText(kat);
            
            int tersedia = total - dipinjam;
            int percent = (total > 0) ? (tersedia * 100) / total : 0;
            
            tvPersen.setText(percent + "%");
            pb.setProgress(percent);
            pb.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), drawables[count % drawables.length], null));
            
            containerKapasitas.addView(itemView);
            count++;
        }
    }

    private class AsetRecyclerAdapter extends RecyclerView.Adapter<AsetRecyclerAdapter.ViewHolder> {
        private List<DataAset> data;
        private int[] cardColors = {
            R.color.card_bg_1, R.color.card_bg_2, R.color.card_bg_3,
            R.color.card_bg_4, R.color.card_bg_5, R.color.card_bg_6
        };

        public AsetRecyclerAdapter(List<DataAset> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aset, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DataAset aset = data.get(position);
            holder.tvNama.setText(aset.getNamaAset());
            holder.tvKategori.setText(aset.getKategori().toUpperCase());
            holder.tvStok.setText(aset.getStokTersedia() + " unit");
            
            // Cycle colors for design match
            holder.cardContent.setBackgroundColor(getResources().getColor(cardColors[position % cardColors.length]));
            
            // Set icon based on category
            holder.tvIcon.setText(getIconForCategory(aset.getKategori()));

            if (aset.isTersedia()) {
                holder.tvStatus.setText("• Tersedia");
                holder.tvStatus.setTextColor(getResources().getColor(R.color.status_available_text));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_available);
            } else {
                holder.tvStatus.setText("• Habis");
                holder.tvStatus.setTextColor(getResources().getColor(R.color.status_damaged_text));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_available);
                holder.tvStatus.getBackground().setTint(0xFFF9E2E2);
            }

            holder.itemView.setOnClickListener(v -> {
                if ("admin".equals(userRole)) {
                    showEditDeleteDialog(aset);
                } else {
                    if (!aset.isTersedia()) {
                        Toast.makeText(getContext(), aset.getNamaAset() + " sedang tidak tersedia", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(getActivity(), FormPeminjamanActivity.class);
                    intent.putExtra("NAMA_ASET", aset.getNamaAset());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNama, tvKategori, tvStok, tvStatus, tvIcon;
            View cardContent;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNama = itemView.findViewById(R.id.tvAsetNama);
                tvKategori = itemView.findViewById(R.id.tvAsetKategori);
                tvStok = itemView.findViewById(R.id.tvAsetStok);
                tvStatus = itemView.findViewById(R.id.tvAsetStatus);
                tvIcon = itemView.findViewById(R.id.tvAsetIcon);
                cardContent = itemView.findViewById(R.id.cardContent);
            }
        }
    }
}
