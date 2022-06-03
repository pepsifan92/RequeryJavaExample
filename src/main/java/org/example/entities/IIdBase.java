package org.example.entities;

import io.requery.Generated;
import io.requery.Key;
import io.requery.Persistable;
import io.requery.Superclass;

@Superclass
public interface IIdBase extends Persistable {
    @Key
    @Generated
    long getId();
}
