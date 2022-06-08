package org.example;

import io.requery.Persistable;
import io.requery.meta.EntityModel;
import io.requery.sql.*;
import org.example.entities.Car;
import org.example.entities.Models;
import org.example.entities.Owner;
import org.example.entities.User;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RequeryOwnerCarRelationTests {
    static EntityDataStore<Persistable> dataStore = null;

    @BeforeAll
    static void setUp() {
        ConnectionProvider connectionProvider = DBConnectionConfig::connection;

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
    @Order(0)
    void checkInsertedCarsAndOwnersExist() {
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

    @Test
    @Order(1)
    void checkOwnerUpdateWorks() {
        final Owner alice = dataStore.select(Owner.class).where(Owner.ID.eq(1L)).get().first();
        assertEquals(2, dataStore.select(Owner.class).get().stream().count(), "2 owners in table");

        assertEquals(40, alice.getAge());
        alice.setAge(50);
        assertEquals(50, alice.getAge());

        dataStore.update(alice);
        final Owner aliceQueriedAgain = dataStore.select(Owner.class).where(Owner.ID.eq(1L)).get().first();
        assertEquals(50, alice.getAge());
        assertEquals(2, dataStore.select(Owner.class).get().stream().count(), "still 2 owners in table");
    }


    @Test
    @Order(2)
    void checkOwnerCopyAndInsertWorks() {
        final Owner alice = dataStore.select(Owner.class).where(Owner.ID.eq(1L)).get().first();
        assertEquals(2, dataStore.select(Owner.class).get().stream().count(), "2 owners in table");

        assertEquals(50, alice.getAge());
        alice.setAge(60);
        assertEquals(60, alice.getAge());

        dataStore.insert(alice);
        assertEquals(3, dataStore.select(Owner.class).get().stream().count(), "3 owners in table after insert");

        final Owner aliceCopy = dataStore.select(Owner.class).where(Owner.ID.eq(3L)).get().first();

        assertEquals(2, aliceCopy.getOwnersCars().size());
        assertEquals("Audi1", aliceCopy.getOwnersCars().get(0).getName());
        assertEquals(4, dataStore.select(Car.class).get().stream().count(), "Still 4 cars. 2 from each owner. Only Owner Alice was copied, not their Cars so far.");
    }

    @Test
    @Order(3)
    void checkOwnerWithCarsCopyAndInsertWorks() {
        final Owner alice = dataStore.select(Owner.class).where(Owner.ID.eq(1L)).get().first();
        assertEquals(3, dataStore.select(Owner.class).get().stream().count(), "Owners in table");
        assertEquals(4, dataStore.select(Car.class).get().stream().count(), "Cars in table");


        assertEquals(60, alice.getAge());
        alice.setAge(70);

        List<Car> previousCars = new ArrayList<>(alice.getOwnersCars()); // IMPORTANT to create a copy of the list, otherwise the references of the elements keep the same
        alice.getOwnersCars().clear(); // remove old cars as belonging
        assertEquals(0, alice.getOwnersCars().size());
        assertEquals(2, previousCars.size());

        previousCars.forEach(car -> {
            car.setName(car.getName() + "copy");
            Car carCopy = new Car();
            carCopy.setName(car.getName());
            carCopy.setPlateNr(car.getPlateNr());


            alice.getOwnersCars().add(car.copy());
        });

        assertEquals(2, alice.getOwnersCars().size());
//        assertEquals("Audi1copy", alice.getOwnersCars().get(0).getName());


        final Owner aliceInsertResult = dataStore.insert(alice);
        assertEquals(4, dataStore.select(Owner.class).get().stream().count(), "Owners in table");
        assertEquals(6, dataStore.select(Car.class).get().stream().count(), "Cars in table");

        assertEquals(4, aliceInsertResult.getId());
        assertEquals(2, aliceInsertResult.getOwnersCars().size());


        final Owner newAlice = dataStore.select(Owner.class)
                .where(Owner.ID.eq(aliceInsertResult.getId()))
                .get()
                .first();


        assertEquals(2, newAlice.getOwnersCars().size());
        assertEquals("Audi1copy", newAlice.getOwnersCars().get(0).getName());
        assertEquals("Audi2copy", newAlice.getOwnersCars().get(1).getName());


    }



}
