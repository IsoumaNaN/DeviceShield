package com.deviceshield.checker.checks;

import android.content.Context;
import android.os.Debug;
import java.io.BufferedReader;
import java.io.FileReader;
import com.deviceshield.checker.model.CheckResult;

public class DebuggerCheck {
    private static native String nativeCheckDebuggerDetail();

    public static CheckResult check(Context context) {
        if (Debug.isDebuggerConnected()) {
            return CheckResult.danger("Debug.isDebuggerConnected() = true (Trình gỡ lỗi JDWP Java đang gắn vào)");
        }
        if (Debug.waitingForDebugger()) {
            return CheckResult.danger("Debug.waitingForDebugger() = true (Ứng dụng đang tạm dừng chờ Debugger)");
        }

        try {
            String detail = nativeCheckDebuggerDetail();
            if (detail != null) return CheckResult.danger(detail);
        } catch (UnsatisfiedLinkError ignored) {}

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/status"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("TracerPid:")) {
                    int pid = Integer.parseInt(line.substring(10).trim());
                    if (pid != 0) {
                        return CheckResult.danger("TracerPid = " + pid + " trong /proc/self/status (Tiến trình đang bị theo dõi)");
                    }
                }
            }
        } catch (Exception ignored) {}
        return CheckResult.safe("TracerPid = 0, ptrace TRACEME khả dụng, không phát hiện debugger.");
    }
}
