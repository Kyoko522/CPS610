SET SERVEROUTPUT ON
BEGIN
  -- Drop tables (includes object tables) first
  FOR t IN (SELECT table_name FROM user_tables) LOOP
    BEGIN
      EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
      DBMS_OUTPUT.PUT_LINE('Dropped table: ' || t.table_name);
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Could not drop table ' || t.table_name || ' : ' || SQLERRM);
    END;
  END LOOP;

  -- Drop views
  FOR v IN (SELECT view_name FROM user_views) LOOP
    BEGIN
      EXECUTE IMMEDIATE 'DROP VIEW ' || v.view_name;
      DBMS_OUTPUT.PUT_LINE('Dropped view: ' || v.view_name);
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Could not drop view ' || v.view_name || ' : ' || SQLERRM);
    END;
  END LOOP;

  -- Drop sequences
  FOR s IN (SELECT sequence_name FROM user_sequences) LOOP
    BEGIN
      EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
      DBMS_OUTPUT.PUT_LINE('Dropped sequence: ' || s.sequence_name);
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Could not drop sequence ' || s.sequence_name || ' : ' || SQLERRM);
    END;
  END LOOP;

  -- Drop synonyms (if you created any)
  FOR sn IN (SELECT synonym_name FROM user_synonyms) LOOP
    BEGIN
      EXECUTE IMMEDIATE 'DROP SYNONYM ' || sn.synonym_name;
      DBMS_OUTPUT.PUT_LINE('Dropped synonym: ' || sn.synonym_name);
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Could not drop synonym ' || sn.synonym_name || ' : ' || SQLERRM);
    END;
  END LOOP;

  -- Drop triggers
  FOR tr IN (SELECT trigger_name FROM user_triggers) LOOP
    BEGIN
      EXECUTE IMMEDIATE 'DROP TRIGGER ' || tr.trigger_name;
      DBMS_OUTPUT.PUT_LINE('Dropped trigger: ' || tr.trigger_name);
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Could not drop trigger ' || tr.trigger_name || ' : ' || SQLERRM);
    END;
  END LOOP;

  -- Drop procedures, functions, packages
  FOR p IN (
    SELECT object_name, object_type
    FROM user_objects
    WHERE object_type IN ('PROCEDURE','FUNCTION','PACKAGE','PACKAGE BODY')
  ) LOOP
    BEGIN
      EXECUTE IMMEDIATE 'DROP ' || p.object_type || ' ' || p.object_name;
      DBMS_OUTPUT.PUT_LINE('Dropped ' || p.object_type || ': ' || p.object_name);
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Could not drop ' || p.object_type || ' ' || p.object_name || ' : ' || SQLERRM);
    END;
  END LOOP;

  -- Drop object types last (and force, because dependencies are common)
  FOR ty IN (
    SELECT type_name
    FROM user_types
    ORDER BY type_name
  ) LOOP
    BEGIN
      EXECUTE IMMEDIATE 'DROP TYPE ' || ty.type_name || ' FORCE';
      DBMS_OUTPUT.PUT_LINE('Dropped type: ' || ty.type_name);
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Could not drop type ' || ty.type_name || ' : ' || SQLERRM);
    END;
  END LOOP;
END;
/

SELECT object_type, COUNT(*) AS cnt
FROM user_objects
GROUP BY object_type
ORDER BY object_type;
