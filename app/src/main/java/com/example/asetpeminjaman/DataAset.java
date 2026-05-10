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
    private long harga; // Harga aset untuk denda jika rusak

    // Constructor kosong diperlukan untuk Firebase
    public DataAset() {}

    public DataAset(int id, String namaAset, String kategori, int stokTotal, String kondisi) {
        this.id = id;
        this.namaAset = namaAset;
        this.kategori = kategori;
        this.stokTotal = stokTotal;
        this.stokDipinjam = 0;
        this.kondisi = kondisi;
        this.harga = tentukanHargaOtomatis(kategori);
    }

    private long tentukanHargaOtomatis(String kategori) {
        switch (kategori) {
            case "Komputer": return 12000000;
            case "Elektronik": return 3500000;
            case "Mikrokomputer": return 1200000;
            case "Mikrokontroler": return 450000;
            case "Aksesoris": return 150000;
            case "Alat Ukur": return 2500000;
            case "Jaringan": return 4500000;
            default: return 1000000;
        }
    }

    // Getters
    public int getId() { return id; }
    public String getNamaAset() { return namaAset; }
    public String getKategori() { return kategori; }
    public int getStokTotal() { return stokTotal; }
    public int getStokDipinjam() { return stokDipinjam; }
    public String getKondisi() { return kondisi; }
    
    public long getHarga() { 
        if (harga <= 0) {
            return tentukanHargaOtomatis(kategori);
        }
        return harga; 
    }

    // Setters (Wajib untuk Firebase mapping)
    public void setId(int id) { this.id = id; }
    public void setNamaAset(String namaAset) { this.namaAset = namaAset; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public void setStokTotal(int stokTotal) { this.stokTotal = stokTotal; }
    public void setStokDipinjam(int stokDipinjam) { this.stokDipinjam = stokDipinjam; }
    public void setKondisi(String kondisi) { this.kondisi = kondisi; }
    public void setHarga(long harga) { this.harga = harga; }

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
