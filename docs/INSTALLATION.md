# ⚙️ Installation Guide — SARANA

> Panduan lengkap instalasi dan setup aplikasi SARANA di lingkungan development.

---

## Prasyarat Sistem

| Software | Versi Minimum |
|----------|---------------|
| Android Studio | Hedgehog (2023.1.1)+ |
| JDK | 8 (bundled dengan Android Studio) |
| Android SDK | API 23 (Android 6.0) |
| Git | 2.x |
| RAM | 8 GB minimum |

---

## 1. Clone Repository

```bash
git clone https://github.com/<username>/PeminjamanAset_Baru.git
cd PeminjamanAset_Baru
```

Buka di Android Studio: **File → Open → pilih folder PeminjamanAset_Baru**

---

## 2. Konfigurasi Firebase

File `app/google-services.json` sudah tersedia dengan konfigurasi project `peminjamanaset-db18f`.

**Jika ingin menggunakan Firebase sendiri:**
1. Buat project di [console.firebase.google.com](https://console.firebase.google.com)
2. Tambahkan app Android dengan package name: `com.example.asetpeminjaman`
3. Unduh `google-services.json` → tempatkan di folder `app/`
4. Aktifkan **Firestore Database** → region `asia-southeast1`

### Firestore Rules (Development)

```javascript
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

## 3. Konfigurasi `app/build.gradle`

Pastikan dependencies berikut ada:

```groovy
dependencies {
    implementation platform("com.google.firebase:firebase-bom:33.1.2")
    implementation "com.google.firebase:firebase-analytics"
    implementation "com.google.firebase:firebase-firestore"
    implementation "androidx.datastore:datastore-preferences:1.1.1"
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
```

---

## 4. Build & Jalankan

```bash
# Sync Gradle (klik "Sync Now" di Android Studio)

# Build debug APK via command line
./gradlew assembleDebug

# Build dan install langsung ke device
./gradlew installDebug
```

Atau tekan **Run ▶** di Android Studio (`Shift + F10`).

---

## 5. Setup Emulator (Opsional)

1. Android Studio → **Device Manager → Create Virtual Device**
2. Pilih: **Pixel 6** → **API 34 (Android 14)**
3. RAM: 2048 MB, Storage: 6 GB
4. Klik **Finish**

---

## 6. Device Fisik

1. Settings → About Phone → Ketuk **Build Number** 7x
2. Developer Options → **USB Debugging** → ON
3. Hubungkan via USB → Pilih **File Transfer**
4. Tekan **Allow** di dialog USB Debugging

---

## 7. Verifikasi Instalasi

- [ ] Splash screen muncul ±2,5 detik
- [ ] Login `admin` / `12345` berhasil
- [ ] Dashboard admin dan KPI tampil
- [ ] Login user `PTIK A 24` / `Maba24ft` berhasil
- [ ] Inventaris aset muncul dari Firestore
- [ ] Form peminjaman dapat disubmit
- [ ] Data muncul di Firebase Console → Firestore

---

## Troubleshooting Cepat

| Error | Solusi |
|-------|--------|
| `google-services.json not found` | Pastikan file ada di `app/` bukan root |
| Gradle sync gagal | **File → Invalidate Caches → Restart** |
| Firebase tidak konek | Cek koneksi internet, validasi `google-services.json` |
| `minSdkVersion` error | Gunakan emulator/device API 23+ |

Lihat [TROUBLESHOOTING.md](TROUBLESHOOTING.md) untuk panduan lengkap.

---

## Credits

Kelompok 2
1. MUH ASYAM ASHARI ANSAR
2. Muhammad Rifqi Ramdani Abdullah
3. Ahmad Fakhri Syafa
4. St. Muslimah Nursalam
