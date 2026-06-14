package com.vitalsync.vitalsync;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HeartRateRecord> records;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

    public HistoryAdapter(List<HeartRateRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HeartRateRecord record = records.get(position);
        Context context = holder.itemView.getContext();

        holder.tvBpm.setText(record.getBpm() + " BPM");
        if (record.getTimestamp() != null) {
            holder.tvTimestamp.setText(dateFormat.format(record.getTimestamp().toDate()));
        }

        int bpm = record.getBpm();
        if (bpm > 100) {
            holder.tvStatus.setText("HIGH");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.highText));
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.highBg)));
        } else if (bpm < 60) {
            holder.tvStatus.setText("LOW");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.lowText));
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.lowBg)));
        } else {
            holder.tvStatus.setText("NORMAL");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.normalText));
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.normalBg)));
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvBpm, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvHistoryTimestamp);
            tvBpm = itemView.findViewById(R.id.tvHistoryBpm);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
        }
    }
}
