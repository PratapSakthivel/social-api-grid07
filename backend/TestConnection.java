import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/grid07db";
        String username = "grid07user";
        String password = "grid07pass";
        
        try {
            System.out.println("Attempting to connect to PostgreSQL...");
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection successful!");
            
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT version()");
            
            if (resultSet.next()) {
                System.out.println("PostgreSQL Version: " + resultSet.getString(1));
            }
            
            connection.close();
            System.out.println("✅ Connection closed successfully!");
            
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}