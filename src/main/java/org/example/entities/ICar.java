package org.example.entities;

import io.requery.Entity;
import io.requery.ManyToOne;
import io.requery.Persistable;


@Entity
public interface ICar extends IIdBase {
    Integer getPlateNr();
    String getName();

    @ManyToOne
    Owner getBelongsToOwner();
}
