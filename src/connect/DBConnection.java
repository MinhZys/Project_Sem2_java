package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String SERVER_NAME = "localhost"; // Hoặc tên máy chủ SQL Server
    private static final String DATABASE_NAME = "FastFoodManagement"; // Tên Database bạn vừa tạo
    private static final String USERNAME = "sa"; // User SQL Server
    private static final String PASSWORD = "sa"; // Password SQL Server

    private static final String URL = "jdbc:sqlserver://" + SERVER_NAME + ":1433;databaseName=" + DATABASE_NAME + ";encrypt=false;trustServerCertificate=true;";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException ex) {
            throw new SQLException("Không tìm thấy JDBC Driver", ex);
        }
    }
}

