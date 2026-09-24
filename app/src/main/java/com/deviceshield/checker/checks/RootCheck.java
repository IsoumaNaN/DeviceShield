package com.deviceshield.checker.checks;

import android.content.Context;
import android.os.Build;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import com.deviceshield.checker.model.CheckResult;

public class RootCheck {
    private static native String nativeCheckRootTier1Detail();
    private static native String nativeCheckRootTier2Detail();

    public static CheckResult checkTier1(Context context) {
        try {
            String detail = nativeCheckRootTier1Detail();
            if (detail != null) return CheckResult.danger(detail);
        } catch (UnsatisfiedLinkError ignored) {}

        String[] paths = {
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/data/local/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) {
                return CheckResult.danger("Tìm thấy nhị phân Superuser: " + path);
            }
        }

        if (Build.TAGS != null && Build.TAGS.contains("test-keys")) {
            return CheckResult.danger("Bản dựng hệ thống mang cờ 'ro.build.tags = test-keys'");
        }
        return CheckResult.safe("Không tìm thấy nhị phân su hay quyền root truyền thống.");
    }

    public static CheckResult checkTier2(Context context) {
        try {
            String detail = nativeCheckRootTier2Detail();
            if (detail != null) return CheckResult.danger(detail);
        } catch (UnsatisfiedLinkError ignored) {}

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/mountinfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("magisk") || line.contains("worker") || line.contains("core/mirror")) {
                    return CheckResult.danger("Tìm thấy mountpoint ẩn của Magisk/KernelSU: " + line.trim());
                }
            }
        } catch (Exception ignored) {}
        return CheckResult.safe("Mount namespace hệ thống sạch, SELinux Enforcing.");
    }
}
