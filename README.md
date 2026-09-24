# DeviceShield

Tool kiểm tra toàn vẹn thiết bị & tài liệu kỹ thuật về cơ chế anti-tamper / threat detection trên ứng dụng `tmpapp` (v2.2.12).

---

## Tổng quan

`tmpapp` sử dụng cơ chế bảo vệ native kết hợp direct kernel syscall và framework hook để kiểm soát môi trường chạy. Khi phát hiện thiết bị có dấu hiệu can thiệp (root, hook, debug, proxy, bàn phím lạ...), ứng dụng sẽ kích hoạt Activity cảnh báo (`:xcmzqsawpqoefcds`) và trả về một trong **24 mã lỗi (Code 0 – 23)**.

Các mã lỗi được chia thành 2 nhóm xử lý:
* **Settings Fixable (10, 11, 16, 19, 23):** Cho phép người dùng bấm nút **"Mở cài đặt"** (`#007AFF`) để tắt tính năng vi phạm.
* **Hard Lockout (Các mã còn lại):** Khóa ứng dụng, chặn tương tác hoàn toàn.

Repo này gồm 2 phần:
1. **Bảng phân tích kỹ thuật 24 mã lỗi:** Chỉ rõ từng file, syscall, cờ hệ thống và API bị lộ.
2. **Source code Android (`DeviceShield`):** App kiểm tra thực tế, mô phỏng đúng 100% logic check và UI cảnh báo gốc.

---

## Bảng tra cứu 24 Mã Lỗi (Threat Detection Matrix)

| Code | Mối đe dọa | Nhóm | Dấu vết / Bằng chứng bị lộ | Cơ chế kiểm tra kỹ thuật |
| :---: | :--- | :---: | :--- | :--- |
| **0** | License Check | Lockout | Package name không khớp `com.tmpapp` hoặc token license trong assets hết hạn/sai chữ ký. | JNI `getPackageName()`, verify RSA token cấu hình trong assets. |
| **1** | Repackaged App | Lockout | Hash SHA-256 cert không khớp bản phát hành; hoặc APK Signing Block bị can thiệp. | Tự mở file APK qua `openat`, parse APK Signing Block v2/v3, băm SHA-256 từng chunk 4KB. |
| **2** | Emulator / VM | Lockout | Tồn tại `/dev/qemu_pipe`, `/dev/socket/qemud`; CPU x86/Goldfish; `ro.kernel.qemu=1`. | Syscall `faccessat(48)`, đọc `/proc/cpuinfo`, đọc prop `ro.hardware`, `ro.kernel.qemu`. |
| **3** | Hooking Tier 1 | Lockout | Thư viện `frida-agent.so`, `libxposed.so` trong bộ nhớ; hoặc cổng `27042` đang mở. | Quét `/proc/self/maps` tìm module hook; quét `/proc/net/tcp` tìm port Frida server. |
| **4** | Debugger Active | Lockout | `TracerPid != 0` trong status; ngắt `ptrace` trả về `-1`; hoặc cờ Java `isDebuggerConnected`. | Direct syscall `ptrace(PTRACE_TRACEME, 117)`, đọc `/proc/self/status`, gọi `android.os.Debug`. |
| **5** | Root Tier 1 | Lockout | Tồn tại binary `su` (`/system/bin/su`, `/data/local/su`); system mount `rw`; cờ `test-keys`. | Direct syscall `faccessat`, đọc `/proc/mounts`, kiểm tra `ro.build.tags`. |
| **6** | Bootloader Unlocked | Lockout | `ro.boot.verifiedbootstate=orange`; `ro.boot.flash.locked=0`; attestation có `deviceLocked=false`. | Sinh key trong Android KeyStore, verify chuỗi ASN.1 Key Attestation OID `1.3.6.1.4.1.11129.2.1.17`. |
| **7** | Fallback 1 | Lockout | Lỗi kiểm tra tính toàn vẹn mức runtime native. | Default handler khi integrity check trả về mã lỗi không xác định. |
| **8** | Virtual Space | Lockout | Thư mục `filesDir` khác tiền tố `/data/user/0/com.tmpapp` (Dual Space, VirtualApp, Parallel Space). | Kiểm tra `context.getFilesDir()`, kiểm tra `/proc/self/cmdline`. |
| **9** | Fallback 2 | Lockout | Lỗi kiểm tra tính toàn vẹn mức runtime native. | Default handler khi integrity check trả về mã lỗi không xác định. |
| **10** | USB Debugging | **Fixable** | `Settings.Global.ADB_ENABLED = 1` hoặc daemon `init.svc.adbd = running`. | `Settings.Global.getInt(cr, ADB_ENABLED, 0) == 1`. |
| **11** | Developer Mode | **Fixable** | `Settings.Global.DEVELOPMENT_SETTINGS_ENABLED = 1`. | `Settings.Global.getInt(cr, DEVELOPMENT_SETTINGS_ENABLED, 0) == 1`. |
| **12** | Custom ROM | Lockout | Biến `ro.lineage.version`, `ro.modversion`; build `userdebug`; tồn tại thư mục `/system/addon.d`. | `__system_property_get`, kiểm tra `/system/addon.d`. |
| **13** | Accessibility Abuse | Lockout | Có Accessibility Service của bên thứ 3 đang chạy ngoài whitelist (TalkBack, Samsung). | `AccessibilityManager.getEnabledAccessibilityServiceList()`, gắn delegate chặn cào view. |
| **14** | Unknown Sources | Lockout | Nguồn cài đặt là `null` hoặc `com.android.packageinstaller` (cài APK thủ công / qua ADB). | `PackageManager.getInstallSourceInfo()` hoặc `getInstallerPackageName()`. |
| **15** | Malware Infection | Lockout | Thiết bị cài đặt các package trojan ngân hàng trong blacklist IOC (GoldPickaxe, FakeID...). | Quét danh sách package cài đặt đối chiếu danh mục chữ ký mã độc. |
| **16** | Untrusted Keyboard | **Fixable** | Bàn phím mặc định ngoài whitelist (chỉ chấp nhận Gboard, Laban Key, bàn phím OEM gốc). | Đọc `Settings.Secure.DEFAULT_INPUT_METHOD`. |
| **17** | Work Profile | Lockout | User ID của app $\ge 10$ hoặc thuộc quản lý của `DevicePolicyManager` (MDM). | Đọc `UserHandle.getIdentifier()`, gọi `dpm.isProfileOwnerApp()`. |
| **18** | Simulated Input | Lockout | MotionEvent mang cờ `FLAG_WINDOW_IS_OBSCURED` (tapjacking); hoặc nguồn không phải touchscreen. | Hook `dispatchTouchEvent`: kiểm tra `event.getFlags()`, `event.getSource()`. |
| **19** | Proxy Active | **Fixable** | Có giá trị `http.proxyHost`; hoặc `LinkProperties.getHttpProxy()` trên kết nối Wi-Fi. | Đọc `System.getProperty("http.proxyHost")`, đọc `LinkProperties`. |
| **20** | Fallback 3 | Lockout | Lỗi kiểm tra tính toàn vẹn mức runtime native. | Default handler khi integrity check trả về mã lỗi không xác định. |
| **21** | Root Tier 2 (Deep) | Lockout | Mount point ẩn Magisk/KernelSU (`core/mirror`, `worker`); mapping `(deleted) r-xp`; SELinux permissive. | Direct syscall đọc `/proc/self/mountinfo`, `/proc/self/maps`, đọc `/sys/fs/selinux/enforce`. |
| **22** | Hook Tier 2 (Deep) | Lockout | 4 byte đầu của hàm `openat` trong libc bị đè lệnh nhảy ARM64 (`LDR X16` / `BR X16` / `B`); memcmp text lệch. | Inline hook detection: đọc opcode prologue của hàm libc trong bộ nhớ, memcmp đoạn `.text`. |
| **23** | VPN Active | **Fixable** | Mạng mang cờ `TRANSPORT_VPN`; hoặc interface ảo `tun0`, `ppp0`, `p2p0` đang UP. | `NetworkCapabilities.hasTransport(TRANSPORT_VPN)`, quét `NetworkInterface.getNetworkInterfaces()`. |

---

## Chi tiết kỹ thuật nổi bật

### 1. Direct Kernel Syscall (ARM64)
Để chống lại việc Frida/Substrate hook các hàm libc cơ bản (`open`, `read`, `ptrace`), module native gọi kernel trực tiếp:

```arm64
// Direct Syscall Stub (libnzxkjmfw.so)
sub_52D9A0:
    MOV         X8, X0          // Mã syscall
    MOV         X0, X1          // arg0
    MOV         X1, X2          // arg1
    MOV         X2, X3          // arg2
    MOV         X3, X4          // arg3
    SVC         #0              // Kernel interrupt
    RET
```

### 2. Kiểm tra chữ ký APK v2/v3 bằng Raw I/O
Không dùng `PackageManager.getPackageInfo()` (dễ bị hook), app tự đọc file APK qua syscall `openat`:
1. Nhảy tới cuối file tìm End of Central Directory (EOCD).
2. Lùi lại tìm khối APK Signing Block (magic header `APK Sig Block 42`).
3. Băm SHA-256 các chunk 4KB của APK và đối chiếu chứng chỉ x509 với cert chuẩn.

### 3. Deep Root Detection (KernelSU / Magisk / APatch)
Không chỉ tìm file `su`, app đọc `/proc/self/mountinfo` để phát hiện mount namespace bị can thiệp:
* Dấu vết `tmpfs` đè lên `/system` hoặc `/sbin/.magisk/mirror`.
* Dấu vết overlayfs `worker` của KernelSU.
* Quét `/proc/self/maps` tìm file thực thi đã bị unmount/xóa (`(deleted) r-xp`).

### 4. Inline Hook Detection
Module native đọc 4 byte đầu tiên của các hàm nhạy cảm (`open`, `openat`, `ptrace`):
* Nếu opcode bắt đầu bằng `0x58000050` (`LDR X16, #8`) hoặc lệnh branch `0x14xxxxxx` (`B <offset>`) $\rightarrow$ Xác định hàm đã bị hook trampoline.

---

## Ứng dụng mẫu DeviceShield

Thư mục `DeviceShield/` chứa toàn bộ source code Android Studio:
* **`app/src/main/cpp/native-lib.cpp`:** Triển khai direct syscall, quét procfs, check inline hook.
* **`app/src/main/java/.../checks/`:** Module hóa từng check từ Code 0 đến 23.
* **`MainActivity.java`:** Dashboard quét thiết bị và hiển thị chi tiết lý do/thông số bị lộ.
* **`ThreatAlertActivity.java`:** Tái tạo chuẩn giao diện cảnh báo của app gốc (Activity độc lập, bảng lỗi 2 cột, nút bấm Settings xanh `#007AFF`).

### Build & Run
```bash
cd DeviceShield
./gradlew assembleDebug
```
File APK xuất ra tại: `app/build/outputs/apk/debug/app-debug.apk`.
