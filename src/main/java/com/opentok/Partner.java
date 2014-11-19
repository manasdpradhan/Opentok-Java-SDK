package com.opentok;

public class Partner {

    private int id;
    private String secret;

    public enum Status {
        ACTIVE, SUSPENDED
    }
    private Status status;
    private String name;
    private long createdAt;


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getSecret() {
        return secret;
    }
    public void setSecret(String secret) {
        this.secret = secret;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(String status) {
        try {
            this.status = Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            //Old values for status were VALID as placeholders for
            //older versions of partner objects, which is equivalent to ACTIVE
            this.status = Status.ACTIVE;
        }
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
