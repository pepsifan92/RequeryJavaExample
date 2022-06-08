package org.example;

import io.requery.Persistable;
import io.requery.meta.EntityModel;
import io.requery.sql.*;
import org.example.entities.Models;
import org.example.entities.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class RequerySimpleUserTests {
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

        insertTestUsers();
    }

    static void insertTestUsers() {
        final User userTom = new User();
        final User userBob = new User();
        userTom.setAge(22);
        userTom.setName("Tom");

        userBob.setAge(25);
        userBob.setName("Bob");

        dataStore.insert(userTom);
        dataStore.insert(userBob);
    }

    @Test
    void checkInsertedUsersExist() {
        final User tom = dataStore.select(User.class).where(User.ID.eq(1L)).get().first();
        final User bob = dataStore.select(User.class).where(User.ID.eq(2L)).get().first();

        assertEquals("Tom", tom.getName());
        assertEquals("Bob", bob.getName());
    }

    @Test
    void checkUsersUpdateWorks() {
        final User tom = dataStore.select(User.class).where(User.ID.eq(1L)).get().first();
        assertEquals("Tom", tom.getName());
        assertEquals(22, tom.getAge());

        tom.setAge(99);
        assertEquals(99, dataStore.update(tom).getAge()); // Update and check result

        final User secondQueryTom = dataStore.select(User.class).where(User.ID.eq(1L)).get().first();
        assertEquals(99, secondQueryTom.getAge());

        // Update back to 22 and check result
        secondQueryTom.setAge(22);
        assertEquals(22, dataStore.update(secondQueryTom).getAge());
    }

    @Test
    void checkInsertOnlyCreatesAndNotUpdates() {
        assertEquals(2, dataStore.select(User.class).get().stream().count());

        final User tom = dataStore.select(User.class).where(User.ID.eq(1L)).get().first();
        assertEquals("Tom", tom.getName());
        assertEquals(22, tom.getAge());

        tom.setAge(10);
        // Insert and check result - is should be a new row. Not any update or relation to previous entries/rows.
        User insertedTom = dataStore.insert(tom);
        assertEquals(3, dataStore.select(User.class).get().stream().count());
        assertEquals(10, insertedTom.getAge());
        assertEquals(3, insertedTom.getId()); // Ensure the inserted user has a new primary key
    }


}
