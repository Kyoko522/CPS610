-- Clear tables in FK-safe order
DELETE FROM GRADE_REPORT;
DELETE FROM PREREQUISITE;
DELETE FROM SECTION;
DELETE FROM COURSE;
DELETE FROM STUDENT;

-- STUDENT(Name, Student_number, Class, Major)
-- Your table has Class as VARCHAR2, so use strings like '4' (or 'Senior', etc.)
INSERT INTO STUDENT (Name, Student_number, Class, Major) VALUES ('Patel',    1,  '4', 'CS');
INSERT INTO STUDENT (Name, Student_number, Class, Major) VALUES ('Singh',    2,  '3', 'CS');
INSERT INTO STUDENT (Name, Student_number, Class, Major) VALUES ('Chen',     3,  '2', 'CS');
INSERT INTO STUDENT (Name, Student_number, Class, Major) VALUES ('Williams', 4,  '4', 'CS');
INSERT INTO STUDENT (Name, Student_number, Class, Major) VALUES ('Garcia',   5,  '1', 'CS');
INSERT INTO STUDENT (Name, Student_number, Class, Major) VALUES ('Kim',      6,  '3', 'CS');
INSERT INTO STUDENT (Name, Student_number, Class, Major) VALUES ('Brown',    7,  '2', 'CS');
INSERT INTO STUDENT (Name, Student_number, Class, Major) VALUES ('Ahmed',    8,  '4', 'CS');
INSERT INTO STUDENT (Name, Student_number, Class, Major) VALUES ('Lee',      9,  '1', 'CS');
INSERT INTO STUDENT (Name, Student_number, Class, Major) VALUES ('Martinez', 10, '3', 'CS');

-- COURSE(Course_name, Course_number, Credit_hours, Department)
INSERT INTO COURSE (Course_name, Course_number, Credit_hours, Department) VALUES ('Computer Science I',      'CPS109', 1, 'CS');
INSERT INTO COURSE (Course_name, Course_number, Credit_hours, Department) VALUES ('Computer Science II',     'CPS209', 1, 'CS');
INSERT INTO COURSE (Course_name, Course_number, Credit_hours, Department) VALUES ('Data Structures',         'CPS305', 1, 'CS');
INSERT INTO COURSE (Course_name, Course_number, Credit_hours, Department) VALUES ('Database Systems I',      'CPS510', 1, 'CS');
INSERT INTO COURSE (Course_name, Course_number, Credit_hours, Department) VALUES ('Operating Systems I',     'CPS590', 1, 'CS');
INSERT INTO COURSE (Course_name, Course_number, Credit_hours, Department) VALUES ('Algorithms',              'CPS616', 1, 'CS');
INSERT INTO COURSE (Course_name, Course_number, Credit_hours, Department) VALUES ('Computer Networks I',     'CPS706', 1, 'CS');
INSERT INTO COURSE (Course_name, Course_number, Credit_hours, Department) VALUES ('Machine Learning',        'CPS803', 1, 'CS');
INSERT INTO COURSE (Course_name, Course_number, Credit_hours, Department) VALUES ('Discrete Mathematics I',  'MTH110', 1, 'MATH');

-- SECTION(Section_identifier, Course_number, Semester, Year, Instructor)
-- Your table defines Section_identifier as VARCHAR2 and Year as NUMBER(4).
-- Use strings for Section_identifier, and 4-digit years to satisfy the CHECK constraint.
INSERT INTO SECTION (Section_identifier, Course_number, Semester, Year, Instructor) VALUES ('101', 'CPS109', 'Fall',   2022, 'Marshall');
INSERT INTO SECTION (Section_identifier, Course_number, Semester, Year, Instructor) VALUES ('102', 'CPS209', 'Winter', 2023, 'Harley');
INSERT INTO SECTION (Section_identifier, Course_number, Semester, Year, Instructor) VALUES ('103', 'CPS305', 'Fall',   2023, 'Varghese');
INSERT INTO SECTION (Section_identifier, Course_number, Semester, Year, Instructor) VALUES ('104', 'CPS510', 'Fall',   2024, 'Sadeghian');
INSERT INTO SECTION (Section_identifier, Course_number, Semester, Year, Instructor) VALUES ('105', 'CPS590', 'Winter', 2024, 'Woungang');
INSERT INTO SECTION (Section_identifier, Course_number, Semester, Year, Instructor) VALUES ('106', 'CPS616', 'Winter', 2025, 'Woungang');
INSERT INTO SECTION (Section_identifier, Course_number, Semester, Year, Instructor) VALUES ('107', 'CPS706', 'Winter', 2025, 'Abhari');
INSERT INTO SECTION (Section_identifier, Course_number, Semester, Year, Instructor) VALUES ('108', 'CPS803', 'Fall',   2025, 'Sadeghian');
INSERT INTO SECTION (Section_identifier, Course_number, Semester, Year, Instructor) VALUES ('109', 'MTH110', 'Fall',   2022, 'Grinshpan');

-- GRADE_REPORT(Student_number, Section_identifier, Grade)
-- Section_identifier must match the SECTION PK type/value (we inserted as strings).
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (1,  '101', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (1,  '102', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (1,  '103', 'B');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (1,  '104', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (1,  '105', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (1,  '106', 'C');

INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (2,  '101', 'B');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (2,  '102', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (2,  '103', 'B');

INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (3,  '101', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (3,  '109', 'A');

INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (4,  '104', 'B');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (4,  '105', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (4,  '108', 'A');

INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (5,  '101', 'B');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (5,  '109', 'C');

INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (6,  '103', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (6,  '104', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (6,  '106', 'B');

INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (7,  '101', 'C');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (7,  '102', 'B');

INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (8,  '105', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (8,  '106', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (8,  '107', 'B');

INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (9,  '101', 'A');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (9,  '109', 'B');

INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (10, '102', 'B');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (10, '103', 'B');
INSERT INTO GRADE_REPORT (Student_number, Section_identifier, Grade) VALUES (10, '104', 'A');

-- PREREQUISITE(Course_number, Prerequisite_number)
INSERT INTO PREREQUISITE (Course_number, Prerequisite_number) VALUES ('CPS209', 'CPS109');
INSERT INTO PREREQUISITE (Course_number, Prerequisite_number) VALUES ('CPS305', 'CPS209');
INSERT INTO PREREQUISITE (Course_number, Prerequisite_number) VALUES ('CPS510', 'CPS305');
INSERT INTO PREREQUISITE (Course_number, Prerequisite_number) VALUES ('CPS616', 'CPS305');
INSERT INTO PREREQUISITE (Course_number, Prerequisite_number) VALUES ('CPS590', 'CPS209');
INSERT INTO PREREQUISITE (Course_number, Prerequisite_number) VALUES ('CPS706', 'CPS590');
INSERT INTO PREREQUISITE (Course_number, Prerequisite_number) VALUES ('CPS803', 'CPS616');

COMMIT;

-- Sanity checks
SELECT * FROM STUDENT;
SELECT * FROM COURSE;
SELECT * FROM SECTION;
SELECT * FROM GRADE_REPORT;
SELECT * FROM PREREQUISITE;

-- Patel transcript-like query (works with your schema)
SELECT S.Name, G.Grade, C.Course_name, C.Credit_hours
FROM STUDENT S
JOIN GRADE_REPORT G ON G.Student_number = S.Student_number
JOIN SECTION SEC ON G.Section_identifier = SEC.Section_identifier
JOIN COURSE C ON SEC.Course_number = C.Course_number
WHERE S.Name = 'Patel';

SELECT ora_database_name FROM dual;
SELECT table_name FROM user_tables WHERE table_name = 'STUDENT';
SELECT COUNT(*) FROM STUDENT;

SELECT S.Name, C.Course_name, G.Grade, C.Credit_hours
FROM STUDENT S
JOIN GRADE_REPORT G ON G.Student_number = S.Student_number
JOIN SECTION SEC ON G.Section_identifier = SEC.Section_identifier
JOIN COURSE C ON SEC.Course_number = C.Course_number
WHERE S.Name = 'Patel';
