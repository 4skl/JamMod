package com.skl.jammod.session;

public enum JamEditPolicy {
    OWNER_ONLY("Owner Only"),
    COLLABORATORS("Collaborators"),
    OPEN("Open to All");

    private final String displayName;

    JamEditPolicy(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public JamEditPolicy next() {
        JamEditPolicy[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}