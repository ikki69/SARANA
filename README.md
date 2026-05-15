<div align="center">

<h1>🏛️ SARANA</h1>
<h3><em>Sistem Peminjaman Aset — Jurusan Teknik Informatika dan Komputer</em></h3>

<p>
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
  <img src="https://img.shields.io/badge/Min%20SDK-23%20(Android%206.0)-brightgreen?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Version-1.0.0-blue?style=for-the-badge"/>
</p>

<p>
  <img src="https://img.shields.io/badge/Status-Active-success?style=flat-square"/>
  <img src="https://img.shields.io/badge/License-Academic-orange?style=flat-square"/>
  <img src="https://img.shields.io/badge/Maintained-Yes-green?style=flat-square"/>
</p>

> **SARANA** adalah aplikasi Android berbasis Firebase untuk manajemen peminjaman aset inventaris Jurusan JTIK. Memungkinkan mahasiswa mengajukan peminjaman aset, dan admin mengelola serta menyetujui seluruh proses secara real-time.

</div>

---

## 📋 Daftar Isi

- [✨ Fitur Utama](#-fitur-utama)
- [🛠️ Tech Stack](#️-tech-stack)
- [🏗️ Arsitektur](#️-arsitektur)
- [📁 Struktur Folder](#-struktur-folder)
- [⚙️ Instalasi](#️-instalasi)
- [🔧 Konfigurasi Environment](#-konfigurasi-environment)
- [🚀 Cara Penggunaan](#-cara-penggunaan)
- [👥 User Roles](#-user-roles)
- [🗄️ Database Schema](#️-database-schema)
- [🔌 API Overview](#-api-overview)
- [🚢 Deployment](#-deployment)
- [🤝 Kontribusi](#-kontribusi)
- [📄 Lisensi](#-lisensi)
- [👨‍💻 Credits](#-credits)

---

## ✨ Fitur Utama

| Fitur | Deskripsi | Role |
|-------|-----------|------|
| 🔐 **Autentikasi Multi-Level** | Login berbasis role (Admin & User) dengan verifikasi Firestore | Semua |
| 📦 **Manajemen Inventaris** | CRUD aset dengan kategori, stok real-time, dan status kondisi | Admin |
| 📝 **Form Peminjaman** | Pengajuan peminjaman multi-item (maks. 5 aset) dengan date/time picker | User |
| ✅ **Approval Workflow** | Admin menyetujui/menolak pengajuan pinjam & pengembalian | Admin |
| 📊 **Dashboard Real-Time** | Statistik KPI live: total aset, pending, terlambat, kapasitas per kategori | Semua |
| 🔔 **Notifikasi Denda** | Kalkulasi denda terlambat otomatis (Rp 50.000/hari) + denda kerusakan manual | Semua |
| 📜 **Riwayat & Filter** | Riwayat transaksi dengan filter multi-tab, pencarian, dan sorting | Semua |
| 🔑 **Lupa Password** | Reset password via verifikasi riwayat karakter password | User |
| ✏️ **Edit Peminjaman** | Modifikasi pengajuan selama masih dalam status "Menunggu Persetujuan" | User |
| 💰 **Konfirmasi Pembayaran** | Admin mengkonfirmasi pelunasan denda untuk menutup transaksi | Admin |

---

## 🛠️ Tech Stack

```
┌─────────────────────────────────────────────────────────────┐
│                         SARANA App                          │
├─────────────────────────────────────────────────────────────┤
│  Frontend (Android)           │  Backend (Firebase)         │
│  ─────────────────────        │  ────────────────────────   │
│  • Java (JDK 8)               │  • Cloud Firestore          │
│  • AndroidX AppCompat         │  • Firebase Analytics       │
│  • Material Components 1.9    │  • Firebase BOM 33.1.2      │
│  • ConstraintLayout 2.1.4     │                             │
│  • RecyclerView               │  Local Storage              │
│  • Fragment Navigation        │  ────────────────────────   │
│  • BottomNavigationView       │  • SharedPreferences        │
│  • DataStore Preferences      │    (Session Management)     │
│  • Gradle 8.7.3               │                             │
├─────────────────────────────────────────────────────────────┤
│  Tools                                                      │
│  • Android Studio             • Firebase Console            │
│  • Gradle Build System        • Google Services Plugin      │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Arsitektur

Aplikasi menggunakan pola arsitektur **Single Activity + Multi Fragment** dengan **Singleton DataManager** sebagai layer akses data.

```
┌─────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                      │
│                                                                 │
│   SplashActivity ──► LoginActivity ──► MainActivity             │
│                                             │                   │
│                          ┌──────────────────┤                   │
│                          │     Fragments    │                   │
│                    ┌─────┼─────┐    ┌───────┼──────────┐        │
│                  USER   │     │   ADMIN    │          │        │
│               HomeFragment  HistoryFragment  AdminDashboard    │
│               InventoryFragment              ApproveFragment   │
│               ProfileFragment                                  │
├─────────────────────────────────────────────────────────────────┤
│                         DATA LAYER                              │
│                                                                 │
│   DataManager (Singleton)                                       │
│   ├── syncAsetFromFirestore()    → Real-time listener           │
│   ├── syncPeminjamanFromFirestore() → Real-time listener        │
│   ├── syncUsersFromFirestore()   → One-time fetch               │
│   └── DataChangeListener[]       → Observer pattern            │
├─────────────────────────────────────────────────────────────────┤
│                      FIREBASE FIRESTORE                         │
│                                                                 │
│   collections:  aset /  peminjaman /  users                    │
└─────────────────────────────────────────────────────────────────┘
```

Untuk detail arsitektur lengkap, lihat [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 📁 Struktur Folder

```
PeminjamanAset_Baru/                             # ← yang tampil di GitHub
├── app/
│   ├── src/main/
│   │   ├── java/com/example/asetpeminjaman/
│   │   │   ├── 📱 Activities
│   │   │   │   ├── SplashActivity.java          # Splash screen + auto-login
│   │   │   │   ├── LoginActivity.java           # Authentication
│   │   │   │   ├── MainActivity.java            # Host + Bottom Navigation
│   │   │   │   ├── FormPeminjamanActivity.java  # Form pinjam / edit
│   │   │   │   ├── DetailPeminjamanActivity.java# Detail & action buttons
│   │   │   │   ├── DaftarAsetActivity.java      # Daftar aset (legacy)
│   │   │   │   └── RiwayatActivity.java         # Riwayat (legacy)
│   │   │   ├── 🧩 Fragments
│   │   │   │   ├── HomeFragment.java            # Dashboard user
│   │   │   │   ├── AdminDashboardFragment.java  # Dashboard admin (KPI)
│   │   │   │   ├── InventoryFragment.java       # Inventaris aset
│   │   │   │   ├── HistoryFragment.java         # Riwayat peminjaman
│   │   │   │   ├── ApproveFragment.java         # Approval admin
│   │   │   │   └── ProfileFragment.java         # Profil user
│   │   │   ├── 📊 Models
│   │   │   │   ├── DataAset.java                # Model aset
│   │   │   │   ├── DataPeminjaman.java          # Model peminjaman
│   │   │   │   └── ItemPinjam.java              # Model item dalam peminjaman
│   │   │   └── 🔧 Utilities
│   │   │       ├── DataManager.java             # Singleton data access layer
│   │   │       └── DateHelper.java              # Kalkulasi tanggal & denda
│   │   ├── res/
│   │   │   ├── layout/                          # 26 file XML layout
│   │   │   ├── values/                          # colors, strings, themes
│   │   │   ├── drawable/                        # Icon, background shapes
│   │   │   ├── menu/                            # Bottom navigation menus
│   │   │   ├── anim/                            # Transisi animasi
│   │   │   └── color/                           # State color selectors
│   │   └── AndroidManifest.xml
│   ├── build.gradle                             # App-level dependencies
│   └── google-services.json                     # Firebase config
├── build.gradle                                 # Root build config
├── settings.gradle                              # Project settings
├── gradle.properties                            # Gradle properties
├── .gitignore                                   # File exclusion rules
└── docs/                                        # 📚 Dokumentasi lengkap
    ├── USER_GUIDE.md
    ├── ROLE_GUIDE.md
    ├── INSTALLATION.md
    ├── API_DOCUMENTATION.md
    ├── ARCHITECTURE.md
    ├── DEPLOYMENT.md
    ├── TROUBLESHOOTING.md
    └── DEVELOPER_GUIDE.md
```

> ⚠️ **File yang TIDAK tampil di GitHub** (di-gitignore):
> - `build/` — output build otomatis (di-generate saat compile)
> - `.gradle/` — cache Gradle lokal
> - `.idea/` — konfigurasi Android Studio
> - `local.properties` — path SDK lokal (berbeda tiap mesin)
> - `*.apk` / `*.aab` — file output build
>
> Setelah clone, jalankan **Gradle Sync** di Android Studio untuk meng-generate folder-folder tersebut secara lokal.

---

## ⚙️ Instalasi

### Prasyarat

- **Android Studio** Hedgehog (2023.1.1) atau lebih baru
- **JDK 8** atau lebih baru
- **Android SDK** API Level 23+
- **Akun Firebase** (project sudah dikonfigurasi)
- **Git**

### Clone & Setup

```bash
# Clone repository
git clone https://github.com/ikki69/SARANA.git

# Masuk ke direktori project
cd PeminjamanAset_Baru

# Buka di Android Studio
# File → Open → pilih folder PeminjamanAset_Baru
```

### Jalankan Aplikasi

```bash
# Sync Gradle dependencies
# Klik "Sync Now" di Android Studio

# Build dan jalankan di emulator atau device fisik
# Run → Run 'app' (Shift+F10)
```

Lihat panduan lengkap di [docs/INSTALLATION.md](docs/INSTALLATION.md).

---

## 🔧 Konfigurasi Environment

File `google-services.json` sudah tersedia di `app/` dengan konfigurasi:

| Parameter | Nilai |
|-----------|-------|
| **Firebase Project ID** | `peminjamanaset-db18f` |
| **Firestore Region** | `asia-southeast1` |
| **Package Name** | `com.example.asetpeminjaman` |
| **Analytics** | ✅ Aktif |

> ⚠️ **Catatan:** File `local.properties` **tidak di-commit** ke repository karena berisi path SDK yang berbeda di tiap mesin. File ini akan dibuat otomatis oleh Android Studio saat pertama kali membuka project.
>
> Untuk deployment production, ganti `google-services.json` dengan konfigurasi Firebase project Anda sendiri.

---

## 🚀 Cara Penggunaan

### Login sebagai Admin
```
Username : admin
Password : 12345
```

### Login sebagai User (Mahasiswa)
Format username: `[PRODI] [KELAS] [ANGKATAN]`
```
Contoh:
Username : PTIK A 24
Password : Maba24ft
```

### Alur Peminjaman
```
User mengajukan → Admin menyetujui → User meminjam → 
User mengajukan kembali → Admin verifikasi kondisi → Selesai/Denda
```

---

## 👥 User Roles

| Role | Akses | Navigasi |
|------|-------|----------|
| **Admin** | Dashboard KPI, Approve/Reject, Kelola Inventaris, Riwayat semua user, Konfirmasi denda | 3-tab: Reports, Approve, Inventory |
| **User** | Dashboard pribadi, Form peminjaman, Riwayat sendiri, Ajukan kembali, Edit (pending) | 3-tab: Home, Inventory, History |

Untuk detail lengkap, lihat [docs/ROLE_GUIDE.md](docs/ROLE_GUIDE.md).

---

## 🗄️ Database Schema

### Firestore Collections

```
firestore/
├── users/{username}
│   ├── username      : String
│   ├── password      : String
│   ├── role          : String ("user")
│   └── passwordHistory : List<String>
│
├── aset/{id}
│   ├── id            : int
│   ├── namaAset      : String
│   ├── kategori      : String
│   ├── stokTotal     : int
│   ├── stokDipinjam  : int
│   ├── kondisi       : String
│   └── harga         : long
│
└── peminjaman/{firebaseId}
    ├── id                    : int
    ├── firebaseId            : String
    ├── accountUsername       : String
    ├── nama                  : String
    ├── nim                   : String
    ├── items                 : List<ItemPinjam>
    ├── tanggalPinjam         : String
    ├── jamPinjam             : String
    ├── tanggalRencanaKembali : String
    ├── jamRencanaKembali     : String
    ├── tanggalAktualKembali  : String
    ├── keperluan             : String
    ├── status                : String
    ├── priority              : String
    ├── dendaTerlambat        : long
    └── dendaRusak            : long
```

---

## 🔌 API Overview

SARANA tidak menggunakan REST API eksternal — seluruh operasi data dilakukan melalui **Firebase Firestore SDK**.

| Operasi | Method | Keterangan |
|---------|--------|------------|
| Sinkronisasi aset | `syncAsetFromFirestore()` | Real-time listener |
| Sinkronisasi peminjaman | `syncPeminjamanFromFirestore()` | Real-time listener |
| Tambah peminjaman | `tambahPeminjaman(DataPeminjaman)` | Auto-generate Firebase ID |
| Update status | `setStatusPeminjaman(id, status, denda)` | Includes stok recalculation |
| Ajukan pengembalian | `ajukanPengembalian(id, tanggal)` | Status → "Menunggu Pengembalian" |
| Konfirmasi pembayaran | `konfirmasiPembayaranDenda(id)` | Status → "Dikembalikan" |

Lihat [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md) untuk detail lengkap.

---

## 🚢 Deployment

### Build Release APK

```bash
# Di Android Studio
Build → Generate Signed Bundle/APK → APK

# Atau via command line
./gradlew assembleRelease
```

> 📌 **Catatan:** File `*.apk` dan `*.aab` (Android App Bundle) **tidak disertakan di repository** karena masuk daftar `.gitignore`. Untuk mendapatkan APK, clone repository lalu build secara lokal menggunakan perintah di atas.

Output APK (lokal): `app/release/app-release.apk`

Lihat panduan lengkap di [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md).

---

## 🤝 Kontribusi

1. **Fork** repository ini
2. **Buat branch** fitur baru: `git checkout -b feature/nama-fitur`
3. **Commit** perubahan: `git commit -m 'feat: tambah fitur X'`
4. **Push** ke branch: `git push origin feature/nama-fitur`
5. **Buat Pull Request**

### Konvensi Commit

```
feat:     Fitur baru
fix:      Perbaikan bug
docs:     Perubahan dokumentasi
style:    Perubahan format/style
refactor: Refactoring kode
test:     Penambahan/perubahan test
```

---

## 📄 Lisensi

Project ini dibuat untuk keperluan akademik — Mata Kuliah **Pemrograman Perangkat Mobile**, Semester 4, Jurusan JTIK.

---

## 👨‍💻 Credits

### Kelompok 2

| No | Nama |
|----|------|
| 1 | **MUH ASYAM ASHARI ANSAR** |
| 2 | **Muhammad Rifqi Ramdani Abdullah** |
| 3 | **Ahmad Fakhri Syafa** |
| 4 | **St. Muslimah Nursalam** |

**Institusi:** Jurusan Teknik Informatika dan Komputer (JTIK)
**Mata Kuliah:** Pemrograman Perangkat Mobile
**Semester:** 4 (Genap 2025/2026)

---

<div align="center">

**SARANA** — *Sistem Peminjaman Aset Jurusan JTIK*

Made with ❤️ by Kelompok 2 PTIK B 24

</div>
