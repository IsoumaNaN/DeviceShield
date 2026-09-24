package com.deviceshield.checker.checks;

import android.content.Context;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.Arrays;
import java.util.List;
import com.deviceshield.checker.model.CheckResult;

public class InstallerSourceCheck {
    private static final List<String> OFFICIAL_STORES = Arrays.asList(
        "com.android.vending",
        "com.sec.android.app.samsungapps",
        "com.huawei.appmarket",
        "com.xiaomi.mipicks",
        "com.oppo.market",
        "com.vivo.appstore"
    );

    public static CheckResult check(Context context) {
        PackageManager pm = context.getPackageManager();
        String installer = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                InstallSourceInfo info = pm.getInstallSourceInfo(context.getPackageName());
                installer = info.getInstallingPackageName();
            } else {
                installer = pm.getInstallerPackageName(context.getPackageName());
            }
        } catch (Exception ignored) {}

        if (installer == null || installer.isEmpty()) {
            return CheckResult.danger("Nguồn cài đặt không xác định (null). Ứng dụng được cài thủ công qua file APK hoặc lệnh ADB.");
        }
        if (!OFFICIAL_STORES.contains(installer)) {
            return CheckResult.danger("Nguồn cài đặt bên thứ ba: '" + installer + "' (Không thuộc danh sách Google Play / Store chính thống)");
        }
        return CheckResult.safe("Cài đặt từ kho ứng dụng chính thống (" + installer + ").");
    }
}
