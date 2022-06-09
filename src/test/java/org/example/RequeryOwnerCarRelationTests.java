package org.example;

import io.requery.Persistable;
import io.requery.meta.EntityModel;
import io.requery.sql.*;
import org.example.entities.Car;
import org.example.entities.Models;
import org.example.entities.Owner;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

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

        assertNull(audi1.getId(), "Primary key null until not inserted");

        // Map cars to owner
        alice.getOwnersCars().add(audi1);
        alice.getOwnersCars().add(audi2);

        lisa.getOwnersCars().add(vw1);
        lisa.getOwnersCars().add(vw2);


        // Insert, with related cars - Insert alters the entities!
        dataStore.insert(alice);
        dataStore.insert(lisa);

        assertNotNull(audi1.getId(), "Primary key set on insert - as foreign key in a list");
    }

    @Test
    @Order(0)
    void TEST_INSERTED_CARS_AND_OWNERS_EXIST() {
        final Owner alice = dataStore.select(Owner.class).where(Owner.ID.eq(1L)).get().first();
        final Owner lisa = dataStore.select(Owner.class).where(Owner.ID.eq(2L)).get().first();

        assertEquals("Alice", alice.getName());
        assertEquals(40, alice.getAge());
        assertEquals(2, alice.getOwnersCars().size(), "Alice owns two cars");
        assertEquals(1L, alice.getOwnersCars().get(0).getId());
        assertEquals("Audi1", alice.getOwnersCars().get(0).getName());
        assertEquals("Audi2", alice.getOwnersCars().get(1).getName());

        assertEquals("Lisa", lisa.getName());
        assertEquals(2, lisa.getOwnersCars().size(), "Lisa owns two cars");
        assertEquals("VW1", lisa.getOwnersCars().get(0).getName());
        assertEquals("VW2", lisa.getOwnersCars().get(1).getName());
    }

    @Test
    @Order(1)
    void TEST_UPDATE_OF_OWNER_WORKS() {
        final Owner alice = dataStore.select(Owner.class).where(Owner.ID.eq(1L)).get().first();
        assertEquals(2, dataStore.select(Owner.class).get().stream().count(), "2 owners in table");

        assertEquals(40, alice.getAge());
        alice.setAge(50);
        assertEquals(50, alice.getAge());

        dataStore.update(alice);
        final Owner aliceQueriedAgain = dataStore.select(Owner.class).where(Owner.ID.eq(1L)).get().first();
        assertEquals(50, aliceQueriedAgain.getAge());
        assertEquals(2, dataStore.select(Owner.class).get().stream().count(), "still 2 owners in table");
    }


    @Test
    @Order(2)
    void TEST_COPY_OF_OWNER_WORKS() {
        // Query an owner, alter it and insert as a new one.
        final Owner alice = dataStore.select(Owner.class).where(Owner.ID.eq(1L)).get().first();
        assertEquals(2, dataStore.select(Owner.class).get().stream().count(), "2 owners in table");
        assertEquals(2, alice.getOwnersCars().size(), "alice has 2 cars");

        assertEquals(50, alice.getAge());
        alice.setAge(60);
        assertEquals(60, alice.getAge());

        assertEquals(1L, alice.getId());
        dataStore.insert(alice);
        assertEquals(3L, alice.getId(), "Original entity got new ID after insert. 'alice' was altered by insert()");
        assertEquals(3, dataStore.select(Owner.class).get().stream().count(), "3 owners in table after insert");

        final Owner aliceQuery = dataStore.select(Owner.class).where(Owner.ID.eq(3L)).get().first();

        // The cars / foreign keys of alice has not changed! They are the same. Was ID 1 and 2, now it's 1,2 too. So no copy of the foreign keys created.
        // So far only a new entry/row of the owner, not a copy of the cars.
        assertEquals(2, aliceQuery.getOwnersCars().size());
        assertEquals(1L, alice.getOwnersCars().get(0).getId(), "Same ID (1) as the original alice entity");
        assertEquals(4, dataStore.select(Car.class).get().stream().count(), "Still 4 cars. 2 from each owner. Only Owner Alice was copied, not their Cars so far.");
    }

    @Test
    @Order(3)
    void TEST_COPY_OF_OWNER_AND_ITS_FOREIGN_KEYS_WORKS() {
        // ======== Currently 3 Owner and 4 Cars ========
        final Owner alice = dataStore.select(Owner.class).where(Owner.ID.eq(3L)).get().first();
        assertEquals(3, dataStore.select(Owner.class).get().stream().count(), "Owners in table");
        assertEquals(4, dataStore.select(Car.class).get().stream().count(), "Cars in table");

        // ======== Copy Owner with ID 3: a copy of alice from previous test ========
        assertEquals(60, alice.getAge());
        final Owner aliceCopy = alice.copy(); // Copy seems to 'flag' the entity to get a new ID on insert.
        assertEquals(60, aliceCopy.getAge());
        assertEquals(2, aliceCopy.getOwnersCars().size());
        assertEquals(1L, aliceCopy.getOwnersCars().get(0).getId(), "Expect the original ID 1 of Audi1");

        // ======== Clear list of cars from alice and add new ones as copy ========
        aliceCopy.getOwnersCars().clear(); // remove old cars as belonging
        assertEquals(0, aliceCopy.getOwnersCars().size());

        // Copy the foreign keys - the cars in the owner's list
        alice.getOwnersCars().forEach(car -> {
            Car carCopy = car.copy();
            carCopy.setName(car.getName() + "copy");

            dataStore.insert(carCopy); // carCopy gets a new ID though insert(). This insert is required. Only add it to the list of aliceCopy doesn't work with a copy for some reason.
            aliceCopy.getOwnersCars().add(carCopy); // carCopy reference now has a new ID and can get added to the owner with correct mapping.
        });

        // ======== Car copies correctly added to owner's car list  ========
        assertEquals(2, aliceCopy.getOwnersCars().size());
        assertEquals(1, aliceCopy.getId(), "Old ID until insert");

        // ======== Insert Owner with copied cars  ========
        final Owner aliceInsertResult = dataStore.insert(aliceCopy);
        assertEquals(4, aliceInsertResult.getId(), "aliceCopy now has now ID 4 after insert");
        assertEquals(2, aliceInsertResult.getOwnersCars().size());
        assertEquals(5L, aliceInsertResult.getOwnersCars().get(0).getId(), "aliceCopy references to new car's ID");
        assertEquals("Audi1copy", aliceInsertResult.getOwnersCars().get(0).getName(), "aliceCopy references to new car's name");

        // ======== Check if tables have expected amount of entries  ========
        assertEquals(4, dataStore.select(Owner.class).get().stream().count(), "Owners in table");
        assertEquals(6, dataStore.select(Car.class).get().stream().count(), "Cars in table");

        // ======== Query the just copied owner  ========
        final Owner newAlice = dataStore.select(Owner.class)
                .where(Owner.ID.eq(4L))
                .get()
                .first();

        // ======== Check if "deep copy" was successful ========
        assertEquals(2, newAlice.getOwnersCars().size());
        assertEquals(5L, newAlice.getOwnersCars().get(0).getId());
        assertEquals("Audi1copy", newAlice.getOwnersCars().get(0).getName());
        assertEquals("Audi2copy", newAlice.getOwnersCars().get(1).getName());
    }
}
