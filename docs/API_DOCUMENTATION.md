# 🔌 API Documentation — SARANA

> Dokumentasi lengkap operasi data Firebase Firestore yang digunakan oleh SARANA.

---

## Overview

SARANA tidak menggunakan REST API eksternal. Seluruh operasi data dilakukan melalui **Firebase Firestore SDK** secara langsung dari Android. Semua akses data dikelola oleh `DataManager.java` (Singleton pattern).

---

## Firebase Collections

### Collection: `aset`

Menyimpan data inventaris aset jurusan.

**Document ID:** `String(id)` — contoh: `"1"`, `"2"`, `"8"`

| Field | Tipe | Keterangan |
|-------|------|------------|
| `id` | `int` | ID unik aset |
| `namaAset` | `String` | Nama aset, contoh: "Proyektor Epson EB-X400" |
| `kategori` | `String` | Kategori aset (lihat tabel kategori) |
| `stokTotal` | `int` | Total unit yang dimiliki |
| `stokDipinjam` | `int` | Unit yang sedang dipinjam |
| `kondisi` | `String` | "Baik" / "Rusak Ringan" / "Rusak Berat" |
| `harga` | `long` | Harga per unit (Rupiah), untuk kalkulasi denda |

**Derived fields (tidak disimpan di Firestore):**
- `stokTersedia` = `stokTotal - stokDipinjam`
- `tersedia` = `stokTersedia > 0`

**Kategori & Harga Default:**

| Kategori | Harga Default |
|----------|--------------|
| `Komputer` | Rp 12.000.000 |
| `Jaringan` | Rp 4.500.000 |
| `Elektronik` | Rp 3.500.000 |
| `Alat Ukur` | Rp 2.500.000 |
| `Mikrokomputer` | Rp 1.200.000 |
| `Mikrokontroler` | Rp 450.000 |
| `Aksesoris` | Rp 150.000 |

---

### Collection: `peminjaman`

Menyimpan semua transaksi peminjaman.

**Document ID:** Auto-generated Firestore ID (disimpan juga di field `firebaseId`)

| Field | Tipe | Keterangan |
|-------|------|------------|
| `id` | `int` | ID numerik transaksi (random 0–99999) |
| `firebaseId` | `String` | Firestore document ID |
| `accountUsername` | `String` | Username akun yang mengajukan |
| `nama` | `String` | Nama lengkap peminjam |
| `nim` | `String` | NIM / NIP peminjam |
| `items` | `List<Map>` | Daftar item yang dipinjam |
| `tanggalPinjam` | `String` | Format: `dd/MM/yyyy` |
| `jamPinjam` | `String` | Format: `HH:mm` |
| `tanggalRencanaKembali` | `String` | Format: `dd/MM/yyyy` |
| `jamRencanaKembali` | `String` | Format: `HH:mm` |
| `tanggalAktualKembali` | `String` | Format: `dd/MM/yyyy`, default: `"-"` |
| `keperluan` | `String` | Deskripsi keperluan peminjaman |
| `status` | `String` | Status transaksi (lihat enum status) |
| `priority` | `String` | `"Normal"` / `"Mendesak"` |
| `dendaTerlambat` | `long` | Nominal denda terlambat (Rupiah) |
| `dendaRusak` | `long` | Nominal denda kerusakan (Rupiah) |

**Struktur `items` (sub-dokumen):**

```json
[
  {
    "namaAset": "Proyektor Epson EB-X400",
    "jumlah": 1
  },
  {
    "namaAset": "Kabel HDMI 5m",
    "jumlah": 2
  }
]
```

**Enum Status:**

| Nilai | Deskripsi |
|-------|-----------|
| `"Menunggu Persetujuan"` | Baru diajukan user |
| `"Dipinjam"` | Disetujui admin, aset sedang dipinjam |
| `"Ditolak"` | Ditolak admin |
| `"Menunggu Pengembalian"` | User mengajukan kembali |
| `"Menunggu Pembayaran"` | Ada denda yang belum dibayar |
| `"Dikembalikan"` | Transaksi selesai |

---

### Collection: `users`

Menyimpan akun mahasiswa.

**Document ID:** Username — contoh: `"PTIK A 24"`

| Field | Tipe | Keterangan |
|-------|------|------------|
| `username` | `String` | Username akun |
| `password` | `String` | Password (plain text) |
| `role` | `String` | `"user"` |
| `passwordHistory` | `List<String>` | 3 karakter terakhir password |

**Contoh dokumen:**

```json
{
  "username": "PTIK A 24",
  "password": "Maba24ft",
  "role": "user",
  "passwordHistory": ["2", "4", "f", "t"]
}
```

> ⚠️ **Catatan Keamanan:** Password disimpan dalam bentuk plain text. Untuk produksi, wajib diimplementasikan hashing (bcrypt/SHA-256).

---

## DataManager API Reference

### Singleton Access

```java
DataManager dm = DataManager.getInstance();
```

---

### Aset Operations

#### `getAllAset()`
```java
List<DataAset> getAllAset()
```
Mengembalikan daftar semua aset dari cache lokal (selalu sinkron dengan Firestore via listener).

---

#### `getAsetByNama(String nama)`
```java
DataAset getAsetByNama(String nama)
// Returns: DataAset atau null jika tidak ditemukan
```

---

#### `tambahAset(DataAset aset)`
```java
void tambahAset(DataAset aset)
```
Menambah aset baru. ID digenerate otomatis (maxId + 1).

**Contoh:**
```java
DataAset baru = new DataAset(0, "Switch Cisco", "Jaringan", 3, "Baik");
baru.setHarga(5000000L); // opsional
dataManager.tambahAset(baru);
```

---

#### `hapusAset(int id)`
```java
void hapusAset(int id)
```
Menghapus aset berdasarkan ID dari Firestore.

---

#### `updateStokAset(int id, int stokBaru)`
```java
void updateStokAset(int id, int stokBaru)
```
Mengupdate field `stokTotal` di Firestore.

---

#### `getTotalAset()`
```java
int getTotalAset()
// Returns: Jumlah total unit semua aset
```

---

#### `getTotalJenisAset()`
```java
int getTotalJenisAset()
// Returns: Jumlah jenis/kategori aset (baris di list)
```

---

### Peminjaman Operations

#### `tambahPeminjaman(DataPeminjaman p)`
```java
void tambahPeminjaman(DataPeminjaman p)
```
Membuat transaksi peminjaman baru. Status awal: `"Menunggu Persetujuan"`. Stok **tidak** langsung dikurangi.

**Contoh:**
```java
List<ItemPinjam> items = new ArrayList<>();
items.add(new ItemPinjam("Proyektor Epson EB-X400", 1));

DataPeminjaman baru = new DataPeminjaman(
    new Random().nextInt(99999),  // id
    "PTIK A 24",                  // accountUsername
    "Budi Santoso",               // nama
    "2401234",                    // nim
    items,
    "15/05/2026", "08:00",        // tanggal & jam pinjam
    "16/05/2026", "17:00",        // tanggal & jam rencana kembali
    "Presentasi tugas akhir"      // keperluan
);

dataManager.tambahPeminjaman(baru);
```

---

#### `updatePeminjaman(DataPeminjaman p)`
```java
void updatePeminjaman(DataPeminjaman p)
```
Update data peminjaman (untuk mode edit). Menghitung ulang stok aset lama vs baru.

---

#### `setStatusPeminjaman(int id, String statusBaru, long dendaRusakManual)`
```java
void setStatusPeminjaman(int id, String statusBaru, long dendaRusakManual)
```
Mengubah status peminjaman dengan logika bisnis:

| Transisi Status | Efek Samping |
|-----------------|--------------|
| `"Menunggu Persetujuan"` → `"Dipinjam"` | Stok aset dikurangi |
| `"Menunggu Pengembalian"` → `"Dikembalikan"` | Stok dikembalikan, hitung denda terlambat |
| `"Menunggu Pengembalian"` → `"Menunggu Pembayaran"` | Stok dikembalikan, set denda |

---

#### `ajukanPengembalian(int id, String tanggalKembali)`
```java
boolean ajukanPengembalian(int id, String tanggalKembali)
// Returns: true jika berhasil
```
Mengubah status menjadi `"Menunggu Pengembalian"` dan mengisi `tanggalAktualKembali`.

---

#### `konfirmasiPembayaranDenda(int id)`
```java
void konfirmasiPembayaranDenda(int id)
```
Mengubah status dari `"Menunggu Pembayaran"` ke `"Dikembalikan"`.

---

### Query Operations

#### Peminjaman berdasarkan user/scope

```java
// Semua peminjaman (admin use)
List<DataPeminjaman> getAllPeminjaman()

// Peminjaman milik user tertentu
List<DataPeminjaman> getAllPeminjamanByUser(String username)

// Peminjaman aktif (belum selesai)
List<DataPeminjaman> getPeminjamanAktif()
List<DataPeminjaman> getPeminjamanAktifByUser(String username)

// Peminjaman selesai
List<DataPeminjaman> getPeminjamanSelesai()
List<DataPeminjaman> getPeminjamanSelesaiByUser(String username)

// Pending (butuh aksi admin)
List<DataPeminjaman> getPeminjamanPending()

// By ID
DataPeminjaman getPeminjamanById(int id)
```

---

#### Counter Methods

```java
// Jumlah pending persetujuan
int getCountPendingPersetujuan()

// Jumlah pending pengembalian
int getCountPendingPengembalian()

// Jumlah yang sedang dipinjam
int getTotalDipinjam(String username, boolean isAdmin)

// Jumlah yang sudah dikembalikan
int getTotalDikembalikan(String username, boolean isAdmin)

// Jumlah yang terlambat
int getTotalTerlambat(String username, boolean isAdmin)

// Total item (unit) yang sedang dipinjam
int getTotalItemDipinjam(String username, boolean isAdmin)
```

---

### Observer Pattern

DataManager menggunakan Observer pattern untuk sinkronisasi real-time:

```java
// Implement listener
DataManager.DataChangeListener myListener = () -> {
    // Dipanggil setiap kali data berubah di Firestore
    updateUI();
};

// Register
dataManager.addListener(myListener);

// Unregister (penting di onDestroyView / onDestroy)
dataManager.removeListener(myListener);
```

---

## DateHelper API

```java
// Cek apakah peminjaman terlambat
boolean isLate = DateHelper.isLate(tanggalRencanaKembali, status);

// Hitung jumlah hari terlambat
int daysLate = DateHelper.getDaysLate(tanggalRencanaKembali, tanggalAktualKembali);
// Jika tanggalAktualKembali = "-" atau null, dihitung dari hari ini

// Dapatkan tanggal hari ini (format dd/MM/yyyy)
String today = DateHelper.getTodayDate();
```

**Formula Denda:**
```
Denda Terlambat = getDaysLate(...) × Rp 50.000
Denda Rusak     = jumlah_unit_rusak × aset.getHarga()
Total Denda     = dendaTerlambat + dendaRusak
```

---

## Firestore Real-time Listeners

DataManager menggunakan `addSnapshotListener` untuk aset dan peminjaman:

```java
// Contoh implementasi di DataManager
asetRef.addSnapshotListener((value, error) -> {
    if (value != null) {
        listAset.clear();
        for (QueryDocumentSnapshot doc : value) {
            DataAset aset = doc.toObject(DataAset.class);
            listAset.add(aset);
        }
        notifyListeners(); // Beritahu semua observer
    }
});
```

Setiap perubahan di Firestore (dari sumber manapun) akan memicu update otomatis di seluruh UI yang sedang aktif.

---

## Credits

Kelompok 2
1. MUH ASYAM ASHARI ANSAR
2. Muhammad Rifqi Ramdani Abdullah
3. Ahmad Fakhri Syafa
4. St. Muslimah Nursalam
