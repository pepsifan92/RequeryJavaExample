package org.example;

import io.requery.Persistable;
import io.requery.meta.EntityModel;
import io.requery.sql.*;
import org.example.entities.Car;
import org.example.entities.Models;
import org.example.entities.Owner;
import org.example.entities.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequeryOwnerCarRelationTests {
    static EntityDataStore<Persistable> dataStore = null;

    @BeforeAll
    static void setUp() {
        ConnectionProvider connectionProvider = () -> DriverManager.getConnection("jdbc:h2:./unittests", "sa", "");

        EntityModel model = Models.DEFAULT;
        Configuration configuration = new ConfigurationBuilder(connectionProvider, model)
//                    .useDefaultLogging()
                .build();

        SchemaModifier schemaModifier = new SchemaModifier(configuration);
        schemaModifier.createTables(TableCreationMode.DROP_CREATE);

        dataStore = new EntityDataStore<>(configuration);

        insertTestOwnerAndCars();
    }

    static void insertTestOwnerAndCars() {
        final Owner alice = new Owner();
        final Owner lisa = new Owner();

        // Set user basics
        alice.setName("Alice");
        alice.setAge(40);

        lisa.setName("Lisa");
        lisa.setAge(41);

        // Create some Cars
        final Car audi1 = new Car();
        final Car audi2 = new Car();
        final Car vw1 = new Car();
        final Car vw2 = new Car();

        audi1.setName("Audi1");
        audi1.setPlateNr(242);

        audi2.setName("Audi2");
        audi2.setPlateNr(243);

        vw1.setName("VW1");
        vw1.setPlateNr(661);

        vw2.setName("VW2");
        vw2.setPlateNr(662);

        // Map cars to owner
        alice.getOwnersCars().add(audi1);
        alice.getOwnersCars().add(audi2);

        lisa.getOwnersCars().add(vw1);
        lisa.getOwnersCars().add(vw2);

        // Insert, with related cars
        dataStore.insert(alice);
        dataStore.insert(lisa);
    }

    @Test
    void checkInsertedUsersExist() {
        final Owner alice = dataStore.select(Owner.class).where(Owner.ID.eq(1L)).get().first();
        final Owner lisa = dataStore.select(Owner.class).where(Owner.ID.eq(2L)).get().first();

        assertEquals("Alice", alice.getName());
        assertEquals(40, alice.getAge());
        assertEquals(2, alice.getOwnersCars().size(), "Alice owns two cars");
        assertEquals("Audi1", alice.getOwnersCars().get(0).getName());
        assertEquals("Audi2", alice.getOwnersCars().get(1).getName());

        assertEquals("Lisa", lisa.getName());
        assertEquals(2, lisa.getOwnersCars().size(), "Lisa owns two cars");
        assertEquals("VW1", lisa.getOwnersCars().get(0).getName());
        assertEquals("VW2", lisa.getOwnersCars().get(1).getName());
    }

//    @Test
//    void checkUsersUpdateWorks() {
//        final User tom = dataStore.select(User.class).where(User.ID.eq(1L)).get().first();
//        assertEquals("Tom", tom.getName());
//        assertEquals(22, tom.getAge());
//
//        tom.setAge(99);
//        assertEquals(99, dataStore.update(tom).getAge()); // Update and check result
//
//        final User secondQueryTom = dataStore.select(User.class).where(User.ID.eq(1L)).get().first();
//        assertEquals(99, secondQueryTom.getAge());
//
//        // Update back to 22 and check result
//        secondQueryTom.setAge(22);
//        assertEquals(22, dataStore.update(secondQueryTom).getAge());
//    }
//
//    @Test
//    void checkInsertOnlyCreatesAndNotUpdates() {
//        assertEquals(2, dataStore.select(User.class).get().stream().count());
//
//        final User tom = dataStore.select(User.class).where(User.ID.eq(1L)).get().first();
//        assertEquals("Tom", tom.getName());
//        assertEquals(22, tom.getAge());
//
//        tom.setAge(10);
//        // Insert and check result - is should be a new row. Not any update or relation to previous entries/rows.
//        User insertedTom = dataStore.insert(tom);
//        assertEquals(3, dataStore.select(User.class).get().stream().count());
//        assertEquals(10, insertedTom.getAge());
//        assertEquals(3, insertedTom.getId()); // Ensure the inserted user has a new primary key
//    }


}
