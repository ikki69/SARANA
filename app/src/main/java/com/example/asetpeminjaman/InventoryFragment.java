package com.example.asetpeminjaman;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends Fragment {

    private ListView listViewAset;
    private EditText etSearch;
    private DataManager dataManager;
    private List<DataAset> listAset;
    private List<DataAset> listAsetFilter;
    private AsetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        dataManager = DataManager.getInstance();
        listViewAset = view.findViewById(R.id.listViewAset);
        etSearch = view.findViewById(R.id.etSearch);

        listAset = dataManager.getAllAset();
        listAsetFilter = new ArrayList<>(listAset);

        adapter = new AsetAdapter(listAsetFilter);
        listViewAset.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAset(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        listViewAset.setOnItemClickListener((parent, view1, position, id) -> {
            DataAset aset = listAsetFilter.get(position);
            if (!aset.isTersedia()) {
                Toast.makeText(getContext(), aset.getNamaAset() + " sedang tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getActivity(), FormPeminjamanActivity.class);
            intent.putExtra("NAMA_ASET", aset.getNamaAset());
            startActivity(intent);
        });

        return view;
    }

    private void filterAset(String query) {
        listAsetFilter.clear();
        if (query.isEmpty()) {
            listAsetFilter.addAll(listAset);
        } else {
            String queryLower = query.toLowerCase();
            for (DataAset aset : listAset) {
                if (aset.getNamaAset().toLowerCase().contains(queryLower) || aset.getKategori().toLowerCase().contains(queryLower)) {
                    listAsetFilter.add(aset);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        listAsetFilter.clear();
        listAsetFilter.addAll(dataManager.getAllAset());
        adapter.notifyDataSetChanged();
    }

    private class AsetAdapter extends ArrayAdapter<DataAset> {
        public AsetAdapter(List<DataAset> data) {
            super(getActivity(), R.layout.item_aset, data);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_aset, parent, false);
            }
            DataAset aset = getItem(position);
            if (aset != null) {
                TextView tvNama = convertView.findViewById(R.id.tvAsetNama);
                TextView tvKategori = convertView.findViewById(R.id.tvAsetKategori);
                TextView tvStok = convertView.findViewById(R.id.tvAsetStok);
                TextView tvDipinjam = convertView.findViewById(R.id.tvAsetDipinjam);
                TextView tvStatus = convertView.findViewById(R.id.tvAsetStatus);
                TextView tvIcon = convertView.findViewById(R.id.tvAsetIcon);
                View iconContainer = convertView.findViewById(R.id.iconContainer);

                tvNama.setText(aset.getNamaAset());
                tvKategori.setText(aset.getKategori() + " • " + aset.getKondisi());
                tvStok.setText("Stok: " + aset.getStokTersedia() + "/" + aset.getStokTotal());
                tvDipinjam.setText("Dipinjam: " + aset.getStokDipinjam());

                // Simple icon handling
                tvIcon.setText("📦");
                if (aset.isTersedia()) {
                    tvStatus.setText("Tersedia");
                    tvStatus.setBackground(getResources().getDrawable(R.drawable.bg_status_dikembalikan));
                } else {
                    tvStatus.setText("Habis");
                    tvStatus.setBackground(getResources().getDrawable(R.drawable.bg_status_dipinjam));
                }
            }
            return convertView;
        }
    }
}
