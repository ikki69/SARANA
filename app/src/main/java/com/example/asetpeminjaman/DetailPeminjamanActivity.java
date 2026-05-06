package com.example.asetpeminjaman;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

/**
 * DetailPeminjamanActivity - Menampilkan detail satu data peminjaman
 *
 * Konsep dari Modul 3 (View & ViewGroup):
 * - TextView untuk menampilkan data detail
 * - Button untuk aksi konfirmasi pengembalian
 * - ImageView untuk tombol back
 *
 * Konsep dari Modul 5 (Activity Lifecycle):
 * - onResume() untuk refresh tampilan
 *
 * Konsep dari Modul 6 (Intent Eksplisit + getIntent/getStringExtra):
 * - Menerima ID peminjaman dari Activity pengirim via getIntent().getIntExtra()
 * - Setelah aksi selesai, mengirim result back ke RiwayatActivity
 */
public class DetailPeminjamanActivity extends AppCompatActivity {

    // Deklarasi Views (Modul 3)
    private TextView tvIdPeminjaman, tvNama, tvNim;
    private TextView tvWaktuPinjam, tvWaktuKembali;
    private TextView tvTanggalAktual, tvKeperluan, tvStatusBanner;
    private LinearLayout containerItems;
    private Button btnKembalikan, btnModif;
    private ImageView btnBack;
    private LinearLayout statusBanner, labelTanggalAktual;

    private DataManager dataManager;
    private int peminjamanId = -1;
    private DataPeminjaman peminjaman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_peminjaman);

        dataManager = DataManager.getInstance();

        // Menghubungkan View (Modul 3 - findViewById)
        initViews();

        // Menerima data ID dari Intent (Modul 6 - getIntent().getIntExtra())
        Intent intent = getIntent();
        peminjamanId = intent.getIntExtra("PEMINJAMAN_ID", -1);

        if (peminjamanId == -1) {
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ambil data peminjaman berdasarkan ID yang diterima
        peminjaman = dataManager.getPeminjamanById(peminjamanId);

        if (peminjaman == null) {
            Toast.makeText(this, "Data peminjaman tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tampilkan data
        tampilkanDetail();

        // Setup click listeners (Modul 3 - Click Event)
        setupClickListeners();
    }

    /**
     * Inisialisasi semua View dengan findViewById (Modul 3)
     */
    private void initViews() {
        tvIdPeminjaman = findViewById(R.id.tvIdPeminjaman);
        tvNama = findViewById(R.id.tvNama);
        tvNim = findViewById(R.id.tvNim);
        containerItems = findViewById(R.id.containerItems);
        tvWaktuPinjam = findViewById(R.id.tvWaktuPinjam);
        tvWaktuKembali = findViewById(R.id.tvWaktuKembali);
        tvTanggalAktual = findViewById(R.id.tvTanggalAktual);
        labelTanggalAktual = findViewById(R.id.labelTanggalAktual);
        tvKeperluan = findViewById(R.id.tvKeperluan);
        tvStatusBanner = findViewById(R.id.tvStatusBanner);
        btnKembalikan = findViewById(R.id.btnKembalikan);
        btnModif = findViewById(R.id.btnModif);
        btnBack = findViewById(R.id.btnBack);
        statusBanner = findViewById(R.id.statusBanner);
    }

    /**
     * Mengisi semua TextView dengan data peminjaman
     * (Modul 3 - TextView.setText())
     */
    private void tampilkanDetail() {
        // Set nilai ke setiap TextView (Modul 3 - View)
        tvIdPeminjaman.setText(peminjaman.getFormattedId());
        tvNama.setText(peminjaman.getNama());
        tvNim.setText(peminjaman.getNim());
        
        // Tampilkan daftar item
        containerItems.removeAllViews();
        for (ItemPinjam item : peminjaman.getItems()) {
            View itemView = getLayoutInflater().inflate(R.layout.item_detail_aset, null);
            TextView tvItemNama = itemView.findViewById(R.id.tvItemNama);
            TextView tvItemJumlah = itemView.findViewById(R.id.tvItemJumlah);
            
            tvItemNama.setText(item.getNamaAset());
            tvItemJumlah.setText(item.getJumlah() + " unit");
            
            containerItems.addView(itemView);
        }

        tvWaktuPinjam.setText(peminjaman.getTanggalPinjam() + " " + peminjaman.getJamPinjam());
        tvWaktuKembali.setText(peminjaman.getTanggalRencanaKembali() + " " + peminjaman.getJamRencanaKembali());
        tvKeperluan.setText(peminjaman.getKeperluan());
        tvStatusBanner.setText(peminjaman.getStatus());

        // Tampilkan status dengan warna yang sesuai
        if (peminjaman.getStatus().equals("Dipinjam")) {
            tvStatusBanner.setTextColor(getResources().getColor(R.color.orange));
            statusBanner.setBackground(getResources().getDrawable(R.drawable.bg_status_dipinjam));
            btnKembalikan.setVisibility(View.VISIBLE);
            btnKembalikan.setText("Ajukan Pengembalian");
            btnModif.setVisibility(View.VISIBLE);
            labelTanggalAktual.setVisibility(View.GONE);
            tvTanggalAktual.setVisibility(View.GONE);
        } else if (peminjaman.getStatus().equals("Menunggu Pengembalian")) {
            tvStatusBanner.setText("Menunggu Persetujuan Kembali");
            tvStatusBanner.setTextColor(getResources().getColor(R.color.orange));
            statusBanner.setBackground(getResources().getDrawable(R.drawable.bg_status_dipinjam));
            btnKembalikan.setVisibility(View.GONE);
            btnModif.setVisibility(View.GONE);
            labelTanggalAktual.setVisibility(View.VISIBLE);
            tvTanggalAktual.setVisibility(View.VISIBLE);
            tvTanggalAktual.setText("Menunggu Admin...");
        } else if (peminjaman.getStatus().equals("Menunggu Persetujuan")) {
            tvStatusBanner.setTextColor(getResources().getColor(R.color.orange));
            statusBanner.setBackground(getResources().getDrawable(R.drawable.bg_status_dipinjam));
            btnKembalikan.setVisibility(View.GONE);
            btnModif.setVisibility(View.VISIBLE);
            labelTanggalAktual.setVisibility(View.GONE);
        } else {
            tvStatusBanner.setTextColor(getResources().getColor(R.color.green));
            statusBanner.setBackground(getResources().getDrawable(R.drawable.bg_status_dikembalikan));
            btnKembalikan.setVisibility(View.GONE);
            btnModif.setVisibility(View.GONE);

            // Tampilkan tanggal aktual kembali (Modul 3 - TextView visibility)
            labelTanggalAktual.setVisibility(View.VISIBLE);
            tvTanggalAktual.setVisibility(View.VISIBLE);
            tvTanggalAktual.setText(peminjaman.getTanggalAktualKembali());
        }
    }

    /**
     * Setup click event listeners (Modul 3 - Button dan Click Event)
     */
    private void setupClickListeners() {

        // Tombol Back - kembali ke activity sebelumnya
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Tutup activity ini
            }
        });

        // Tombol Konfirmasi Pengembalian
        btnKembalikan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                konfirmasiPengembalian();
            }
        });

        // Tombol Modifikasi
        btnModif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailPeminjamanActivity.this, FormPeminjamanActivity.class);
                intent.putExtra("PEMINJAMAN_ID", peminjamanId);
                intent.putExtra("IS_EDIT_MODE", true);
                startActivity(intent);
            }
        });
    }

    /**
     * Proses konfirmasi pengembalian aset
     * (Modul 3 - Button Click Event + Toast)
     */
    private void konfirmasiPengembalian() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Pengembalian")
                .setMessage("Apakah Anda yakin ingin mengembalikan aset ini? Permintaan akan dikirimkan ke Admin untuk disetujui.")
                .setPositiveButton("Ya, Kembalikan", (dialog, which) -> {
                    prosesKirimKeAdmin();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void prosesKirimKeAdmin() {
        // Dapatkan tanggal hari ini sebagai tanggal aktual kembali
        Calendar calendar = Calendar.getInstance();
        String tanggalKembali = String.format("%02d/%02d/%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));

        // Proses pengembalian melalui DataManager
        boolean berhasil = dataManager.ajukanPengembalian(peminjamanId, tanggalKembali);

        if (berhasil) {
            // Update referensi data
            peminjaman = dataManager.getPeminjamanById(peminjamanId);

            // Refresh tampilan detail
            tampilkanDetail();

            // Dialog informasi sukses terkirim
            new AlertDialog.Builder(this)
                    .setTitle("Berhasil")
                    .setMessage("Permintaan pengembalian telah dikirim ke Admin. Mohon tunggu persetujuan agar status berubah menjadi Selesai.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            Toast.makeText(this, "Gagal mengirim permintaan", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data saat kembali ke halaman ini (Modul 5 - Lifecycle)
        if (peminjaman != null) {
            peminjaman = dataManager.getPeminjamanById(peminjamanId);
            if (peminjaman != null) tampilkanDetail();
        }
    }
}
