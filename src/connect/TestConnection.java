package connect;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                System.out.println("✅ Kết nối thành công tới SQL Server!");
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
