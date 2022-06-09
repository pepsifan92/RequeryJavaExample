package org.example.entities;

import io.requery.*;


@Entity(copyable = true)
public interface ICar {
    @Key
    @Generated
    Long getId();
    void setId(Long id);

    Integer getPlateNr();
    String getName();

    @ManyToOne
    Owner getBelongsToOwner();
}
