package com.example.asetpeminjaman;

import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class untuk data peminjaman aset yang kompatibel dengan Firebase
 */
public class DataPeminjaman {

    private int id;
    private String firebaseId; // Untuk referensi dokumen Firestore
    private String nama;
    private String nim;
    private String accountUsername; // Menandai pemilik akun yang mengajukan
    private List<ItemPinjam> items = new ArrayList<>();
    private String tanggalPinjam;
    private String jamPinjam;
    private String tanggalRencanaKembali;
    private String jamRencanaKembali;
    private String tanggalAktualKembali;
    private String keperluan;
    private String status; // "Menunggu Persetujuan", "Dipinjam", "Menunggu Pengembalian", "Dikembalikan", "Ditolak"
    private String priority = "Normal"; // "Normal", "Mendesak"

    // Constructor kosong diperlukan untuk Firebase
    public DataPeminjaman() {}

    public DataPeminjaman(int id, String accountUsername, String nama, String nim, List<ItemPinjam> items,
                          String tanggalPinjam, String jamPinjam,
                          String tanggalRencanaKembali, String jamRencanaKembali, String keperluan) {
        this.id = id;
        this.accountUsername = accountUsername;
        this.nama = nama;
        this.nim = nim;
        this.items = items;
        this.tanggalPinjam = tanggalPinjam;
        this.jamPinjam = jamPinjam;
        this.tanggalRencanaKembali = tanggalRencanaKembali;
        this.jamRencanaKembali = jamRencanaKembali;
        this.tanggalAktualKembali = "-";
        this.keperluan = keperluan;
        this.status = "Menunggu Persetujuan";
    }

    // Getters dan Setters (Wajib untuk Firebase mapping)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAccountUsername() { return accountUsername; }
    public void setAccountUsername(String accountUsername) { this.accountUsername = accountUsername; }
    
    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }
    
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    
    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }
    
    public List<ItemPinjam> getItems() { return items; }
    public void setItems(List<ItemPinjam> items) { this.items = items; }

    @Exclude
    public String getNamaAset() {
        if (items == null || items.isEmpty()) return "-";
        if (items.size() == 1) return items.get(0).getNamaAset();
        return items.get(0).getNamaAset() + " dan " + (items.size() - 1) + " item lainnya";
    }
    
    @Exclude
    public int getJumlah() {
        if (items == null) return 0;
        int total = 0;
        for (ItemPinjam item : items) total += item.getJumlah();
        return total;
    }
    
    public String getTanggalPinjam() { return tanggalPinjam; }
    public void setTanggalPinjam(String tanggalPinjam) { this.tanggalPinjam = tanggalPinjam; }

    // Compatibility Setters for old Firebase data
    public void setNamaAset(String namaAset) {
        if (items == null) items = new ArrayList<>();
        if (items.isEmpty()) {
            items.add(new ItemPinjam(namaAset, 0));
        } else if (items.get(0).getNamaAset() == null || items.get(0).getNamaAset().equals("-")) {
            items.get(0).setNamaAset(namaAset);
        }
    }

    public void setJumlah(int jumlah) {
        if (items == null) items = new ArrayList<>();
        if (items.isEmpty()) {
            items.add(new ItemPinjam("-", jumlah));
        } else {
            items.get(0).setJumlah(jumlah);
        }
    }

    public String getJamPinjam() { return jamPinjam; }
    public void setJamPinjam(String jamPinjam) { this.jamPinjam = jamPinjam; }
    
    public String getTanggalRencanaKembali() { return tanggalRencanaKembali; }
    public void setTanggalRencanaKembali(String tanggalRencanaKembali) { this.tanggalRencanaKembali = tanggalRencanaKembali; }

    public String getJamRencanaKembali() { return jamRencanaKembali; }
    public void setJamRencanaKembali(String jamRencanaKembali) { this.jamRencanaKembali = jamRencanaKembali; }
    
    public String getTanggalAktualKembali() { return tanggalAktualKembali; }
    public void setTanggalAktualKembali(String tanggal) { this.tanggalAktualKembali = tanggal; }
    
    public String getKeperluan() { return keperluan; }
    public void setKeperluan(String keperluan) { this.keperluan = keperluan; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    @Exclude
    public boolean isAktif() {
        return !"Dikembalikan".equals(status) && !"Ditolak".equals(status);
    }

    @Exclude
    public String getFormattedId() {
        return String.format("#%05d", id);
    }
}
