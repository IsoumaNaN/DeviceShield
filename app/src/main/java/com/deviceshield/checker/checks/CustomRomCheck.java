package com.deviceshield.checker.checks;

import android.content.Context;
import android.os.Build;
import java.io.File;
import com.deviceshield.checker.model.CheckResult;

public class CustomRomCheck {
    private static native String nativeCheckCustomRomDetail();

    public static CheckResult check(Context context) {
        try {
            String detail = nativeCheckCustomRomDetail();
            if (detail != null) return CheckResult.danger(detail);
        } catch (UnsatisfiedLinkError ignored) {}

        if (Build.DISPLAY != null && (Build.DISPLAY.toLowerCase().contains("lineage") || Build.DISPLAY.toLowerCase().contains("crdroid"))) {
            return CheckResult.danger("Bản hiển thị ROM tùy biến: 'Build.DISPLAY = " + Build.DISPLAY + "'");
        }
        if (new File("/system/addon.d").exists()) {
            return CheckResult.danger("Tồn tại thư mục kịch bản OTA ROM tùy biến: /system/addon.d");
        }
        return CheckResult.safe("Bản dựng hệ điều hành chính thức từ OEM (" + Build.DISPLAY + ").");
    }
}
