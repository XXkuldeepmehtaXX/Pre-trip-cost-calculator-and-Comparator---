import java.sql.*;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        String url = "jdbc:mysql://localhost:3306/trip_db";
        return DriverManager.getConnection(url, "root", "");
    }
}