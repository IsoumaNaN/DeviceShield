package com.deviceshield.checker.checks;

import android.content.Context;
import android.provider.Settings;
import java.util.Arrays;
import java.util.List;
import com.deviceshield.checker.model.CheckResult;

public class KeyboardCheck {
    private static final List<String> TRUSTED_KEYBOARDS = Arrays.asList(
        "com.google.android.inputmethod.latin",
        "com.vng.inputmethod.labankey",
        "com.samsung.android.honeyboard",
        "com.huawei.skytone"
    );

    public static CheckResult check(Context context) {
        String defaultIme = Settings.Secure.getString(
            context.getContentResolver(), 
            Settings.Secure.DEFAULT_INPUT_METHOD
        );
        if (defaultIme == null) {
            return CheckResult.safe("Không thể xác định IME mặc định.");
        }

        for (String trusted : TRUSTED_KEYBOARDS) {
            if (defaultIme.contains(trusted)) {
                return CheckResult.safe("Bàn phím tin cậy: " + defaultIme);
            }
        }
        return CheckResult.danger("Bàn phím không thuộc danh sách tin cậy: '" + defaultIme + "' (Nguy cơ Keylogger)");
    }
}
