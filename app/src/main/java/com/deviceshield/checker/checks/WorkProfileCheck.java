package com.deviceshield.checker.checks;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.UserHandle;
import java.lang.reflect.Method;
import com.deviceshield.checker.model.CheckResult;

public class WorkProfileCheck {
    public static CheckResult check(Context context) {
        try {
            UserHandle userHandle = android.os.Process.myUserHandle();
            Method getIdentifier = UserHandle.class.getMethod("getIdentifier");
            int userId = (int) getIdentifier.invoke(userHandle);
            if (userId >= 10) {
                return CheckResult.danger("Ứng dụng chạy trong User Profile ID = " + userId + " (Không gian Work Profile doanh nghiệp)");
            }
        } catch (Exception ignored) {}

        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm != null && dpm.isProfileOwnerApp(context.getPackageName())) {
            return CheckResult.danger("Ứng dụng bị quản lý bởi Device Policy Manager (MDM)");
        }
        return CheckResult.safe("Chạy trên Personal User Profile chính của thiết bị (User ID 0).");
    }
}
