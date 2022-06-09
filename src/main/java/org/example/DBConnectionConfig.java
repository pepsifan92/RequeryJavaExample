package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionConfig {

    public static Connection connection() throws SQLException {
        return getH2Connection();
    }

    private static Connection getMySQLConnection() throws SQLException {
//        System.out.println("Using MySQL Connection");
        return DriverManager.getConnection("jdbc:mysql://127.0.0.1/requeryTest", "root", "");
    }

    private static Connection getH2Connection() throws SQLException {
//        System.out.println("Using H2 Connection");
        return DriverManager.getConnection("jdbc:h2:./requeryTest", "sa", "");
    }
}
