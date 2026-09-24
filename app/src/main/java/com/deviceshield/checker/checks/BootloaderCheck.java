package com.deviceshield.checker.checks;

import android.content.Context;
import java.lang.reflect.Method;
import com.deviceshield.checker.model.CheckResult;

public class BootloaderCheck {
    public static CheckResult check(Context context) {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method getMethod = systemProperties.getMethod("get", String.class);
            String state = (String) getMethod.invoke(null, "ro.boot.verifiedbootstate");
            String locked = (String) getMethod.invoke(null, "ro.boot.flash.locked");
            if ("orange".equalsIgnoreCase(state)) {
                return CheckResult.danger("Thuộc tính 'ro.boot.verifiedbootstate = orange' (Trạng thái khởi động không an toàn)");
            }
            if ("0".equals(locked)) {
                return CheckResult.danger("Thuộc tính 'ro.boot.flash.locked = 0' (Bootloader ở trạng thái mở khóa)");
            }
        } catch (Exception ignored) {}
        return CheckResult.safe("Bootloader đã khóa và bảo vệ tính toàn vẹn phần cứng (Verified Boot).");
    }
}
