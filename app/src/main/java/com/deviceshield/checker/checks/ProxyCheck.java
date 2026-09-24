package com.deviceshield.checker.checks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.provider.Settings;
import com.deviceshield.checker.model.CheckResult;

public class ProxyCheck {
    public static CheckResult check(Context context) {
        String host = System.getProperty("http.proxyHost");
        String port = System.getProperty("http.proxyPort");
        if (host != null && !host.isEmpty()) {
            return CheckResult.danger("Phát hiện System Property: 'http.proxyHost = " + host + ":" + port + "'");
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network active = cm.getActiveNetwork();
            if (active != null) {
                LinkProperties lp = cm.getLinkProperties(active);
                if (lp != null && lp.getHttpProxy() != null) {
                    return CheckResult.danger("Phát hiện HTTP Proxy trên kết nối Wi-Fi: " + lp.getHttpProxy().getHost() + ":" + lp.getHttpProxy().getPort());
                }
            }
        }

        try {
            String globalProxy = Settings.Global.getString(context.getContentResolver(), Settings.Global.HTTP_PROXY);
            if (globalProxy != null && !globalProxy.isEmpty()) {
                return CheckResult.danger("Cài đặt hệ thống: 'Settings.Global.HTTP_PROXY = " + globalProxy + "'");
            }
        } catch (Exception ignored) {}

        return CheckResult.safe("Không có cấu hình proxy trung gian.");
    }
}
