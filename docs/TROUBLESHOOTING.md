# 🔧 Troubleshooting Guide — SARANA

> Panduan mengatasi masalah umum yang ditemui saat menggunakan atau mengembangkan SARANA.

---

## Daftar Isi

- [Masalah Instalasi & Build](#masalah-instalasi--build)
- [Masalah Login & Autentikasi](#masalah-login--autentikasi)
- [Masalah Firebase & Koneksi](#masalah-firebase--koneksi)
- [Masalah Data & Sinkronisasi](#masalah-data--sinkronisasi)
- [Masalah Fitur Peminjaman](#masalah-fitur-peminjaman)
- [Masalah UI & Tampilan](#masalah-ui--tampilan)
- [Error Umum & Solusi](#error-umum--solusi)
- [Debug Tips](#debug-tips)
- [Credits](#credits)

---

## Masalah Instalasi & Build

### ❌ Gradle Sync Failed

**Gejala:** Android Studio menampilkan "Gradle sync failed" saat membuka project.

**Solusi:**
```bash
# 1. Hapus cache Gradle
./gradlew clean

# 2. Di Android Studio
File → Invalidate Caches → Invalidate and Restart

# 3. Jika masih gagal, hapus folder .gradle
# Windows: C:\Users\<Username>\.gradle\caches
# Hapus folder "caches" lalu sync ulang
```

---

### ❌ `google-services.json` Not Found

**Gejala:**
```
FAILURE: Build failed with an exception.
File google-services.json is missing.
```

**Solusi:**
- Pastikan file `google-services.json` ada di **`app/`** (bukan di root project)
- Struktur yang benar:
  ```
  PeminjamanAset_Baru/
  └── app/
      └── google-services.json  ✅
  ```

---

### ❌ `minSdkVersion` Mismatch

**Gejala:**
```
INSTALL_FAILED_OLDER_SDK
```

**Solusi:** Gunakan emulator atau device Android versi **6.0 (API 23) atau lebih baru**.

---

### ❌ Build Gagal: `Duplicate class`

**Gejala:**
```
Duplicate class kotlin.collections.jdk8.CollectionsJDK8Kt
```

**Solusi:** Tambahkan di `gradle.properties`:
```properties
android.useAndroidX=true
android.enableJetifier=true
```

---

### ❌ `Cannot resolve symbol 'R'`

**Gejala:** Seluruh referensi `R.id.*`, `R.layout.*` bergaris merah.

**Solusi:**
```
Build → Clean Project
Build → Rebuild Project
```
Atau: **File → Invalidate Caches → Restart**

---

## Masalah Login & Autentikasi

### ❌ Login Admin Gagal

**Gejala:** Memasukkan `admin` / `12345` tapi login gagal.

**Kemungkinan Penyebab & Solusi:**

| Penyebab | Solusi |
|----------|--------|
| Spasi ekstra di username/password | Pastikan tidak ada spasi sebelum/sesudah |
| Caps Lock aktif | Password bersifat case-sensitive |
| Credential sudah diubah di kode | Cek `LoginActivity.java` baris ~143 |

```java
// LoginActivity.java — credential admin hardcoded
if (user.equalsIgnoreCase("admin") && Objects.equals(pass, "12345")) {
```

---

### ❌ Login User Gagal — "Username atau Password salah"

**Kemungkinan Penyebab:**

1. **Format username salah** — harus persis: `PTIK A 24` (dengan spasi, kapital)
2. **Password salah** — default: `Maba24ft` (case-sensitive, huruf besar M dan f kecil)
3. **User belum ada di Firestore** — cek Firebase Console → `users` collection
4. **Koneksi internet bermasalah** — muncul pesan "Koneksi database bermasalah"

**Solusi:**
```
1. Cek koneksi internet device
2. Buka Firebase Console → Firestore → users collection
3. Verifikasi dokumen dengan username yang dicoba
4. Pastikan field "username" dan "password" sesuai
```

---

### ❌ "Data verifikasi belum siap" saat Lupa Password

**Penyebab:** Field `passwordHistory` belum ada di dokumen user di Firestore.

**Solusi:**
- Sistem akan otomatis mengisi `passwordHistory` saat `DataManager` diinisialisasi
- Tunggu beberapa detik, lalu coba lagi
- Atau cek di Firebase Console → `users/{username}` → tambahkan field `passwordHistory` secara manual:
  ```json
  "passwordHistory": ["2", "4", "f", "t"]
  ```
  *(3 karakter terakhir password: untuk "Maba24ft" → "2","4","f","t" — namun verifikasi gunakan gabungan 3 char terakhir = "4ft")*

  > **Koreksi:** Kunci = `history[0] + history[1] + history[2]` = `"2" + "4" + "f"` = `"24f"` atau sesuai implementasi di LoginActivity baris ~89.

---

### ❌ Aplikasi Langsung Masuk (Bypass Login)

**Penyebab:** Sesi sebelumnya masih tersimpan di SharedPreferences.

**Solusi:** Logout terlebih dahulu, atau hapus data aplikasi:
```
Settings → Apps → SARANA → Storage → Clear Data
```

---

## Masalah Firebase & Koneksi

### ❌ Data Tidak Muncul / Kosong

**Gejala:** Inventaris, riwayat, atau dashboard menampilkan data kosong.

**Langkah Diagnosis:**

1. **Cek koneksi internet** — Firestore butuh internet untuk sync awal
2. **Cek Firebase Console:**
   - Buka [console.firebase.google.com](https://console.firebase.google.com)
   - Project `peminjamanaset-db18f` → Firestore Database
   - Verifikasi collections `aset`, `peminjaman`, `users` ada
3. **Cek Firestore Rules** — pastikan rules mengizinkan read:
   ```javascript
   allow read, write: if true; // untuk development
   ```
4. **Restart aplikasi** — kadang listener Firestore perlu reinisialisasi

---

### ❌ `FirebaseFirestoreException: PERMISSION_DENIED`

**Penyebab:** Firestore Security Rules memblokir akses.

**Solusi (Development):**
```javascript
// Di Firebase Console → Firestore → Rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

---

### ❌ Data Lama Masih Muncul Setelah Diubah

**Penyebab:** Cache lokal `DataManager` belum diupdate.

**Solusi:** Firestore listener seharusnya trigger otomatis. Jika tidak:
- Tutup dan buka kembali fragment (navigasi keluar lalu masuk lagi)
- Restart aplikasi

---

### ❌ `Failed to get document` / Network Error

**Solusi:**
```
1. Pastikan Wi-Fi / data seluler aktif
2. Pastikan tidak ada VPN yang memblokir Firebase
3. Coba dengan jaringan berbeda
4. Cek status Firebase: status.firebase.google.com
```

---

## Masalah Data & Sinkronisasi

### ❌ Stok Tidak Berkurang Setelah Disetujui

**Penyebab:** `setStatusPeminjaman` mungkin tidak terpanggil dengan benar.

**Diagnosis:**
```java
// Cek di DataManager.setStatusPeminjaman()
// Pastikan transisi status benar:
// "Menunggu Persetujuan" → "Dipinjam" (bukan status lain)
if (statusBaru.equals("Dipinjam") && statusLama.equals("Menunggu Persetujuan")) {
    // Stok dikurangi di sini
}
```

---

### ❌ Denda Tidak Terhitung

**Penyebab:** Tanggal di Firestore tidak sesuai format `dd/MM/yyyy`.

**Diagnosis:**
```java
// DateHelper.getDaysLate() menggunakan format dd/MM/yyyy
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
```

**Solusi:** Pastikan input tanggal menggunakan DatePicker (bukan ketik manual).

---

### ❌ ID Peminjaman Duplikat

**Penyebab:** ID numerik digenerate dengan `new Random().nextInt(99999)`.

**Catatan:** Ini adalah keterbatasan implementasi saat ini. Kemungkinan duplikat kecil tapi ada. Untuk fix, gunakan Firestore auto-ID:

```java
// TODO: Ganti dengan auto-increment atau UUID
// Saat ini: new Random().nextInt(99999)
```

---

## Masalah Fitur Peminjaman

### ❌ Tidak Bisa Submit Form — "Stok tidak mencukupi"

**Diagnosis:**
1. Buka tab Inventaris
2. Lihat stok tersedia untuk aset yang ingin dipinjam
3. Jika badge menunjukkan "Habis" → tunggu ada yang mengembalikan

---

### ❌ Tombol "KEMBALIKAN ASET" Tidak Muncul

**Penyebab:** Status peminjaman bukan "Dipinjam".

**Tombol muncul sesuai status:**

| Status | Tombol yang Muncul |
|--------|-------------------|
| Menunggu Persetujuan | MODIFIKASI |
| Dipinjam | KEMBALIKAN ASET |
| Menunggu Pengembalian | (tidak ada) |
| Menunggu Pembayaran | KONFIRMASI PEMBAYARAN (admin) |
| Dikembalikan | (tidak ada) |

---

### ❌ Tidak Bisa Edit Peminjaman

**Penyebab:** Edit hanya diizinkan saat status **"Menunggu Persetujuan"**.

**Solusi:** Jika sudah "Dipinjam" atau status lain, peminjaman tidak bisa diedit.

---

## Masalah UI & Tampilan

### ❌ Bottom Navigation Tidak Sesuai Role

**Gejala:** Admin melihat menu user, atau sebaliknya.

**Solusi:**
1. Logout: **Profil → Keluar**
2. Hapus data aplikasi: **Settings → Apps → SARANA → Clear Data**
3. Login kembali dengan akun yang benar

**Root cause check:**
```java
// MainActivity.java
SharedPreferences pref = getSharedPreferences("USER_DATA", MODE_PRIVATE);
userRole = pref.getString("role", "user");
// Cek apakah "role" tersimpan dengan benar saat login
```

---

### ❌ Splash Screen Terlalu Lama / Langsung Skip

**Konfigurasi:**
```java
// SplashActivity.java
private static final int SPLASH_DURATION = 2500; // 2.5 detik
```

Ubah nilai `SPLASH_DURATION` sesuai kebutuhan.

---

### ❌ Animasi Navigasi Tidak Muncul

**Penyebab:** File animasi `R.anim.fade_in` / `fade_out` / `nav_pop_up` mungkin tidak ada.

**Solusi:** Pastikan folder `res/anim/` berisi file:
- `fade_in.xml`
- `fade_out.xml`
- `nav_pop_up.xml`

---

## Error Umum & Solusi

| Error | Lokasi | Solusi |
|-------|--------|--------|
| `NullPointerException` pada `getActivity()` | Fragment lifecycle | Selalu cek `if (isAdded())` sebelum akses activity |
| `IllegalStateException: Fragment not attached` | Async callback di Fragment | Gunakan `if (isAdded() && getContext() != null)` |
| `NetworkOnMainThreadException` | Firestore sudah handle ini | Tidak perlu khawatir, Firestore ops berjalan di background thread |
| `ClassCastException` di Firestore mapping | Model mismatch | Pastikan field names di Java model = field names di Firestore |

---

## Debug Tips

### Aktifkan Logging Firestore

```java
// Di Application class atau MainActivity
FirebaseFirestore.setLoggingEnabled(true);
```

### Cek Logcat

Filter di Android Studio Logcat:
- `tag:HistoryDebug` — debug history fragment
- `tag:Firebase` — log Firebase
- `tag:Firestore` — log Firestore spesifik

```java
// Contoh log yang ada di kode
android.util.Log.d("HistoryDebug", "Total data: " + listTampil.size());
```

### Verifikasi Data di Firebase Console

1. Buka [console.firebase.google.com](https://console.firebase.google.com)
2. Project `peminjamanaset-db18f` → **Firestore Database**
3. Inspect collections `aset`, `peminjaman`, `users`

### Reset Data Firestore (Development)

Jika perlu reset data aset ke kondisi awal:
1. Hapus semua dokumen di collection `aset`
2. Restart aplikasi → `DataManager` akan otomatis mengisi data awal

---

## Credits

Kelompok 2
1. MUH ASYAM ASHARI ANSAR
2. Muhammad Rifqi Ramdani Abdullah
3. Ahmad Fakhri Syafa
4. St. Muslimah Nursalam

---

*Dokumen ini merupakan bagian dari dokumentasi resmi SARANA. Lihat [README.md](../README.md) untuk gambaran umum project.*
