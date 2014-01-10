package com.opentok.api;

import java.util.UUID;

public class Archive {
    public enum ArchiveState {
        available, deleted, failed, started, stopped, uploaded, unknown
    }

    private long createdAt = System.currentTimeMillis();
    private int duration = 0;
    private UUID id;
    private String name;
    private int partnerId;
    private String reason = "";
    private String sessionId;
    private int size = 0;
    private ArchiveState status = ArchiveState.unknown;
    private String url;

    public Archive() {
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ArchiveState getStatus() {
        return status;
    }

    public void setStatus(ArchiveState status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Archive [createdAt=" + createdAt + ", duration=" + duration + ", id=" + id + ", name=" + name
                + ", partnerId=" + partnerId + ", reason=" + reason + ", sessionId=" + sessionId + ", size=" + size
                + ", status=" + status + ", uri=" + url + "]";
    }

}