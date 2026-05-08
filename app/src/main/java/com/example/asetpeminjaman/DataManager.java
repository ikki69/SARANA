package com.example.asetpeminjaman;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

    private static DataManager instance;
    private final FirebaseFirestore db;
    private final CollectionReference peminjamanRef;
    private final CollectionReference asetRef;
    private final CollectionReference usersRef;

    private List<DataPeminjaman> listPeminjaman;
    private List<DataAset> listAset;
    private List<DataChangeListener> listeners = new ArrayList<>();

    public interface DataChangeListener {
        void onDataChanged();
    }

    public void addListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }

    private DataManager() {
        db = FirebaseFirestore.getInstance();
        peminjamanRef = db.collection("peminjaman");
        asetRef = db.collection("aset");
        usersRef = db.collection("users");
        
        listPeminjaman = new ArrayList<>();
        listAset = new ArrayList<>();
        
        // Inisialisasi data ke Firestore jika masih kosong
        syncAsetFromFirestore();
        syncPeminjamanFromFirestore();
        syncUsersFromFirestore();
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void syncAsetFromFirestore() {
        asetRef.addSnapshotListener((value, error) -> {
            if (value != null) {
                listAset.clear();
                for (QueryDocumentSnapshot doc : value) {
                    DataAset aset = doc.toObject(DataAset.class);
                    listAset.add(aset);
                }
                
                // Jika data aset kosong di Firestore, isikan data awal
                if (listAset.isEmpty()) {
                    inisialisasiDataAset();
                }
                notifyListeners();
            }
        });
    }

    private void syncUsersFromFirestore() {
        // Cek apakah salah satu user contoh sudah ada
        usersRef.document("PTIK A 23").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() == null || !task.getResult().exists()) {
                    // Jika user contoh belum ada, lakukan inisialisasi masal
                    inisialisasiDataUsers();
                }
            }
        });
    }

    private void inisialisasiDataUsers() {
        String[] prodiList = {"PTIK", "TEKOM"};
        int[] cohorts = {23, 24, 25};

        for (String prodi : prodiList) {
            char maxClass = prodi.equals("PTIK") ? 'I' : 'F';
            for (char c = 'A'; c <= maxClass; c++) {
                for (int year : cohorts) {
                    String username = prodi + " " + c + " " + year;
                    Map<String, Object> user = new HashMap<>();
                    user.put("username", username);
                    user.put("password", "Maba24ft"); // Default password: Maba24ft
                    user.put("role", "user");

                    usersRef.document(username).set(user);
                }
            }
        }
    }

    private void inisialisasiDataAset() {
        List<DataAset> dataAwal = new ArrayList<>();
        dataAwal.add(new DataAset(1, "Proyektor Epson EB-X400", "Elektronik", 5, "Baik"));
        dataAwal.add(new DataAset(2, "Kabel HDMI 5m", "Aksesoris", 10, "Baik"));
        dataAwal.add(new DataAset(3, "Pointer Laser Logitech", "Elektronik", 8, "Baik"));
        dataAwal.add(new DataAset(4, "Kamera Canon EOS 800D", "Elektronik", 3, "Baik"));
        dataAwal.add(new DataAset(5, "Tripod Takara", "Aksesoris", 4, "Baik"));
        dataAwal.add(new DataAset(6, "Speaker Portable Simbadda", "Elektronik", 2, "Baik"));
        dataAwal.add(new DataAset(7, "Roll Kabel 10m", "Aksesoris", 6, "Baik"));
        dataAwal.add(new DataAset(8, "Microphone Wireless", "Elektronik", 4, "Baik"));

        for (DataAset aset : dataAwal) {
            asetRef.document(String.valueOf(aset.getId())).set(aset);
        }
    }

    private void syncPeminjamanFromFirestore() {
        peminjamanRef.addSnapshotListener((value, error) -> {
            if (value != null) {
                listPeminjaman.clear();
                for (QueryDocumentSnapshot doc : value) {
                    DataPeminjaman p = doc.toObject(DataPeminjaman.class);
                    listPeminjaman.add(p);
                }
                notifyListeners();
            }
        });
    }

    public void updatePeminjaman(DataPeminjaman p) {
        if (p.getFirebaseId() == null) return;
        
        // Ambil data lama untuk hitung selisih stok
        DataPeminjaman lama = getPeminjamanById(p.getId());
        if (lama != null) {
            // Kembalikan stok lama
            for (ItemPinjam itemLama : lama.getItems()) {
                DataAset asetLama = getAsetByNama(itemLama.getNamaAset());
                if (asetLama != null) {
                    asetLama.setStokDipinjam(Math.max(0, asetLama.getStokDipinjam() - itemLama.getJumlah()));
                    asetRef.document(String.valueOf(asetLama.getId())).set(asetLama);
                }
            }
        }
        
        // Kurangi stok baru
        for (ItemPinjam itemBaru : p.getItems()) {
            DataAset asetBaru = getAsetByNama(itemBaru.getNamaAset());
            if (asetBaru != null) {
                asetBaru.setStokDipinjam(asetBaru.getStokDipinjam() + itemBaru.getJumlah());
                asetRef.document(String.valueOf(asetBaru.getId())).set(asetBaru);
            }
        }
        
        peminjamanRef.document(p.getFirebaseId()).set(p);
    }

    public void tambahPeminjaman(DataPeminjaman peminjaman) {
        String id = peminjamanRef.document().getId();
        peminjaman.setFirebaseId(id);
        peminjamanRef.document(id).set(peminjaman);
        
        // Stok TIDAK dikurangi di sini karena butuh persetujuan Admin
    }

    public void tambahAset(DataAset aset) {
        // Cari ID tertinggi untuk menentukan ID baru
        int maxId = 0;
        for (DataAset a : listAset) {
            if (a.getId() > maxId) maxId = a.getId();
        }
        aset.setId(maxId + 1);
        
        asetRef.document(String.valueOf(aset.getId())).set(aset);
    }

    public void hapusAset(int id) {
        asetRef.document(String.valueOf(id)).delete();
    }

    public void updateStokAset(int id, int stokBaru) {
        asetRef.document(String.valueOf(id)).update("stokTotal", stokBaru);
    }

    public void setStatusPeminjaman(int id, String statusBaru) {
        DataPeminjaman p = getPeminjamanById(id);
        if (p == null || p.getFirebaseId() == null) return;

        String statusLama = p.getStatus();
        
        peminjamanRef.document(p.getFirebaseId()).update("status", statusBaru);
        
        // Logika update stok saat disetujui pinjam
        if (statusBaru.equals("Dipinjam") && statusLama.equals("Menunggu Persetujuan")) {
            for (ItemPinjam item : p.getItems()) {
                DataAset aset = getAsetByNama(item.getNamaAset());
                if (aset != null) {
                    aset.setStokDipinjam(aset.getStokDipinjam() + item.getJumlah());
                    asetRef.document(String.valueOf(aset.getId())).set(aset);
                }
            }
        }
        // Logika update stok saat disetujui kembali
        else if (statusBaru.equals("Dikembalikan") && statusLama.equals("Menunggu Pengembalian")) {
            for (ItemPinjam item : p.getItems()) {
                DataAset aset = getAsetByNama(item.getNamaAset());
                if (aset != null) {
                    aset.setStokDipinjam(Math.max(0, aset.getStokDipinjam() - item.getJumlah()));
                    asetRef.document(String.valueOf(aset.getId())).set(aset);
                }
            }
        }
    }

    public boolean ajukanPengembalian(int id, String tanggalKembali) {
        DataPeminjaman p = getPeminjamanById(id);
        if (p == null || p.getFirebaseId() == null) return false;

        peminjamanRef.document(p.getFirebaseId()).update(
            "status", "Menunggu Pengembalian",
            "tanggalAktualKembali", tanggalKembali
        );
        return true;
    }

    public boolean kembalikanAset(int id, String tanggalKembali) {
        // Method ini sekarang digantikan oleh ajukanPengembalian untuk User 
        // dan setStatusPeminjaman("Dikembalikan") untuk Admin
        return ajukanPengembalian(id, tanggalKembali);
    }

    public List<DataPeminjaman> getPeminjamanPending() {
        List<DataPeminjaman> result = new ArrayList<>();
        for (DataPeminjaman p : listPeminjaman) {
            if (p.getStatus().equals("Menunggu Persetujuan") || 
                p.getStatus().equals("Menunggu Pengembalian")) {
                result.add(p);
            }
        }
        return result;
    }
    
    public int getCountPendingPersetujuan() {
        int count = 0;
        for (DataPeminjaman p : listPeminjaman) if (p.getStatus().equals("Menunggu Persetujuan")) count++;
        return count;
    }

    public int getCountPendingPengembalian() {
        int count = 0;
        for (DataPeminjaman p : listPeminjaman) if (p.getStatus().equals("Menunggu Pengembalian")) count++;
        return count;
    }

    public List<DataPeminjaman> getAllPeminjaman() { return listPeminjaman; }

    public List<DataPeminjaman> getAllPeminjamanByUser(String username) {
        List<DataPeminjaman> result = new ArrayList<>();
        for (DataPeminjaman p : listPeminjaman) {
            if (username.equals(p.getAccountUsername())) result.add(p);
        }
        return result;
    }
    
    public DataPeminjaman getPeminjamanById(int id) {
        for (DataPeminjaman p : listPeminjaman) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public List<DataPeminjaman> getPeminjamanAktif() {
        List<DataPeminjaman> result = new ArrayList<>();
        for (DataPeminjaman p : listPeminjaman) {
            if (p.isAktif()) result.add(p);
        }
        return result;
    }

    public List<DataPeminjaman> getPeminjamanAktifByUser(String username) {
        List<DataPeminjaman> result = new ArrayList<>();
        for (DataPeminjaman p : listPeminjaman) {
            if (p.isAktif() && username.equals(p.getAccountUsername())) result.add(p);
        }
        return result;
    }

    public List<DataPeminjaman> getPeminjamanSelesaiByUser(String username) {
        List<DataPeminjaman> result = new ArrayList<>();
        for (DataPeminjaman p : listPeminjaman) {
            if (!p.isAktif() && username.equals(p.getAccountUsername())) result.add(p);
        }
        return result;
    }

    public List<DataPeminjaman> getPeminjamanSelesai() {
        List<DataPeminjaman> result = new ArrayList<>();
        for (DataPeminjaman p : listPeminjaman) {
            if (!p.isAktif()) result.add(p);
        }
        return result;
    }

    public List<DataAset> getAllAset() { return listAset; }

    public DataAset getAsetByNama(String nama) {
        for (DataAset a : listAset) {
            if (a.getNamaAset().equals(nama)) return a;
        }
        return null;
    }

    public int getTotalAset() {
        int total = 0;
        for (DataAset a : listAset) {
            total += a.getStokTotal();
        }
        return total;
    }

    public int getTotalJenisAset() { return listAset.size(); }
    public int getTotalDipinjam(String username, boolean isAdmin) {
        int count = 0;
        for (DataPeminjaman p : listPeminjaman) {
            if ("Dipinjam".equals(p.getStatus())) {
                if (isAdmin || username.equals(p.getAccountUsername())) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getTotalDikembalikan(String username, boolean isAdmin) {
        int count = 0;
        for (DataPeminjaman p : listPeminjaman) {
            if ("Dikembalikan".equals(p.getStatus())) {
                if (isAdmin || username.equals(p.getAccountUsername())) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getTotalTerlambat(String username, boolean isAdmin) {
        int count = 0;
        for (DataPeminjaman p : listPeminjaman) {
            if (DateHelper.isLate(p.getTanggalRencanaKembali(), p.getStatus())) {
                if (isAdmin || username.equals(p.getAccountUsername())) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getTotalItemDipinjam(String username, boolean isAdmin) {
        int total = 0;
        for (DataPeminjaman p : listPeminjaman) {
            if ("Dipinjam".equals(p.getStatus())) {
                if (isAdmin || username.equals(p.getAccountUsername())) {
                    total += p.getJumlah();
                }
            }
        }
        return total;
    }
}
