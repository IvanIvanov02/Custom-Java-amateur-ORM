package Orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MyConnection {

    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/";
    private static Connection connection;

    private MyConnection() {}
    public static void createConnection(String dbName,String username,String password) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user",username);
        properties.setProperty("password",password);

        connection = DriverManager.getConnection(CONNECTION_URL + dbName,properties);
    }

    public static Connection getConnection() {
        return connection;
    }

}
