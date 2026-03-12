import java.io.*;
import java.sql.*;

class CalGPA {
    public static void main(String args[]) throws SQLException, IOException {
        try {
            // Class.forName("oracle.jdbc.driver.OrableDriver");
            Class.forName("oracle.jdbc.driver.OracleDriver"); 
        } catch (ClassNotFoundException x) {
            System.out.println("Driver could not be loaded.");
            return;
        }
        
        String dbacct, passwrd, name;
        char grade;
        int credit;
        
        // dbacct = readEntry("Enter database account: "); //system
        // passwrd = readEntry("Enter password: "); //password123
        dbacct = "system"; // hardcoded
        passwrd = "password123"; // hardcoded
        
        // Connection conn = DriverManager.getConnection("jdbc:oracle:oci8:"+dbacct+"/"+passwrd); // Original
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", dbacct, passwrd);
        
        String stmt1 = 
            "SELECT G.Grade, C.Credit_hours " +                   
            "FROM STUDENT S, GRADE_REPORT G, SECTION SEC, COURSE C " + 
            "WHERE G.Student_number = S.Student_number AND " +         
            "      G.Section_identifier = SEC.Section_identifier AND " +
            "      SEC.Course_number = C.Course_number AND " +               
            "      UPPER(S.Name) = UPPER(?)";                           
        
        PreparedStatement p = conn.prepareStatement(stmt1);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Please enter your name: ");
        name = reader.readLine();
        
        p.clearParameters();                       
        p.setString(1, name);   
        ResultSet r = p.executeQuery();
        
        double count = 0, sum = 0, avg = 0;
        
        while (r.next()) {
            grade = r.getString(1).charAt(0);
            credit = r.getInt(2); 
            
            switch (grade) {
                case 'A': sum = sum + (4 * credit); count = count + credit; break;
                case 'B': sum = sum + (3 * credit); count = count + credit; break;
                case 'C': sum = sum + (2 * credit); count = count + credit; break;
                case 'D': sum = sum + (1 * credit); count = count + credit; break;
                case 'F': sum = sum + (0 * credit); count = count + credit; break;
                default: System.out.println("This grade " + grade + " will not be calculated."); break;
            }
        }
        
        avg = sum / count;
        
        System.out.println("Student named " + name + " has a grade point average " + avg + ".");
        
        r.close();
    }
}
// javac -cp "lib/ojdbc8.jar" CalGPA.java 
// java -cp ".:lib/ojdbc8.jar" CalGPA