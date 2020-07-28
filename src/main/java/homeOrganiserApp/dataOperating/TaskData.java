package homeOrganiserApp.dataOperating;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import homeOrganiserApp.Task;
import homeOrganiserApp.alerting.Alerts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;

public final class TaskData {

    private static final TaskData instance = new TaskData();

    // DB connection constants
    private static final String DB_NAME = "tasks.db";
    private static final String DB_FILE_PATH = System.getProperty("user.home")+"\\Documents\\"
            +DB_NAME;
    private static final String CONNECTION_STRING = "jdbc:sqlite:"+DB_FILE_PATH;
    // Tables and columns
    private static final String TABLE_TASKS = "Tasks";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "descripiton";
    private static final String COLUMN_DEADLINE = "deadline";

    // Queries
    private static final String CREATE_TASKS_TABLE_QUERY = "CREATE TABLE "+TABLE_TASKS+
            "("+COLUMN_ID+" INTEGER PRIMARY KEY, "+
            COLUMN_TITLE+" STRING, "+
            COLUMN_DESCRIPTION+" TEXT NOT NULL, "+
            COLUMN_DEADLINE+" STRING)";
    private static final String QUERY_TASKS = "SELECT "+
            COLUMN_ID+", "+
            COLUMN_TITLE+", "+
            COLUMN_DESCRIPTION+", "+
            COLUMN_DEADLINE+
            " FROM "+TABLE_TASKS;
    private static final String INSERT_NEW_TASK_QUERY = "INSERT INTO "+TABLE_TASKS+
            "("+COLUMN_TITLE+", "+
            COLUMN_DESCRIPTION+", "+
            COLUMN_DEADLINE+") VALUES (?, ?, ?)";
    private static final String UPDATE_TASK_QUERY = "UPDATE "+TABLE_TASKS+
            " SET "+COLUMN_TITLE+" = ?, "+
            COLUMN_DESCRIPTION+" = ?, "+
            COLUMN_DEADLINE+" = ? WHERE "+
            COLUMN_ID+" = ?";
    private static final String DELETE_TASK_QUERY = "DELETE FROM "+TABLE_TASKS+" WHERE "+COLUMN_ID+" = ?";
    private static final String COUNT_TASKS_QUERY = "SELECT COUNT("+COLUMN_ID+") FROM "+TABLE_TASKS;

    private Connection conn;
    private PreparedStatement createTasksTable;
    private PreparedStatement queryTasks;
    private PreparedStatement insertNewTask;
    private PreparedStatement updateTask;
    private PreparedStatement deleteTask;
    private PreparedStatement countTasks;
    // Flag to tell if db file ever existed in the system
    private boolean dbFileExisted = true;

    public static TaskData getInstance() { return instance; }

    public boolean open() {
        // If database file doesn't exist in the system, create it
        if(checkIfDatabaseFileExists()) {
            try {
                conn = DriverManager.getConnection(CONNECTION_STRING);
                if(!dbFileExisted) {
                    createTasksTable();
                }
                countTasks = conn.prepareStatement(COUNT_TASKS_QUERY);
                queryTasks = conn.prepareStatement(QUERY_TASKS);
                insertNewTask = conn.prepareStatement(INSERT_NEW_TASK_QUERY);
                updateTask = conn.prepareStatement(UPDATE_TASK_QUERY);
                deleteTask = conn.prepareStatement(DELETE_TASK_QUERY);
            } catch (SQLException e) {
                Alerts.showError("Error connecting to database");
                return false;
            }
        } else {
            // Flag of existing file is turned
            dbFileExisted = false;
            // Perform again with created file, but in knowledge that file not existed in the system and tables
            // and columns must be created
            open();
        }
        return true;
    }

    public void close() {
        try {
            if (createTasksTable != null)
                createTasksTable.close();
            if (queryTasks != null)
                queryTasks.close();
            if(insertNewTask!=null)
                insertNewTask.close();
            if(updateTask!=null)
                updateTask.close();
            if(deleteTask!=null)
                deleteTask.close();
            if(countTasks!=null)
                countTasks.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            Alerts.showError("Error closing connection with database");
        }
    }

    public ObservableList<Task> loadTasks() {
        ObservableList<Task> tasks = FXCollections.observableArrayList();
        try {
            ResultSet result = queryTasks.executeQuery();
            while(result.next()) {
                int id = result.getInt(1);
                String title = result.getString(2);
                String description = result.getString(3);
                LocalDateTime deadline = LocalDateTime.parse(result.getString(4));
                // Create new task
                Task task = Task.newTask(title, description, deadline);
                // Set task id used for deleting and updating
                task.setId(id);
                tasks.add(task);
            }
        } catch (SQLException e) {
            Alerts.showError("Error quering database");
        }
        return tasks;
    }
    private boolean checkIfDatabaseFileExists() {
        // Path to db file
        Path filePath = Paths.get(DB_FILE_PATH);
        try {
            // Creates db file if it didn't exist before
            if(!Files.exists(filePath)) {
                Files.createFile(filePath);
                return false;
            }
        } catch (IOException e) {
            Alerts.showError("Cannot create database file");
            return false;
        }
        return true;
    }
    public boolean saveNewTask(Task task) {
        try {
            // Generate id for task
            int id = generateIdForNewTask();
            if(id != -1) {
                // If task and id is valid, add it to database
                task.setId(id);
                insertNewTask.setString(1, task.getTitle());
                insertNewTask.setString(2, task.getDescription());
                insertNewTask.setString(3, task.getDeadline().toString());
                if (insertNewTask.executeUpdate() != 1)
                    throw new SQLException("Couldn't insert new task");
                return true;
            }
        } catch (SQLException e) {
             Alerts.showError("Error creating new task");
        }
        return false;
    }
    public boolean updateTask(Task task) {
        try {
            updateTask.setString(1, task.getTitle());
            updateTask.setString(2, task.getDescription());
            updateTask.setString(3, task.getDeadline().toString());
            updateTask.setInt(4, task.getId());
            if(updateTask.executeUpdate()!=1)
                throw new SQLException("Affected rows over 1");
        } catch (SQLException e) {
            Alerts.showError("Something went wrong updating task");
            return false;
        }
        return true;
    }
    public boolean deleteTask(int taskId) {
        try {
            deleteTask.setInt(1, taskId);
            if(deleteTask.executeUpdate()!=1)
                throw new SQLException("Affected rows over 1");
        } catch (SQLException e) {
            Alerts.showError("Error deleting task");
            return false;
        }
        return true;
    }
    private void createTasksTable() {
        try {
            createTasksTable = conn.prepareStatement(CREATE_TASKS_TABLE_QUERY);
            createTasksTable.execute();
        } catch (SQLException e) {
            Alerts.showError("Error creating tasks table");
        }
    }
    private int generateIdForNewTask() {
        try {
            // Get last task id
            ResultSet numberOfTasks = countTasks.executeQuery();
            // Generate new task id
            return numberOfTasks.getInt(1) + 1;
        } catch (SQLException e) {
            Alerts.showError("Error generating id for new task");
        }
        return -1;
    }
}
