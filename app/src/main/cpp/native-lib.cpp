#include <jni.h>
#include <string>
#include <vector>
#include <fcntl.h>
#include <unistd.h>
#include <dlfcn.h>
#include <cstdio>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/ptrace.h>
#include <sys/system_properties.h>
#include <android/log.h>
#include <cstring>
#include <cstdlib>

#define TAG "DeviceShield-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

#if defined(__aarch64__)
extern "C" long raw_syscall(long num, long a0, long a1, long a2, long a3) {
    register long x8 __asm__("x8") = num;
    register long x0 __asm__("x0") = a0;
    register long x1 __asm__("x1") = a1;
    register long x2 __asm__("x2") = a2;
    register long x3 __asm__("x3") = a3;
    __asm__ __volatile__(
        "svc #0"
        : "+r"(x0)
        : "r"(x8), "r"(x1), "r"(x2), "r"(x3)
        : "memory"
    );
    return x0;
}
#else
extern "C" long raw_syscall(long num, long a0, long a1, long a2, long a3) {
    return syscall(num, a0, a1, a2, a3);
}
#endif

static ssize_t safe_read_line(int fd, char *buf, size_t max_len) {
    size_t i = 0;
    char c = 0;
    while (i < max_len - 1) {
        ssize_t ret = read(fd, &c, 1);
        if (ret <= 0) break;
        buf[i++] = c;
        if (c == '\n') break;
    }
    buf[i] = '\0';
    return i;
}

// [Code 0] License Check
extern "C" JNIEXPORT jstring JNICALL
Java_com_deviceshield_checker_checks_LicenseCheck_nativeCheckLicenseDetail(
        JNIEnv *env, jobject /* this */, jstring packageName) {
    if (!packageName) return env->NewStringUTF("Lỗi: Không tìm thấy thông tin Package Name");
    const char *pkg = env->GetStringUTFChars(packageName, nullptr);
    std::string result = "";
    if (strcmp(pkg, "com.tmpapp") != 0 && strcmp(pkg, "com.deviceshield.checker") != 0) {
        result = std::string("Package name không hợp lệ: '") + pkg + "' (Kỳ vọng: com.tmpapp). Không có license hợp lệ từ hệ thống.";
    }
    env->ReleaseStringUTFChars(packageName, pkg);
    if (!result.empty()) return env->NewStringUTF(result.c_str());
    return nullptr;
}

// [Code 2] Emulator Check
extern "C" JNIEXPORT jstring JNICALL
Java_com_deviceshield_checker_checks_EmulatorCheck_nativeCheckEmulatorDetail(
        JNIEnv *env, jobject /* this */) {
    const char *emu_pipes[] = {
        "/dev/socket/qemud", "/dev/qemu_pipe",
        "/dev/goldfish_pipe", "/dev/vboxuser", "/dev/vboxguest", nullptr
    };
    for (int i = 0; emu_pipes[i]; i++) {
        if (access(emu_pipes[i], F_OK) == 0) {
            std::string res = std::string("Phát hiện driver ảo hóa / pipe giả lập: ") + emu_pipes[i];
            return env->NewStringUTF(res.c_str());
        }
    }

    int fd = open("/proc/cpuinfo", O_RDONLY);
    if (fd >= 0) {
        char buf[2048];
        ssize_t n = read(fd, buf, sizeof(buf) - 1);
        close(fd);
        if (n > 0) {
            buf[n] = '\0';
            if (strstr(buf, "Goldfish")) return env->NewStringUTF("Phát hiện CPU Goldfish giả lập trong /proc/cpuinfo");
            if (strstr(buf, "intel") || strstr(buf, "amd")) return env->NewStringUTF("Phát hiện kiến trúc vi xử lý x86/x86_64 chạy giả lập trong /proc/cpuinfo");
        }
    }

    char prop[PROP_VALUE_MAX];
    if (__system_property_get("ro.kernel.qemu", prop) > 0 && strcmp(prop, "1") == 0) {
        return env->NewStringUTF("System property 'ro.kernel.qemu = 1' (Kernel QEMU đang chạy)");
    }
    if (__system_property_get("ro.hardware", prop) > 0) {
        if (strstr(prop, "goldfish") || strstr(prop, "ranchu") || strstr(prop, "vbox")) {
            std::string res = std::string("Phát hiện phần cứng ảo 'ro.hardware = ") + prop + "'";
            return env->NewStringUTF(res.c_str());
        }
    }
    return nullptr;
}

// [Code 3] Hooking Tier 1 Check
extern "C" JNIEXPORT jstring JNICALL
Java_com_deviceshield_checker_checks_HookCheck_nativeCheckHookTier1Detail(
        JNIEnv *env, jobject /* this */) {
    int fd = open("/proc/self/maps", O_RDONLY);
    if (fd >= 0) {
        char line[512];
        while (safe_read_line(fd, line, sizeof(line)) > 0) {
            const char* suspicious[] = {
                "frida-agent", "frida-gadget", "libxposed", 
                "libsubstrate", "libsandhook", "libepic", "libpine", nullptr
            };
            for (int i = 0; suspicious[i]; i++) {
                if (strstr(line, suspicious[i])) {
                    close(fd);
                    std::string res = std::string("Phát hiện module hook trong /proc/self/maps: ") + suspicious[i];
                    return env->NewStringUTF(res.c_str());
                }
            }
        }
        close(fd);
    }

    int net_fd = open("/proc/net/tcp", O_RDONLY);
    if (net_fd >= 0) {
        char line[256];
        while (safe_read_line(net_fd, line, sizeof(line)) > 0) {
            if (strstr(line, ":69A2") || strstr(line, ":69A3")) {
                close(net_fd);
                return env->NewStringUTF("Phát hiện cổng kết nối Frida Server (27042 / 27043) đang lắng nghe trong /proc/net/tcp");
            }
        }
        close(net_fd);
    }
    return nullptr;
}

// [Code 4] Debugger Check
extern "C" JNIEXPORT jstring JNICALL
Java_com_deviceshield_checker_checks_DebuggerCheck_nativeCheckDebuggerDetail(
        JNIEnv *env, jobject /* this */) {
    long ret = raw_syscall(117 /* __NR_ptrace on ARM64 */, 0 /* PTRACE_TRACEME */, 0, 1, 0);
    if (ret < 0) {
        return env->NewStringUTF("Ngắt ptrace(PTRACE_TRACEME) thất bại (-1 EPERM). Đã có tiến trình debugger khác (GDB/LLDB/IDA) gắn kèm.");
    }

    int fd = open("/proc/self/status", O_RDONLY);
    if (fd >= 0) {
        char buf[1024];
        ssize_t n = read(fd, buf, sizeof(buf) - 1);
        close(fd);
        if (n > 0) {
            buf[n] = '\0';
            char *tracer = strstr(buf, "TracerPid:");
            if (tracer) {
                int pid = atoi(tracer + 10);
                if (pid != 0) {
                    std::string res = std::string("Phát hiện TracerPid = ") + std::to_string(pid) + " trong /proc/self/status (Tiến trình đang bị theo dõi)";
                    return env->NewStringUTF(res.c_str());
                }
            }
        }
    }
    return nullptr;
}

// [Code 5] Root Tier 1 Check
extern "C" JNIEXPORT jstring JNICALL
Java_com_deviceshield_checker_checks_RootCheck_nativeCheckRootTier1Detail(
        JNIEnv *env, jobject /* this */) {
    const char *su_paths[] = {
        "/system/bin/su", "/system/xbin/su", "/sbin/su",
        "/system/sd/xbin/su", "/system/bin/failsafe/su",
        "/data/local/xbin/su", "/data/local/bin/su", "/data/local/su",
        "/system/app/Superuser.apk", nullptr
    };
    for (int i = 0; su_paths[i]; i++) {
        if (access(su_paths[i], F_OK) == 0) {
            std::string res = std::string("Phát hiện file nhị phân cấp quyền root: ") + su_paths[i];
            return env->NewStringUTF(res.c_str());
        }
    }

    char tags[PROP_VALUE_MAX];
    if (__system_property_get("ro.build.tags", tags) > 0 && strstr(tags, "test-keys")) {
        return env->NewStringUTF("Bản dựng hệ thống sử dụng cờ test-keys ('ro.build.tags = test-keys') thay vì release-keys");
    }
    return nullptr;
}

// [Code 12] Custom ROM Check
extern "C" JNIEXPORT jstring JNICALL
Java_com_deviceshield_checker_checks_CustomRomCheck_nativeCheckCustomRomDetail(
        JNIEnv *env, jobject /* this */) {
    const char *rom_props[] = {
        "ro.modversion", "ro.lineage.version", "ro.cm.version",
        "ro.carbon.version", "ro.resurrection.version", "ro.crdroid.version", nullptr
    };
    char prop[PROP_VALUE_MAX];
    for (int i = 0; rom_props[i]; i++) {
        if (__system_property_get(rom_props[i], prop) > 0) {
            std::string res = std::string("Phát hiện thuộc tính Custom ROM: '") + rom_props[i] + " = " + prop + "'";
            return env->NewStringUTF(res.c_str());
        }
    }

    if (__system_property_get("ro.build.type", prop) > 0) {
        if (strcmp(prop, "userdebug") == 0 || strcmp(prop, "eng") == 0) {
            std::string res = std::string("Kiểu bản dựng ROM thử nghiệm 'ro.build.type = ") + prop + "'";
            return env->NewStringUTF(res.c_str());
        }
    }

    if (access("/system/addon.d", F_OK) == 0) {
        return env->NewStringUTF("Tồn tại thư mục kịch bản OTA của Custom ROM: /system/addon.d");
    }
    return nullptr;
}

// [Code 21] Root Tier 2 (Deep Root / Magisk / KernelSU)
extern "C" JNIEXPORT jstring JNICALL
Java_com_deviceshield_checker_checks_RootCheck_nativeCheckRootTier2Detail(
        JNIEnv *env, jobject /* this */) {
    int fd = open("/proc/self/mountinfo", O_RDONLY);
    if (fd >= 0) {
        char buf[4096];
        ssize_t n;
        while ((n = read(fd, buf, sizeof(buf) - 1)) > 0) {
            buf[n] = '\0';
            const char* suspicious[] = { "magisk", "worker", "/debug_ramdisk", "core/mirror", "core/img", nullptr };
            for (int i = 0; suspicious[i]; i++) {
                if (strstr(buf, suspicious[i])) {
                    close(fd);
                    std::string res = std::string("Phát hiện mount point ẩn của Magisk/KernelSU: ") + suspicious[i] + " trong /proc/self/mountinfo";
                    return env->NewStringUTF(res.c_str());
                }
            }
        }
        close(fd);
    }

    int maps_fd = open("/proc/self/maps", O_RDONLY);
    if (maps_fd >= 0) {
        char line[512];
        while (safe_read_line(maps_fd, line, sizeof(line)) > 0) {
            if (strstr(line, " (deleted)") && strstr(line, "r-xp")) {
                close(maps_fd);
                return env->NewStringUTF("Phát hiện file nhị phân ẩn đã bị unlinked nhưng vẫn giữ mapping thực thi: (deleted) r-xp trong /proc/self/maps");
            }
        }
        close(maps_fd);
    }

    int se_fd = open("/sys/fs/selinux/enforce", O_RDONLY);
    if (se_fd >= 0) {
        char val = 0;
        read(se_fd, &val, 1);
        close(se_fd);
        if (val == '0') return env->NewStringUTF("SELinux đang ở chế độ Permissive (/sys/fs/selinux/enforce = 0), cho phép leo quyền hệ thống");
    }
    return nullptr;
}

// [Code 22] Hook Tier 2 (Inline Hook / Memory Tampering)
extern "C" JNIEXPORT jstring JNICALL
Java_com_deviceshield_checker_checks_HookCheck_nativeCheckHookTier2Detail(
        JNIEnv *env, jobject /* this */) {
    void *func = dlsym(RTLD_DEFAULT, "openat");
    if (!func) {
        func = dlsym(RTLD_DEFAULT, "open");
    }

#if defined(__aarch64__)
    if (func) {
        uint32_t first_insn = *(uint32_t *)func;
        if (first_insn == 0x58000050 || (first_insn & 0xFC000000) == 0x14000000) {
            char hex_buf[64];
            snprintf(hex_buf, sizeof(hex_buf), "0x%08X", first_insn);
            std::string res = std::string("Phát hiện lệnh nhảy Inline Hook (") + hex_buf + ") đè lên prologue của hàm libc open/openat";
            return env->NewStringUTF(res.c_str());
        }
    }
#endif
    return nullptr;
}
