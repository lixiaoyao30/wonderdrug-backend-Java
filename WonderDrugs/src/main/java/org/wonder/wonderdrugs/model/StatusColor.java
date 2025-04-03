package org.wonder.wonderdrugs.model;

public class StatusColor {
    private String name;
    private String siteStatus;
    private String statusColor;

    // 构造函数
    public StatusColor() {
    }

    public StatusColor(String name, String siteStatus, String statusColor) {
        this.name = name;
        this.siteStatus = siteStatus;
        this.statusColor = statusColor;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSiteStatus() {
        return siteStatus;
    }

    public void setSiteStatus(String siteStatus) {
        this.siteStatus = siteStatus;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
}
