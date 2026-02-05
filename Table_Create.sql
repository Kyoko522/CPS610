-- SCHOOL database schema

--Drop in dependency order (optional)
DROP TABLE GRADE_REPORT;
DROP TABLE PREREQUISITE;
DROP TABLE SECTION;
DROP TABLE COURSE;
DROP TABLE STUDENT;

CREATE TABLE STUDENT (
  Name           VARCHAR2(50)  NOT NULL,
  Student_number NUMBER(10)    NOT NULL,
  Class          VARCHAR2(20),
  Major          VARCHAR2(50),
  CONSTRAINT PK_STUDENT PRIMARY KEY (Student_number)
);

CREATE TABLE COURSE (
  Course_name   VARCHAR2(100) NOT NULL,
  Course_number VARCHAR2(20)  NOT NULL,
  Credit_hours  NUMBER(2)     NOT NULL,
  Department    VARCHAR2(50),
  CONSTRAINT PK_COURSE PRIMARY KEY (Course_number)
);

CREATE TABLE PREREQUISITE (
  Course_number        VARCHAR2(20) NOT NULL,
  Prerequisite_number  VARCHAR2(20) NOT NULL,
  CONSTRAINT PK_PREREQUISITE PRIMARY KEY (Course_number, Prerequisite_number),
  CONSTRAINT FK_PREREQ_COURSE FOREIGN KEY (Course_number)
    REFERENCES COURSE (Course_number),
  CONSTRAINT FK_PREREQ_REQ FOREIGN KEY (Prerequisite_number)
    REFERENCES COURSE (Course_number),
  CONSTRAINT CHK_PREREQ_NOT_SELF CHECK (Course_number <> Prerequisite_number)
);

CREATE TABLE SECTION (
  Section_identifier VARCHAR2(20)  NOT NULL,
  Course_number      VARCHAR2(20)  NOT NULL,
  Semester           VARCHAR2(10)  NOT NULL,
  Year               NUMBER(4)     NOT NULL,
  Instructor         VARCHAR2(50),
  CONSTRAINT PK_SECTION PRIMARY KEY (Section_identifier),
  CONSTRAINT FK_SECTION_COURSE FOREIGN KEY (Course_number)
    REFERENCES COURSE (Course_number),
  CONSTRAINT CHK_SEMESTER CHECK (UPPER(Semester) IN ('SPRING','SUMMER','FALL','WINTER')),
  CONSTRAINT CHK_YEAR CHECK (Year BETWEEN 1900 AND 2100)
);

CREATE TABLE GRADE_REPORT (
  Student_number     NUMBER(10)    NOT NULL,
  Section_identifier VARCHAR2(20)  NOT NULL,
  Grade              VARCHAR2(2),
  CONSTRAINT PK_GRADE_REPORT PRIMARY KEY (Student_number, Section_identifier),
  CONSTRAINT FK_GR_STUDENT FOREIGN KEY (Student_number)
    REFERENCES STUDENT (Student_number),
  CONSTRAINT FK_GR_SECTION FOREIGN KEY (Section_identifier)
    REFERENCES SECTION (Section_identifier)
);