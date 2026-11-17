package com.edu.crimepredictor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommunityReport implements Serializable {
    private String id;
    private String area;
    private String crime;
    private String desc;
    private String photoUri;
    private long timestamp;
    private String by;
    private String role; // "user" or "le"
    private String status; // "New", "Verified by SAPS", "Under investigation", "Resolved", "False / Rejected"
    private int confirmCount;
    private List<String> confirmedBy;
    private String verifiedByOfficer;

    public CommunityReport() {
        this.confirmedBy = new ArrayList<>();
    }

    public CommunityReport(String id, String area, String crime, String desc, String photoUri,
                           long timestamp, String by, String role, String status, int confirmCount) {
        this.id = id;
        this.area = area;
        this.crime = crime;
        this.desc = desc;
        this.photoUri = photoUri;
        this.timestamp = timestamp;
        this.by = by;
        this.role = role;
        this.status = status;
        this.confirmCount = confirmCount;
        this.confirmedBy = new ArrayList<>();
        this.verifiedByOfficer = "";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCrime() { return crime; }
    public void setCrime(String crime) { this.crime = crime; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getBy() { return by; }
    public void setBy(String by) { this.by = by; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status != null ? status : "New"; }
    public void setStatus(String status) { this.status = status; }

    public int getConfirmCount() { return confirmCount; }
    public void setConfirmCount(int confirmCount) { this.confirmCount = confirmCount; }

    public List<String> getConfirmedBy() {
        if (confirmedBy == null) confirmedBy = new ArrayList<>();
        return confirmedBy;
    }

    public String getVerifiedByOfficer() { return verifiedByOfficer != null ? verifiedByOfficer : ""; }
    public void setVerifiedByOfficer(String verifiedByOfficer) { this.verifiedByOfficer = verifiedByOfficer; }

    public boolean isVerifiedBySaps() {
        String s = getStatus().toLowerCase();
        return "le".equalsIgnoreCase(role) || s.contains("verified") || s.contains("investigation") || s.contains("resolved");
    }

    public boolean isRejected() {
        String s = getStatus().toLowerCase();
        return s.contains("false") || s.contains("rejected");
    }

    public boolean hasUserConfirmed(String email) {
        return getConfirmedBy().contains(email != null ? email.toLowerCase() : "");
    }

    public void addConfirmation(String email) {
        if (email != null && !hasUserConfirmed(email)) {
            getConfirmedBy().add(email.toLowerCase());
            confirmCount++;
        }
    }
}
