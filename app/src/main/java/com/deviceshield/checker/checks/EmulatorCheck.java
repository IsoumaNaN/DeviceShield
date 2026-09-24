package com.deviceshield.checker.checks;

import android.content.Context;
import android.os.Build;
import java.io.File;
import com.deviceshield.checker.model.CheckResult;

public class EmulatorCheck {
    private static native String nativeCheckEmulatorDetail();

    public static CheckResult check(Context context) {
        try {
            String nativeDetail = nativeCheckEmulatorDetail();
            if (nativeDetail != null) {
                return CheckResult.danger(nativeDetail);
            }
        } catch (UnsatisfiedLinkError ignored) {}

        if (new File("/dev/qemu_pipe").exists()) {
            return CheckResult.danger("Tồn tại driver giao tiếp ảo hóa: /dev/qemu_pipe");
        }
        if (new File("/dev/socket/qemud").exists()) {
            return CheckResult.danger("Tồn tại socket daemon máy ảo: /dev/socket/qemud");
        }
        if (Build.HARDWARE.contains("goldfish") || Build.HARDWARE.contains("ranchu")) {
            return CheckResult.danger("Phần cứng giả lập: Build.HARDWARE = " + Build.HARDWARE);
        }
        if (Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator")) {
            return CheckResult.danger("Model thiết bị máy ảo: Build.MODEL = " + Build.MODEL);
        }
        if (Build.MANUFACTURER.contains("Genymotion")) {
            return CheckResult.danger("Nhà sản xuất máy ảo: Build.MANUFACTURER = Genymotion");
        }
        return CheckResult.safe("Thiết bị phần cứng vật lý ARM nguyên bản.");
    }
}
