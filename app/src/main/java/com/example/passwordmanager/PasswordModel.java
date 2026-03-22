package com.example.passwordmanager;

public class PasswordModel {
    private final long id;
    private final String name;
    private final String website;
    private final String username;
    private final String password;

    public PasswordModel(long id, String name, String website, String username, String password) {
        this.id = id;
        this.name = name;
        this.website = website;
        this.username = username;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWebsite() {
        return website;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
