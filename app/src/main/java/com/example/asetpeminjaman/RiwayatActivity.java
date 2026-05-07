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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class RiwayatActivity extends AppCompatActivity {

    // Views (Modul 3)
    private ListView listViewRiwayat;
    private LinearLayout emptyRiwayat;
    private EditText etSearch;
    private Button btnTabSemua, btnTabDipinjam, btnTabKembali;
    private ImageView btnBack;

    private DataManager dataManager;
    private List<DataPeminjaman> listMaster;
    private List<DataPeminjaman> listTampil;
    private PeminjamanAdapter adapter;

    private String filterAktif = "SEMUA"; // State filter saat ini

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        dataManager = DataManager.getInstance();

        // Menghubungkan View (Modul 3 - findViewById)
        listViewRiwayat = findViewById(R.id.listViewRiwayat);
        emptyRiwayat = findViewById(R.id.emptyRiwayat);
        etSearch = findViewById(R.id.etSearchRiwayat);
        btnTabSemua = findViewById(R.id.btnTabSemua);
        btnTabDipinjam = findViewById(R.id.btnTabDipinjam);
        btnTabKembali = findViewById(R.id.btnTabKembali);
        btnBack = findViewById(R.id.btnBack);

        listMaster = new ArrayList<>();
        listTampil = new ArrayList<>();

        // Setup adapter ListView
        adapter = new PeminjamanAdapter(listTampil);
        listViewRiwayat.setAdapter(adapter);

        // Search realtime
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { 
                filterData(s.toString()); 
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Setup click listeners (Modul 3 - Click Event)
        setupClickListeners();

        // Cek filter dari Intent (jika ada)
        String filterExtra = getIntent().getStringExtra("FILTER");
        if (filterExtra != null) {
            filterAktif = filterExtra;
        }

        // Tampilkan data awal
        tampilkanData(filterAktif);
        updateTabStyle(filterAktif);
    }

    private void filterData(String query) {
        listTampil.clear();
        if (query.isEmpty()) {
            listTampil.addAll(listMaster);
        } else {
            String q = query.toLowerCase();
            for (DataPeminjaman p : listMaster) {
                // Cari berdasarkan Nama Peminjam atau Nama Aset
                boolean matchNama = p.getNama().toLowerCase().contains(q);
                boolean matchAset = false;
                for (ItemPinjam item : p.getItems()) {
                    if (item.getNamaAset().toLowerCase().contains(q)) {
                        matchAset = true;
                        break;
                    }
                }
                
                if (matchNama || matchAset) {
                    listTampil.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
        
        // Toggle empty state view
        if (listTampil.isEmpty()) {
            listViewRiwayat.setVisibility(View.GONE);
            emptyRiwayat.setVisibility(View.VISIBLE);
        } else {
            listViewRiwayat.setVisibility(View.VISIBLE);
            emptyRiwayat.setVisibility(View.GONE);
        }
    }

    /**
     * Setup semua click event listeners
     */
    private void setupClickListeners() {

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Tab Semua
        btnTabSemua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterAktif = "SEMUA";
                tampilkanData("SEMUA");
                updateTabStyle("SEMUA");
            }
        });

        // Tab Dipinjam
        btnTabDipinjam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterAktif = "DIPINJAM";
                tampilkanData("DIPINJAM");
                updateTabStyle("DIPINJAM");
            }
        });

        // Tab Dikembalikan
        btnTabKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterAktif = "KEMBALI";
                tampilkanData("KEMBALI");
                updateTabStyle("KEMBALI");
            }
        });

        // Klik item list -> buka detail peminjaman
        // Mengirim data ID menggunakan Intent putExtra (Modul 6)
        listViewRiwayat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataPeminjaman p = listTampil.get(position);

                // Intent Eksplisit ke DetailPeminjamanActivity (Modul 6)
                Intent intent = new Intent(RiwayatActivity.this,
                        DetailPeminjamanActivity.class);

                // Mengirim ID peminjaman antar Activity (Modul 6 - putExtra)
                intent.putExtra("PEMINJAMAN_ID", p.getId());
                startActivity(intent);
            }
        });
    }

    /**
     * Menampilkan data sesuai filter yang dipilih
     */
    private void tampilkanData(String filter) {
        listMaster.clear();

        switch (filter) {
            case "SEMUA":
                listMaster.addAll(dataManager.getAllPeminjaman());
                break;
            case "DIPINJAM":
                listMaster.addAll(dataManager.getPeminjamanAktif());
                break;
            case "KEMBALI":
                listMaster.addAll(dataManager.getPeminjamanSelesai());
                break;
        }

        // Balik urutan: terbaru di atas
        java.util.Collections.reverse(listMaster);

        // Terapkan filter pencarian yang mungkin sedang aktif
        filterData(etSearch.getText().toString());
    }

    /**
     * Update tampilan tab yang aktif
     * (Modul 3 - Button, mengubah background secara programatik)
     */
    private void updateTabStyle(String aktif) {
        // Reset semua tab ke inactive style
        btnTabSemua.setBackground(getResources().getDrawable(R.drawable.bg_tab_inactive));
        btnTabDipinjam.setBackground(getResources().getDrawable(R.drawable.bg_tab_inactive));
        btnTabKembali.setBackground(getResources().getDrawable(R.drawable.bg_tab_inactive));
        btnTabSemua.setTextColor(getResources().getColor(R.color.text_secondary));
        btnTabDipinjam.setTextColor(getResources().getColor(R.color.text_secondary));
        btnTabKembali.setTextColor(getResources().getColor(R.color.text_secondary));

        // Highlight tab aktif
        switch (aktif) {
            case "SEMUA":
                btnTabSemua.setBackground(getResources().getDrawable(R.drawable.bg_tab_active));
                btnTabSemua.setTextColor(getResources().getColor(R.color.white));
                break;
            case "DIPINJAM":
                btnTabDipinjam.setBackground(getResources().getDrawable(R.drawable.bg_tab_active));
                btnTabDipinjam.setTextColor(getResources().getColor(R.color.white));
                break;
            case "KEMBALI":
                btnTabKembali.setBackground(getResources().getDrawable(R.drawable.bg_tab_active));
                btnTabKembali.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data saat kembali ke halaman ini (Modul 5 - Lifecycle)
        tampilkanData(filterAktif);
    }

    /**
     * Custom ArrayAdapter untuk menampilkan item peminjaman
     * (Modul 3 - View, Modul 4 - Layout dengan item_peminjaman.xml)
     */
    private class PeminjamanAdapter extends ArrayAdapter<DataPeminjaman> {

        public PeminjamanAdapter(List<DataPeminjaman> data) {
            super(RiwayatActivity.this, R.layout.item_peminjaman, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.item_peminjaman, parent, false);
            }

            DataPeminjaman p = getItem(position);
            if (p == null) return convertView;

            // Isi data ke TextView (Modul 3 - TextView)
            TextView tvNama = convertView.findViewById(R.id.tvItemNama);
            TextView tvNim = convertView.findViewById(R.id.tvItemNim);
            LinearLayout containerItems = convertView.findViewById(R.id.containerItemPeminjaman);
            TextView tvTanggal = convertView.findViewById(R.id.tvItemTanggal);
            TextView tvStatus = convertView.findViewById(R.id.tvItemStatus);

            tvNama.setText(p.getNama() + " (@" + p.getAccountUsername() + ")");
            tvNim.setText(p.getNim());
            
            // Tampilkan list item secara dinamis
            containerItems.removeAllViews();
            for (ItemPinjam item : p.getItems()) {
                View subItem = getLayoutInflater().inflate(R.layout.item_peminjaman_subitem, containerItems, false);
                TextView tvSubNama = subItem.findViewById(R.id.tvSubItemNama);
                TextView tvSubJumlah = subItem.findViewById(R.id.tvSubItemJumlah);
                
                tvSubNama.setText(item.getNamaAset());
                tvSubJumlah.setText("x" + item.getJumlah());
                containerItems.addView(subItem);
            }

            tvTanggal.setText("Pinjam: " + p.getTanggalPinjam()
                    + "  |  Kembali: " + p.getTanggalRencanaKembali());
            tvStatus.setText(p.getStatus());

            // Tambahkan Klik pada kartu
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), DetailPeminjamanActivity.class);
                intent.putExtra("PEMINJAMAN_ID", p.getId());
                getContext().startActivity(intent);
            });

            // Ubah warna status badge sesuai kondisi
            if (p.isAktif()) {
                tvStatus.setTextColor(getResources().getColor(R.color.orange));
                tvStatus.setBackground(getResources().getDrawable(R.drawable.bg_status_dipinjam));
            } else {
                tvStatus.setTextColor(getResources().getColor(R.color.green));
                tvStatus.setBackground(getResources().getDrawable(R.drawable.bg_status_dikembalikan));
            }

            return convertView;
        }
    }
}
