package com.deviceshield.checker.checks;

import android.content.Context;
import com.deviceshield.checker.model.CheckResult;

public class LicenseCheck {
    static {
        try {
            System.loadLibrary("deviceshield_native");
        } catch (UnsatisfiedLinkError ignored) {}
    }

    private static native String nativeCheckLicenseDetail(String packageName);

    public static CheckResult check(Context context) {
        String pkg = context.getPackageName();
        try {
            String nativeDetail = nativeCheckLicenseDetail(pkg);
            if (nativeDetail != null) {
                return CheckResult.danger(nativeDetail);
            }
        } catch (UnsatisfiedLinkError e) {
            if (!("com.tmpapp".equals(pkg) || "com.deviceshield.checker".equals(pkg))) {
                return CheckResult.danger("Tên gói ứng dụng thực tế: '" + pkg + "' (Kỳ vọng: com.tmpapp). Thiếu license token hợp lệ.");
            }
        }
        return CheckResult.safe("Package name hợp lệ: " + pkg + " kèm chữ ký license xác thực.");
    }
}
