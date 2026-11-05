package com.skl.jammod.session;

import java.util.Objects;

public class JamTrack {
    private final String url;
    private String title;
    private long durationMs;

    public JamTrack(String url, String title, long durationMs) {
        this.url = url;
        this.title = (title != null && !title.isBlank()) ? title : url;
        this.durationMs = durationMs;
    }

    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JamTrack jamTrack = (JamTrack) o;
        return Objects.equals(url, jamTrack.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}