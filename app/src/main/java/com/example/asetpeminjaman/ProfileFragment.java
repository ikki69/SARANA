package com.example.asetpeminjaman;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvName = view.findViewById(R.id.tvProfileName);
        if (getActivity() != null) {
            SharedPreferences pref = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
            tvName.setText(pref.getString("username", "Pengguna Aset"));
        }

        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Hapus data sesi di SharedPreferences
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();

                Toast.makeText(getActivity(), "Berhasil keluar", Toast.LENGTH_SHORT).show();

                // Pindah kembali ke LoginActivity dan bersihkan stack activity
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }
}
