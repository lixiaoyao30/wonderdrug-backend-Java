package org.wonder.wonderdrugs.model;

public enum SiteStatus {
    ACTIVE("active_site__v"),
    SUSPENDED("suspended_site__v"),
    COMPLETED("completed_site__v"),
    INACTIVE("inactive_site__v"),
    ON_HOLD("on_hold__v"),
    TERMINATED("terminated_site__v");

    private final String value;

    SiteStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SiteStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SiteStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
