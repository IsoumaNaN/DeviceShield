package com.deviceshield.checker.checks;

import android.view.InputDevice;
import android.view.MotionEvent;
import com.deviceshield.checker.model.CheckResult;

public class SimulatedInputCheck {
    public static CheckResult checkEvent(MotionEvent event) {
        int flags = event.getFlags();
        if ((flags & 0x01) != 0 || (flags & 0x02) != 0) {
            return CheckResult.danger("Phát hiện cờ FLAG_WINDOW_IS_OBSCURED (Màn hình có lớp phủ / Tapjacking che chắn)");
        }
        if ((flags & 0x00400000) != 0) {
            return CheckResult.danger("Phát hiện cờ FLAG_IS_GENERATED_BY_ACCESSIBILITY (Chạm được tạo tự động bởi dịch vụ trợ năng)");
        }
        if ((event.getSource() & InputDevice.SOURCE_TOUCHSCREEN) != InputDevice.SOURCE_TOUCHSCREEN) {
            return CheckResult.danger("Nguồn sự kiện chạm không phải màn hình cảm ứng vật lý (Source: " + event.getSource() + ")");
        }
        return CheckResult.safe("Cảm ứng trực tiếp từ người dùng thật trên màn hình cảm ứng.");
    }
}
