package com.example.asetpeminjaman;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.List;

/**
 * DaftarAsetActivity - Menampilkan inventaris semua aset jurusan
 *
 * Konsep dari Modul 3 (View & ViewGroup):
 * - Menggunakan ListView untuk menampilkan list data
 * - Custom ArrayAdapter untuk mengisi data ke ListView
 *
 * Konsep dari Modul 4 (Layout):
 * - Menggunakan LinearLayout dan item layout (item_aset.xml)
 *
 * Konsep dari Modul 6 (Intent Eksplisit):
 * - Tombol Back menggunakan finish()
 * - Klik item langsung membuka form peminjaman dengan data aset
 */
public class DaftarAsetActivity extends AppCompatActivity {

    // Views (Modul 3)
    private ListView listViewAset;
    private EditText etSearch;
    private ImageView btnBack;
    private FloatingActionButton fabAddAset;

    private DataManager dataManager;
    private List<DataAset> listAset;
    private List<DataAset> listAsetFilter;
    private AsetAdapter adapter;
    private DataManager.DataChangeListener dataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_aset);

        dataManager = DataManager.getInstance();

        // Setup listener untuk update otomatis
        dataListener = () -> {
            listAset = dataManager.getAllAset();
            filterAset(etSearch.getText().toString());
        };
        dataManager.addListener(dataListener);

        // Menghubungkan View (Modul 3 - findViewById)
        listViewAset = findViewById(R.id.listViewAset);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        fabAddAset = findViewById(R.id.fabAddAset);

        // Cek Role - Hanya Admin yang bisa tambah aset
        SharedPreferences pref = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        String role = pref.getString("role", "user");
        if ("admin".equals(role)) {
            fabAddAset.setVisibility(View.VISIBLE);
        }

        listAset = dataManager.getAllAset();
        listAsetFilter = new ArrayList<>(listAset);

        // Setup adapter untuk ListView (Modul 3 - View)
        adapter = new AsetAdapter(listAsetFilter);
        listViewAset.setAdapter(adapter);

        // Setup listener
        setupListeners();
    }

    private void setupListeners() {
        // Click listener tombol Back (Modul 3 - Click Event)
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Search realtime - TextWatcher (Modul 3 - EditText)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAset(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Klik item ListView
        listViewAset.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataAset aset = listAsetFilter.get(position);
                showDetailAsetDialog(aset);
            }
        });

        // Klik FAB Tambah Aset
        fabAddAset.setOnClickListener(v -> showAddAsetDialog());
    }

    private void showDetailAsetDialog(DataAset aset) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_detail_aset, null);
        builder.setView(dialogView);

        TextView tvIcon = dialogView.findViewById(R.id.tvDetAsetIcon);
        TextView tvNama = dialogView.findViewById(R.id.tvDetAsetNama);
        TextView tvKat = dialogView.findViewById(R.id.tvDetAsetKategori);
        TextView tvKon = dialogView.findViewById(R.id.tvDetAsetKondisi);
        TextView tvStokTotal = dialogView.findViewById(R.id.tvDetAsetStokTotal);
        TextView tvStokPinjam = dialogView.findViewById(R.id.tvDetAsetStokPinjam);
        TextView tvStokTersedia = dialogView.findViewById(R.id.tvDetAsetStokTersedia);
        Button btnPinjam = dialogView.findViewById(R.id.btnDetPinjam);
        Button btnHapus = dialogView.findViewById(R.id.btnDetHapus);

        tvNama.setText(aset.getNamaAset());
        tvKat.setText(aset.getKategori());
        tvKon.setText(aset.getKondisi());
        tvStokTotal.setText(aset.getStokTotal() + " Unit");
        tvStokPinjam.setText(aset.getStokDipinjam() + " Unit");
        tvStokTersedia.setText(aset.getStokTersedia() + " Unit");

        // Set Icon
        switch (aset.getKategori()) {
            case "Komputer": tvIcon.setText("💻"); break;
            case "Elektronik": tvIcon.setText("🔌"); break;
            case "Mikrokomputer": tvIcon.setText("🍓"); break;
            case "Mikrokontroler": tvIcon.setText("🤖"); break;
            case "Aksesoris": tvIcon.setText("🔗"); break;
            case "Alat Ukur": tvIcon.setText("📏"); break;
            case "Jaringan": tvIcon.setText("🌐"); break;
            default: tvIcon.setText("📦"); break;
        }

        SharedPreferences pref = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        String role = pref.getString("role", "user");

        if ("admin".equals(role)) {
            btnHapus.setVisibility(View.VISIBLE);
            btnHapus.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Hapus Aset")
                        .setMessage("Apakah Anda yakin ingin menghapus " + aset.getNamaAset() + "?")
                        .setPositiveButton("Ya, Hapus", (d, w) -> {
                            dataManager.hapusAset(aset.getId());
                            Toast.makeText(this, "Aset dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            });
        } else {
            btnPinjam.setVisibility(View.VISIBLE);
            if (!aset.isTersedia()) {
                btnPinjam.setEnabled(false);
                btnPinjam.setText("STOK HABIS");
                btnPinjam.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.darker_gray)));
            }
            btnPinjam.setOnClickListener(v -> {
                Intent intent = new Intent(DaftarAsetActivity.this, FormPeminjamanActivity.class);
                intent.putExtra("NAMA_ASET", aset.getNamaAset());
                startActivity(intent);
            });
        }

        builder.show();
    }

    private void showAddAsetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tambah_aset, null);
        builder.setView(dialogView);

        EditText etNama = dialogView.findViewById(R.id.etAddNamaAset);
        Spinner spKategori = dialogView.findViewById(R.id.spAddKategori);
        EditText etStok = dialogView.findViewById(R.id.etAddStok);
        Spinner spKondisi = dialogView.findViewById(R.id.spAddKondisi);

        // Setup Spinner Kategori
        String[] kategoriArr = {"Komputer", "Elektronik", "Mikrokomputer", "Mikrokontroler", "Aksesoris", "Alat Ukur", "Jaringan"};
        ArrayAdapter<String> katAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategoriArr);
        katAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKategori.setAdapter(katAdapter);

        // Setup Spinner Kondisi
        String[] kondisiArr = {"Baik", "Rusak Ringan", "Rusak Berat"};
        ArrayAdapter<String> konAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kondisiArr);
        konAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKondisi.setAdapter(konAdapter);

        builder.setTitle("Tambah Aset Baru");
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String nama = etNama.getText().toString().trim();
            String kategori = spKategori.getSelectedItem().toString();
            String stokStr = etStok.getText().toString().trim();
            String kondisi = spKondisi.getSelectedItem().toString();

            if (nama.isEmpty() || stokStr.isEmpty()) {
                Toast.makeText(this, "Harap isi semua data", Toast.LENGTH_SHORT).show();
                return;
            }

            int stok = Integer.parseInt(stokStr);
            DataAset baru = new DataAset(0, nama, kategori, stok, kondisi);
            dataManager.tambahAset(baru);
            Toast.makeText(this, "Aset berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    /**
     * Filter daftar aset berdasarkan query pencarian
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null && dataListener != null) {
            dataManager.removeListener(dataListener);
        }
    }

    private void filterAset(String query) {
        listAsetFilter.clear();
        if (query.isEmpty()) {
            listAsetFilter.addAll(listAset);
        } else {
            String queryLower = query.toLowerCase();
            for (DataAset aset : listAset) {
                if (aset.getNamaAset().toLowerCase().contains(queryLower)
                        || aset.getKategori().toLowerCase().contains(queryLower)) {
                    listAsetFilter.add(aset);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data aset saat kembali ke halaman ini
        listAsetFilter.clear();
        listAsetFilter.addAll(dataManager.getAllAset());
        adapter.notifyDataSetChanged();
    }

    /**
     * Custom ArrayAdapter untuk ListView aset
     * Menggunakan item_aset.xml sebagai layout setiap baris
     * (Modul 3 - View, Modul 4 - Layout)
     */
    private class AsetAdapter extends ArrayAdapter<DataAset> {

        public AsetAdapter(List<DataAset> data) {
            super(DaftarAsetActivity.this, R.layout.item_aset, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_aset, parent, false);
            }

            DataAset aset = getItem(position);
            if (aset == null) return convertView;

            // Mengisi data ke TextView pada item layout (Modul 3 - TextView)
            TextView tvNama = convertView.findViewById(R.id.tvAsetNama);
            TextView tvKategori = convertView.findViewById(R.id.tvAsetKategori);
            TextView tvStok = convertView.findViewById(R.id.tvAsetStok);
            TextView tvDipinjam = convertView.findViewById(R.id.tvAsetDipinjam);
            TextView tvStatus = convertView.findViewById(R.id.tvAsetStatus);
            TextView tvIcon = convertView.findViewById(R.id.tvAsetIcon);
            android.view.View iconContainer = convertView.findViewById(R.id.iconContainer);

            tvNama.setText(aset.getNamaAset());
            tvKategori.setText(aset.getKategori() + " • " + aset.getKondisi());
            tvStok.setText("Stok: " + aset.getStokTersedia() + "/" + aset.getStokTotal());
            tvDipinjam.setText("Dipinjam: " + aset.getStokDipinjam());

            // Set icon dan warna sesuai kategori (Figma: colored circles)
            switch (aset.getKategori()) {
                case "Komputer":
                    tvIcon.setText("💻");
                    iconContainer.setBackground(getResources().getDrawable(R.drawable.bg_button_primary));
                    break;
                case "Elektronik":
                    tvIcon.setText("🔌");
                    iconContainer.setBackgroundColor(getResources().getColor(R.color.accent_orange));
                    break;
                case "Mikrokomputer":
                    tvIcon.setText("🍓");
                    iconContainer.setBackgroundColor(getResources().getColor(R.color.accent_red));
                    break;
                case "Mikrokontroler":
                    tvIcon.setText("🤖");
                    iconContainer.setBackgroundColor(getResources().getColor(R.color.accent_blue));
                    break;
                case "Aksesoris":
                    tvIcon.setText("🔗");
                    iconContainer.setBackgroundColor(getResources().getColor(R.color.accent_purple));
                    break;
                case "Alat Ukur":
                    tvIcon.setText("📏");
                    iconContainer.setBackgroundColor(getResources().getColor(R.color.accent_gold));
                    break;
                case "Jaringan":
                    tvIcon.setText("🌐");
                    iconContainer.setBackground(getResources().getDrawable(R.drawable.bg_button_primary));
                    break;
                default:
                    tvIcon.setText("📦");
                    iconContainer.setBackground(getResources().getDrawable(R.drawable.bg_button_primary));
                    break;
            }

            if (aset.isTersedia()) {
                tvStatus.setText("Tersedia");
                tvStatus.setTextColor(getResources().getColor(R.color.status_kembali_text));
                tvStatus.setBackground(getResources().getDrawable(R.drawable.bg_status_dikembalikan));
            } else {
                tvStatus.setText("Habis");
                tvStatus.setTextColor(getResources().getColor(R.color.status_dipinjam_text));
                tvStatus.setBackground(getResources().getDrawable(R.drawable.bg_status_dipinjam));
            }

            return convertView;
        }
    }
}
