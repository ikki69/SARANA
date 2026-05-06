package com.example.asetpeminjaman;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private ListView listViewRiwayat;
    private LinearLayout emptyRiwayat;
    private EditText etSearch;
    private Button btnTabSemua, btnTabDipinjam, btnTabKembali;
    private DataManager dataManager;
    private List<DataPeminjaman> listMaster;
    private List<DataPeminjaman> listTampil;
    private PeminjamanAdapter adapter;
    private String filterAktif = "SEMUA";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        dataManager = DataManager.getInstance();
        listViewRiwayat = view.findViewById(R.id.listViewRiwayat);
        emptyRiwayat = view.findViewById(R.id.emptyRiwayat);
        etSearch = view.findViewById(R.id.etSearchHistory);
        btnTabSemua = view.findViewById(R.id.btnTabSemua);
        btnTabDipinjam = view.findViewById(R.id.btnTabDipinjam);
        btnTabKembali = view.findViewById(R.id.btnTabKembali);

        listMaster = new ArrayList<>();
        listTampil = new ArrayList<>();
        adapter = new PeminjamanAdapter(listTampil);
        listViewRiwayat.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterData(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        setupClickListeners();
        tampilkanData("SEMUA");

        return view;
    }

    private void filterData(String query) {
        listTampil.clear();
        if (query.isEmpty()) {
            listTampil.addAll(listMaster);
        } else {
            String q = query.toLowerCase();
            for (DataPeminjaman p : listMaster) {
                if (p.getNama().toLowerCase().contains(q) || p.getNamaAset().toLowerCase().contains(q)) {
                    listTampil.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        btnTabSemua.setOnClickListener(v -> { filterAktif = "SEMUA"; tampilkanData("SEMUA"); updateTabStyle("SEMUA"); });
        btnTabDipinjam.setOnClickListener(v -> { filterAktif = "DIPINJAM"; tampilkanData("DIPINJAM"); updateTabStyle("DIPINJAM"); });
        btnTabKembali.setOnClickListener(v -> { filterAktif = "KEMBALI"; tampilkanData("KEMBALI"); updateTabStyle("KEMBALI"); });

        listViewRiwayat.setOnItemClickListener((parent, view, position, id) -> {
            DataPeminjaman p = listTampil.get(position);
            Intent intent = new Intent(getActivity(), DetailPeminjamanActivity.class);
            intent.putExtra("PEMINJAMAN_ID", p.getId());
            startActivity(intent);
        });
    }

    private void tampilkanData(String filter) {
        listMaster.clear();
        
        SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        String username = pref.getString("username", "");

        switch (filter) {
            case "SEMUA": listMaster.addAll(dataManager.getAllPeminjamanByUser(username)); break;
            case "DIPINJAM": listMaster.addAll(dataManager.getPeminjamanAktifByUser(username)); break;
            case "KEMBALI": listMaster.addAll(dataManager.getPeminjamanSelesaiByUser(username)); break;
        }
        java.util.Collections.reverse(listMaster);
        filterData(etSearch.getText().toString());
        emptyRiwayat.setVisibility(listTampil.isEmpty() ? View.VISIBLE : View.GONE);
        listViewRiwayat.setVisibility(listTampil.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateTabStyle(String aktif) {
        btnTabSemua.setBackground(getResources().getDrawable(aktif.equals("SEMUA") ? R.drawable.bg_tab_active : R.drawable.bg_tab_inactive));
        btnTabDipinjam.setBackground(getResources().getDrawable(aktif.equals("DIPINJAM") ? R.drawable.bg_tab_active : R.drawable.bg_tab_inactive));
        btnTabKembali.setBackground(getResources().getDrawable(aktif.equals("KEMBALI") ? R.drawable.bg_tab_active : R.drawable.bg_tab_inactive));
        
        int white = getResources().getColor(R.color.white);
        int secondary = getResources().getColor(R.color.text_secondary);
        btnTabSemua.setTextColor(aktif.equals("SEMUA") ? white : secondary);
        btnTabDipinjam.setTextColor(aktif.equals("DIPINJAM") ? white : secondary);
        btnTabKembali.setTextColor(aktif.equals("KEMBALI") ? white : secondary);
    }

    @Override
    public void onResume() {
        super.onResume();
        tampilkanData(filterAktif);
    }

    private class PeminjamanAdapter extends ArrayAdapter<DataPeminjaman> {
        public PeminjamanAdapter(List<DataPeminjaman> data) {
            super(getActivity(), R.layout.item_peminjaman, data);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_peminjaman, parent, false);
            }
            DataPeminjaman p = getItem(position);
            if (p != null) {
                ((TextView)convertView.findViewById(R.id.tvItemNama)).setText(p.getNama());
                ((TextView)convertView.findViewById(R.id.tvItemNim)).setText(p.getNim());
                
                LinearLayout container = convertView.findViewById(R.id.containerItemPeminjaman);
                container.removeAllViews();
                for (ItemPinjam item : p.getItems()) {
                    View subItem = getLayoutInflater().inflate(R.layout.item_peminjaman_subitem, container, false);
                    ((TextView)subItem.findViewById(R.id.tvSubItemNama)).setText(item.getNamaAset());
                    ((TextView)subItem.findViewById(R.id.tvSubItemJumlah)).setText("x" + item.getJumlah());
                    container.addView(subItem);
                }
                
                ((TextView)convertView.findViewById(R.id.tvItemTanggal)).setText("Pinjam: " + p.getTanggalPinjam() + " | Kembali: " + p.getTanggalRencanaKembali());
                TextView tvStatus = convertView.findViewById(R.id.tvItemStatus);
                tvStatus.setText(p.getStatus());
                tvStatus.setBackground(getResources().getDrawable(p.isAktif() ? R.drawable.bg_status_dipinjam : R.drawable.bg_status_dikembalikan));
            }
            return convertView;
        }
    }
}
