package org.example.entities;

import io.requery.Entity;

@Entity
public interface IUser extends IDatedDeviceIdBase {
    Integer getAge();
    String getName();
}
