# 🏗️ Architecture Documentation — SARANA

> Dokumentasi arsitektur teknis aplikasi SARANA secara lengkap.

---

## Daftar Isi

- [Gambaran Umum Arsitektur](#gambaran-umum-arsitektur)
- [Layer Architecture](#layer-architecture)
- [Navigation Architecture](#navigation-architecture)
- [Data Flow Architecture](#data-flow-architecture)
- [Authentication Flow](#authentication-flow)
- [State Management](#state-management)
- [Database Architecture](#database-architecture)
- [Module Relationships](#module-relationships)
- [Design Patterns](#design-patterns)
- [UI Architecture](#ui-architecture)
- [Credits](#credits)

---

## Gambaran Umum Arsitektur

SARANA adalah aplikasi **Native Android** yang menggunakan pola arsitektur **Single Activity + Multi Fragment** dengan backend **Firebase Firestore** sebagai cloud database real-time.

```
┌──────────────────────────────────────────────────────────────┐
│                    SARANA APPLICATION                        │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │               PRESENTATION LAYER                       │  │
│  │  Activities: Splash, Login, Main, FormPeminjaman,      │  │
│  │              DetailPeminjaman, DaftarAset, Riwayat     │  │
│  │  Fragments:  Home, AdminDashboard, Inventory,          │  │
│  │              History, Approve, Profile                 │  │
│  └────────────────────────┬───────────────────────────────┘  │
│                           │ DataChangeListener (Observer)    │
│  ┌────────────────────────▼───────────────────────────────┐  │
│  │                  DATA LAYER                            │  │
│  │           DataManager (Singleton)                      │  │
│  │  - In-memory cache: listAset, listPeminjaman           │  │
│  │  - Business logic: stok, denda, status transitions    │  │
│  │  - Observer notification                               │  │
│  └────────────────────────┬───────────────────────────────┘  │
│                           │ Firestore SDK                    │
│  ┌────────────────────────▼───────────────────────────────┐  │
│  │              FIREBASE CLOUD LAYER                       │  │
│  │  Firestore:  /aset, /peminjaman, /users               │  │
│  │  Analytics:  Event tracking                            │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────┐                            │
│  │       LOCAL STORAGE          │                            │
│  │  SharedPreferences: session  │                            │
│  │  (username, role)            │                            │
│  └──────────────────────────────┘                            │
└──────────────────────────────────────────────────────────────┘
```

---

## Layer Architecture

### 1. Presentation Layer (UI)

Seluruh UI dibangun menggunakan **AndroidX** dan **Material Components**. Terdiri dari:

#### Activities

| Activity | Tanggung Jawab |
|----------|---------------|
| `SplashActivity` | Entry point, cek sesi login, redirect ke Login/Main |
| `LoginActivity` | Autentikasi user & admin, forgot password flow |
| `MainActivity` | Host activity — mengelola fragment via BottomNavigationView |
| `FormPeminjamanActivity` | Form create & edit peminjaman |
| `DetailPeminjamanActivity` | View detail + action buttons (return, modify, pay) |
| `DaftarAsetActivity` | *(Legacy)* Daftar aset lama |
| `RiwayatActivity` | *(Legacy)* Riwayat lama |

#### Fragments

| Fragment | Role | Navigasi |
|----------|------|---------|
| `HomeFragment` | User dashboard | User: tab 1 |
| `AdminDashboardFragment` | Admin KPI dashboard | Admin: tab 1 |
| `InventoryFragment` | Inventaris aset (shared) | User: tab 2 / Admin: tab 3 |
| `HistoryFragment` | Riwayat peminjaman (shared) | User: tab 3 / Admin: tab 3 |
| `ApproveFragment` | Approval queue | Admin: tab 2 |
| `ProfileFragment` | Profil & logout | User: (opsional) |

### 2. Data Layer

Dikelola sepenuhnya oleh `DataManager.java`:

- **Singleton** — satu instance untuk seluruh siklus aplikasi
- **In-memory cache** — `listAset` dan `listPeminjaman` selalu sinkron
- **Real-time listeners** — `addSnapshotListener` untuk aset & peminjaman
- **Business logic** — kalkulasi stok, denda, transisi status
- **Observer pattern** — `DataChangeListener` interface

### 3. Firebase Cloud Layer

- **Cloud Firestore** — primary database, real-time sync
- **Firebase Analytics** — event tracking (bawaan)
- **Google Services** — konfigurasi via `google-services.json`

### 4. Local Storage

- **SharedPreferences** (`USER_DATA`) — menyimpan sesi login:
  - `username`: string username yang sedang login
  - `role`: `"admin"` atau `"user"`

---

## Navigation Architecture

### User Navigation

```
SplashActivity
    │
    ├── (sudah login) ──► MainActivity
    │                         │
    │                    BottomNavigation
    │                    ┌────┼────────┐
    │                    │    │        │
    │               Home  Inventory  History
    │                    
    └── (belum login) ──► LoginActivity ──► MainActivity
```

### Admin Navigation

```
LoginActivity (admin)
    │
    └──► MainActivity
              │
         BottomNavigation (Admin Menu)
         ┌────┼──────────┐
         │    │          │
      Reports  Approve  Inventory
    (Dashboard)
```

### Fragment Transitions

```java
// Animasi: fade_in / fade_out (R.anim.fade_in, R.anim.fade_out)
transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
transaction.replace(R.id.fragment_container, fragment);
transaction.commit();
```

### Tab Switching dengan Filter

```java
// Dari fragment ke fragment dengan data/filter
((MainActivity) getActivity()).switchToTab(R.id.nav_history, "TERLAMBAT");

// Fragment menerima filter melalui Bundle
if (getArguments() != null) {
    String filter = getArguments().getString("FILTER_TYPE");
    filterAktif = filter; // "SEMUA", "DIPINJAM", "TERLAMBAT", dll
}
```

---

## Data Flow Architecture

### Alur Create Peminjaman

```
User → FormPeminjamanActivity
           │
           ├── Validasi form (client-side)
           │   ├── Field tidak kosong
           │   ├── Tanggal kembali > tanggal pinjam
           │   └── Stok tersedia mencukupi
           │
           └── DataManager.tambahPeminjaman(p)
                   │
                   ├── Generate Firestore document ID
                   ├── Set status = "Menunggu Persetujuan"
                   └── peminjamanRef.document(id).set(p)
                           │
                           └── Firestore Cloud
                                   │
                           Snapshot Listener triggers
                                   │
                           DataManager.listPeminjaman updated
                                   │
                           notifyListeners()
                                   │
                           All active Fragments refresh UI
```

### Alur Approval Admin

```
Admin → ApproveFragment
           │
           └── btnApprove.onClick()
                   │
                   └── DataManager.setStatusPeminjaman(id, "Dipinjam", 0)
                           │
                           ├── Find peminjaman by id
                           ├── For each item:
                           │   └── aset.stokDipinjam += item.jumlah
                           │       asetRef.set(aset)  ← update Firestore
                           ├── p.status = "Dipinjam"
                           └── peminjamanRef.set(p)   ← update Firestore
                                   │
                           Both Firestore updates trigger listeners
                                   │
                           UI updates everywhere in real-time
```

### Alur Kalkulasi Denda

```
Admin Approve Pengembalian "Ada yang Rusak"
    │
    ├── Input: jumlah_rusak per item
    │
    ├── Hitung dendaRusak:
    │   └── Σ (jumlah_rusak[i] × aset[i].harga)
    │
    └── DataManager.setStatusPeminjaman(id, "Dikembalikan", dendaRusak)
            │
            ├── Hitung dendaTerlambat:
            │   └── getDaysLate(tanggalRencana, tanggalAktual) × 50_000
            │
            ├── p.dendaTerlambat = dendaTerlambat
            ├── p.dendaRusak     = dendaRusak
            │
            ├── if totalDenda > 0:
            │   └── p.status = "Menunggu Pembayaran"
            └── else:
                └── p.status = "Dikembalikan"
```

---

## Authentication Flow

```
╔══════════════════════════════════════════╗
║          AUTHENTICATION FLOW             ║
╠══════════════════════════════════════════╣
║                                          ║
║  SplashActivity                          ║
║  └── SharedPreferences.get("username")   ║
║       ├── Not null → MainActivity        ║
║       └── Null → LoginActivity           ║
║                                          ║
║  LoginActivity                           ║
║  ├── Hardcoded check:                    ║
║  │   user="admin" && pass="12345"        ║
║  │   → SharedPrefs: role="admin"         ║
║  │   → MainActivity (admin layout)       ║
║  │                                       ║
║  └── Firestore check:                    ║
║      users.where(username=X, pass=Y)     ║
║      ├── Found:                          ║
║      │   → SharedPrefs: role="user"      ║
║      │   → MainActivity (user layout)    ║
║      └── Not found: show error Toast     ║
║                                          ║
║  Forgot Password:                        ║
║  └── users/{username}.passwordHistory    ║
║      → Compare: history[0]+[1]+[2]       ║
║      → If match: allow password reset    ║
║                                          ║
╚══════════════════════════════════════════╝
```

### Session Management

```java
// Login - simpan sesi
SharedPreferences pref = getSharedPreferences("USER_DATA", MODE_PRIVATE);
pref.edit()
    .putString("username", username)
    .putString("role", "user") // atau "admin"
    .apply();

// Logout - hapus sesi
pref.edit().clear().apply();

// Cek sesi di SplashActivity
String username = pref.getString("username", null);
// null = belum login
```

---

## State Management

SARANA tidak menggunakan ViewModel/LiveData. State dikelola melalui:

### 1. In-Memory Cache (DataManager)

```java
// DataManager menyimpan data dalam memory
private List<DataPeminjaman> listPeminjaman;
private List<DataAset> listAset;

// Selalu up-to-date via Firestore listener
```

### 2. Observer Pattern

```java
// Interface
public interface DataChangeListener {
    void onDataChanged();
}

// Subscribe di Fragment
dataListener = () -> {
    if (isAdded()) {
        refreshUI();
    }
};
dataManager.addListener(dataListener);

// Unsubscribe (wajib untuk mencegah memory leak)
@Override
public void onDestroyView() {
    super.onDestroyView();
    dataManager.removeListener(dataListener);
}
```

### 3. Fragment Arguments (State Passing)

```java
// Dari MainActivity ke HistoryFragment
Bundle bundle = new Bundle();
bundle.putString("FILTER_TYPE", "TERLAMBAT");
fragment.setArguments(bundle);
```

### 4. Intent Extras (Activity ke Activity)

```java
// Ke DetailPeminjamanActivity
intent.putExtra("PEMINJAMAN_ID", peminjaman.getId());

// Ke FormPeminjamanActivity (edit mode)
intent.putExtra("IS_EDIT_MODE", true);
intent.putExtra("PEMINJAMAN_ID", p.getId());
intent.putExtra("NAMA_ASET", aset.getNamaAset()); // preselect
```

---

## Database Architecture

### Firestore Structure

```
firestore-root/
├── aset/                          ← Collection
│   ├── 1/                         ← Document (ID = "1")
│   │   ├── id: 1
│   │   ├── namaAset: "Proyektor Epson EB-X400"
│   │   ├── kategori: "Elektronik"
│   │   ├── stokTotal: 5
│   │   ├── stokDipinjam: 2
│   │   ├── kondisi: "Baik"
│   │   └── harga: 3500000
│   └── 2/ ...
│
├── peminjaman/                    ← Collection
│   ├── {auto-id}/                 ← Document
│   │   ├── id: 12345
│   │   ├── firebaseId: "{auto-id}"
│   │   ├── accountUsername: "PTIK A 24"
│   │   ├── nama: "Budi Santoso"
│   │   ├── nim: "2401234"
│   │   ├── items: [...]           ← Array of Maps
│   │   ├── tanggalPinjam: "15/05/2026"
│   │   ├── jamPinjam: "08:00"
│   │   ├── tanggalRencanaKembali: "16/05/2026"
│   │   ├── jamRencanaKembali: "17:00"
│   │   ├── tanggalAktualKembali: "-"
│   │   ├── keperluan: "Presentasi"
│   │   ├── status: "Dipinjam"
│   │   ├── priority: "Normal"
│   │   ├── dendaTerlambat: 0
│   │   └── dendaRusak: 0
│   └── {auto-id}/ ...
│
└── users/                         ← Collection
    ├── PTIK A 24/                 ← Document (ID = username)
    │   ├── username: "PTIK A 24"
    │   ├── password: "Maba24ft"
    │   ├── role: "user"
    │   └── passwordHistory: ["2","4","f","t"]
    └── PTIK B 24/ ...
```

### Inisialisasi Data Otomatis

Saat collection masih kosong, `DataManager` melakukan inisialisasi otomatis:

**Aset (8 item default):**
```
1. Proyektor Epson EB-X400   (Elektronik, 5 unit)
2. Kabel HDMI 5m             (Aksesoris, 10 unit)
3. Pointer Laser Logitech    (Elektronik, 8 unit)
4. Kamera Canon EOS 800D     (Elektronik, 3 unit)
5. Tripod Takara             (Aksesoris, 4 unit)
6. Speaker Portable Simbadda (Elektronik, 2 unit)
7. Roll Kabel 10m            (Aksesoris, 6 unit)
8. Microphone Wireless       (Elektronik, 4 unit)
```

**Users (auto-generate):**
- PTIK: Kelas A–I × Angkatan 23, 24, 25 = 27 akun
- TEKOM: Kelas A–F × Angkatan 23, 24, 25 = 18 akun
- **Total: 45 akun user**

---

## Module Relationships

```
┌─────────────────────────────────────────────────┐
│                  DEPENDENCY GRAPH               │
│                                                 │
│  SplashActivity                                 │
│       └── SharedPreferences                     │
│                                                 │
│  LoginActivity                                  │
│       ├── FirebaseFirestore                     │
│       ├── SharedPreferences                     │
│       └── DataManager (init trigger)            │
│                                                 │
│  MainActivity                                   │
│       └── SharedPreferences (role check)        │
│                                                 │
│  [All Fragments]                                │
│       ├── DataManager (singleton)               │
│       │       ├── DataAset (model)              │
│       │       ├── DataPeminjaman (model)        │
│       │       ├── ItemPinjam (model)            │
│       │       ├── DateHelper (utility)          │
│       │       └── FirebaseFirestore             │
│       └── SharedPreferences (session)           │
│                                                 │
│  FormPeminjamanActivity                         │
│       ├── DataManager                           │
│       └── SharedPreferences                     │
│                                                 │
│  DetailPeminjamanActivity                       │
│       ├── DataManager                           │
│       ├── DateHelper                            │
│       └── SharedPreferences                     │
└─────────────────────────────────────────────────┘
```

---

## Design Patterns

### 1. Singleton Pattern — DataManager

```java
public class DataManager {
    private static DataManager instance;

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private DataManager() {
        // Setup Firestore + listeners
    }
}
```

### 2. Observer Pattern — DataChangeListener

```java
public interface DataChangeListener {
    void onDataChanged();
}

// Publisher (DataManager)
private List<DataChangeListener> listeners = new ArrayList<>();

private void notifyListeners() {
    for (DataChangeListener listener : listeners) {
        listener.onDataChanged();
    }
}

// Subscriber (Fragment)
dataListener = this::refreshData;
dataManager.addListener(dataListener);
```

### 3. ViewHolder Pattern — RecyclerView

Digunakan di `InventoryFragment` (AsetRecyclerAdapter) dan `HistoryFragment` (HistoryAdapter):

```java
class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvNama, tvKategori, tvStok;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        tvNama = itemView.findViewById(R.id.tvAsetNama);
        // ...
    }
}
```

### 4. Adapter Pattern — ListView/RecyclerView

- `ApproveFragment.ApproveAdapter` (BaseAdapter)
- `InventoryFragment.AsetRecyclerAdapter` (RecyclerView.Adapter)
- `HistoryFragment.HistoryAdapter` (RecyclerView.Adapter)

---

## UI Architecture

### Theme

- **Parent Theme:** `Theme.MaterialComponents.Light.NoActionBar`
- **Primary Color:** `#2B7A6F` (Teal)
- **Background:** `#F8F6F1` (Cream)
- **Font:** `sans-serif` (system default)

### Color System

| Token | Hex | Penggunaan |
|-------|-----|-----------|
| `primary` | `#2B7A6F` | Button, active tab, accent |
| `primary_dark` | `#1E5C54` | Dark variant |
| `header_dark` | `#1C1C1C` | Header background |
| `background_cream` | `#F8F6F1` | Screen background |
| `status_available` | `#2B7A6F` / `#D1EAE7` | Available badge |
| `status_borrowed` | `#5B8DB8` / `#D7E5F0` | Borrowed badge |
| `status_late` | `#C75B5B` / `#F9E2E2` | Late badge |
| `status_partial` | `#C9A227` / `#FDF1D3` | Pending badge |

### Layout Structure

```
activity_main.xml
└── ConstraintLayout
    ├── FrameLayout (fragment_container)  ← Fragment host
    └── BottomNavigationView (bottom_navigation)

fragment_home.xml / fragment_admin_dashboard.xml
└── NestedScrollView
    └── LinearLayout (vertical)
        ├── Header Card
        ├── Stats Grid (4 cards)
        ├── Progress Card
        └── Active Loans Section
```

---

## Credits

Kelompok 2
1. MUH ASYAM ASHARI ANSAR
2. Muhammad Rifqi Ramdani Abdullah
3. Ahmad Fakhri Syafa
4. St. Muslimah Nursalam

---

*Dokumen ini merupakan bagian dari dokumentasi resmi SARANA. Lihat [README.md](../README.md) untuk gambaran umum project.*
