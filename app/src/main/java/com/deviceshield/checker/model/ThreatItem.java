package com.deviceshield.checker.model;

public class ThreatItem {
    private final int code;
    private final String name;
    private final String descVi;
    private final String descEn;
    private final boolean fixable;
    private final String settingsAction;
    private boolean detected;
    private String details;

    public ThreatItem(int code, String name, String descVi, String descEn, boolean fixable, String settingsAction) {
        this.code = code;
        this.name = name;
        this.descVi = descVi;
        this.descEn = descEn;
        this.fixable = fixable;
        this.settingsAction = settingsAction;
        this.detected = false;
        this.details = "";
    }

    public int getCode() { return code; }
    public String getName() { return name; }
    public String getDescVi() { return descVi; }
    public String getDescEn() { return descEn; }
    public boolean isFixable() { return fixable; }
    public String getSettingsAction() { return settingsAction; }
    public boolean isDetected() { return detected; }
    public void setDetected(boolean detected) { this.detected = detected; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
