# 📱 Sistem Peminjaman Aset Jurusan
### Aplikasi Android Studio - Tugas Pemrograman Mobile

---

## 📋 Informasi Proyek

| Item | Detail |
|------|--------|
| **Nama Aplikasi** | Sistem Peminjaman Aset Jurusan |
| **Package** | com.example.asetpeminjaman |
| **Min SDK** | API 21 (Android 5.0 Lollipop) |
| **Target SDK** | API 33 (Android 13) |
| **Language** | Java |

---

## 🎯 Modul yang Diimplementasikan

### Modul 3 - View dan ViewGroup
- **TextView** → Menampilkan label, judul, statistik dashboard
- **EditText** → Input nama, NIM, jumlah, keperluan pada form peminjaman
- **Button** → Tombol ajukan, reset, kembali, filter tab
- **ImageView** → Tombol back (icon panah)
- **Spinner** → Dropdown pilihan aset pada form
- **Toast** → Notifikasi konfirmasi & pesan error
- **Click Event** → Semua tombol menggunakan `setOnClickListener`
- **ListView + Custom Adapter** → Daftar aset & riwayat peminjaman

### Modul 4 - Layout
- **LinearLayout** (vertical) → Container utama semua activity
- **LinearLayout** (horizontal) → Baris statistik & tab filter
- **ConstraintLayout** → SplashActivity
- **ScrollView** → Form peminjaman agar bisa di-scroll
- **Gradient Background** → Drawable XML untuk header & splash

### Modul 5 - Activity Lifecycle
- **onCreate()** → Inisialisasi View dan data di semua activity
- **onStart()** → Tercatat di semua activity
- **onResume()** → Refresh data di MainActivity, RiwayatActivity, DaftarAsetActivity
- **onPause(), onStop(), onDestroy()** → Override lengkap di SplashActivity
- Lifecycle penuh diimplementasikan di SplashActivity

### Modul 6 - Intent
- **Intent Eksplisit** → Semua navigasi antar activity (6 activity)
- **putExtra** → Kirim PEMINJAMAN_ID dari Riwayat ke Detail
- **putExtra** → Kirim NAMA_ASET dari DaftarAset ke FormPeminjaman
- **getIntExtra / getStringExtra** → Terima data di activity tujuan
- **finish()** → Semua tombol back menggunakan finish()
- **noHistory** → SplashActivity tidak masuk back stack

---

## 🏗️ Struktur Activity

```
SplashActivity (LAUNCHER)
    ↓ Intent Eksplisit (2.5 detik)
MainActivity (Dashboard)
    ├── Intent → DaftarAsetActivity
    │               ↓ Intent + putExtra(NAMA_ASET)
    │           FormPeminjamanActivity
    ├── Intent → FormPeminjamanActivity
    └── Intent → RiwayatActivity
                    ↓ Intent + putExtra(PEMINJAMAN_ID)
                DetailPeminjamanActivity
```

---

## 📂 Struktur File

```
app/src/main/
├── java/com/example/asetpeminjaman/
│   ├── DataPeminjaman.java      (Model data peminjaman)
│   ├── DataAset.java            (Model data aset)
│   ├── DataManager.java         (Singleton data manager)
│   ├── SplashActivity.java      (Splash screen + full lifecycle)
│   ├── MainActivity.java        (Dashboard utama)
│   ├── FormPeminjamanActivity.java (Form input peminjaman)
│   ├── DaftarAsetActivity.java  (List inventaris aset)
│   ├── RiwayatActivity.java     (Riwayat semua peminjaman)
│   └── DetailPeminjamanActivity.java (Detail + konfirmasi kembali)
├── res/
│   ├── layout/
│   │   ├── activity_splash.xml
│   │   ├── activity_main.xml
│   │   ├── activity_form_peminjaman.xml
│   │   ├── activity_daftar_aset.xml
│   │   ├── activity_riwayat.xml
│   │   ├── activity_detail_peminjaman.xml
│   │   ├── item_aset.xml
│   │   └── item_peminjaman.xml
│   ├── drawable/
│   │   ├── bg_splash.xml, bg_header.xml, bg_card.xml
│   │   ├── bg_button_primary.xml, bg_button_green.xml
│   │   ├── bg_edittext.xml
│   │   └── bg_status_dipinjam.xml, bg_status_dikembalikan.xml
│   └── values/
│       ├── strings.xml, colors.xml, themes.xml
```

---

## 🚀 Cara Membuka di Android Studio

1. Buka **Android Studio**
2. Pilih **File → Open**
3. Arahkan ke folder **AssetPeminjaman** yang diekstrak
4. Tunggu Gradle sync selesai (~1-3 menit, perlu koneksi internet)
5. Jalankan di **emulator** atau **device fisik** (Android 5.0+)

---

## 📦 Data Aset Default (8 item)

| No | Nama Aset | Kategori | Stok |
|----|-----------|----------|------|
| 1 | Laptop Dell Inspiron | Komputer | 5 |
| 2 | Proyektor Epson | Elektronik | 3 |
| 3 | Raspberry Pi 4 | Mikrokomputer | 10 |
| 4 | Arduino Uno | Mikrokontroler | 15 |
| 5 | Kabel HDMI | Aksesoris | 20 |
| 6 | Multimeter Digital | Alat Ukur | 8 |
| 7 | Toolset Elektronik | Alat Ukur | 6 |
| 8 | Switch Hub 8-Port | Jaringan | 4 |

---

*Dibuat untuk mata kuliah Pemrograman Mobile - Teknik Komputer*
