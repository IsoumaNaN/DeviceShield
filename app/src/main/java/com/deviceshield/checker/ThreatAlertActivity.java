package com.deviceshield.checker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.deviceshield.checker.checks.ThreatCode;
import com.deviceshield.checker.model.ThreatItem;

public class ThreatAlertActivity extends Activity {
    public static final String EXTRA_CODES = "d";
    public static final String EXTRA_DETAILS = "details_map";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);

        String codesStr = getIntent().getStringExtra(EXTRA_CODES);
        List<Integer> codeList = parseCodes(codesStr);
        Map<Integer, ThreatItem> defs = ThreatCode.createDefinitions();

        // Sample simulated details mapping if not passed via intent
        Map<Integer, String> detailsMap = getSampleDetailsMap();
        if (getIntent().getSerializableExtra(EXTRA_DETAILS) instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<Integer, String> extraMap = (HashMap<Integer, String>) getIntent().getSerializableExtra(EXTRA_DETAILS);
            detailsMap.putAll(extraMap);
        }

        boolean hasFixable = false;
        for (int c : codeList) {
            if (c == 10 || c == 11 || c == 16 || c == 19 || c == 23) {
                hasFixable = true;
                break;
            }
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        int p16 = dpToPx(this, 16.0f);
        rootLayout.setPadding(p16, p16, p16, p16);
        rootLayout.setBackgroundColor(-1); // white

        // Alert Icon
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(android.R.drawable.ic_dialog_alert);
        imageView.setColorFilter(Color.parseColor("#D32F2F"), PorterDuff.Mode.SRC_IN);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(this, 48.0f), dpToPx(this, 48.0f));
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        iconParams.topMargin = dpToPx(this, 24.0f);
        iconParams.bottomMargin = dpToPx(this, 8.0f);
        imageView.setLayoutParams(iconParams);
        rootLayout.addView(imageView);

        // Alert Title
        TextView titleView = new TextView(this);
        if ("vi".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
            titleView.setText("Thiết bị của bạn đang gặp những vấn đề sau");
        } else {
            titleView.setText("Your device is encountering following issues");
        }
        titleView.setTextColor(Color.parseColor("#212121"));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f);
        titleView.setTypeface(null, 1);
        titleView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-2, -2);
        titleParams.gravity = Gravity.CENTER_HORIZONTAL;
        titleParams.bottomMargin = dpToPx(this, 24.0f);
        titleView.setLayoutParams(titleParams);
        rootLayout.addView(titleView);

        // Table Layout
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(-1, -2));

        // Table Header
        TableRow headerRow = new TableRow(this);
        int p8 = dpToPx(this, 8.0f);
        headerRow.setPadding(p8, p8, p8, p8);

        TextView colCode = new TextView(this);
        colCode.setText("vi".equalsIgnoreCase(Locale.getDefault().getLanguage()) ? "Mã Lỗi" : "Code");
        colCode.setTextColor(Color.parseColor("#444444"));
        colCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
        colCode.setTypeface(null, 1);
        colCode.setLayoutParams(new TableRow.LayoutParams(0, -2, 0.2f));
        headerRow.addView(colCode);

        TextView colDesc = new TextView(this);
        colDesc.setText("vi".equalsIgnoreCase(Locale.getDefault().getLanguage()) ? "Diễn giải & Bằng chứng lộ" : "Description & Evidence");
        colDesc.setTextColor(Color.parseColor("#444444"));
        colDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
        colDesc.setTypeface(null, 1);
        colDesc.setLayoutParams(new TableRow.LayoutParams(0, -2, 0.8f));
        colDesc.setPadding(p8, 0, 0, 0);
        headerRow.addView(colDesc);

        tableLayout.addView(headerRow);
        tableLayout.addView(createDivider(this));

        // Table Rows
        for (int c : codeList) {
            ThreatItem item = defs.get(c);
            String desc = (item != null) ? 
                ("vi".equalsIgnoreCase(Locale.getDefault().getLanguage()) ? item.getDescVi() : item.getDescEn()) :
                "Thiết bị của bạn không an toàn";
            String evidence = detailsMap.get(c);
            if (evidence == null) evidence = "Vi phạm tính toàn vẹn hệ thống";

            TableRow row = new TableRow(this);
            row.setPadding(p8, p8, p8, p8);

            TextView codeTv = new TextView(this);
            codeTv.setText(String.valueOf(c));
            codeTv.setTextColor(Color.parseColor("#D32F2F"));
            codeTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
            codeTv.setTypeface(null, 1);
            codeTv.setLayoutParams(new TableRow.LayoutParams(0, -2, 0.2f));
            row.addView(codeTv);

            LinearLayout cellDesc = new LinearLayout(this);
            cellDesc.setOrientation(LinearLayout.VERTICAL);
            cellDesc.setLayoutParams(new TableRow.LayoutParams(0, -2, 0.8f));
            cellDesc.setPadding(p8, 0, 0, 0);

            TextView descTv = new TextView(this);
            descTv.setText(desc);
            descTv.setTextColor(Color.parseColor("#212121"));
            descTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.0f);
            cellDesc.addView(descTv);

            // Detailed Evidence Badge
            LinearLayout evidenceBox = new LinearLayout(this);
            evidenceBox.setOrientation(LinearLayout.VERTICAL);
            GradientDrawable boxBg = new GradientDrawable();
            boxBg.setColor(Color.parseColor("#FFF3E0"));
            boxBg.setCornerRadius(dpToPx(this, 4.0f));
            evidenceBox.setBackground(boxBg);
            evidenceBox.setPadding(dpToPx(this, 6.0f), dpToPx(this, 4.0f), dpToPx(this, 6.0f), dpToPx(this, 4.0f));
            LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(-1, -2);
            boxParams.topMargin = dpToPx(this, 6.0f);
            evidenceBox.setLayoutParams(boxParams);

            TextView labelTv = new TextView(this);
            labelTv.setText("LÝ DO / THÔNG SỐ BỊ LỘ:");
            labelTv.setTextColor(Color.parseColor("#E65100"));
            labelTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f);
            labelTv.setTypeface(null, Typeface.BOLD);
            evidenceBox.addView(labelTv);

            TextView evidenceTv = new TextView(this);
            evidenceTv.setText(evidence);
            evidenceTv.setTextColor(Color.parseColor("#BF360C"));
            evidenceTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.0f);
            evidenceTv.setTypeface(Typeface.MONOSPACE);
            evidenceBox.addView(evidenceTv);

            cellDesc.addView(evidenceBox);
            row.addView(cellDesc);

            tableLayout.addView(row);
            tableLayout.addView(createDivider(this));
        }

        rootLayout.addView(tableLayout);
        scrollView.addView(rootLayout);

        if (!hasFixable) {
            setContentView(scrollView);
            return;
        }

        // RelativeLayout container with bottom action button
        RelativeLayout container = new RelativeLayout(this);
        container.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        container.setBackgroundColor(-1);

        Button settingsBtn = createSettingsButton(this, codeList);
        settingsBtn.setId(View.generateViewId());

        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(-1, -2);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        btnParams.leftMargin = p16;
        btnParams.rightMargin = p16;
        btnParams.bottomMargin = dpToPx(this, 24.0f);
        settingsBtn.setLayoutParams(btnParams);

        RelativeLayout.LayoutParams scrollParams = new RelativeLayout.LayoutParams(-1, -1);
        scrollParams.addRule(RelativeLayout.ABOVE, settingsBtn.getId());
        scrollParams.bottomMargin = dpToPx(this, 16.0f);
        scrollView.setLayoutParams(scrollParams);

        container.addView(scrollView);
        container.addView(settingsBtn);
        setContentView(container);
    }

    private Map<Integer, String> getSampleDetailsMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(0, "Package name không khớp: 'com.deviceshield.checker' (Kỳ vọng: com.tmpapp)");
        map.put(1, "Chữ ký APK không khớp khóa chính thức của nhà phát hành");
        map.put(2, "Phát hiện driver ảo hóa /dev/qemu_pipe và CPU x86");
        map.put(3, "Tìm thấy frida-agent.so trong /proc/self/maps hoặc cổng TCP 27042 đang mở");
        map.put(4, "TracerPid != 0 trong /proc/self/status (Tiến trình đang bị debugger theo dõi)");
        map.put(5, "Tìm thấy file nhị phân cấp quyền: /system/bin/su hoặc ro.build.tags=test-keys");
        map.put(6, "ro.boot.verifiedbootstate=orange hoặc deviceLocked=false (Bootloader đã mở khóa)");
        map.put(8, "Đường dẫn filesDir bất thường (Ứng dụng chạy trong môi trường nhân bản VirtualApp)");
        map.put(10, "Settings.Global.ADB_ENABLED = 1 (Chế độ gỡ lỗi USB đang BẬT)");
        map.put(11, "Settings.Global.DEVELOPMENT_SETTINGS_ENABLED = 1 (Tùy chọn nhà phát triển đang BẬT)");
        map.put(12, "Phát hiện thuộc tính Custom ROM (ro.lineage.version hoặc /system/addon.d)");
        map.put(13, "Dịch vụ trợ năng bên thứ ba đang chạy (Nguy cơ đọc trộm màn hình / click tự động)");
        map.put(14, "Nguồn cài đặt không xác định (null / com.android.packageinstaller)");
        map.put(15, "Phát hiện gói ứng dụng mã độc trong danh mục đen IOC");
        map.put(16, "Bàn phím hiện tại không nằm trong danh sách tin cậy (Nguy cơ keylogger)");
        map.put(17, "Ứng dụng chạy trong User Profile ID >= 10 (Hồ sơ công việc MDM quản trị)");
        map.put(18, "Cờ MotionEvent chứa FLAG_WINDOW_IS_OBSCURED (Màn hình bị che phủ / Tapjacking)");
        map.put(19, "Phát hiện HTTP Proxy kích hoạt: http.proxyHost hoặc Wi-Fi proxy");
        map.put(21, "Phát hiện mount point ẩn của Magisk/KernelSU trong /proc/self/mountinfo");
        map.put(22, "Phát hiện Inline Hook (LDR X16 / BR X16) đè lên prologue hàm openat");
        map.put(23, "Kết nối qua giao thức VPN (Interface ảo 'tun0' đang kích hoạt)");
        return map;
    }

    private Button createSettingsButton(Activity activity, List<Integer> codes) {
        Button button = new Button(activity);
        String label = "vi".equalsIgnoreCase(Locale.getDefault().getLanguage()) ? "Mở cài đặt" : "Open Settings";
        button.setText(label);
        button.setTextColor(-1); // white
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f);
        button.setTypeface(null, 1);
        button.setAllCaps(false);
        button.setPadding(dpToPx(activity, 16.0f), dpToPx(activity, 14.0f), dpToPx(activity, 16.0f), dpToPx(activity, 14.0f));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(activity, 8.0f));
        bg.setColor(Color.parseColor("#007AFF")); // Signature iOS / modern Android blue
        button.setBackground(bg);

        button.setOnClickListener(v -> {
            for (int c : codes) {
                if (c == 10 || c == 11) {
                    activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                    return;
                } else if (c == 16) {
                    activity.startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
                    return;
                } else if (c == 19) {
                    activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    return;
                } else if (c == 23) {
                    activity.startActivity(new Intent(Settings.ACTION_VPN_SETTINGS));
                    return;
                }
            }
        });
        return button;
    }

    private static View createDivider(Context context) {
        View view = new View(context);
        int m8 = dpToPx(context, 8.0f);
        int m4 = dpToPx(context, 4.0f);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(-1, dpToPx(context, 1.0f));
        params.setMargins(m8, m4, m8, m4);
        view.setLayoutParams(params);
        view.setBackgroundColor(Color.parseColor("#E0E0E0"));
        return view;
    }

    private static int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    private static List<Integer> parseCodes(String str) {
        List<Integer> list = new ArrayList<>();
        if (str == null || str.trim().isEmpty()) return list;
        for (String part : str.split(",")) {
            String trim = part.trim();
            if (!trim.isEmpty()) {
                try {
                    list.add(Integer.parseInt(trim));
                } catch (NumberFormatException ignored) {}
            }
        }
        return list;
    }
}
