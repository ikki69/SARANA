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
                String targetStatus = request.getStatus().equals("Menunggu Persetujuan") ? "Dipinjam" : "Dikembalikan";
                dataManager.setStatusPeminjaman(request.getId(), targetStatus);
                Toast.makeText(getContext(), "Permintaan disetujui", Toast.LENGTH_SHORT).show();
            });

            btnReject.setOnClickListener(v -> {
                String targetStatus = request.getStatus().equals("Menunggu Persetujuan") ? "Ditolak" : "Dipinjam";
                dataManager.setStatusPeminjaman(request.getId(), targetStatus);
                Toast.makeText(getContext(), "Permintaan ditolak", Toast.LENGTH_SHORT).show();
            });

            btnMoreOptions.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DetailPeminjamanActivity.class);
                intent.putExtra("PEMINJAMAN_ID", request.getId());
                startActivity(intent);
            });

            return convertView;
        }
    }
}
