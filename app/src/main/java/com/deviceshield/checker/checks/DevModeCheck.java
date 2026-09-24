package com.deviceshield.checker.checks;

import android.content.Context;
import android.provider.Settings;
import com.deviceshield.checker.model.CheckResult;

public class DevModeCheck {
    public static CheckResult check(Context context) {
        try {
            int dev = Settings.Global.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
            if (dev == 1) {
                return CheckResult.danger("Cài đặt hệ thống: 'Settings.Global.DEVELOPMENT_SETTINGS_ENABLED = 1' (Tùy chọn nhà phát triển đang BẬT)");
            }
        } catch (Exception ignored) {}
        return CheckResult.safe("Tùy chọn nhà phát triển đang TẮT (DEVELOPMENT_SETTINGS_ENABLED = 0).");
    }
}
