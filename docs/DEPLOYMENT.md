# 🚢 Deployment Guide — SARANA

> Panduan build, signing, dan distribusi APK aplikasi SARANA.

---

## Daftar Isi

- [Environment Overview](#environment-overview)
- [1. Persiapan Sebelum Build](#1-persiapan-sebelum-build)
- [2. Build Debug APK](#2-build-debug-apk)
- [3. Build Release APK](#3-build-release-apk)
- [4. Signing APK](#4-signing-apk)
- [5. Firestore Security Rules (Production)](#5-firestore-security-rules-production)
- [6. Distribusi APK](#6-distribusi-apk)
- [7. Firebase Console Setup](#7-firebase-console-setup)
- [Checklist Pre-Deployment](#checklist-pre-deployment)
- [Credits](#credits)

---

## Environment Overview

| Environment | Tujuan | Firebase Project |
|-------------|--------|-----------------|
| **Development** | Testing lokal, debug | `peminjamanaset-db18f` |
| **Production** | Distribusi ke pengguna | Sama / buat baru |

> Saat ini SARANA menggunakan **satu Firebase project** untuk semua environment.

---

## 1. Persiapan Sebelum Build

### Verifikasi Konfigurasi

```gradle
// app/build.gradle
android {
    defaultConfig {
        applicationId "com.example.asetpeminjaman"
        minSdk 23
        targetSdk 34
        versionCode 1       // Increment setiap release baru
        versionName "1.0"   // Versi yang tampil ke pengguna
    }

    buildTypes {
        release {
            minifyEnabled false   // Set true untuk ProGuard
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'),
                         'proguard-rules.pro'
        }
    }
}
```

### Verifikasi `google-services.json`

Pastikan file ada di `app/google-services.json` dan package name sesuai:
```
com.example.asetpeminjaman
```

### Clean Project

```bash
./gradlew clean
```

---

## 2. Build Debug APK

Debug APK digunakan untuk testing internal. Ditandatangani secara otomatis dengan debug keystore.

```bash
# Via command line
./gradlew assembleDebug

# Output:
# app/build/outputs/apk/debug/app-debug.apk
```

**Via Android Studio:**
- **Build → Build Bundle(s) / APK(s) → Build APK(s)**

---

## 3. Build Release APK

### Via Android Studio (Direkomendasikan)

1. **Build → Generate Signed Bundle / APK**
2. Pilih **APK** → Klik **Next**
3. Pilih atau buat **Keystore** (lihat bagian Signing)
4. Isi **Key alias**, **Key password**, **Store password**
5. Pilih **release** build variant
6. Klik **Finish**

Output: `app/release/app-release.apk`

### Via Command Line

```bash
# Dengan keystore sudah dikonfigurasi di build.gradle
./gradlew assembleRelease

# Output:
# app/build/outputs/apk/release/app-release.apk
# atau app/release/app-release.apk
```

---

## 4. Signing APK

### Buat Keystore Baru

```bash
keytool -genkey -v \
  -keystore sarana-release-key.jks \
  -alias sarana-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Isi informasi yang diminta:
- **First and Last Name:** Kelompok 2 JTIK
- **Organizational Unit:** JTIK
- **Organization:** Universitas ...
- **City / State / Country:** Makassar / Sulawesi Selatan / ID

> ⚠️ **PENTING:** Simpan file `.jks` dan password dengan aman. Tidak bisa recovery jika hilang!

### Konfigurasi Signing di `app/build.gradle`

```gradle
android {
    signingConfigs {
        release {
            storeFile file('sarana-release-key.jks')
            storePassword 'your_store_password'
            keyAlias 'sarana-key'
            keyPassword 'your_key_password'
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'),
                         'proguard-rules.pro'
        }
    }
}
```

> ⚠️ **Jangan commit password ke Git!** Gunakan `local.properties` atau environment variables.

### Menggunakan `local.properties` untuk Password

```properties
# local.properties (tidak di-commit)
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=sarana-key
KEY_PASSWORD=your_key_password
```

```gradle
// app/build.gradle
def localProps = new Properties()
localProps.load(new FileInputStream(rootProject.file("local.properties")))

signingConfigs {
    release {
        storeFile file('sarana-release-key.jks')
        storePassword localProps["KEYSTORE_PASSWORD"]
        keyAlias localProps["KEY_ALIAS"]
        keyPassword localProps["KEY_PASSWORD"]
    }
}
```

---

## 5. Firestore Security Rules (Production)

Untuk production, **wajib** menggunakan rules yang lebih ketat:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users: hanya bisa baca/tulis dokumen milik sendiri
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Aset: semua authenticated user bisa baca, hanya admin bisa tulis
    match /aset/{asetId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
                   get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }

    // Peminjaman: user hanya bisa akses milik sendiri, admin akses semua
    match /peminjaman/{peminjamanId} {
      allow read: if request.auth != null && (
        resource.data.accountUsername == request.auth.uid ||
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin'
      );
      allow write: if request.auth != null;
    }
  }
}
```

> 📝 **Catatan:** Rules di atas mengasumsikan implementasi Firebase Authentication. Saat ini SARANA menggunakan autentikasi manual (Firestore + SharedPreferences), bukan Firebase Auth. Untuk production enterprise, migrasi ke Firebase Auth direkomendasikan.

### Rules Sementara (Saat Ini Digunakan)

```javascript
// Test mode — open access
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

## 6. Distribusi APK

### Distribusi Manual (Sideload)

1. Transfer `app-release.apk` ke device via USB, email, atau cloud storage
2. Di device: **Settings → Security → Install Unknown Apps → Allow**
3. Buka file APK → **Install**

### Distribusi via Firebase App Distribution

1. Buka Firebase Console → **App Distribution**
2. Upload APK
3. Tambahkan email tester
4. Kirim undangan

```bash
# Via Firebase CLI
npm install -g firebase-tools
firebase login
firebase appdistribution:distribute app-release.apk \
  --app 1:213449864979:android:2b8ea43fc21fab29ef4ce6 \
  --groups "testers" \
  --release-notes "Release v1.0.0"
```

### Distribusi via Google Play (Future)

1. Buat akun **Google Play Developer** (biaya $25 sekali)
2. Buat **App Bundle** (`.aab`):
   ```bash
   ./gradlew bundleRelease
   # Output: app/build/outputs/bundle/release/app-release.aab
   ```
3. Upload ke Play Console → Production / Internal Testing

---

## 7. Firebase Console Setup

### Monitoring

1. **Firebase Console → Analytics** — pantau event penggunaan
2. **Firebase Console → Firestore → Usage** — pantau reads/writes/deletes
3. **Firebase Console → Crashlytics** *(TODO: belum diimplementasikan)*

### Backup Data

```bash
# Export data Firestore via Firebase CLI
firebase firestore:export gs://peminjamanaset-db18f.firebasestorage.app/backups/$(date +%Y%m%d)

# Import (restore)
firebase firestore:import gs://peminjamanaset-db18f.firebasestorage.app/backups/20260515
```

---

## Checklist Pre-Deployment

### Build

- [ ] `versionCode` sudah diincrement
- [ ] `versionName` sudah diupdate
- [ ] `minifyEnabled` dikonfigurasi sesuai kebutuhan
- [ ] Build release berhasil tanpa error
- [ ] APK sudah di-sign dengan release keystore

### Testing

- [ ] Login admin berfungsi
- [ ] Login user berfungsi
- [ ] Form peminjaman dapat disubmit
- [ ] Approval flow berfungsi end-to-end
- [ ] Kalkulasi denda benar
- [ ] Logout berfungsi
- [ ] Tidak ada crash di logcat

### Firebase

- [ ] Firestore rules sudah dikonfigurasi
- [ ] Data aset tersedia di Firestore
- [ ] Data users tersedia di Firestore
- [ ] Analytics aktif

### Distribusi

- [ ] APK dapat diinstall di device target (API 23+)
- [ ] APK sudah didistribusikan ke tester

---

## Credits

Kelompok 2
1. MUH ASYAM ASHARI ANSAR
2. Muhammad Rifqi Ramdani Abdullah
3. Ahmad Fakhri Syafa
4. St. Muslimah Nursalam

---

*Dokumen ini merupakan bagian dari dokumentasi resmi SARANA. Lihat [README.md](../README.md) untuk gambaran umum project.*
