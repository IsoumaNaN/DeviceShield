package com.deviceshield.checker.model;

public class CheckResult {
    private final boolean detected;
    private final String evidence;

    public CheckResult(boolean detected, String evidence) {
        this.detected = detected;
        this.evidence = evidence;
    }

    public boolean isDetected() {
        return detected;
    }

    public String getEvidence() {
        return evidence;
    }

    public static CheckResult danger(String evidence) {
        return new CheckResult(true, evidence);
    }

    public static CheckResult safe(String safeMessage) {
        return new CheckResult(false, safeMessage);
    }
}
