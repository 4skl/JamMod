package com.skl.jammod.client;

public enum OverlayCorner {
    TOP_RIGHT("Top-Right"),
    TOP_LEFT("Top-Left"),
    BOTTOM_RIGHT("Bottom-Right"),
    BOTTOM_LEFT("Bottom-Left");

    private final String displayName;

    OverlayCorner(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public OverlayCorner next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static OverlayCorner fromString(String str) {
        for (OverlayCorner c : values()) {
            if (c.name().equalsIgnoreCase(str) || c.getDisplayName().equalsIgnoreCase(str)) {
                return c;
            }
        }
        return TOP_RIGHT;
    }
}