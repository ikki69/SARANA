# 👨‍💻 Developer Guide — SARANA

> Panduan lengkap untuk developer yang ingin berkontribusi atau mengembangkan SARANA.

---

## Daftar Isi

- [Tech Stack & Versi](#tech-stack--versi)
- [Setup Development Environment](#setup-development-environment)
- [Struktur Kode](#struktur-kode)
- [Coding Conventions](#coding-conventions)
- [Menambah Fitur Baru](#menambah-fitur-baru)
- [Menambah Aset Kategori Baru](#menambah-aset-kategori-baru)
- [Menambah Status Peminjaman Baru](#menambah-status-peminjaman-baru)
- [Testing](#testing)
- [Known Issues & TODO](#known-issues--todo)
- [Contribution Guide](#contribution-guide)
- [Credits](#credits)

---

## Tech Stack & Versi

| Teknologi | Versi | Keterangan |
|-----------|-------|-----------|
| **Java** | JDK 8 (1.8) | Bahasa utama |
| **Android SDK** | compileSdk 34, minSdk 23 | Target Android 14, min Android 6 |
| **Gradle** | 8.7.3 | Build system |
| **Firebase BOM** | 33.1.2 | Bill of Materials untuk Firebase |
| **Firebase Firestore** | (via BOM) | Cloud database |
| **Firebase Analytics** | (via BOM) | Analytics |
| **Material Components** | 1.9.0 | UI components |
| **AndroidX AppCompat** | 1.6.1 | Backward compatibility |
| **ConstraintLayout** | 2.1.4 | Layout engine |
| **DataStore Preferences** | 1.1.1 | Key-value storage (belum diimplementasikan penuh) |

---

## Setup Development Environment

### 1. Install Tools

```bash
# 1. Android Studio (Hedgehog+)
# Download: https://developer.android.com/studio

# 2. Git
# Download: https://git-scm.com

# 3. Clone project
git clone https://github.com/<username>/PeminjamanAset_Baru.git
cd PeminjamanAset_Baru
```

### 2. Buka di Android Studio

```
File → Open → pilih folder PeminjamanAset_Baru
```

Tunggu Gradle sync selesai (1–3 menit pertama kali).

### 3. Konfigurasi Firebase

Pastikan `app/google-services.json` ada. Untuk development, gunakan yang sudah ada (project `peminjamanaset-db18f`).

### 4. Run Aplikasi

- **Emulator:** Device Manager → Buat AVD Pixel 6 API 34
- **Device fisik:** Aktifkan USB Debugging → Hubungkan via USB
- **Run:** `Shift + F10` atau klik ▶

---

## Struktur Kode

### Package: `com.example.asetpeminjaman`

```
com.example.asetpeminjaman/
│
├── ACTIVITIES (Android Activity)
│   ├── SplashActivity.java         Entry point, cek sesi
│   ├── LoginActivity.java          Auth: Firestore + hardcoded admin
│   ├── MainActivity.java           Host: BottomNav + Fragment manager
│   ├── FormPeminjamanActivity.java Create & edit peminjaman
│   ├── DetailPeminjamanActivity.java Detail + user/admin actions
│   ├── DaftarAsetActivity.java     [Legacy - tidak digunakan aktif]
│   └── RiwayatActivity.java        [Legacy - tidak digunakan aktif]
│
├── FRAGMENTS (Android Fragment)
│   ├── HomeFragment.java           Dashboard user: stats + peminjaman aktif
│   ├── AdminDashboardFragment.java Dashboard admin: KPI + kapasitas
│   ├── InventoryFragment.java      Grid aset + CRUD (admin) / view (user)
│   ├── HistoryFragment.java        Riwayat: filter, search, sort
│   ├── ApproveFragment.java        Queue pending: approve/reject
│   └── ProfileFragment.java       Profil: tampilkan username + logout
│
├── MODELS (POJO / Data classes)
│   ├── DataAset.java               Model aset inventaris
│   ├── DataPeminjaman.java         Model transaksi peminjaman
│   └── ItemPinjam.java             Model item di dalam peminjaman
│
└── UTILITIES
    ├── DataManager.java            Singleton: akses data + business logic
    └── DateHelper.java             Kalkulasi tanggal, denda terlambat
```

---

## Coding Conventions

### Penamaan

```java
// Class: PascalCase
public class DataPeminjaman { }

// Method: camelCase
public void tambahPeminjaman() { }

// Variable: camelCase
private String namaAset;
private int stokDipinjam;

// Constant: UPPER_SNAKE_CASE
private static final int SPLASH_DURATION = 2500;

// Layout ID: snake_case
R.id.tvNamaPeminjam
R.id.btnAjukan
R.id.containerItems
```

### Resource Naming

```xml
<!-- Layout files -->
activity_*.xml       → untuk Activity
fragment_*.xml       → untuk Fragment
item_*.xml           → untuk RecyclerView/ListView item
dialog_*.xml         → untuk AlertDialog

<!-- View ID -->
tv*  → TextView
btn* → Button
et*  → EditText
iv*  → ImageView
pb*  → ProgressBar
rv*  → RecyclerView
lv*  → ListView
sp*  → Spinner
fab* → FloatingActionButton
```

### Model Class Requirements

Semua model yang di-mapping ke Firestore **wajib** memiliki:
1. **Empty constructor** (diperlukan oleh Firestore SDK)
2. **Getters dan Setters** untuk semua field
3. **`@Exclude`** untuk computed properties yang tidak disimpan ke Firestore

```java
public class DataAset {

    private int id;
    private String namaAset;

    // WAJIB: empty constructor untuk Firestore
    public DataAset() {}

    // Constructor dengan parameter
    public DataAset(int id, String namaAset, ...) { ... }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // @Exclude untuk computed property (tidak disimpan ke Firestore)
    @Exclude
    public int getStokTersedia() {
        return stokTotal - stokDipinjam;
    }
}
```

### Fragment Lifecycle

Selalu unsubscribe listener di `onDestroyView()`:

```java
@Override
public void onDestroyView() {
    super.onDestroyView();
    // WAJIB: mencegah memory leak
    if (dataManager != null && dataListener != null) {
        dataManager.removeListener(dataListener);
    }
}
```

Selalu cek `isAdded()` sebelum akses context di callback async:

```java
dataListener = () -> {
    if (isAdded()) {  // WAJIB
        updateUI();
    }
};
```

---

## Menambah Fitur Baru

### Contoh: Menambah Fragment Baru

**1. Buat file Java Fragment:**

```java
// app/src/main/java/com/example/asetpeminjaman/NamaFragment.java
package com.example.asetpeminjaman;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;

public class NamaFragment extends Fragment {

    private DataManager dataManager;
    private DataManager.DataChangeListener dataListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nama, container, false);

        dataManager = DataManager.getInstance();
        dataListener = this::refreshData;
        dataManager.addListener(dataListener);

        refreshData();
        return view;
    }

    private void refreshData() {
        if (!isAdded()) return;
        // Update UI
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dataManager.removeListener(dataListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }
}
```

**2. Buat layout XML:**

```xml
<!-- res/layout/fragment_nama.xml -->
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- Konten fragment -->

    </LinearLayout>
</ScrollView>
```

**3. Daftarkan di `MainActivity.java`:**

```java
// Di bottomNavigationView.setOnItemSelectedListener
} else if (itemId == R.id.nav_nama_baru) {
    fragment = new NamaFragment();
}
```

**4. Tambahkan menu item:**

```xml
<!-- res/menu/bottom_nav_menu.xml atau bottom_nav_menu_admin.xml -->
<item
    android:id="@+id/nav_nama_baru"
    android:icon="@drawable/ic_icon"
    android:title="Nama Menu" />
```

---

## Menambah Aset Kategori Baru

Kategori aset digunakan di beberapa tempat. Saat menambah kategori baru, update semua lokasi berikut:

### 1. `DataAset.java` — `tentukanHargaOtomatis()`

```java
private long tentukanHargaOtomatis(String kategori) {
    switch (kategori) {
        case "Komputer":      return 12000000;
        case "Elektronik":    return 3500000;
        // Tambahkan kasus baru:
        case "Drone":         return 8000000; // ← tambah di sini
        default:              return 1000000;
    }
}
```

### 2. `InventoryFragment.java` — `showAddAsetDialog()` & `getIconForCategory()`

```java
// Kategori di dialog tambah aset
String[] kategoriArr = {
    "Komputer", "Elektronik", "Mikrokomputer",
    "Mikrokontroler", "Aksesoris", "Alat Ukur", "Jaringan",
    "Drone"  // ← tambah di sini
};

// Icon untuk kategori baru
private String getIconForCategory(String category) {
    switch (category) {
        case "Drone": return "🚁"; // ← tambah di sini
        // ...
    }
}
```

### 3. `AdminDashboardFragment.java` — icon sudah dinamis

Dashboard admin menampilkan kapasitas per kategori secara dinamis dari data Firestore, tidak perlu update manual.

---

## Menambah Status Peminjaman Baru

Status disimpan sebagai `String` di Firestore. Saat menambah status baru:

### 1. `DataPeminjaman.java` — update `isAktif()`

```java
@Exclude
public boolean isAktif() {
    // Aktif = bukan status terminal
    return !"Dikembalikan".equals(status)
        && !"Ditolak".equals(status)
        && !"StatusBaru".equals(status); // ← tambah jika status terminal
}
```

### 2. `DataManager.java` — update `setStatusPeminjaman()`

```java
public void setStatusPeminjaman(int id, String statusBaru, long dendaRusakManual) {
    // Tambahkan logika transisi untuk status baru
    else if (statusBaru.equals("StatusBaru") && statusLama.equals("PreviousStatus")) {
        // Logika bisnis
        p.setStatus(statusBaru);
    }
}
```

### 3. `DetailPeminjamanActivity.java` — update `renderData()`

```java
} else if ("StatusBaru".equals(p.getStatus())) {
    // Update UI: warna banner, tombol, label
    statusBannerRoot.setBackgroundResource(R.drawable.bg_approve_card_normal);
    tvStatusBanner.setText("• Status Baru");
    btnKembalikan.setVisibility(View.VISIBLE);
}
```

### 4. `HistoryFragment.java` — update filter & styling

```java
// Di filterData()
} else if (filterAktif.equals("STATUS_BARU")) {
    matchesFilter = "StatusBaru".equals(p.getStatus());
}

// Di HistoryAdapter.onBindViewHolder() — update card color
} else if ("StatusBaru".equals(p.getStatus())) {
    holder.root.setBackgroundResource(R.drawable.bg_approve_card_teal);
    holder.tvStatus.setText("• Label Baru");
}
```

---

## Testing

### Unit Testing

```java
// Lokasi: app/src/test/java/com/example/asetpeminjaman/
// Jalankan: ./gradlew test

// Contoh test DateHelper
@Test
public void testGetDaysLate_withLateReturn() {
    int days = DateHelper.getDaysLate("01/05/2026", "05/05/2026");
    assertEquals(4, days);
}

@Test
public void testGetDaysLate_withOnTimeReturn() {
    int days = DateHelper.getDaysLate("10/05/2026", "08/05/2026");
    assertEquals(0, days); // tidak terlambat
}
```

### Instrumented Testing

```java
// Lokasi: app/src/androidTest/java/com/example/asetpeminjaman/
// Jalankan: ./gradlew connectedAndroidTest (butuh device/emulator)
```

### Manual Testing Checklist

Sebelum setiap commit, jalankan:

- [ ] Login admin berhasil
- [ ] Login user berhasil
- [ ] Inventaris tampil
- [ ] Form peminjaman submit berhasil
- [ ] Approval admin berhasil
- [ ] Pengajuan kembali berhasil
- [ ] Kalkulasi denda benar
- [ ] Konfirmasi pembayaran berhasil
- [ ] Logout berhasil

---

## Known Issues & TODO

### 🐛 Known Bugs

| Issue | Lokasi | Prioritas |
|-------|--------|-----------|
| ID peminjaman bisa duplikat (random int) | `FormPeminjamanActivity.java:278` | Medium |
| Password disimpan plain text di Firestore | `DataManager.java` + `LoginActivity.java` | High |
| Memory leak potensial jika listener tidak di-remove | Semua Fragment | Medium |
| `DaftarAsetActivity` & `RiwayatActivity` tidak digunakan | Legacy files | Low |

### 🚀 TODO / Future Features

| Fitur | Keterangan | Estimasi |
|-------|-----------|----------|
| **Firebase Authentication** | Ganti autentikasi manual dengan Firebase Auth | High effort |
| **Push Notification** | Notifikasi saat status peminjaman berubah | Medium effort |
| **Password Hashing** | Implementasi bcrypt/SHA-256 untuk password | High priority |
| **ProGuard/R8** | Aktifkan minification untuk release build | Low effort |
| **Pagination Firestore** | Untuk skalabilitas data besar | Medium effort |
| **Export PDF** | Export riwayat transaksi ke PDF | Medium effort |
| **QR Code Scanner** | Scan QR untuk identifikasi aset | High effort |
| **Dark Mode** | Dukungan tema gelap | Medium effort |
| **Offline Support** | Firestore offline persistence | Low effort |
| **Unit Tests** | Coverage minimal 60% | Ongoing |

### Mengatasi ID Duplikat (Fix Saran)

```java
// Saat ini (bermasalah)
new Random().nextInt(99999)

// Solusi 1: Gunakan timestamp
(int) (System.currentTimeMillis() % 99999)

// Solusi 2: Auto-increment dari Firestore
// Query max(id) + 1
```

### Mengaktifkan Firestore Offline Persistence

```java
// Di DataManager constructor
FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)  // default sudah true di Android
    .build();
db.setFirestoreSettings(settings);
```

---

## Contribution Guide

### Workflow Git

```bash
# 1. Fork repository
# 2. Clone fork
git clone https://github.com/<your-username>/PeminjamanAset_Baru.git

# 3. Buat branch fitur
git checkout -b feature/nama-fitur
# atau: git checkout -b fix/nama-bug

# 4. Develop & commit
git add .
git commit -m "feat: deskripsi singkat perubahan"

# 5. Push
git push origin feature/nama-fitur

# 6. Buat Pull Request ke main branch
```

### Konvensi Commit Message

```
feat:     Fitur baru
fix:      Perbaikan bug
docs:     Perubahan dokumentasi
style:    Format/style (tidak mengubah logika)
refactor: Refactoring kode
test:     Test baru atau perbaikan test
chore:    Maintenance (update dependency, config, dll)

Contoh:
feat: tambah fitur notifikasi push
fix: perbaiki kalkulasi denda terlambat
docs: update README installation guide
refactor: ekstrak kalkulasi denda ke DateHelper
```

### Code Review Checklist

Sebelum membuat PR, pastikan:

- [ ] Kode mengikuti konvensi naming
- [ ] Tidak ada `System.out.println()` tersisa (gunakan `Log.d()`)
- [ ] Listener di-unregister di `onDestroyView()`
- [ ] Semua callback async cek `isAdded()`
- [ ] Tidak ada hardcoded string (gunakan `strings.xml`)
- [ ] Model Firestore punya empty constructor dan getters/setters
- [ ] Manual testing checklist sudah dijalankan

---

## Credits

Kelompok 2
1. MUH ASYAM ASHARI ANSAR
2. Muhammad Rifqi Ramdani Abdullah
3. Ahmad Fakhri Syafa
4. St. Muslimah Nursalam

---

*Dokumen ini merupakan bagian dari dokumentasi resmi SARANA. Lihat [README.md](../README.md) untuk gambaran umum project.*
