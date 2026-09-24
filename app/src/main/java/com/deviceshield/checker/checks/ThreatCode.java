package com.deviceshield.checker.checks;

import android.provider.Settings;
import java.util.LinkedHashMap;
import java.util.Map;
import com.deviceshield.checker.model.ThreatItem;

public class ThreatCode {
    public static final int CODE_0_LICENSE = 0;
    public static final int CODE_1_REPACKAGE = 1;
    public static final int CODE_2_EMULATOR = 2;
    public static final int CODE_3_HOOKING_TIER1 = 3;
    public static final int CODE_4_DEBUGGER = 4;
    public static final int CODE_5_ROOT_TIER1 = 5;
    public static final int CODE_6_BOOTLOADER = 6;
    public static final int CODE_7_FALLBACK1 = 7;
    public static final int CODE_8_VIRTUAL_SPACE = 8;
    public static final int CODE_9_FALLBACK2 = 9;
    public static final int CODE_10_USB_DEBUG = 10;
    public static final int CODE_11_DEV_MODE = 11;
    public static final int CODE_12_CUSTOM_ROM = 12;
    public static final int CODE_13_ACCESSIBILITY = 13;
    public static final int CODE_14_UNKNOWN_SOURCE = 14;
    public static final int CODE_15_MALWARE = 15;
    public static final int CODE_16_UNTRUSTED_KEYBOARD = 16;
    public static final int CODE_17_WORK_PROFILE = 17;
    public static final int CODE_18_SIMULATED_INPUT = 18;
    public static final int CODE_19_PROXY = 19;
    public static final int CODE_20_FALLBACK3 = 20;
    public static final int CODE_21_ROOT_TIER2 = 21;
    public static final int CODE_22_HOOK_TIER2 = 22;
    public static final int CODE_23_VPN = 23;

    public static Map<Integer, ThreatItem> createDefinitions() {
        Map<Integer, ThreatItem> map = new LinkedHashMap<>();

        map.put(0, new ThreatItem(0, "License Check",
                "Chưa đăng ký giấy phép cho gói com.tmpapp",
                "Invalid or unauthorized license for the package com.tmpapp",
                false, null));

        map.put(1, new ThreatItem(1, "Repackaged App",
                "Bạn không thể chạy ứng dụng đã bị đóng gói lại. Để sử dụng được ứng dụng bạn cần tải phiên bản chính thống trên Google Play Store (Android) hoặc App Store (iOS)",
                "You cannot run the repackaged application. To use the app, you need to download the official version from the Google Play Store (Android) or the App Store (iOS).",
                false, null));

        map.put(2, new ThreatItem(2, "Emulator / VM",
                "Ứng dụng này không hỗ trợ chạy trên máy ảo và các trình giả lập",
                "This application does not support running on virtual machines and emulators.",
                false, null));

        map.put(3, new ThreatItem(3, "Hooking Tier 1",
                "Ứng dụng đang chạy trên môi trường không an toàn (hooking). Ứng dụng có thể bị các phần mềm độc hại can thiệp vào luồng thực thi, giám sát hoặc thay đổi hành vi hệ thống",
                "The application is running in an insecure environment (hooking). Unauthorized malicious software may interfere with the application's execution thread to monitor or modify system behavior.",
                false, null));

        map.put(4, new ThreatItem(4, "Debugger Active",
                "Ứng dụng không thể chạy trên các công cụ gỡ lỗi. Công cụ gỡ lỗi ứng dụng di động chỉ dùng để hỗ trợ các nhà phát triển trong việc xác định và sửa chữa các lỗi trong quá trình phát triển ứng dụng.",
                "The application cannot run on debugging tools. Mobile application debugging tools are intended only to assist developers in identifying and fixing errors during the application development process.",
                false, null));

        map.put(5, new ThreatItem(5, "Root Tier 1",
                "Ứng dụng không thể chạy trên phiên bản hệ điều hành đã bị can thiệp và chỉnh sửa (bị root đối với Android, bị jailbreak đối với iOS). Để chạy được ứng dụng, vui lòng khôi phục lại cài đặt gốc của điện thoại",
                "The application cannot run on an operating system version that has been tampered with or modified (rooted for Android, jailbroken for iOS). To run the application, please restore the factory settings of the phone.",
                false, null));

        map.put(6, new ThreatItem(6, "Bootloader Unlocked",
                "Thiết bị đã bị mở khoá trình khởi động và không còn an toàn",
                "The device's bootloader has been unlocked and is no longer secure.",
                false, null));

        map.put(7, new ThreatItem(7, "Insecure Fallback 1",
                "Thiết bị của bạn không an toàn",
                "Your device is insecure.",
                false, null));

        map.put(8, new ThreatItem(8, "Virtual Space",
                "Ứng dụng đang chạy trong môi trường ảo hoặc trong một ứng dụng khác",
                "The application is running in a virtual environment or within another application.",
                false, null));

        map.put(9, new ThreatItem(9, "Insecure Fallback 2",
                "Thiết bị của bạn không an toàn",
                "Your device is insecure.",
                false, null));

        map.put(10, new ThreatItem(10, "USB Debugging",
                "Thiết bị đang bật chế độ gỡ lỗi USB debugging. Vui lòng tắt chế độ gỡ lỗi để sử dụng ứng dụng",
                "The device is currently in USB debugging mode. Please turn off debugging mode to use the application.",
                true, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));

        map.put(11, new ThreatItem(11, "Developer Mode",
                "Thiết bị đang bật chế độ nhà phát triển. Vui lòng tắt chế độ nhà phát triển để sử dụng ứng dụng",
                "The device is currently in developer mode. Please turn off developer mode to use the application.",
                true, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));

        map.put(12, new ThreatItem(12, "Custom ROM",
                "Thiết bị đang sử dụng ROM tùy chỉnh. Vui lòng khôi phục về hệ điều hành gốc.",
                "The device is using custom ROMs. Please restore it to the original operating system.",
                false, null));

        map.put(13, new ThreatItem(13, "Accessibility Abuse",
                "Dịch vụ trợ năng đang chạy. Vui lòng tắt dịch vụ trợ năng của điện thoại để tiếp tục sử dụng.",
                "An accessibility service is running. Please turn off the active accessibility service to continue.",
                false, null));

        map.put(14, new ThreatItem(14, "Unknown Sources",
                "Ứng dụng được cài đặt từ các nguồn không rõ nguồn gốc hoặc từ một cửa hàng bên thứ ba. Vui lòng gỡ cài đặt ứng dụng.",
                "The app is installed from unknown sources or a third-party store. Please uninstall it.",
                false, null));

        map.put(15, new ThreatItem(15, "Malware Infection",
                "Thiết bị của bạn đã bị nhiễm mã độc. Các phần mềm độc hại này có thể chiếm quyền điều khiển điện thoại, chiếm đoạt tài khoản ngân hàng và thực hiện các hành vi gây hại khác.",
                "Your device has been infected with malware. Malicious software can take control of your phone, access sensitive data, and perform other harmful actions.",
                false, null));

        map.put(16, new ThreatItem(16, "Untrusted Keyboard",
                "Thiết bị hiện đang sử dụng bàn phím không có trong danh sách được tin cậy. Vui lòng thay đổi sang loại bàn phím khác.",
                "The device is currently using a keyboard that is not on the trusted list. Please switch to a different keyboard.",
                true, Settings.ACTION_INPUT_METHOD_SETTINGS));

        map.put(17, new ThreatItem(17, "Work Profile",
                "Ứng dụng đang được cài đặt trong hồ sơ công việc. Vui lòng cài đặt lại ứng dụng.",
                "The application is installed in a work profile. Please reinstall the application.",
                false, null));

        map.put(18, new ThreatItem(18, "Simulated Input",
                "Ứng dụng không cho phép nhập liệu mô phỏng vì lý do bảo mật",
                "For security reasons, the application does not allow simulated input.",
                false, null));

        map.put(19, new ThreatItem(19, "Proxy Active",
                "Thiết bị đang bật chế độ proxy. Vui lòng tắt chế độ proxy trên điện thoại",
                "A proxy is active on your device. Please disable the proxy configuration.",
                true, Settings.ACTION_WIFI_SETTINGS));

        map.put(20, new ThreatItem(20, "Insecure Fallback 3",
                "Thiết bị của bạn không an toàn",
                "Your device is insecure.",
                false, null));

        map.put(21, new ThreatItem(21, "Root Tier 2",
                "Thiết bị của bạn có vẻ đã bị bẻ khóa (root), điều này có thể ảnh hưởng đến tính bảo mật và ổn định. Vui lòng cài đặt lại phiên bản phần mềm gốc của điện thoại.",
                "Your device appears to be rooted, which can affect security and stability. Please reinstall the original software version of your phone.",
                false, null));

        map.put(22, new ThreatItem(22, "Hook Tier 2",
                "Thiết bị của bạn có vẻ đã bị can thiệp hệ thống (hook), điều này có thể ảnh hưởng đến tính bảo mật và ổn định. Vui lòng cài đặt lại phiên bản phần mềm gốc của điện thoại.",
                "Your device appears to have been hooked, which can affect security and stability. Please reinstall the original software version of your phone.",
                false, null));

        map.put(23, new ThreatItem(23, "VPN Active",
                "Thiết bị đang kết nối VPN. Vui lòng ngắt kết nối VPN và thử lại.",
                "Your device is currently connected to a VPN. Please disconnect the VPN and try again.",
                true, Settings.ACTION_VPN_SETTINGS));

        return map;
    }
}
