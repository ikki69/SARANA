package com.example.asetpeminjaman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.core.content.res.ResourcesCompat;

public class DetailPeminjamanActivity extends AppCompatActivity {

    private TextView tvId, tvNama, tvNim, tvWaktuPinjam, tvWaktuKembali, tvTanggalAktual, tvKeperluan, tvStatusBanner;
    private TextView tvDendaTerlambat, tvDendaRusak, tvTotalDenda;
    private LinearLayout containerItems, labelTanggalAktual, statusBannerRoot, layoutDenda;
    private Button btnKembalikan, btnModif, btnKonfirmasiBayar;
    private DataManager dataManager;
    private DataPeminjaman p;
    private String userRole = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_peminjaman);

        dataManager = DataManager.getInstance();
        int idPeminjaman = getIntent().getIntExtra("PEMINJAMAN_ID", -1);
        p = dataManager.getPeminjamanById(idPeminjaman);

        if (p == null) {
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ambil role
        android.content.SharedPreferences pref = getSharedPreferences("USER_DATA", MODE_PRIVATE);
        userRole = pref.getString("role", "user");

        bindViews();
        renderData();
        setupListeners();
    }

    private void bindViews() {
        tvId = findViewById(R.id.tvIdPeminjaman);
        tvNama = findViewById(R.id.tvNama);
        tvNim = findViewById(R.id.tvNim);
        tvWaktuPinjam = findViewById(R.id.tvWaktuPinjam);
        tvWaktuKembali = findViewById(R.id.tvWaktuKembali);
        tvTanggalAktual = findViewById(R.id.tvTanggalAktual);
        tvKeperluan = findViewById(R.id.tvKeperluan);
        tvStatusBanner = findViewById(R.id.tvStatusBanner);
        statusBannerRoot = findViewById(R.id.statusBannerRoot);
        containerItems = findViewById(R.id.containerItems);
        labelTanggalAktual = findViewById(R.id.labelTanggalAktual);
        layoutDenda = findViewById(R.id.layoutDenda);
        tvDendaTerlambat = findViewById(R.id.tvDendaTerlambat);
        tvDendaRusak = findViewById(R.id.tvDendaRusak);
        tvTotalDenda = findViewById(R.id.tvTotalDenda);
        btnKembalikan = findViewById(R.id.btnKembalikan);
        btnModif = findViewById(R.id.btnModif);
        btnKonfirmasiBayar = findViewById(R.id.btnKembalikan); // Re-use ID or add new in XML
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void renderData() {
        tvId.setText("#PM-" + String.format("%04d", p.getId()));
        tvNama.setText(p.getNama());
        tvNim.setText(p.getNim() + " - Mhs. Aktif");
        tvWaktuPinjam.setText(p.getTanggalPinjam());
        tvWaktuKembali.setText(p.getTanggalRencanaKembali());
        tvKeperluan.setText(p.getKeperluan());

        tvStatusBanner.setText(p.getStatus());
        
        // Default Button Visibility
        btnKembalikan.setVisibility(View.GONE);
        btnModif.setVisibility(View.GONE);
        
        if ("Dipinjam".equals(p.getStatus())) {
            statusBannerRoot.setBackgroundResource(R.drawable.bg_approve_card_normal);
            tvStatusBanner.getBackground().setTint(0xFFD7E5F0);
            tvStatusBanner.setTextColor(0xFF5B8DB8);
            labelTanggalAktual.setVisibility(View.GONE);
            btnKembalikan.setVisibility(View.VISIBLE);
        } else if ("Menunggu Persetujuan".equals(p.getStatus())) {
            statusBannerRoot.setBackgroundResource(R.drawable.bg_approve_card_teal);
            tvStatusBanner.getBackground().setTint(0xFFEEEEEE);
            tvStatusBanner.setTextColor(0xFF9E9E9E);
            labelTanggalAktual.setVisibility(View.GONE);
            btnModif.setVisibility(View.VISIBLE);
        } else if ("Menunggu Pengembalian".equals(p.getStatus())) {
            statusBannerRoot.setBackgroundResource(R.drawable.bg_approve_card_teal);
            tvStatusBanner.getBackground().setTint(0xFFFDF1D3);
            tvStatusBanner.setTextColor(0xFFC9A227);
            labelTanggalAktual.setVisibility(View.GONE);
        } else if ("Menunggu Pembayaran".equals(p.getStatus())) {
            statusBannerRoot.setBackgroundResource(R.drawable.bg_approve_card_urgent);
            tvStatusBanner.getBackground().setTint(0xFFF9E2E2);
            tvStatusBanner.setTextColor(0xFFC75B5B);
            tvStatusBanner.setText("• Belum Lunas");
            
            labelTanggalAktual.setVisibility(View.VISIBLE);
            tvTanggalAktual.setText(p.getTanggalAktualKembali());
            
            // Tampilkan tombol bayar jika admin
            if ("admin".equals(userRole)) {
                btnKembalikan.setVisibility(View.VISIBLE);
                btnKembalikan.setText("KONFIRMASI PEMBAYARAN");
                btnKembalikan.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFC75B5B));
            }
        } else if ("Dikembalikan".equals(p.getStatus())) {
            statusBannerRoot.setBackgroundResource(R.drawable.bg_approve_card_urgent);
            tvStatusBanner.getBackground().setTint(0xFFD1EAE7);
            tvStatusBanner.setTextColor(0xFF2B7A6F);
            tvStatusBanner.setText("• Selesai (Lunas)");
            
            labelTanggalAktual.setVisibility(View.VISIBLE);
            tvTanggalAktual.setText(p.getTanggalAktualKembali());
        } else {
            labelTanggalAktual.setVisibility(View.GONE);
        }

        // Render Denda
        long currentDendaTerlambat = p.getDendaTerlambat();
        if ("Dipinjam".equals(p.getStatus()) || "Menunggu Pengembalian".equals(p.getStatus())) {
            int daysLate = DateHelper.getDaysLate(p.getTanggalRencanaKembali(), "-");
            if (daysLate > 0) {
                currentDendaTerlambat = daysLate * 50000L;
            }
        }

        long totalDenda = currentDendaTerlambat + p.getDendaRusak();

        if (totalDenda > 0 || "Dikembalikan".equals(p.getStatus()) || "Menunggu Pembayaran".equals(p.getStatus())) {
            layoutDenda.setVisibility(View.VISIBLE);
            tvDendaTerlambat.setText("Rp " + String.format(Locale.getDefault(), "%,d", currentDendaTerlambat));
            tvDendaRusak.setText("Rp " + String.format(Locale.getDefault(), "%,d", p.getDendaRusak()));
            tvTotalDenda.setText("Rp " + String.format(Locale.getDefault(), "%,d", totalDenda));
        } else {
            layoutDenda.setVisibility(View.GONE);
        }

        containerItems.removeAllViews();
        for (ItemPinjam item : p.getItems()) {
            TextView tvItem = new TextView(this);
            tvItem.setText("• " + item.getNamaAset() + " x" + item.getJumlah());
            tvItem.setTextColor(0xFF1C1C1C);
            tvItem.setTextSize(14);
            tvItem.setPadding(0, 4, 0, 4);
            containerItems.addView(tvItem);
        }
    }

    private void setupListeners() {
        btnKembalikan.setOnClickListener(v -> {
            if ("Menunggu Pembayaran".equals(p.getStatus())) {
                dataManager.konfirmasiPembayaranDenda(p.getId());
                Toast.makeText(this, "Pembayaran dikonfirmasi. Transaksi Selesai.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            boolean success = dataManager.ajukanPengembalian(p.getId(), today);
            
            if (success) {
                Toast.makeText(this, "Pengembalian diajukan, menunggu persetujuan Admin", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal mengajukan pengembalian", Toast.LENGTH_SHORT).show();
            }
        });

        btnModif.setOnClickListener(v -> {
            if (!"Menunggu Persetujuan".equals(p.getStatus())) {
                Toast.makeText(this, "Hanya peminjaman yang menunggu persetujuan yang dapat dimodifikasi", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Intent intent = new Intent(this, FormPeminjamanActivity.class);
            intent.putExtra("IS_EDIT_MODE", true);
            intent.putExtra("PEMINJAMAN_ID", p.getId());
            startActivity(intent);
            finish();
        });
    }
}
