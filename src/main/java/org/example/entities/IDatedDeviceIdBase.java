package org.example.entities;

import io.requery.*;

import java.time.ZonedDateTime;

@Superclass
public interface IDatedDeviceIdBase extends Persistable {
    @Key
    @Generated
    long getId();

    ZonedDateTime getCreateDateTime();
    void setCreateDateTime(ZonedDateTime time);

    Integer getAge();

    @PreInsert
    default void onPreInsert() {
        setCreateDateTime(ZonedDateTime.now());
    }
}
