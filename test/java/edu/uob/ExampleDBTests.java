package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class ExampleDBTests {

    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName() {
        String randomName = "";
        for(int i=0; i<10 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // A basic test that creates a database, creates a table, inserts some test data, then queries it.
    // It then checks the response to see that a couple of the entries in the table are returned as expected
    @Test
    public void testBasicCreateAndQuery() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Simon"), "An attempt was made to add Simon to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Chris"), "An attempt was made to add Chris to the table, but they were not returned by SELECT *");
    }

    // A test to make sure that querying returns a valid ID (this test also implicitly checks the "==" condition)
    // (these IDs are used to create relations between tables, so it is essential that suitable IDs are being generated and returned !)
    @Test
    public void testQueryID() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        String response = sendCommandToServer("SELECT id FROM marks WHERE name == 'Simon';");
        // Convert multi-lined responses into just a single line
        String singleLine = response.replace("\n"," ").trim();
        // Split the line on the space character
        String[] tokens = singleLine.split(" ");
        // Check that the very last token is a number (which should be the ID of the entry)
        String lastToken = tokens[tokens.length-1];
        try {
            Integer.parseInt(lastToken);
        } catch (NumberFormatException nfe) {
            fail("The last token returned by `SELECT id FROM marks WHERE name == 'Simon';` should have been an integer ID, but was " + lastToken);
        }
    }

    // A test to make sure that databases can be reopened after server restart
    @Test
    public void testTablePersistsAfterRestart() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Simon"), "Simon was added to a table and the server restarted - but Simon was not returned by SELECT *");
    }

    // Test to make sure that the [ERROR] tag is returned in the case of an error (and NOT the [OK] tag)
    @Test
    public void testForErrorTag() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        String response = sendCommandToServer("SELECT * FROM libraryfines;");
        assertTrue(response.contains("[ERROR]"), "An attempt was made to access a non-existent table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An attempt was made to access a non-existent table, however an [OK] tag was returned");
    }


    //MY OWN TESTS

    @Test
    public void testUpdateCommand() {
        String dbName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + dbName + ";");
        sendCommandToServer("USE " + dbName + ";");

        sendCommandToServer("CREATE TABLE employees (name, age, salary);");
        sendCommandToServer("INSERT INTO employees VALUES ('Alice', 30, 50000);");
        sendCommandToServer("INSERT INTO employees VALUES ('Bob', 40, 70000);");

        sendCommandToServer("UPDATE employees SET salary = 60000 WHERE name == 'Alice';");

        String response = sendCommandToServer("SELECT salary FROM employees WHERE name == 'Alice';");
        assertTrue(response.contains("60000"), "Alice's salary should be updated to 60000.");
    }

    @Test
    public void testAlterTableAddDropColumn() {
        String dbName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + dbName + ";");
        sendCommandToServer("USE " + dbName + ";");

        sendCommandToServer("CREATE TABLE students (name, age);");
        sendCommandToServer("ALTER TABLE students ADD grade;");

        sendCommandToServer("INSERT INTO students VALUES ('Charlie', 22, 'B');");
        String response = sendCommandToServer("SELECT * FROM students;");
        assertTrue(response.contains("Charlie"), "Charlie should be in the table.");
        assertTrue(response.contains("B"), "Grade column should exist after ALTER TABLE ADD.");

        sendCommandToServer("ALTER TABLE students DROP age;");
        String responseAfterDrop = sendCommandToServer("SELECT * FROM students;");
        assertFalse(responseAfterDrop.contains("22"), "Age column should be removed.");
    }

    @Test
    public void testDeleteCommand() {
        String dbName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + dbName + ";");
        sendCommandToServer("USE " + dbName + ";");

        sendCommandToServer("CREATE TABLE products (name, price);");
        sendCommandToServer("INSERT INTO products VALUES ('Laptop', 1200);");
        sendCommandToServer("INSERT INTO products VALUES ('Tablet', 600);");

        sendCommandToServer("DELETE FROM products WHERE name == 'Laptop';");

        String response = sendCommandToServer("SELECT * FROM products;");
        assertFalse(response.contains("Laptop"), "Laptop should be deleted from the table.");
        assertTrue(response.contains("Tablet"), "Tablet should remain in the table.");
    }

    @Test
    public void testJoinCourseworkAndMarks() {
        String dbName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + dbName + ";");
        sendCommandToServer("USE " + dbName + ";");

        // Create coursework and marks tables
        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");

        // Insert data into coursework
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 2);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 4);");

        // Insert data into marks
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");

        // Perform the join operation
        String response = sendCommandToServer("JOIN coursework AND marks ON submission AND id;");
        System.out.println(response);

        // Expected results
        assertTrue(response.contains("1\tOXO\tRob\t35\tFALSE"), "Row 1 should be in the JOIN result.");
        assertTrue(response.contains("2\tDB\tSimon\t65\tTRUE"), "Row 2 should be in the JOIN result.");
        assertTrue(response.contains("3\tOXO\tChris\t20\tFALSE"), "Row 3 should be in the JOIN result.");
        assertTrue(response.contains("4\tSTAG\tSion\t55\tTRUE"), "Row 4 should be in the JOIN result.");

    }


        @Test
    public void testDropTableAndDatabase() {
        String dbName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + dbName + ";");
        sendCommandToServer("USE " + dbName + ";");

        sendCommandToServer("CREATE TABLE books (title, author);");
        sendCommandToServer("INSERT INTO books VALUES ('1984', 'George Orwell');");

        sendCommandToServer("DROP TABLE books;");
        String response = sendCommandToServer("SELECT * FROM books;");
        assertTrue(response.contains("[ERROR]"), "Table 'books' should not exist after DROP TABLE.");

        sendCommandToServer("DROP DATABASE " + dbName + ";");
        String responseDB = sendCommandToServer("USE " + dbName + ";");
        assertTrue(responseDB.contains("[ERROR]"), "Database should not exist after DROP DATABASE.");
    }


}