package org.example.entities;

import io.requery.CascadeAction;
import io.requery.Entity;
import io.requery.OneToMany;
import io.requery.query.MutableResult;

import java.util.List;

@Entity(copyable = true)
public interface IOwner extends IDatedDeviceIdBase {
    String getName();

    @OneToMany(mappedBy = "belongsToOwner",  cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
    List<Car> getOwnersCars();
}
