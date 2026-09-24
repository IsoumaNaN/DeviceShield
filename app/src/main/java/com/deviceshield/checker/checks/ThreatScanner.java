package com.deviceshield.checker.checks;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.deviceshield.checker.model.CheckResult;
import com.deviceshield.checker.model.ThreatItem;

public class ThreatScanner {
    public interface ScanCallback {
        void onProgress(int completed, int total, String checkName);
        void onComplete(List<ThreatItem> results, List<Integer> detectedCodes);
    }

    public static void scanAsync(Context context, ScanCallback callback) {
        new Thread(() -> {
            Map<Integer, ThreatItem> items = ThreatCode.createDefinitions();
            List<Integer> detectedCodes = new ArrayList<>();
            int total = items.size();
            int current = 0;

            for (Map.Entry<Integer, ThreatItem> entry : items.entrySet()) {
                int code = entry.getKey();
                ThreatItem item = entry.getValue();
                current++;

                if (callback != null) {
                    callback.onProgress(current, total, item.getName());
                }

                CheckResult result;
                switch (code) {
                    case ThreatCode.CODE_0_LICENSE:
                        result = LicenseCheck.check(context);
                        break;
                    case ThreatCode.CODE_1_REPACKAGE:
                        result = RepackageCheck.check(context);
                        break;
                    case ThreatCode.CODE_2_EMULATOR:
                        result = EmulatorCheck.check(context);
                        break;
                    case ThreatCode.CODE_3_HOOKING_TIER1:
                        result = HookCheck.checkTier1(context);
                        break;
                    case ThreatCode.CODE_4_DEBUGGER:
                        result = DebuggerCheck.check(context);
                        break;
                    case ThreatCode.CODE_5_ROOT_TIER1:
                        result = RootCheck.checkTier1(context);
                        break;
                    case ThreatCode.CODE_6_BOOTLOADER:
                        result = BootloaderCheck.check(context);
                        break;
                    case ThreatCode.CODE_8_VIRTUAL_SPACE:
                        result = VirtualSpaceCheck.check(context);
                        break;
                    case ThreatCode.CODE_10_USB_DEBUG:
                        result = UsbDebugCheck.check(context);
                        break;
                    case ThreatCode.CODE_11_DEV_MODE:
                        result = DevModeCheck.check(context);
                        break;
                    case ThreatCode.CODE_12_CUSTOM_ROM:
                        result = CustomRomCheck.check(context);
                        break;
                    case ThreatCode.CODE_13_ACCESSIBILITY:
                        result = AccessibilityCheck.check(context);
                        break;
                    case ThreatCode.CODE_14_UNKNOWN_SOURCE:
                        result = InstallerSourceCheck.check(context);
                        break;
                    case ThreatCode.CODE_15_MALWARE:
                        result = MalwareCheck.check(context);
                        break;
                    case ThreatCode.CODE_16_UNTRUSTED_KEYBOARD:
                        result = KeyboardCheck.check(context);
                        break;
                    case ThreatCode.CODE_17_WORK_PROFILE:
                        result = WorkProfileCheck.check(context);
                        break;
                    case ThreatCode.CODE_18_SIMULATED_INPUT:
                        result = CheckResult.safe("Bộ lọc MotionEvent bảo vệ thao tác cảm ứng theo thời gian thực.");
                        break;
                    case ThreatCode.CODE_19_PROXY:
                        result = ProxyCheck.check(context);
                        break;
                    case ThreatCode.CODE_21_ROOT_TIER2:
                        result = RootCheck.checkTier2(context);
                        break;
                    case ThreatCode.CODE_22_HOOK_TIER2:
                        result = HookCheck.checkTier2(context);
                        break;
                    case ThreatCode.CODE_23_VPN:
                        result = VpnCheck.check(context);
                        break;
                    default:
                        result = CheckResult.safe("Kiểm tra tính toàn vẹn hệ thống.");
                        break;
                }

                item.setDetected(result.isDetected());
                item.setDetails(result.getEvidence());

                if (result.isDetected()) {
                    detectedCodes.add(code);
                }

                try {
                    Thread.sleep(25);
                } catch (InterruptedException ignored) {}
            }

            if (callback != null) {
                callback.onComplete(new ArrayList<>(items.values()), detectedCodes);
            }
        }).start();
    }
}
