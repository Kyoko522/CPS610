DROP TABLE Professor;
DROP TYPE professor_type;
DROP TYPE depart_type;

CREATE TYPE depart_type AS OBJECT (
    Name VARCHAR2(50),
    Faculty VARCHAR2(50),
    Building VARCHAR2(50),
    Phone VARCHAR2(15)
);
/

CREATE TYPE professor_type AS OBJECT (
    Name VARCHAR2(50),
    Emp_id NUMBER(10),
    Email VARCHAR2(100),
    Department depart_type,
    Income NUMBER(10,2)
);
/

CREATE TABLE Professor OF professor_type;

INSERT INTO Professor VALUES (
    professor_type('John Smith', 1001, 'john.smith@university.com',
        depart_type('Computer Science', 'Engineering', 'Building A', '416-555-0101'), 55000)
);

INSERT INTO Professor VALUES (
    professor_type('Jane Doe', 1002, 'jane.doe@university.com',
        depart_type('Mathematics', 'Science', 'Building B', '416-555-0102'), 48000)
);

INSERT INTO Professor VALUES (
    professor_type('Robert Brown', 1003, 'robert.brown@university.com',
        depart_type('Physics', 'Science', 'Building C', '416-555-0103'), 35000)
);

INSERT INTO Professor VALUES (
    professor_type('Emily Davis', 1004, 'emily.davis@university.com',
        depart_type('Chemistry', 'Science', 'Building D', '416-555-0104'), 62000)
);

INSERT INTO Professor VALUES (
    professor_type('Michael Wilson', 1005, 'michael.wilson@university.com',
        depart_type('English', 'Arts', 'Building E', '416-555-0105'), 38000)
);

COMMIT;

SELECT * FROM Professor;

-- Question 1 - Part 1
SET SERVEROUTPUT ON

BEGIN
    dbms_output.put_line('Welcome to Oracle PL-SQL ');
END;
/

-- Calculate 30 percent of all professors
BEGIN
    FOR prof IN (SELECT Name, Income FROM Professor) LOOP
        DBMS_OUTPUT.PUT_LINE('Professor: ' || prof.Name || 
                             ' | Income: $' || prof.Income || 
                             ' | Tax (30%): $' || (prof.Income * 0.30));
    END LOOP;
END;
/

-- Show professor under 40 000
CREATE OR REPLACE PROCEDURE find_low_income AS
BEGIN
    
    FOR prof IN (SELECT Name, Income FROM Professor WHERE Income < 40000) LOOP
        DBMS_OUTPUT.PUT_LINE('Professor: ' || prof.Name || ' | Income: $' || prof.Income);
    END LOOP;
END;
/

EXEC find_low_income;

-- Average of all professor
CREATE OR REPLACE PROCEDURE calculate_avg_income AS
    v_avg_income NUMBER(10,2);
BEGIN
    SELECT AVG(Income) INTO v_avg_income FROM Professor;
    
    DBMS_OUTPUT.PUT_LINE('Average Income of All Professors: $' || v_avg_income);
END;
/

EXEC calculate_avg_income;

