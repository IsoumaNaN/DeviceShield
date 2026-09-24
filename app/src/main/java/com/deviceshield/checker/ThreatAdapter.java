package com.deviceshield.checker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.deviceshield.checker.model.ThreatItem;

public class ThreatAdapter extends RecyclerView.Adapter<ThreatAdapter.ViewHolder> {
    private final List<ThreatItem> items;
    private final Context context;

    public ThreatAdapter(Context context, List<ThreatItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_threat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThreatItem item = items.get(position);
        holder.tvCode.setText(String.format("Code %02d", item.getCode()));
        holder.tvName.setText(item.getName());
        holder.tvDesc.setText(item.getDescVi());

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(16);

        GradientDrawable evidenceBg = new GradientDrawable();
        evidenceBg.setCornerRadius(8);

        if (item.isDetected()) {
            holder.tvStatus.setText("VI PHẠM");
            holder.tvStatus.setTextColor(Color.WHITE);
            badgeBg.setColor(Color.parseColor("#E53935"));
            holder.tvCode.setTextColor(Color.parseColor("#E53935"));

            // Show evidence in highlighted danger box
            holder.layoutEvidence.setVisibility(View.VISIBLE);
            evidenceBg.setColor(Color.parseColor("#FFEBEE"));
            holder.layoutEvidence.setBackground(evidenceBg);
            holder.tvEvidenceLabel.setText("NGUYÊN NHÂN KÍCH HOẠT / LỘ THÔNG SỐ:");
            holder.tvEvidenceLabel.setTextColor(Color.parseColor("#C62828"));
            holder.tvEvidence.setText(item.getDetails());
            holder.tvEvidence.setTextColor(Color.parseColor("#B71C1C"));
        } else {
            holder.tvStatus.setText("AN TOÀN");
            holder.tvStatus.setTextColor(Color.WHITE);
            badgeBg.setColor(Color.parseColor("#43A047"));
            holder.tvCode.setTextColor(Color.parseColor("#43A047"));

            // Show safe verification message
            holder.layoutEvidence.setVisibility(View.VISIBLE);
            evidenceBg.setColor(Color.parseColor("#E8F5E9"));
            holder.layoutEvidence.setBackground(evidenceBg);
            holder.tvEvidenceLabel.setText("KẾT QUẢ KIỂM TRA TÍNH TOÀN VẸN:");
            holder.tvEvidenceLabel.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvEvidence.setText(item.getDetails());
            holder.tvEvidence.setTextColor(Color.parseColor("#1B5E20"));
        }
        holder.tvStatus.setBackground(badgeBg);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvName, tvDesc, tvStatus, tvEvidenceLabel, tvEvidence;
        LinearLayout layoutEvidence;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvThreatCode);
            tvName = itemView.findViewById(R.id.tvThreatName);
            tvDesc = itemView.findViewById(R.id.tvThreatDesc);
            tvStatus = itemView.findViewById(R.id.tvThreatStatus);
            tvEvidenceLabel = itemView.findViewById(R.id.tvEvidenceLabel);
            tvEvidence = itemView.findViewById(R.id.tvThreatEvidence);
            layoutEvidence = itemView.findViewById(R.id.layoutEvidence);
        }
    }
}
