package com.edu.crimepredictor;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String email;
    private String passwordHash;
    private String role; // "user" (Citizen) or "le" (Law Enforcement)
    private String area;
    private String badge;
    private String station;

    public User() {}

    public User(String name, String email, String passwordHash, String role, String area, String badge, String station) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.area = area;
        this.badge = badge;
        this.station = station;
    }

    public String getName() { return name != null ? name : "Citizen"; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email != null ? email : ""; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash != null ? passwordHash : ""; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role != null ? role : "user"; }
    public void setRole(String role) { this.role = role; }

    public boolean isLawEnforcement() { return "le".equalsIgnoreCase(role); }

    public String getArea() { return area != null && !area.isEmpty() ? area : "Tembisa"; }
    public void setArea(String area) { this.area = area; }

    public String getBadge() { return badge != null ? badge : ""; }
    public void setBadge(String badge) { this.badge = badge; }

    public String getStation() { return station != null ? station : ""; }
    public void setStation(String station) { this.station = station; }
}
