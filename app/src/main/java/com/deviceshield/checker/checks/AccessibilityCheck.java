package com.deviceshield.checker.checks;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.view.accessibility.AccessibilityManager;
import java.util.Arrays;
import java.util.List;
import com.deviceshield.checker.model.CheckResult;

public class AccessibilityCheck {
    private static final List<String> WHITELIST = Arrays.asList(
        "com.google.android.marvin.talkback",
        "com.samsung.accessibility",
        "com.sec.android.app.voicenote"
    );

    public static CheckResult check(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null || !am.isEnabled()) {
            return CheckResult.safe("Không có dịch vụ trợ năng nào đang kích hoạt.");
        }

        List<AccessibilityServiceInfo> services = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        if (services == null || services.isEmpty()) {
            return CheckResult.safe("Không có dịch vụ trợ năng nào đang kích hoạt.");
        }

        for (AccessibilityServiceInfo info : services) {
            if (info.getResolveInfo() != null && info.getResolveInfo().serviceInfo != null) {
                String pkg = info.getResolveInfo().serviceInfo.packageName;
                boolean isWhitelisted = false;
                for (String white : WHITELIST) {
                    if (pkg.startsWith(white)) {
                        isWhitelisted = true;
                        break;
                    }
                }
                if (!isWhitelisted) {
                    return CheckResult.danger("Dịch vụ trợ năng bên thứ ba đang chạy: '" + pkg + "' (Có nguy cơ đọc trộm màn hình / click tự động)");
                }
            }
        }
        return CheckResult.safe("Chỉ có dịch vụ trợ năng tin cậy của hệ thống.");
    }
}
