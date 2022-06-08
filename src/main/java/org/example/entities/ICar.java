package org.example.entities;

import io.requery.*;


@Entity(copyable = true)
public interface ICar {
    @Key
    @Generated
    long getId();

    Integer getPlateNr();
    String getName();

    @ManyToOne
    Owner getBelongsToOwner();
}
