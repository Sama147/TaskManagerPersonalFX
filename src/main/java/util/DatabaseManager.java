package util; // Assuming this class is in the 'util' package

import model.Priority; // Import your Priority enum
import model.Section;  // Import your Section class
import model.Task;     // Import your Task class

import java.sql.Connection;       // Explicitly import Connection
import java.sql.DriverManager;    // Explicitly import DriverManager
import java.sql.PreparedStatement; // Explicitly import PreparedStatement
import java.sql.ResultSet;        // Explicitly import ResultSet
import java.sql.SQLException;     // Explicitly import SQLException
import java.sql.Statement;        // Explicitly import Statement

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap; // To preserve section order when loading
import java.util.Date; // For java.util.Date usage

/**
 * The DatabaseManager class handles all interactions with the MySQL database.
 * It provides methods for connecting, saving, loading, updating, and deleting
 * Section and Task data.
 */
public class DatabaseManager {

    // --- Database Connection Details ---
    // IMPORTANT: Replace these with your actual MySQL credentials and database name
    private static final String DB_URL = "jdbc:mysql://localhost:3306/task_manager_db";
    private static final String DB_USER = "root"; // Your MySQL username
    private static final String DB_PASSWORD = "SQLpass147"; // Your MySQL password

    /**
     * Establishes a connection to the MySQL database.
     * @return A Connection object if successful, null otherwise.
     */
    private Connection getConnection() {
        try {
            // Explicitly load the MySQL JDBC driver.
            // This is often not strictly necessary for modern JDBC drivers (4.0+),
            // but can resolve "No suitable driver found" issues in certain environments.
            Class.forName("com.mysql.cj.jdbc.Driver"); // UNCOMMENT THIS LINE

            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            // In a real application, you'd log this error and perhaps show a user-friendly message.
            return null;
        } catch (ClassNotFoundException e) { // Add this catch block for ClassNotFoundException
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            return null;
        }
    }

    // --- CRUD Operations for Sections ---

    /**
     * Saves a new section to the database. If a section with the same name already exists,
     * it will not be added again (due to UNIQUE constraint in DB).
     * @param sectionName The name of the section to save.
     * @return The ID of the newly inserted section, or -1 if insertion failed or section already exists.
     */
    public int saveSection(String sectionName) {
        String sql = "INSERT IGNORE INTO sections (name) VALUES (?)"; // INSERT IGNORE prevents errors if name exists
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (conn == null) { // Added null check for connection
                System.err.println("Database connection is null for saveSection.");
                return -1;
            }

            pstmt.setString(1, sectionName);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the generated ID
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving section '" + sectionName + "': " + e.getMessage());
        }
        return -1; // Indicate failure or no new insertion
    }

    /**
     * Loads all sections from the database.
     * @return A Map where keys are section names and values are Section objects (without tasks loaded yet).
     */
    public Map<String, Section> loadAllSections() {
        // Using LinkedHashMap to preserve the order in which sections were added to the DB (by ID)
        Map<String, Section> sections = new LinkedHashMap<>();
        String sql = "SELECT id, name FROM sections ORDER BY id ASC"; // Order by ID to maintain insertion order

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (conn == null) { // Added null check for connection
                System.err.println("Database connection is null for loadAllSections.");
                return sections;
            }

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Section section = new Section(name);
                // Note: We don't load tasks here. Tasks will be loaded separately.
                sections.put(name, section);
            }
        } catch (SQLException e) {
            System.err.println("Error loading sections: " + e.getMessage());
        }
        return sections;
    }

    /**
     * Deletes a section from the database. Due to ON DELETE CASCADE,
     * all associated tasks will also be deleted.
     * @param sectionName The name of the section to delete.
     * @return true if the section was deleted, false otherwise.
     */
    public boolean deleteSection(String sectionName) {
        String sql = "DELETE FROM sections WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) { // Added null check for connection
                System.err.println("Database connection is null for deleteSection.");
                return false;
            }

            pstmt.setString(1, sectionName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting section '" + sectionName + "': " + e.getMessage());
            return false;
        }
    }

    // --- CRUD Operations for Tasks ---

    /**
     * Saves a task to the database. If the task has an ID (meaning it's already in DB), it updates it.
     * Otherwise, it inserts a new task.
     * @param task The Task object to save.
     * @param sectionName The name of the section this task belongs to.
     * @return The ID of the saved/updated task, or -1 if failed.
     */
    public int saveTask(Task task, String sectionName) {
        String selectSectionIdSql = "SELECT id FROM sections WHERE name = ?";
        String insertTaskSql = "INSERT INTO tasks (name, priority, due_date, section_id) VALUES (?, ?, ?, ?)";
        // String updateTaskSql = "UPDATE tasks SET name = ?, priority = ?, due_date = ?, section_id = ? WHERE id = ?"; // Uncomment if adding Task ID

        int sectionId = -1;
        try (Connection conn = getConnection()) { // Connection obtained here
            if (conn == null) { // Added null check for connection
                System.err.println("Database connection is null for saveTask.");
                return -1;
            }

            // Get section_id
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSectionIdSql)) {
                selectStmt.setString(1, sectionName);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        sectionId = rs.getInt("id");
                    } else {
                        System.err.println("Section '" + sectionName + "' not found when saving task.");
                        return -1;
                    }
                }
            }

            // For now, always insert. If you add an 'id' field to Task,
            // you'd add logic here to check if task.getId() != 0 and then use updateTaskSql.
            try (PreparedStatement pstmt = conn.prepareStatement(insertTaskSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, task.getName());
                pstmt.setString(2, task.getPriority().name()); // Convert enum to String
                pstmt.setDate(3, new java.sql.Date(task.getDueDate().getTime())); // Convert util.Date to sql.Date
                pstmt.setInt(4, sectionId);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            return generatedKeys.getInt(1); // Return the generated ID
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving task '" + task.getName() + "': " + e.getMessage());
        }
        return -1;
    }

    /**
     * Loads all tasks for a given section from the database.
     * @param sectionName The name of the section to load tasks for.
     * @return A List of Task objects for the specified section.
     */
    public List<Task> loadTasksForSection(String sectionName) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.name, t.priority, t.due_date FROM tasks t JOIN sections s ON t.section_id = s.id WHERE s.name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) { // Added null check for connection
                System.err.println("Database connection is null for loadTasksForSection.");
                return tasks;
            }

            pstmt.setString(1, sectionName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    Priority priority = Priority.valueOf(rs.getString("priority")); // Convert String back to enum
                    Date dueDate = rs.getDate("due_date"); // Returns java.sql.Date, compatible with java.util.Date

                    tasks.add(new Task(name, priority, dueDate));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading tasks for section '" + sectionName + "': " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing priority enum: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Deletes a specific task from the database.
     * @param taskName The name of the task to delete.
     * @param sectionName The name of the section the task belongs to.
     * @return true if the task was deleted, false otherwise.
     */
    public boolean deleteTask(String taskName, String sectionName) {
        String sql = "DELETE t FROM tasks t JOIN sections s ON t.section_id = s.id WHERE t.name = ? AND s.name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) { // Added null check for connection
                System.err.println("Database connection is null for deleteTask.");
                return false;
            }

            pstmt.setString(1, taskName);
            pstmt.setString(2, sectionName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting task '" + taskName + "' from section '" + sectionName + "': " + e.getMessage());
            return false;
        }
    }
}
