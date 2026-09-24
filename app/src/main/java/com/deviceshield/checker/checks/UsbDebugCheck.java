package com.deviceshield.checker.checks;

import android.content.Context;
import android.provider.Settings;
import com.deviceshield.checker.model.CheckResult;

public class UsbDebugCheck {
    public static CheckResult check(Context context) {
        try {
            int adb = Settings.Global.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
            if (adb == 1) {
                return CheckResult.danger("Cài đặt hệ thống: 'Settings.Global.ADB_ENABLED = 1' (Chế độ gỡ lỗi USB đang BẬT)");
            }
        } catch (Exception ignored) {}
        return CheckResult.safe("Chế độ gỡ lỗi USB đang TẮT (ADB_ENABLED = 0).");
    }
}
