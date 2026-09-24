package com.deviceshield.checker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.deviceshield.checker.checks.ThreatCode;
import com.deviceshield.checker.checks.ThreatScanner;
import com.deviceshield.checker.model.ThreatItem;

public class MainActivity extends AppCompatActivity {
    private TextView tvScoreTitle, tvScoreSubtitle, tvScanningStatus;
    private ProgressBar progressBar;
    private Button btnScan, btnViewOriginalAlert;
    private RecyclerView recyclerView;
    private ThreatAdapter adapter;
    private List<ThreatItem> threatList;
    private List<Integer> detectedCodesList = new ArrayList<>();
    private HashMap<Integer, String> detectedDetailsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvScoreTitle = findViewById(R.id.tvScoreTitle);
        tvScoreSubtitle = findViewById(R.id.tvScoreSubtitle);
        tvScanningStatus = findViewById(R.id.tvScanningStatus);
        progressBar = findViewById(R.id.progressBar);
        btnScan = findViewById(R.id.btnScan);
        btnViewOriginalAlert = findViewById(R.id.btnViewOriginalAlert);
        recyclerView = findViewById(R.id.recyclerView);

        threatList = new ArrayList<>(ThreatCode.createDefinitions().values());
        adapter = new ThreatAdapter(this, threatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnScan.setOnClickListener(v -> runFullScan());

        btnViewOriginalAlert.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            if (detectedCodesList.isEmpty()) {
                // If clean, trigger sample codes (USB Debug & Dev Mode) for demonstration
                sb.append("10,11");
            } else {
                for (int i = 0; i < detectedCodesList.size(); i++) {
                    sb.append(detectedCodesList.get(i));
                    if (i < detectedCodesList.size() - 1) sb.append(",");
                }
            }
            Intent intent = new Intent(MainActivity.this, ThreatAlertActivity.class);
            intent.putExtra(ThreatAlertActivity.EXTRA_CODES, sb.toString());
            intent.putExtra(ThreatAlertActivity.EXTRA_DETAILS, detectedDetailsMap);
            startActivity(intent);
        });

        // Run automatic initial scan
        runFullScan();
    }

    private void runFullScan() {
        btnScan.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvScanningStatus.setVisibility(View.VISIBLE);
        tvScanningStatus.setText("Đang kiểm tra tính toàn vẹn hệ thống...");

        ThreatScanner.scanAsync(this, new ThreatScanner.ScanCallback() {
            @Override
            public void onProgress(int completed, int total, String checkName) {
                runOnUiThread(() -> {
                    progressBar.setProgress((completed * 100) / total);
                    tvScanningStatus.setText(String.format("Đang quét [%d/%d]: %s", completed, total, checkName));
                });
            }

            @Override
            public void onComplete(List<ThreatItem> results, List<Integer> detectedCodes) {
                runOnUiThread(() -> {
                    detectedCodesList = detectedCodes;
                    detectedDetailsMap.clear();
                    for (ThreatItem item : results) {
                        if (item.isDetected()) {
                            detectedDetailsMap.put(item.getCode(), item.getDetails());
                        }
                    }

                    threatList.clear();
                    threatList.addAll(results);
                    adapter.notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);
                    tvScanningStatus.setVisibility(View.GONE);
                    btnScan.setEnabled(true);

                    if (detectedCodes.isEmpty()) {
                        tvScoreTitle.setText("THIẾT BỊ AN TOÀN");
                        tvScoreTitle.setTextColor(Color.parseColor("#43A047"));
                        tvScoreSubtitle.setText("0/24 mối đe dọa được phát hiện");
                    } else {
                        tvScoreTitle.setText("PHÁT HIỆN NGUY CƠ BẢO MẬT");
                        tvScoreTitle.setTextColor(Color.parseColor("#E53935"));
                        tvScoreSubtitle.setText(String.format("%d/24 cơ chế phát hiện đã kích hoạt", detectedCodes.size()));
                        btnViewOriginalAlert.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}
