package com.deviceshield.checker.checks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import java.net.NetworkInterface;
import java.util.Enumeration;
import com.deviceshield.checker.model.CheckResult;

public class VpnCheck {
    public static CheckResult check(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network active = cm.getActiveNetwork();
            if (active != null) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(active);
                if (caps != null) {
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        return CheckResult.danger("NetworkCapabilities mang cờ TRANSPORT_VPN (Kết nối định tuyến qua VPN)");
                    }
                    if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                        return CheckResult.danger("Thiếu cờ NET_CAPABILITY_NOT_VPN trên mạng kết nối");
                    }
                }
            }
        }

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp()) {
                    String name = ni.getName().toLowerCase();
                    if (name.contains("tun") || name.contains("ppp") || name.contains("p2p") || name.contains("tap")) {
                        return CheckResult.danger("Phát hiện Network Interface ảo VPN đang UP: '" + ni.getName() + "'");
                    }
                }
            }
        } catch (Exception ignored) {}

        return CheckResult.safe("Kết nối mạng trực tiếp, không sử dụng đường hầm VPN.");
    }
}
