# 📱 User Guide — SARANA

> **SARANA** — Sistem Peminjaman Aset Jurusan JTIK
> Panduan lengkap penggunaan aplikasi untuk mahasiswa dan admin.

---

## Daftar Isi

- [1. Memulai Aplikasi](#1-memulai-aplikasi)
- [2. Login](#2-login)
- [3. Dashboard User](#3-dashboard-user)
- [4. Mengajukan Peminjaman](#4-mengajukan-peminjaman)
- [5. Melihat Inventaris Aset](#5-melihat-inventaris-aset)
- [6. Riwayat Peminjaman](#6-riwayat-peminjaman)
- [7. Detail Peminjaman](#7-detail-peminjaman)
- [8. Mengajukan Pengembalian](#8-mengajukan-pengembalian)
- [9. Lupa Password](#9-lupa-password)
- [10. Logout](#10-logout)
- [11. Panduan Admin](#11-panduan-admin)
- [12. FAQ](#12-faq)
- [Credits](#credits)

---

## 1. Memulai Aplikasi

Saat pertama kali membuka aplikasi SARANA, Anda akan melihat **Splash Screen** selama ±2,5 detik. Sistem akan otomatis mengecek apakah Anda sudah login sebelumnya:

- ✅ **Sudah pernah login** → Langsung masuk ke Dashboard
- ❌ **Belum login** → Diarahkan ke halaman Login

---

## 2. Login

### Cara Login

1. Buka aplikasi **SARANA**
2. Pada halaman **Login**, masukkan:
   - **Username**: Sesuai format akun yang diberikan
   - **Password**: Password akun Anda
3. Tekan tombol **SIGN IN**

### Format Akun Mahasiswa

Username mengikuti format: `[PRODI] [KELAS] [ANGKATAN]`

| Program Studi | Contoh Username |
|---------------|----------------|
| PTIK | `PTIK A 24`, `PTIK B 23`, `PTIK I 25` |
| TEKOM | `TEKOM A 24`, `TEKOM F 23` |

> **Password default:** `Maba24ft`

### Akun Admin

```
Username : admin
Password : 12345
```

> ⚠️ **Catatan Keamanan:** Segera ganti password default Anda setelah login pertama menggunakan fitur Lupa Password.

---

## 3. Dashboard User

Setelah login sebagai **mahasiswa**, Anda akan melihat halaman **Home** yang berisi:

### Statistik Pribadi

| Kartu | Informasi |
|-------|-----------|
| 📦 **Total Aset** | Jumlah total inventaris JTIK |
| 📤 **Dipinjam** | Jumlah peminjaman aktif Anda |
| ✅ **Dikembalikan** | Total peminjaman selesai |
| ⏰ **Terlambat** | Peminjaman melewati batas waktu |

> 💡 **Tip:** Ketuk kartu statistik untuk langsung menuju daftar terkait.

### Progress Bar Kapasitas

Menampilkan persentase item yang sedang Anda pinjam dibandingkan total inventaris.

### Peminjaman Aktif

Menampilkan **3 peminjaman aktif terbaru** Anda secara langsung di beranda. Jika ada denda berjalan, akan ditampilkan secara real-time.

### Quick Action

- Tombol **"+ Pinjam Aset"** → Langsung membuka Form Peminjaman
- Link **"Lihat Semua"** → Menuju tab Riwayat

---

## 4. Mengajukan Peminjaman

### Langkah-langkah

1. Dari beranda, tekan **"+ Pinjam Aset"** atau buka tab **Inventaris** dan pilih aset
2. Isi formulir peminjaman:

| Field | Keterangan |
|-------|------------|
| **Nama Peminjam** | Nama lengkap sesuai KTM |
| **NIM / NIP** | Nomor Induk Mahasiswa/Pegawai |
| **Tanggal Pinjam** | Pilih tanggal (Date Picker) |
| **Jam Pinjam** | Pilih jam (Time Picker) |
| **Tanggal Kembali** | Rencana tanggal pengembalian |
| **Jam Kembali** | Rencana jam pengembalian |
| **Keperluan** | Jelaskan tujuan peminjaman |
| **Aset** | Pilih dari dropdown (tersedia dan stoknya) |
| **Jumlah** | Masukkan jumlah unit yang dipinjam |

3. Gunakan **"+ Tambah Aset"** untuk menambah jenis aset (maksimal 5 jenis per pengajuan)
4. Tekan **"AJUKAN PEMINJAMAN"**

### Validasi Sistem

- ❌ Tanggal pengembalian tidak boleh sebelum tanggal peminjaman
- ❌ Semua field wajib diisi
- ❌ Jumlah yang diminta tidak boleh melebihi stok tersedia
- ❌ Minimal 1 aset harus dipilih
- ❌ Maksimal 5 jenis aset per pengajuan

### Status Setelah Pengajuan

Setelah berhasil diajukan, status peminjaman akan menjadi **"Menunggu Persetujuan"** hingga Admin menyetujui.

---

## 5. Melihat Inventaris Aset

1. Buka tab **Inventaris** (ikon di navigasi bawah)
2. Lihat semua aset dalam tampilan **grid 2 kolom**
3. Setiap kartu aset menampilkan:
   - 🔣 Ikon kategori
   - Nama aset
   - Kategori (ELEKTRONIK, AKSESORIS, dll)
   - Stok tersedia
   - Badge status (**Tersedia** / **Habis**)

4. Tekan kartu aset yang **Tersedia** untuk langsung membuka Form Peminjaman dengan aset tersebut sudah terisi

### Kapasitas per Kategori

Di bagian bawah inventaris, terdapat progress bar per kategori yang menunjukkan persentase ketersediaan aset.

---

## 6. Riwayat Peminjaman

1. Buka tab **Riwayat** di navigasi bawah
2. Lihat semua riwayat peminjaman Anda

### Filter Tab

| Tab | Menampilkan |
|-----|-------------|
| **Semua** | Seluruh riwayat |
| **Dipinjam** | Sedang dipinjam / Menunggu pengembalian |
| **Terlambat** | Melewati batas waktu pengembalian |
| **Kembali** | Sudah dikembalikan |
| **Denda** | Memiliki denda (terlambat / rusak) |

### Fitur Pencarian

Ketik di kolom **Cari** untuk mencari berdasarkan:
- Nama peminjam
- NIM
- ID transaksi
- Nama aset

### Sorting

Gunakan dropdown **Sort** untuk mengurutkan berdasarkan:
- **Terbaru** — Paling baru di atas
- **Terlama** — Paling lama di atas
- **Kategori** — Urut per kategori aset

### "Tampilkan Semua"

Secara default hanya 5 transaksi yang ditampilkan. Tekan **"Tampilkan Semua"** untuk melihat seluruh riwayat.

---

## 7. Detail Peminjaman

Tekan transaksi manapun dari Riwayat atau Beranda untuk melihat detail lengkap:

- **ID Transaksi** (format: #PM-XXXX)
- **Nama & NIM** peminjam
- **Daftar aset** yang dipinjam beserta jumlah
- **Waktu pinjam** & **rencana kembali**
- **Tanggal aktual kembali** (jika sudah dikembalikan)
- **Keperluan** peminjaman
- **Status** peminjaman
- **Rincian denda** (jika ada): denda terlambat, denda rusak, total denda

---

## 8. Mengajukan Pengembalian

Saat aset siap dikembalikan:

1. Buka halaman **Detail Peminjaman** (status: **Dipinjam**)
2. Tekan tombol **"KEMBALIKAN ASET"**
3. Sistem akan mengubah status menjadi **"Menunggu Pengembalian"**
4. Admin akan memverifikasi kondisi aset
5. Jika kondisi baik → Status berubah ke **"Dikembalikan"**
6. Jika ada kerusakan → Admin menginput denda, status menjadi **"Menunggu Pembayaran"**

---

## 9. Lupa Password

Jika lupa password:

1. Di halaman Login, tekan **"Lupa Password?"**
2. Masukkan **Username** Anda
3. Masukkan **3 karakter terakhir password lama** (digabung sebagai kunci verifikasi)
4. Tekan **"Verifikasi"**
5. Jika berhasil, masukkan **password baru** (minimal 5 karakter)
6. Tekan **"Simpan"**

> 💡 Kunci verifikasi dibentuk dari 3 karakter terakhir password Anda. Contoh: password `Maba24ft` → kunci = `"4" + "f" + "t"` = `4ft`

---

## 10. Logout

Ada dua cara logout:

1. **Dari Beranda** — Tekan ikon ← (back/logout) di pojok kanan atas
2. **Dari Profil** — Buka tab Profil → Tekan tombol **"Keluar"**

Setelah logout, sesi Anda akan dihapus dan akan diarahkan kembali ke halaman Login.

---

## 11. Panduan Admin

> Panduan lengkap untuk Admin tersedia di [ROLE_GUIDE.md](ROLE_GUIDE.md)

Fitur eksklusif Admin:
- **Dashboard KPI**: Total aset, jenis aset, pending pinjam, pending kembali, kapasitas per kategori
- **Kelola Inventaris**: Tambah aset baru, hapus aset, tambah stok
- **Persetujuan**: Setujui / tolak pengajuan pinjam & pengembalian
- **Input Denda Kerusakan**: Hitung denda per item rusak berdasarkan harga aset
- **Konfirmasi Pembayaran**: Tutup transaksi setelah denda dilunasi
- **Riwayat Semua User**: Admin dapat melihat seluruh riwayat transaksi sistem

---

## 12. FAQ

**Q: Mengapa peminjaman saya masih berstatus "Menunggu Persetujuan"?**
> A: Pengajuan Anda belum disetujui oleh Admin. Harap tunggu konfirmasi.

**Q: Berapa denda keterlambatan per hari?**
> A: Rp 50.000 per hari keterlambatan, dihitung otomatis oleh sistem.

**Q: Bisakah saya mengubah pengajuan yang sudah diajukan?**
> A: Ya, selama status masih **"Menunggu Persetujuan"**. Buka Detail → Tekan "MODIFIKASI".

**Q: Bagaimana cara mengetahui aset mana yang tersedia?**
> A: Buka tab **Inventaris**. Aset dengan badge **"Tersedia"** dapat dipinjam.

**Q: Mengapa saya tidak bisa memilih aset tertentu?**
> A: Stok aset tersebut mungkin habis (semua sedang dipinjam). Tunggu hingga ada yang dikembalikan.

**Q: Apakah bisa meminjam lebih dari 1 jenis aset sekaligus?**
> A: Ya, bisa hingga **5 jenis aset** dalam 1 pengajuan. Gunakan tombol **"+ Tambah Aset"** di form.

---

## Credits

## Credits

Kelompok 2
1. MUH ASYAM ASHARI ANSAR
2. Muhammad Rifqi Ramdani Abdullah
3. Ahmad Fakhri Syafa
4. St. Muslimah Nursalam

---

*Dokumen ini merupakan bagian dari dokumentasi resmi SARANA. Lihat [README.md](../README.md) untuk gambaran umum project.*
