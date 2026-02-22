# CPS610

Before running ensure the Oracle Docker instance has port 1521 open and change the code to your personal credentials and servicename

```
//Java

dbacct ="<Enter your DB Username>"

    passwrd ="<Enter your DB Password>""

Connection conn = DriverManager.getConnection(

"jdbc:oracle:thin:@//localhost:1521/<Enter Your DB Service Name>",

    dbacct,

    passwrd

    );
```








Once connected to Sql Developer Run the Table_Create.sql First to Create the tables were going to use

Followed by the Table_Insert.sql to add the data were going the be Quering 

Compile the Oracle Sql driver with  `javac -cp "lib/ojdbc8.jar:." CalGPA.java`

Then Run using `java -cp "lib/ojdbc8.jar:." CalGPA`

Enter the name Patel and it should give you the GPA.
