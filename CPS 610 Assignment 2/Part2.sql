CREATE OR REPLACE TYPE depart_type AS OBJECT (
  name     VARCHAR2(50),
  faculty  VARCHAR2(50),
  building VARCHAR2(50),
  phone    VARCHAR2(20)
);


CREATE OR REPLACE TYPE professor_type AS OBJECT (
  name   VARCHAR2(50),
  emp_id NUMBER,
  email  VARCHAR2(80),
  dept   depart_type
);


CREATE TABLE Professor OF professor_type;

INSERT INTO Professor VALUES (
  professor_type('Alice Brown', 1001, 'alice@uni.ca',
    depart_type('Computer Science', 'Engineering', 'ENG-201', '416-555-1001'))
);

INSERT INTO Professor VALUES (
  professor_type('Bilal Khan', 1002, 'bilal@uni.ca',
    depart_type('Mathematics', 'Science', 'SCI-110', '416-555-1002'))
);

INSERT INTO Professor VALUES (
  professor_type('Chloe Chen', 1003, 'chloe@uni.ca',
    depart_type('Biology', 'Science', 'SCI-220', '416-555-1003'))
);

INSERT INTO Professor VALUES (
  professor_type('Diego Silva', 1004, 'diego@uni.ca',
    depart_type('Economics', 'Business', 'BUS-310', '416-555-1004'))
);

INSERT INTO Professor VALUES (
  professor_type('Emma Patel', 1005, 'emma@uni.ca',
    depart_type('History', 'Arts', 'ART-105', '416-555-1005'))
);

COMMIT;


COLUMN name FORMAT A18
COLUMN email FORMAT A22
COLUMN dept_name FORMAT A18
COLUMN faculty FORMAT A12
COLUMN building FORMAT A10
COLUMN phone FORMAT A14

SELECT
  p.name,
  p.emp_id,
  p.email,
  p.dept.name     AS dept_name,
  p.dept.faculty  AS faculty,
  p.dept.building AS building,
  p.dept.phone    AS phone
FROM Professor p;

ALTER TYPE professor_type ADD ATTRIBUTE (income NUMBER(10,2)) CASCADE;


DESC Professor;


UPDATE Professor p SET p.income = 72000 WHERE p.emp_id = 1001;
UPDATE Professor p SET p.income = 38000 WHERE p.emp_id = 1002;
UPDATE Professor p SET p.income = 91000 WHERE p.emp_id = 1003;
UPDATE Professor p SET p.income = 45000 WHERE p.emp_id = 1004;
UPDATE Professor p SET p.income = 33000 WHERE p.emp_id = 1005;

COMMIT;

SET SERVEROUTPUT ON

DECLARE
  v_tax NUMBER(10,2);
BEGIN
  FOR r IN (SELECT name, income FROM Professor ORDER BY emp_id) LOOP
    v_tax := r.income * 0.30;
    DBMS_OUTPUT.PUT_LINE(r.name || ' tax (30%): ' || TO_CHAR(v_tax, '9999990.00'));
  END LOOP;
END;



CREATE OR REPLACE PROCEDURE show_low_income_professors IS
BEGIN
  DBMS_OUTPUT.PUT_LINE('Professors with income < 40000:');

  FOR r IN (SELECT name, income FROM Professor WHERE income < 40000 ORDER BY income) LOOP
    DBMS_OUTPUT.PUT_LINE(r.name || ' - ' || TO_CHAR(r.income, '9999990.00'));
  END LOOP;
END;


SET SERVEROUTPUT ON
EXEC show_low_income_professors;


CREATE OR REPLACE PROCEDURE show_average_income IS
  v_avg NUMBER(10,2);
BEGIN
  SELECT AVG(income) INTO v_avg FROM Professor;

  DBMS_OUTPUT.PUT_LINE('Average income: ' || TO_CHAR(v_avg, '9999990.00'));
END;


SET SERVEROUTPUT ON
EXEC show_average_income;
