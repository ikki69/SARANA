# 👥 Role Guide — SARANA

> Panduan lengkap untuk setiap role pengguna dalam sistem SARANA.

---

## Daftar Isi

- [Ringkasan Role](#ringkasan-role)
- [Role: User (Mahasiswa)](#role-user-mahasiswa)
- [Role: Admin](#role-admin)
- [Matriks Permission](#matriks-permission)
- [Siklus Status Peminjaman](#siklus-status-peminjaman)
- [Credits](#credits)

---

## Ringkasan Role

SARANA memiliki **2 role pengguna** dengan hak akses yang berbeda:

| Aspek | User (Mahasiswa) | Admin |
|-------|-----------------|-------|
| **Autentikasi** | Firestore (`users` collection) | Hardcoded (`admin` / `12345`) |
| **Navigasi** | 3 tab: Home, Inventaris, Riwayat | 3 tab: Reports, Approve, Inventaris |
| **Scope Data** | Hanya data milik sendiri | Seluruh data sistem |
| **Manajemen Aset** | View only | Full CRUD |
| **Persetujuan** | ❌ Tidak bisa | ✅ Bisa |
| **Reset Password** | Melalui verifikasi riwayat | N/A (hardcoded) |

---

## Role: User (Mahasiswa)

### Deskripsi

Role **User** adalah mahasiswa Jurusan JTIK yang menggunakan aplikasi SARANA untuk meminjam dan mengelola peminjaman aset inventaris jurusan.

### Cara Mendapatkan Akses

Akun User dibuat otomatis oleh sistem saat inisialisasi. Format username:

```
[PRODI] [KELAS] [ANGKATAN]

Contoh:
- PTIK A 23  (Prodi PTIK, Kelas A, Angkatan 2023)
- PTIK B 24  (Prodi PTIK, Kelas B, Angkatan 2024)
- TEKOM A 25 (Prodi TEKOM, Kelas A, Angkatan 2025)
```

**Akun yang dibuat secara otomatis:**

| Prodi | Rentang Kelas | Angkatan |
|-------|---------------|----------|
| PTIK | A sampai I | 23, 24, 25 |
| TEKOM | A sampai F | 23, 24, 25 |

**Password default:** `Maba24ft`

### Hak Akses & Fitur

#### ✅ Diizinkan

| Fitur | Keterangan |
|-------|------------|
| **Login / Logout** | Masuk dan keluar dari aplikasi |
| **Lihat Dashboard** | Statistik peminjaman pribadi (total aset, dipinjam, kembali, terlambat) |
| **Lihat Inventaris** | Melihat semua aset beserta ketersediaan stok |
| **Ajukan Peminjaman** | Mengisi form peminjaman dengan 1–5 jenis aset |
| **Edit Peminjaman** | Modifikasi form selama status "Menunggu Persetujuan" |
| **Lihat Riwayat Sendiri** | Hanya melihat transaksi peminjaman milik sendiri |
| **Filter & Cari Riwayat** | Filter berdasarkan status, cari berdasarkan nama/NIM/ID |
| **Lihat Detail Peminjaman** | Detail lengkap transaksi termasuk denda |
| **Ajukan Pengembalian** | Memulai proses pengembalian aset (status → "Menunggu Pengembalian") |
| **Reset Password** | Mengubah password via verifikasi riwayat karakter |

#### ❌ Tidak Diizinkan (Restricted)

| Fitur | Alasan Pembatasan |
|-------|------------------|
| Melihat data peminjaman user lain | Privacy & data isolation |
| Menyetujui / menolak pengajuan | Wewenang Admin saja |
| Menambah / menghapus aset | Wewenang Admin saja |
| Mengubah stok aset | Wewenang Admin saja |
| Mengkonfirmasi pembayaran denda | Wewenang Admin saja |
| Melihat dashboard KPI global | Wewenang Admin saja |
| Melihat semua riwayat sistem | Wewenang Admin saja |

### Workflow User (Alur Kerja)

```
1. LOGIN
   └── Masukkan username & password
   
2. BROWSE ASET
   └── Tab "Inventaris" → Lihat aset tersedia
   
3. AJUKAN PEMINJAMAN
   └── Tekan aset / "Pinjam Aset" → Isi form → Submit
   └── Status: "Menunggu Persetujuan"
   
4. TUNGGU PERSETUJUAN
   └── Admin akan menyetujui atau menolak
   └── Status berubah: "Dipinjam" atau "Ditolak"
   
   [Jika masih menunggu & ingin diubah]
   └── Buka Detail → Tekan "MODIFIKASI" → Edit form → Submit
   
5. PROSES PEMINJAMAN (Status: "Dipinjam")
   └── Ambil aset dari admin
   └── Gunakan sesuai keperluan
   
6. KEMBALIKAN ASET
   └── Buka Detail → Tekan "KEMBALIKAN ASET"
   └── Status: "Menunggu Pengembalian"
   
7. VERIFIKASI ADMIN
   └── Admin memeriksa kondisi aset
   
   [Kondisi baik]
   └── Status: "Dikembalikan" ✅
   
   [Ada kerusakan]
   └── Admin input denda → Status: "Menunggu Pembayaran"
   └── Bayar denda → Admin konfirmasi → Status: "Dikembalikan" ✅
```

### User Journey

**Skenario: Mahasiswa meminjam proyektor untuk presentasi**

```
Budi (PTIK A 24) hendak presentasi besok. Ia butuh proyektor dan HDMI.

1. Budi buka aplikasi SARANA
2. Login dengan username "PTIK A 24" dan password "Maba24ft"
3. Di beranda, Budi menekan "+ Pinjam Aset"
4. Mengisi form:
   - Nama: Budi Santoso
   - NIM: 2401234
   - Tanggal Pinjam: 15/05/2026, 08:00
   - Tanggal Kembali: 16/05/2026, 17:00
   - Keperluan: Presentasi Tugas Akhir
   - Aset 1: Proyektor Epson EB-X400 × 1
   - Aset 2: Kabel HDMI 5m × 1
5. Submit → Status: "Menunggu Persetujuan"
6. Admin menyetujui → Status: "Dipinjam"
7. Budi ambil proyektor & HDMI dari admin
8. Setelah presentasi, Budi buka Detail → "KEMBALIKAN ASET"
9. Admin verifikasi kondisi, semua baik → "Dikembalikan"
```

---

## Role: Admin

### Deskripsi

Role **Admin** adalah pengelola sistem SARANA yang bertanggung jawab atas manajemen aset, persetujuan peminjaman, verifikasi pengembalian, dan pengelolaan denda.

### Credential Admin

```
Username : admin
Password : 12345
```

> 🔒 **Catatan:** Akun Admin bersifat hardcoded di `LoginActivity.java`. Tidak tersimpan di Firestore. Tidak memiliki fitur reset password.

### Hak Akses & Fitur

#### ✅ Akses Penuh Admin

| Fitur | Keterangan |
|-------|------------|
| **Dashboard KPI Global** | Total aset, jenis aset, pending pinjam, pending kembali, kapasitas per kategori |
| **Lihat Semua Riwayat** | Melihat seluruh transaksi dari semua user |
| **Filter & Cari Global** | Filter dan cari transaksi semua mahasiswa |
| **Approve Peminjaman** | Menyetujui pengajuan → stok berkurang, status "Dipinjam" |
| **Tolak Peminjaman** | Menolak pengajuan → status "Ditolak" |
| **Approve Pengembalian** | Verifikasi kondisi aset setelah kembali |
| **Input Denda Kerusakan** | Menghitung & memasukkan denda per item rusak × harga aset |
| **Konfirmasi Pembayaran** | Menutup transaksi setelah denda dilunasi |
| **Tambah Aset** | Menambah aset baru ke inventaris |
| **Hapus Aset** | Menghapus aset dari inventaris |
| **Tambah Stok** | Menambah jumlah unit stok aset yang ada |
| **Lihat Detail Aset** | Detail stok total, dipinjam, tersedia, kondisi, harga |
| **Logout** | Keluar dari sistem |

#### Navigasi Admin

Admin memiliki menu navigasi bawah yang berbeda dari user:

| Tab | Icon | Fragment | Fungsi |
|-----|------|----------|--------|
| **Reports** | 📊 | `AdminDashboardFragment` | Dashboard KPI & statistik |
| **Approve** | ✅ | `ApproveFragment` | Daftar pengajuan pending |
| **Inventaris** | 📦 | `InventoryFragment` | Kelola aset (CRUD) |

### Workflow Admin (Alur Kerja)

#### Alur Persetujuan Peminjaman

```
1. Admin membuka tab "Approve"
2. Melihat daftar pengajuan dengan status:
   - 🔵 "Pinjam" → dari User yang mengajukan peminjaman baru
   - 🟡 "Pengembalian" → dari User yang mengajukan kembali
   
3. Untuk pengajuan PINJAM:
   ├── ✅ Setujui → Status "Dipinjam", stok dikurangi
   └── ❌ Tolak  → Status "Ditolak"
   
4. Untuk pengajuan KEMBALI:
   ├── ✅ "Ya, Semua Baik" → Status "Dikembalikan", stok dikembalikan
   └── ⚠️ "Ada yang Rusak" → Input jumlah rusak per item
       └── Sistem hitung: jumlah_rusak × harga_aset = denda_rusak
           ├── + denda terlambat (jika ada)
           └── Status "Menunggu Pembayaran" / "Dikembalikan"
```

#### Alur Manajemen Aset

```
1. Admin membuka tab "Inventaris"
2. Lihat semua aset dalam grid
3. Tekan kartu aset untuk membuka dialog detail:
   - Lihat stok total, dipinjam, tersedia
   - Tekan "TAMBAH STOK" → Input unit tambahan
   - Tekan "HAPUS" → Konfirmasi penghapusan

4. Tekan FAB (+) untuk menambah aset baru:
   - Nama aset
   - Kategori (Komputer/Elektronik/Mikrokomputer/dll)
   - Jumlah stok
   - Harga (opsional, auto dari kategori jika kosong)
   - Kondisi (Baik/Rusak Ringan/Rusak Berat)
```

#### Harga Otomatis per Kategori

Jika harga tidak diisi saat menambah aset, sistem akan menggunakan harga default:

| Kategori | Harga Default |
|----------|--------------|
| Komputer | Rp 12.000.000 |
| Jaringan | Rp 4.500.000 |
| Elektronik | Rp 3.500.000 |
| Alat Ukur | Rp 2.500.000 |
| Mikrokomputer | Rp 1.200.000 |
| Mikrokontroler | Rp 450.000 |
| Aksesoris | Rp 150.000 |
| Lainnya | Rp 1.000.000 |

#### Kalkulasi Denda

| Jenis Denda | Cara Hitung |
|-------------|------------|
| **Denda Terlambat** | `jumlah_hari_terlambat × Rp 50.000` |
| **Denda Kerusakan** | `jumlah_unit_rusak × harga_aset` |
| **Total Denda** | `denda_terlambat + denda_rusak` |

### User Journey Admin

**Skenario: Admin memproses pengembalian dengan denda**

```
Admin menerima notifikasi pengembalian dari Budi.

1. Admin buka tab "Approve"
2. Melihat card berwarna KUNING: "• Pengembalian - Request Kembali"
   - Nama: Budi Santoso | NIM: 2401234
   - Aset: Proyektor Epson EB-X400 × 1, Kabel HDMI 5m × 1
   - Kembali: 20/05/2026 (terlambat 4 hari)

3. Admin tekan "✅ SETUJUI"
4. Dialog muncul: "Apakah semua aset dikembalikan dalam kondisi baik?"
5. Admin tekan "Ada yang Rusak"
6. Dialog input kerusakan:
   - Proyektor Epson EB-X400 (Dipinjam: 1) → Admin isi: 1 unit rusak
   - Kabel HDMI 5m (Dipinjam: 1) → Admin isi: 0 unit rusak
7. Admin tekan "Proses Denda"

Sistem menghitung:
- Denda Terlambat: 4 hari × Rp 50.000 = Rp 200.000
- Denda Rusak: 1 unit Proyektor × Rp 3.500.000 = Rp 3.500.000
- Total: Rp 3.700.000
- Status → "Menunggu Pembayaran"

8. Setelah Budi membayar denda, Admin buka Detail transaksi
9. Tekan "KONFIRMASI PEMBAYARAN"
10. Status → "Dikembalikan" ✅
```

---

## Matriks Permission

| Fitur | User | Admin |
|-------|:----:|:-----:|
| Login / Logout | ✅ | ✅ |
| Lihat inventaris | ✅ | ✅ |
| Ajukan peminjaman | ✅ | ❌ |
| Edit peminjaman (pending) | ✅ | ❌ |
| Ajukan pengembalian | ✅ | ❌ |
| Lihat riwayat sendiri | ✅ | ✅ |
| Lihat semua riwayat | ❌ | ✅ |
| Filter & sort riwayat | ✅ | ✅ |
| Lihat detail peminjaman | ✅ | ✅ |
| Reset password | ✅ | ❌ |
| Setujui/tolak pinjam | ❌ | ✅ |
| Verifikasi pengembalian | ❌ | ✅ |
| Input denda kerusakan | ❌ | ✅ |
| Konfirmasi pembayaran | ❌ | ✅ |
| Tambah aset baru | ❌ | ✅ |
| Hapus aset | ❌ | ✅ |
| Tambah stok aset | ❌ | ✅ |
| Lihat dashboard KPI global | ❌ | ✅ |
| Lihat kapasitas per kategori | ✅ (milik) | ✅ (global) |

---

## Siklus Status Peminjaman

```
                    ┌─────────────────────┐
              ┌────►│  Menunggu Persetujuan │◄── User Submit Form
              │     └──────────┬──────────┘
              │                │
              │         ┌──────┴──────┐
              │         │  Admin Cek   │
              │         └──────┬──────┘
              │                │
              │        ┌───────┴────────┐
              │        │                │
              │   ┌────▼────┐      ┌────▼───┐
              │   │ Dipinjam │      │ Ditolak │
              │   └────┬────┘      └────────┘
              │        │
              │   User Ajukan Kembali
              │        │
              │   ┌────▼───────────────┐
              │   │ Menunggu Pengembalian│
              │   └────────┬───────────┘
              │            │
              │     ┌──────┴──────┐
              │     │  Admin Cek   │
              │     └──────┬──────┘
              │            │
              │    ┌───────┴────────┐
              │    │                │
              │ ┌──▼──────────┐ ┌───▼──────────────┐
              │ │ Dikembalikan│ │ Menunggu Pembayaran│
              │ └─────────────┘ └───────┬────────────┘
              │                         │
              │                  Admin Konfirmasi
              │                         │
              └─────────────────────────┘
                     Dikembalikan ✅
```

### Penjelasan Status

| Status | Warna | Artinya |
|--------|-------|---------|
| **Menunggu Persetujuan** | ⚪ Abu-abu | User sudah submit, menunggu Admin |
| **Dipinjam** | 🔵 Biru | Admin menyetujui, aset sedang dipinjam |
| **Ditolak** | ⚫ Gelap | Admin menolak pengajuan |
| **Menunggu Pengembalian** | 🟡 Kuning | User mengajukan kembali, menunggu verifikasi Admin |
| **Menunggu Pembayaran** | 🔴 Merah | Ada denda, menunggu pembayaran dari peminjam |
| **Dikembalikan** | 🟢 Hijau | Transaksi selesai, aset sudah dikembalikan |

---

## Credits

Kelompok 2
1. MUH ASYAM ASHARI ANSAR
2. Muhammad Rifqi Ramdani Abdullah
3. Ahmad Fakhri Syafa
4. St. Muslimah Nursalam

---

*Dokumen ini merupakan bagian dari dokumentasi resmi SARANA. Lihat [README.md](../README.md) untuk gambaran umum project.*
