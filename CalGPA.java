import java.io.*;
import java.sql.*;

class CalGPA {
    public static void main(String args[]) throws SQLException, IOException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver"); 
        } catch (ClassNotFoundException x) {
            System.out.println("Driver could not be loaded.");
            return;
        }
        
        String dbacct, passwrd, name;
        char grade;
        int credit;
        

        dbacct = "kaiadams"; // hardcoded
        passwrd = "password123"; // hardcoded
        

        Connection conn = DriverManager.getConnection(
            "jdbc:oracle:thin:@//localhost:1521/orcl",
            dbacct,
            passwrd
        );

        
        // SQL query to get all grades for a specific student name
        String stmt1 = 
            "SELECT G.Grade, C.Credit_hours " +                              // Select grade and credit hours
            "FROM STUDENT S, GRADE_REPORT G, SECTION SEC, COURSE C " +       // From these 4 tables (with aliases)
            "WHERE G.Student_number = S.Student_number AND " +               // Join: Link student to their grades
            "      G.Section_identifier = SEC.Section_identifier AND " +     // Join: Link grade to section taken
            "      SEC.Course_number = C.Course_number AND " +               // Join: Link section to course (for credits)
            "      UPPER(S.Name) = UPPER(?)";                                // Filter: Match student name (case-insensitive)                        
        
        PreparedStatement p = conn.prepareStatement(stmt1);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Please enter your name: ");
        name = reader.readLine();
        
        p.clearParameters();                        //reset the preset parameter the (?) in the embedded SQL statement
        p.setString(1, name);       //set the string to the first unknown/place holder in the code (the first ?)
        ResultSet r = p.executeQuery();
        
        double count = 0, sum = 0, avg = 0;
        
        while (r.next()) {

            grade = r.getString(1).charAt(0); // Fixed
            credit = r.getInt(2); // Fixed
            
            // Lab grading scale: A=4, B=3, C=2, D=1, F=0
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


// To run use the following command in terminal
// javac -cp "lib/ojdbc8.jar" CalGPA.java
// java -cp ".:lib/ojdbc8.jar" CalGPA