package com.edu.crimepredictor.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String email = "";

    public String name;
    public String passwordHash;
    public String role;
    public String homeArea;
    public String badgeNumber;
    public String policeStation;

    public UserEntity() {}

    public UserEntity(@NonNull String email, String name, String passwordHash, String role,
                      String homeArea, String badgeNumber, String policeStation) {
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.role = role;
        this.homeArea = homeArea;
        this.badgeNumber = badgeNumber;
        this.policeStation = policeStation;
    }
}
