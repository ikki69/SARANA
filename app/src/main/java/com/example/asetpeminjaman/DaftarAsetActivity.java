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

    private DataManager dataManager;
    private List<DataAset> listAset;
    private List<DataAset> listAsetFilter;
    private AsetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_aset);

        dataManager = DataManager.getInstance();

        // Menghubungkan View (Modul 3 - findViewById)
        listViewAset = findViewById(R.id.listViewAset);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);

        listAset = dataManager.getAllAset();
        listAsetFilter = new ArrayList<>(listAset);

        // Setup adapter untuk ListView (Modul 3 - View)
        adapter = new AsetAdapter(listAsetFilter);
        listViewAset.setAdapter(adapter);

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

        // Klik item ListView -> buka form peminjaman untuk aset tersebut
        listViewAset.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataAset aset = listAsetFilter.get(position);

                if (!aset.isTersedia()) {
                    Toast.makeText(DaftarAsetActivity.this,
                            aset.getNamaAset() + " sedang tidak tersedia",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Intent Eksplisit: buka form peminjaman (Modul 6)
                // Kirim nama aset via putExtra (Modul 6 - putExtra)
                Intent intent = new Intent(DaftarAsetActivity.this,
                        FormPeminjamanActivity.class);
                intent.putExtra("NAMA_ASET", aset.getNamaAset());
                startActivity(intent);
            }
        });
    }

    /**
     * Filter daftar aset berdasarkan query pencarian
     */
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
