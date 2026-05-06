package com.example.asetpeminjaman;

public class ItemPinjam {
    private String namaAset;
    private int jumlah;

    public ItemPinjam() {}

    public ItemPinjam(String namaAset, int jumlah) {
        this.namaAset = namaAset;
        this.jumlah = jumlah;
    }

    public String getNamaAset() { return namaAset; }
    public void setNamaAset(String namaAset) { this.namaAset = namaAset; }

    public int getJumlah() { return jumlah; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }
}
