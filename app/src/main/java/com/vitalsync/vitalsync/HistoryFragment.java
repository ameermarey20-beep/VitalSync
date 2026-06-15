package com.vitalsync.vitalsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private TextView tvHistorySubtitle;
    private HistoryAdapter adapter;
    private List<HeartRateRecord> recordList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        tvHistorySubtitle = view.findViewById(R.id.tvHistorySubtitle);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(recordList);
        rvHistory.setAdapter(adapter);

        checkConnectionAndFetch();

        return view;
    }

    private void checkConnectionAndFetch() {
        boolean isConnected = false;
        if (getActivity() instanceof MainActivity) {
            isConnected = ((MainActivity) getActivity()).isBluetoothConnected();
        }

        if (isConnected) {
            fetchHistoryFromFirestore();
        } else {
            tvHistorySubtitle.setText("Waiting for live connection with Vital Sync robot...");
            recordList.clear();
            adapter.notifyDataSetChanged();
        }
    }

    private void fetchHistoryFromFirestore() {
        FireBaseServices fbs = FireBaseServices.getInstance();
        String currentUserId = fbs.getAuth().getUid();

        if (currentUserId == null) return;

        fbs.getFirestore().collection("heart_rates")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        tvHistorySubtitle.setText("Error loading records");
                        return;
                    }

                    if (value != null) {
                        recordList.clear();
                        recordList.addAll(value.toObjects(HeartRateRecord.class));
                        adapter.notifyDataSetChanged();
                        
                        int count = recordList.size();
                        tvHistorySubtitle.setText(count + (count == 1 ? " Record found" : " Records found"));
                    }
                });
    }
}
