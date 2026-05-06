package com.example.asetpeminjaman;

/**
 * Model class untuk data aset/inventaris jurusan yang kompatibel dengan Firebase
 */
public class DataAset {

    private int id;
    private String namaAset;
    private String kategori;
    private int stokTotal;
    private int stokDipinjam;
    private String kondisi;

    // Constructor kosong diperlukan untuk Firebase
    public DataAset() {}

    public DataAset(int id, String namaAset, String kategori, int stokTotal, String kondisi) {
        this.id = id;
        this.namaAset = namaAset;
        this.kategori = kategori;
        this.stokTotal = stokTotal;
        this.stokDipinjam = 0;
        this.kondisi = kondisi;
    }

    // Getters
    public int getId() { return id; }
    public String getNamaAset() { return namaAset; }
    public String getKategori() { return kategori; }
    public int getStokTotal() { return stokTotal; }
    public int getStokDipinjam() { return stokDipinjam; }
    public String getKondisi() { return kondisi; }

    // Setters (Wajib untuk Firebase mapping)
    public void setId(int id) { this.id = id; }
    public void setNamaAset(String namaAset) { this.namaAset = namaAset; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public void setStokTotal(int stokTotal) { this.stokTotal = stokTotal; }
    public void setStokDipinjam(int stokDipinjam) { this.stokDipinjam = stokDipinjam; }
    public void setKondisi(String kondisi) { this.kondisi = kondisi; }

    /**
     * Stok tersedia = stok total - stok yang sedang dipinjam
     */
    public int getStokTersedia() {
        return stokTotal - stokDipinjam;
    }

    /**
     * Mengecek apakah aset masih tersedia untuk dipinjam
     */
    public boolean isTersedia() {
        return getStokTersedia() > 0;
    }

    @Override
    public String toString() {
        return namaAset; // Dipakai untuk Spinner display
    }
}
