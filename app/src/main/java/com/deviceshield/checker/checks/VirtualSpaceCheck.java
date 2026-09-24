package com.deviceshield.checker.checks;

import android.content.Context;
import com.deviceshield.checker.model.CheckResult;

public class VirtualSpaceCheck {
    public static CheckResult check(Context context) {
        String dataPath = context.getFilesDir().getAbsolutePath();
        String expected1 = "/data/data/" + context.getPackageName();
        String expected2 = "/data/user/0/" + context.getPackageName();
        if (!dataPath.startsWith(expected1) && !dataPath.startsWith(expected2)) {
            return CheckResult.danger("Đường dẫn dữ liệu bất thường: '" + dataPath + "' (Ứng dụng đang chạy trong container/môi trường nhân bản)");
        }
        return CheckResult.safe("Đường dẫn lưu trữ dữ liệu chuẩn của hệ thống (" + dataPath + ").");
    }
}
