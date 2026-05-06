# 📱 Aplikasi SARANA

> Aplikasi Peminjaman Aset Jurusan Teknik Informatika dan Komputer (JTIK) Universitas Negeri Makassar berbasis Android untuk memanajemen inventaris barang dan memudahkan proses peminjaman serta pengembalian aset.

---

## 🚀 Fitur Utama

### 👤 Manajemen Pengguna
* **Splash Screen**: Tampilan awal aplikasi saat dimuat.
* **Login**: Autentikasi pengguna untuk mengakses fitur aplikasi secara aman.
* **User Profile**: Pengelolaan informasi profil pengguna beserta fitur *logout*.

### 🎓 Fitur Peminjam (User)
* **Dashboard (Home)**: Ringkasan informasi dan akses cepat ke fitur utama aplikasi.
* **Daftar Aset (Inventory)**: Katalog aset yang tersedia untuk dipinjam beserta detail informasi barang.
* **Form Peminjaman**: Formulir digital untuk mengajukan peminjaman aset dengan mudah.
* **Riwayat Peminjaman**: Pelacakan status pengajuan peminjaman yang sedang berlangsung maupun yang sudah selesai (Status: *Dipinjam* / *Dikembalikan*).

### 🔑 Fitur Admin
* **Admin Dashboard**: Panel pemantauan khusus untuk melihat aktivitas peminjaman secara keseluruhan.
* **Manajemen Peminjaman**: Fitur untuk menyetujui, menolak, atau mengelola status peminjaman aset dari pengguna.

---

## 🛠️ Teknologi yang Digunakan

* **Bahasa Pemrograman**: Java (Android SDK)
* **Backend & Database**: Firebase Services (Google Services Integration)
* **Arsitektur**: *Fragment-based UI* untuk navigasi yang mulus menggunakan *Bottom Navigation*.
* **UI/UX**: Custom XML Drawables untuk desain tombol, *card*, dan status indikator yang lebih modern.

---

## ⚙️ Cara Instalasi

Ikuti langkah-langkah berikut untuk menjalankan aplikasi di *environment* lokal Anda:

1. **Clone repositori ini:**
   ```bash
   git clone https://github.com/ikki69/SARANA.git
   ```
2. **Buka Proyek**: Buka direktori proyek menggunakan **Android Studio**.
3. **Sinkronisasi**: Pastikan konfigurasi SDK dan Gradle Anda sudah sesuai dan tersinkronisasi (*Sync Project with Gradle Files*).
4. **Integrasi Firebase (Opsional untuk pengembangan mandiri)**:
   * Buat project Firebase Anda sendiri di [Firebase Console](https://firebase.google.com).
   * Unduh file konfigurasi `google-services.json` Anda.
   * Ganti file `google-services.json` yang ada di dalam direktori `app/` dengan file milik Anda.
5. **Jalankan Aplikasi**: *Build* dan *Run* aplikasi menggunakan Emulator atau langsung di perangkat Android fisik Anda.
6. **Login**: Jika menggunakan database yang telah disediakan, jika ingin login sebagai user, silahkan menginput pada kolom *username*: rifqi dan *password*:123456
7. **Admin**: Jika ingin login sebagai admin, silahkan menginput pada kolom *username*: admin dan *password*:12345

---

## 👨‍💻 Kontributor

Proyek ini dikembangkan oleh **Kelompok 2**:

* **Muh Asyam Ashari Ansar**
* **Muhammad Rifqi Ramdani Abdullah**
* **Ahmad Fakhri Syafa**
* **St. Muslimah Nursalam**

---
© 2026 JTIK UNM - Aplikasi SARANA
