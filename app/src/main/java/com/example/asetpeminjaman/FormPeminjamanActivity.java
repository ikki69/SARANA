package com.example.asetpeminjaman;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.LayoutInflater;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FormPeminjamanActivity extends AppCompatActivity {

    private EditText etNama, etNim, etTanggalPinjam, etJamPinjam, etTanggalKembali, etJamKembali, etKeperluan;
    private LinearLayout containerItems;
    private Button btnAjukan, btnReset, btnAddItem;
    private ImageView btnBack;

    private DataManager dataManager;
    private List<DataAset> listAset;
    private List<View> itemViews = new ArrayList<>();
    private int editPeminjamanId = -1;
    private boolean isEditMode = false;
    private DataPeminjaman existingPeminjaman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_peminjaman);

        dataManager = DataManager.getInstance();
        listAset = dataManager.getAllAset();
        
        // Cek apakah ini mode edit
        isEditMode = getIntent().getBooleanExtra("IS_EDIT_MODE", false);
        editPeminjamanId = getIntent().getIntExtra("PEMINJAMAN_ID", -1);

        initViews();
        setupClickListeners();
        
        if (isEditMode && editPeminjamanId != -1) {
            loadExistingData();
        } else {
            setTanggalHariIni();
            String preselectedAset = getIntent().getStringExtra("NAMA_ASET");
            if (preselectedAset != null) {
                addItemRow(new ItemPinjam(preselectedAset, 1));
            } else {
                addItemRow(null); // Tambahkan satu baris kosong di awal
            }
        }
    }

    private void loadExistingData() {
        existingPeminjaman = dataManager.getPeminjamanById(editPeminjamanId);
        if (existingPeminjaman != null) {
            etNama.setText(existingPeminjaman.getNama());
            etNim.setText(existingPeminjaman.getNim());
            etTanggalPinjam.setText(existingPeminjaman.getTanggalPinjam());
            etJamPinjam.setText(existingPeminjaman.getJamPinjam());
            etTanggalKembali.setText(existingPeminjaman.getTanggalRencanaKembali());
            etJamKembali.setText(existingPeminjaman.getJamRencanaKembali());
            etKeperluan.setText(existingPeminjaman.getKeperluan());
            
            // Populate items
            for (ItemPinjam item : existingPeminjaman.getItems()) {
                addItemRow(item);
            }
            
            btnAjukan.setText("SIMPAN PERUBAHAN");
        }
    }

    private void initViews() {
        etNama = findViewById(R.id.etNama);
        etNim = findViewById(R.id.etNim);
        etTanggalPinjam = findViewById(R.id.etTanggalPinjam);
        etJamPinjam = findViewById(R.id.etJamPinjam);
        etTanggalKembali = findViewById(R.id.etTanggalKembali);
        etJamKembali = findViewById(R.id.etJamKembali);
        etKeperluan = findViewById(R.id.etKeperluan);
        containerItems = findViewById(R.id.containerItems);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnAjukan = findViewById(R.id.btnAjukan);
        btnReset = findViewById(R.id.btnReset);
        btnBack = findViewById(R.id.btnBack);
    }

    private void addItemRow(ItemPinjam item) {
        if (itemViews.size() >= 5) {
            Toast.makeText(this, "Maksimal 5 jenis aset", Toast.LENGTH_SHORT).show();
            return;
        }

        View rowView = LayoutInflater.from(this).inflate(R.layout.item_form_aset, null);
        Spinner spinner = rowView.findViewById(R.id.spinnerAsetRow);
        EditText etJumlah = rowView.findViewById(R.id.etJumlahRow);
        ImageView btnRemove = rowView.findViewById(R.id.btnRemoveItem);

        // Setup Spinner
        List<String> namaAset = new ArrayList<>();
        namaAset.add("-- Pilih Aset --");
        for (DataAset aset : listAset) {
            namaAset.add(aset.getNamaAset() + " (Tersedia: " + aset.getStokTersedia() + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaAset);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (item != null) {
            etJumlah.setText(String.valueOf(item.getJumlah()));
            for (int i = 0; i < listAset.size(); i++) {
                if (listAset.get(i).getNamaAset().equals(item.getNamaAset())) {
                    spinner.setSelection(i + 1);
                    break;
                }
            }
        }

        btnRemove.setOnClickListener(v -> {
            if (itemViews.size() > 1) {
                containerItems.removeView(rowView);
                itemViews.remove(rowView);
            } else {
                Toast.makeText(this, "Minimal 1 aset harus dipilih", Toast.LENGTH_SHORT).show();
            }
        });

        containerItems.addView(rowView);
        itemViews.add(rowView);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddItem.setOnClickListener(v -> addItemRow(null));

        etTanggalPinjam.setOnClickListener(v -> showDatePicker(etTanggalPinjam));
        etJamPinjam.setOnClickListener(v -> showTimePicker(etJamPinjam));
        etTanggalKembali.setOnClickListener(v -> showDatePicker(etTanggalKembali));
        etJamKembali.setOnClickListener(v -> showTimePicker(etJamKembali));

        btnAjukan.setOnClickListener(v -> ajukanPeminjaman());
        btnReset.setOnClickListener(v -> {
            resetForm();
            Toast.makeText(this, "Form telah direset", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDatePicker(final EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String tanggal = String.format("%02d/%02d/%04d", day, month + 1, year);
            targetEditText.setText(tanggal);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(final EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String waktu = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            targetEditText.setText(waktu);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void setTanggalHariIni() {
        Calendar calendar = Calendar.getInstance();
        etTanggalPinjam.setText(String.format("%02d/%02d/%04d", 
            calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
        etJamPinjam.setText(String.format(Locale.getDefault(), "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
    }

    private void ajukanPeminjaman() {
        String nama = etNama.getText().toString().trim();
        String nim = etNim.getText().toString().trim();
        String tanggalPinjam = etTanggalPinjam.getText().toString().trim();
        String jamPinjam = etJamPinjam.getText().toString().trim();
        String tanggalKembali = etTanggalKembali.getText().toString().trim();
        String jamKembali = etJamKembali.getText().toString().trim();
        String keperluan = etKeperluan.getText().toString().trim();

        if (nama.isEmpty() || nim.isEmpty() || tanggalPinjam.isEmpty() || jamPinjam.isEmpty() || 
            tanggalKembali.isEmpty() || jamKembali.isEmpty() || keperluan.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field utama!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ItemPinjam> selectedItems = new ArrayList<>();
        for (View row : itemViews) {
            Spinner spinner = row.findViewById(R.id.spinnerAsetRow);
            EditText etJumlahRow = row.findViewById(R.id.etJumlahRow);
            
            if (spinner.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Pilih aset pada setiap baris!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String jumlahStr = etJumlahRow.getText().toString().trim();
            if (jumlahStr.isEmpty()) {
                Toast.makeText(this, "Isi jumlah unit!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int jumlah = Integer.parseInt(jumlahStr);
            String namaAset = listAset.get(spinner.getSelectedItemPosition() - 1).getNamaAset();
            
            // Cek stok (sederhana)
            DataAset aset = dataManager.getAsetByNama(namaAset);
            if (aset != null) {
                int stokTersedia = aset.getStokTersedia();
                // Jika edit, hitung ulang stok tersedia
                if (isEditMode && existingPeminjaman != null) {
                    for (ItemPinjam itemLama : existingPeminjaman.getItems()) {
                        if (itemLama.getNamaAset().equals(namaAset)) {
                            stokTersedia += itemLama.getJumlah();
                            break;
                        }
                    }
                }
                
                if (stokTersedia < jumlah) {
                    Toast.makeText(this, "Stok " + namaAset + " tidak mencukupi!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            selectedItems.add(new ItemPinjam(namaAset, jumlah));
        }

        if (isEditMode && existingPeminjaman != null) {
            existingPeminjaman.setNama(nama);
            existingPeminjaman.setNim(nim);
            existingPeminjaman.setItems(selectedItems);
            existingPeminjaman.setTanggalPinjam(tanggalPinjam);
            existingPeminjaman.setJamPinjam(jamPinjam);
            existingPeminjaman.setTanggalRencanaKembali(tanggalKembali);
            existingPeminjaman.setJamRencanaKembali(jamKembali);
            existingPeminjaman.setKeperluan(keperluan);
            
            dataManager.updatePeminjaman(existingPeminjaman);
            Toast.makeText(this, "Peminjaman berhasil diperbarui!", Toast.LENGTH_LONG).show();
        } else {
            SharedPreferences pref = getSharedPreferences("USER_DATA", MODE_PRIVATE);
            String accountUsername = pref.getString("username", "Pengguna");

            DataPeminjaman baru = new DataPeminjaman(
                new Random().nextInt(99999), accountUsername, nama, nim, selectedItems,
                tanggalPinjam, jamPinjam, tanggalKembali, jamKembali, keperluan
            );
            dataManager.tambahPeminjaman(baru);
            Toast.makeText(this, "Peminjaman diajukan! Menunggu persetujuan Admin.", Toast.LENGTH_LONG).show();
        }

        finish();
    }

    private void resetForm() {
        etNama.setText(""); etNim.setText("");
        etTanggalKembali.setText(""); etJamKembali.setText(""); etKeperluan.setText("");
        containerItems.removeAllViews();
        itemViews.clear();
        setTanggalHariIni();
        addItemRow(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
