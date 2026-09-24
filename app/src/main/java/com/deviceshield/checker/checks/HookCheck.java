package com.deviceshield.checker.checks;

import android.content.Context;
import java.io.BufferedReader;
import java.io.FileReader;
import com.deviceshield.checker.model.CheckResult;

public class HookCheck {
    private static native String nativeCheckHookTier1Detail();
    private static native String nativeCheckHookTier2Detail();

    public static CheckResult checkTier1(Context context) {
        try {
            String detail = nativeCheckHookTier1Detail();
            if (detail != null) return CheckResult.danger(detail);
        } catch (UnsatisfiedLinkError ignored) {}

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/maps"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("frida-agent") || line.contains("frida-gadget")) {
                    return CheckResult.danger("Tìm thấy module Frida inject trong /proc/self/maps: " + line.trim());
                }
                if (line.contains("libxposed") || line.contains("libsubstrate")) {
                    return CheckResult.danger("Tìm thấy framework hook trong /proc/self/maps: " + line.trim());
                }
            }
        } catch (Exception ignored) {}
        return CheckResult.safe("Không phát hiện thư viện hook hay cổng debug hook.");
    }

    public static CheckResult checkTier2(Context context) {
        try {
            String detail = nativeCheckHookTier2Detail();
            if (detail != null) return CheckResult.danger(detail);
        } catch (UnsatisfiedLinkError ignored) {}
        return CheckResult.safe("Prologue và phân đoạn .text của hàm hệ thống không bị can thiệp.");
    }
}
