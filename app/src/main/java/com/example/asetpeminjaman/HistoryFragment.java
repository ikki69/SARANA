package com.example.asetpeminjaman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private LinearLayout emptyRiwayat;
    private EditText etSearch;
    private TextView tvActiveCount, tvResultLabel;
    private MaterialButton btnTabSemua, btnTabDipinjam, btnTabTerlambat, btnTabKembali, btnShowAll;
    private Spinner spinnerSort;
    private DataManager dataManager;
    private List<DataPeminjaman> listMaster = new ArrayList<>();
    private List<DataPeminjaman> listTampil = new ArrayList<>();
    private String filterAktif = "SEMUA";
    private String sortAktif = "Terbaru";
    private HistoryAdapter adapter;
    private boolean isShowAll = false;
    private String userRole = "user";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Ambil filter dari arguments jika ada
        if (getArguments() != null) {
            String filter = getArguments().getString("FILTER_TYPE");
            if (filter != null) {
                filterAktif = filter;
            }
        }

        dataManager = DataManager.getInstance();
        rvHistory = view.findViewById(R.id.rvHistory);
        emptyRiwayat = view.findViewById(R.id.emptyRiwayat);
        etSearch = view.findViewById(R.id.etSearchHistory);
        tvActiveCount = view.findViewById(R.id.tvActiveCount);
        tvResultLabel = view.findViewById(R.id.tvResultLabel);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        
        btnTabSemua = view.findViewById(R.id.btnTabSemua);
        btnTabDipinjam = view.findViewById(R.id.btnTabDipinjam);
        btnTabTerlambat = view.findViewById(R.id.btnTabTerlambat);
        btnTabKembali = view.findViewById(R.id.btnTabKembali);
        btnShowAll = view.findViewById(R.id.btnShowAll);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter();
        rvHistory.setAdapter(adapter);

        btnShowAll.setOnClickListener(v -> {
            isShowAll = true;
            renderList();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterData(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        setupTabs();
        setupSort();
        loadData();

        return view;
    }

    private void setupSort() {
        String[] options = {"Terbaru", "Terlama", "Kategori"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortAktif = options[position];
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupTabs() {
        btnTabSemua.setOnClickListener(v -> { filterAktif = "SEMUA"; updateUI(); });
        btnTabDipinjam.setOnClickListener(v -> { filterAktif = "DIPINJAM"; updateUI(); });
        btnTabTerlambat.setOnClickListener(v -> { filterAktif = "TERLAMBAT"; updateUI(); });
        btnTabKembali.setOnClickListener(v -> { filterAktif = "KEMBALI"; updateUI(); });
    }

    private void loadData() {
        SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        String username = pref.getString("username", "");
        String role = pref.getString("role", "user");

        listMaster.clear();
        if ("admin".equals(role)) {
            listMaster.addAll(dataManager.getAllPeminjaman());
        } else {
            listMaster.addAll(dataManager.getAllPeminjamanByUser(username));
        }
        Collections.reverse(listMaster);
        
        int activeCount = dataManager.getPeminjamanAktif().size();
        tvActiveCount.setText(activeCount + " peminjaman aktif saat ini");
        
        updateUI();
    }

    private void updateUI() {
        if (!isAdded()) return;
        
        updateTabStyle();
        filterData(etSearch.getText().toString());
    }

    private void filterData(String query) {
        listTampil.clear();
        isShowAll = false; // Reset status setiap filter berubah
        String q = query.toLowerCase().trim();

        for (DataPeminjaman p : listMaster) {
            // Pencarian berdasarkan Nama, NIM, ID, atau Nama Aset
            boolean matchesSearch = q.isEmpty() || 
                p.getNama().toLowerCase().contains(q) || 
                p.getNim().toLowerCase().contains(q) ||
                String.valueOf(p.getId()).contains(q) ||
                p.getNamaAset().toLowerCase().contains(q);

            boolean matchesFilter = false;

            if (filterAktif.equals("SEMUA")) {
                matchesFilter = true;
            } else if (filterAktif.equals("DIPINJAM")) {
                matchesFilter = "Dipinjam".equals(p.getStatus()) || "Menunggu Pengembalian".equals(p.getStatus());
            } else if (filterAktif.equals("TERLAMBAT")) {
                matchesFilter = isLate(p);
            } else if (filterAktif.equals("KEMBALI")) {
                matchesFilter = "Dikembalikan".equals(p.getStatus());
            }

            if (matchesSearch && matchesFilter) {
                listTampil.add(p);
            }
        }

        sortData();
        renderList();
    }

    private void sortData() {
        if (listTampil.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Collections.sort(listTampil, new Comparator<DataPeminjaman>() {
            @Override
            public int compare(DataPeminjaman p1, DataPeminjaman p2) {
                if (sortAktif.equals("Terbaru") || sortAktif.equals("Terlama")) {
                    try {
                        Date d1 = sdf.parse(p1.getTanggalPinjam());
                        Date d2 = sdf.parse(p2.getTanggalPinjam());
                        if (sortAktif.equals("Terbaru")) return d2.compareTo(d1);
                        else return d1.compareTo(d2);
                    } catch (ParseException e) {
                        return 0;
                    }
                } else if (sortAktif.equals("Kategori")) {
                    String kat1 = getFirstCategory(p1);
                    String kat2 = getFirstCategory(p2);
                    return kat1.compareTo(kat2);
                }
                return 0;
            }
        });
    }

    private String getFirstCategory(DataPeminjaman p) {
        if (p.getItems() == null || p.getItems().isEmpty()) return "Lainnya";
        String firstItemNama = p.getItems().get(0).getNamaAset();
        DataAset aset = dataManager.getAsetByNama(firstItemNama);
        return (aset != null) ? aset.getKategori() : "Lainnya";
    }

    private boolean isLate(DataPeminjaman p) {
        return DateHelper.isLate(p.getTanggalRencanaKembali(), p.getStatus());
    }

    private void renderList() {
        tvResultLabel.setText("Menampilkan " + listTampil.size() + " transaksi");
        
        if (listTampil.isEmpty()) {
            emptyRiwayat.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            btnShowAll.setVisibility(View.GONE);
        } else {
            emptyRiwayat.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            
            // Logika tombol Tampilkan Semua
            if (!isShowAll && listTampil.size() > 5) {
                btnShowAll.setVisibility(View.VISIBLE);
            } else {
                btnShowAll.setVisibility(View.GONE);
            }
            
            // Log debug (bisa dihapus nanti)
            android.util.Log.d("HistoryDebug", "Total data: " + listTampil.size() + ", showing: " + adapter.getItemCount());
            
            adapter.notifyDataSetChanged();
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_peminjaman_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DataPeminjaman p = listTampil.get(position);
            
            holder.tvId.setText("#PM-" + String.format("%04d", p.getId()));
            holder.tvName.setText(p.getNama());
            
            SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
            String role = pref.getString("role", "user");

            if ("admin".equals(role)) {
                holder.tvAccount.setVisibility(View.VISIBLE);
                holder.tvAccount.setText("Akun: @" + p.getAccountUsername());
            } else {
                holder.tvAccount.setVisibility(View.GONE);
            }

            holder.tvInfo.setText(p.getNim() + " - Mhs. Aktif");
            holder.tvDeadline.setText(p.getTanggalRencanaKembali());

            if (isLate(p)) {
                holder.root.setBackgroundResource(R.drawable.bg_approve_card_urgent);
                holder.tvStatus.setText("• Terlambat");
                holder.tvStatus.getBackground().setTint(0xFFF9E2E2);
                holder.tvStatus.setTextColor(0xFFC75B5B);
            } else if ("Dipinjam".equals(p.getStatus()) || "Menunggu Pengembalian".equals(p.getStatus())) {
                holder.root.setBackgroundResource(R.drawable.bg_approve_card_teal);
                holder.tvStatus.setText("• Dipinjam");
                holder.tvStatus.getBackground().setTint(0xFFD1EAE7);
                holder.tvStatus.setTextColor(0xFF2B7A6F);
            } else if ("Dikembalikan".equals(p.getStatus())) {
                holder.root.setBackgroundResource(R.drawable.bg_approve_card_normal);
                holder.tvStatus.setText("• Selesai");
                holder.tvStatus.getBackground().setTint(0xFFD7E5F0);
                holder.tvStatus.setTextColor(0xFF5B8DB8);
            } else {
                holder.root.setBackgroundResource(R.drawable.bg_approve_card_teal);
                holder.root.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF9E9E9E));
                holder.tvStatus.setText("• " + p.getStatus());
                holder.tvStatus.getBackground().setTint(0xFFEEEEEE);
                holder.tvStatus.setTextColor(0xFF9E9E9E);
            }

            holder.itemsContainer.removeAllViews();
            for (ItemPinjam item : p.getItems()) {
                TextView tvItem = new TextView(getContext());
                tvItem.setText("• " + item.getNamaAset() + " x" + item.getJumlah());
                tvItem.setTextColor(0xFF1C1C1C);
                tvItem.setTextSize(13);
                holder.itemsContainer.addView(tvItem);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DetailPeminjamanActivity.class);
                intent.putExtra("PEMINJAMAN_ID", p.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            if (!isShowAll && listTampil.size() > 5) {
                return 5;
            }
            return listTampil.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvId, tvStatus, tvName, tvAccount, tvInfo, tvDeadline;
            LinearLayout itemsContainer;
            View root;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.tvHistoryId);
                tvStatus = itemView.findViewById(R.id.tvHistoryStatusBadge);
                tvName = itemView.findViewById(R.id.tvHistoryName);
                tvAccount = itemView.findViewById(R.id.tvHistoryAccount);
                tvInfo = itemView.findViewById(R.id.tvHistoryUserInfo);
                tvDeadline = itemView.findViewById(R.id.tvHistoryDeadline);
                itemsContainer = itemView.findViewById(R.id.containerHistoryItems);
                root = itemView.findViewById(R.id.cardHistoryRoot);
            }
        }
    }

    private void updateTabStyle() {
        MaterialButton[] buttons = {btnTabSemua, btnTabDipinjam, btnTabTerlambat, btnTabKembali};
        String[] types = {"SEMUA", "DIPINJAM", "TERLAMBAT", "KEMBALI"};
        
        int teal = getResources().getColor(R.color.primary);
        
        for (int i = 0; i < buttons.length; i++) {
            boolean isActive = filterAktif.equals(types[i]);
            
            if (isActive) {
                buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(teal));
                buttons[i].setTextColor(0xFFFFFFFF);
                buttons[i].setStrokeWidth(0);
            } else {
                buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
                buttons[i].setTextColor(teal);
                buttons[i].setStrokeColor(android.content.res.ColorStateList.valueOf(teal));
                buttons[i].setStrokeWidth((int) (1.5 * getResources().getDisplayMetrics().density));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}
