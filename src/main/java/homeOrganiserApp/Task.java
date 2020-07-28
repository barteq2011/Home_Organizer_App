package homeOrganiserApp;

import java.time.LocalDateTime;

public class Task {

    private int id;
    private String title;
    private String description;
    private LocalDateTime deadline;

    private Task(String title, String description, LocalDateTime deadline) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }

    public static Task newTask(String title, String description, LocalDateTime deadline) {
        return new Task(title, description, deadline);
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public int getId() {
        return id;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Other methods
    @Override
    public String toString() {
        return title;
    }
}
