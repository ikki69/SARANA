package com.example.asetpeminjaman;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class ApproveFragment extends Fragment {

    private ListView lvApproveRequests;
    private TextView tvApproveStatusTitle;
    private DataManager dataManager;
    private ApproveAdapter adapter;
    private List<DataPeminjaman> listPending;
    private DataManager.DataChangeListener dataListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_approve, container, false);

        dataManager = DataManager.getInstance();
        lvApproveRequests = view.findViewById(R.id.lvApproveRequests);
        tvApproveStatusTitle = view.findViewById(R.id.tvApproveStatusTitle);

        listPending = new ArrayList<>();
        adapter = new ApproveAdapter();
        lvApproveRequests.setAdapter(adapter);

        dataListener = this::refreshData;
        dataManager.addListener(dataListener);

        refreshData();

        return view;
    }

    private void refreshData() {
        if (!isAdded()) return;
        listPending.clear();
        listPending.addAll(dataManager.getPeminjamanPending());
        
        int count = listPending.size();
        tvApproveStatusTitle.setText("MENUNGGU PERSETUJUAN • " + count + " REQUEST");
        
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataManager != null && dataListener != null) {
            dataManager.removeListener(dataListener);
        }
    }

    private class ApproveAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listPending.size();
        }

        @Override
        public DataPeminjaman getItem(int position) {
            return listPending.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_approve_card, parent, false);
            }

            DataPeminjaman request = getItem(position);
            
            LinearLayout root = convertView.findViewById(R.id.cardApproveRoot);
            TextView tvReqId = convertView.findViewById(R.id.tvApproveReqId);
            TextView tvTime = convertView.findViewById(R.id.tvApproveTime);
            TextView tvPriority = convertView.findViewById(R.id.tvApprovePriority);
            TextView tvName = convertView.findViewById(R.id.tvApproveName);
            TextView tvUserInfo = convertView.findViewById(R.id.tvApproveUserInfo);
            TextView tvDateNeeded = convertView.findViewById(R.id.tvApproveDateNeeded);
            LinearLayout containerItems = convertView.findViewById(R.id.containerApproveItems);
            View btnApprove = convertView.findViewById(R.id.btnApproveRequest);
            View btnReject = convertView.findViewById(R.id.btnRejectRequest);
            View btnMoreOptions = convertView.findViewById(R.id.btnMoreOptions);

            // Styling based on request type
            boolean isReturnRequest = "Menunggu Pengembalian".equals(request.getStatus());
            
            if (isReturnRequest) {
                root.setBackgroundResource(R.drawable.bg_approve_card_orange);
                tvPriority.setText("• Pengembalian");
                tvPriority.getBackground().setTint(0xFFFDF1D3);
                tvPriority.setTextColor(0xFFE8883A);
            } else if ("Mendesak".equals(request.getPriority())) {
                root.setBackgroundResource(R.drawable.bg_approve_card_urgent);
                tvPriority.setText("• Mendesak");
                tvPriority.getBackground().setTint(0xFFF9E2E2);
                tvPriority.setTextColor(0xFFC75B5B);
            } else {
                root.setBackgroundResource(R.drawable.bg_approve_card_normal);
                tvPriority.setText("• Pinjam");
                tvPriority.getBackground().setTint(0xFFD7E5F0);
                tvPriority.setTextColor(0xFF5B8DB8);
            }

            tvReqId.setText("#REQ-" + String.format("%04d", request.getId()));
            tvTime.setText(isReturnRequest ? "Request Kembali" : "Request Pinjam");
            tvName.setText(request.getNama());
            tvUserInfo.setText(request.getNim() + " - Mhs. JTIK");
            tvDateNeeded.setText(isReturnRequest ? "Kembali: " + request.getTanggalAktualKembali() : "Butuh: " + request.getTanggalPinjam());

            containerItems.removeAllViews();
            for (ItemPinjam item : request.getItems()) {
                TextView tvItem = new TextView(getContext());
                tvItem.setText(item.getNamaAset() + " x" + item.getJumlah());
                tvItem.setTextColor(0xFF1C1C1C);
                tvItem.setTextSize(13);
                containerItems.addView(tvItem);
            }

            btnApprove.setOnClickListener(v -> {
                if ("Menunggu Pengembalian".equals(request.getStatus())) {
                    showReturnApprovalDialog(request);
                } else {
                    dataManager.setStatusPeminjaman(request.getId(), "Dipinjam", 0);
                    Toast.makeText(getContext(), "Permintaan pinjam disetujui", Toast.LENGTH_SHORT).show();
                }
            });

            btnReject.setOnClickListener(v -> {
                String targetStatus = request.getStatus().equals("Menunggu Persetujuan") ? "Ditolak" : "Dipinjam";
                dataManager.setStatusPeminjaman(request.getId(), targetStatus, 0);
                Toast.makeText(getContext(), "Permintaan ditolak", Toast.LENGTH_SHORT).show();
            });

            btnMoreOptions.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DetailPeminjamanActivity.class);
                intent.putExtra("PEMINJAMAN_ID", request.getId());
                startActivity(intent);
            });

            return convertView;
        }

        private void showReturnApprovalDialog(DataPeminjaman request) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
            builder.setTitle("Konfirmasi Pengembalian");
            builder.setMessage("Apakah semua aset dikembalikan dalam kondisi baik?");
            
            builder.setPositiveButton("Ya, Semua Baik", (dialog, which) -> {
                dataManager.setStatusPeminjaman(request.getId(), "Dikembalikan", 0);
                Toast.makeText(getContext(), "Pengembalian disetujui", Toast.LENGTH_SHORT).show();
            });
            
            builder.setNeutralButton("Ada yang Rusak", (dialog, which) -> {
                showDamageFineInputDialog(request);
            });
            
            builder.setNegativeButton("Batal", null);
            builder.show();
        }

        private void showDamageFineInputDialog(DataPeminjaman request) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
            builder.setTitle("Input Kerusakan Aset");

            LinearLayout rootLayout = new LinearLayout(getContext());
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            rootLayout.setPadding(50, 40, 50, 10);
            
            android.widget.ScrollView scrollView = new android.widget.ScrollView(getContext());
            scrollView.addView(rootLayout);
            builder.setView(scrollView);

            List<android.widget.EditText> inputList = new ArrayList<>();
            for (ItemPinjam item : request.getItems()) {
                TextView tvItem = new TextView(getContext());
                tvItem.setText(item.getNamaAset() + " (Dipinjam: " + item.getJumlah() + ")");
                tvItem.setPadding(0, 20, 0, 5);
                tvItem.setTextColor(0xFF1C1C1C);
                rootLayout.addView(tvItem);

                android.widget.EditText etCount = new android.widget.EditText(getContext());
                etCount.setHint("Jumlah yang rusak");
                etCount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                etCount.setText("0");
                rootLayout.addView(etCount);
                inputList.add(etCount);
            }

            builder.setPositiveButton("Proses Denda", (dialog, which) -> {
                long totalDendaRusak = 0;
                for (int i = 0; i < request.getItems().size(); i++) {
                    ItemPinjam item = request.getItems().get(i);
                    String val = inputList.get(i).getText().toString();
                    int rusakCount = val.isEmpty() ? 0 : Integer.parseInt(val);
                    
                    // Batasi agar jumlah rusak tidak melebihi jumlah pinjam
                    if (rusakCount > item.getJumlah()) rusakCount = item.getJumlah();
                    
                    if (rusakCount > 0) {
                        DataAset aset = dataManager.getAsetByNama(item.getNamaAset());
                        if (aset != null) {
                            totalDendaRusak += (aset.getHarga() * rusakCount);
                        }
                    }
                }
                
                dataManager.setStatusPeminjaman(request.getId(), "Dikembalikan", totalDendaRusak);
                Toast.makeText(getContext(), "Pengembalian diproses dengan denda kerusakan", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Batal", null);
            builder.show();
        }
    }
}
