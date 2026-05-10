package com.example.asetpeminjaman;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateHelper {
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    public static synchronized boolean isLate(String tanggalRencanaKembali, String status) {
        if (tanggalRencanaKembali == null || tanggalRencanaKembali.equals("-")) return false;
        
        // Hanya cek terlambat jika sudah dipinjam atau sedang proses pengembalian
        if (!"Dipinjam".equals(status) && !"Menunggu Pengembalian".equals(status)) return false;

        try {
            Date deadline = sdf.parse(tanggalRencanaKembali);
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date today = cal.getTime();
            
            return deadline != null && deadline.before(today);
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized String getTodayDate() {
        return sdf.format(new Date());
    }

    public static synchronized int getDaysLate(String tanggalRencanaKembali, String tanggalAktualKembali) {
        if (tanggalRencanaKembali == null || tanggalRencanaKembali.equals("-")) return 0;
        
        try {
            Date deadline = sdf.parse(tanggalRencanaKembali);
            Date actual = (tanggalAktualKembali == null || tanggalAktualKembali.equals("-")) 
                            ? new Date() : sdf.parse(tanggalAktualKembali);
            
            if (actual != null && deadline != null && actual.after(deadline)) {
                long diff = actual.getTime() - deadline.getTime();
                return (int) (diff / (1000 * 60 * 60 * 24));
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
}
