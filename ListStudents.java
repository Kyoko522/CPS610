import java.io.*;
import java.sql.*;

public class ListStudents {
    public static void main(String args[]) throws SQLException, IOException {
        
        // Load Oracle JDBC Driver
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException x) {
            System.out.println("Driver could not be loaded.");
            return;
        }
        
        // Database credentials
        String dbacct = "system";
        String passwrd = "password123";
        
        // Connect using SID format
        Connection conn = DriverManager.getConnection(
            "jdbc:oracle:thin:@localhost:1521:XE",
            dbacct, 
            passwrd
        );
        
        System.out.println("Connected to database!\n");
        
        Statement stmt = conn.createStatement();
        
        // DEBUG 1: Which database are we connected to?
        ResultSet r1 = stmt.executeQuery("SELECT ora_database_name FROM dual");
        r1.next();
        System.out.println("Database name: " + r1.getString(1));
        r1.close();
        
        // DEBUG 2: Which user are we connected as?
        ResultSet r2 = stmt.executeQuery("SELECT USER FROM dual");
        r2.next();
        System.out.println("Connected as user: " + r2.getString(1));
        r2.close();
        
        // DEBUG 3: What tables does this user own?
        System.out.println("\nTables owned by current user:");
        ResultSet r3 = stmt.executeQuery("SELECT table_name FROM user_tables");
        while (r3.next()) {
            System.out.println("  - " + r3.getString(1));
        }
        r3.close();
        
        // DEBUG 4: Check if STUDENT table exists anywhere
        System.out.println("\nSearching for STUDENT table in all schemas:");
        ResultSet r4 = stmt.executeQuery("SELECT owner, table_name FROM all_tables WHERE table_name = 'STUDENT'");
        while (r4.next()) {
            System.out.println("  - Owner: " + r4.getString(1) + ", Table: " + r4.getString(2));
        }
        r4.close();
        
        // DEBUG 5: Try to count rows in STUDENT
        System.out.println("\nTrying to count STUDENT rows:");
        try {
            ResultSet r5 = stmt.executeQuery("SELECT COUNT(*) FROM STUDENT");
            r5.next();
            System.out.println("  Row count: " + r5.getInt(1));
            r5.close();
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
        
        stmt.close();
        conn.close();
    }
}