package org.example;

import io.requery.Persistable;
import io.requery.meta.EntityModel;
import io.requery.sql.*;
import org.example.entities.Models;
import org.example.entities.User;

import java.sql.DriverManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello world!");

        EntityDataStore<Persistable> dataStore;

        ConnectionProvider connectionProvider = () -> DriverManager.getConnection("jdbc:h2:./test", "sa", "");

        EntityModel model = Models.DEFAULT;
        Configuration configuration = new ConfigurationBuilder(connectionProvider, model)
//                    .useDefaultLogging()
                .build();

        SchemaModifier schemaModifier = new SchemaModifier(configuration);
        schemaModifier.createTables(TableCreationMode.DROP_CREATE);

        dataStore = new EntityDataStore<>(configuration);


        User userTom = new User();
        userTom.setAge(22);
        userTom.setName("Tom");
        User resultUser = dataStore.insert(userTom);

        System.out.print("resultUser:");
        System.out.println(resultUser);
    }



}
