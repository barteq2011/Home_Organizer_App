package homeOrganiserApp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import homeOrganiserApp.alerting.Alerts;
import homeOrganiserApp.dataOperating.QueryThread;
import homeOrganiserApp.dataOperating.TaskData;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class MainWindowController {
    private DateTimeFormatter formatter;
    @FXML private MenuBar mainMenuBar;
    @FXML private ListView<Task> tasksList;
    @FXML private Label taskTitleLabel, taskDeadLineLabel;
    @FXML private TextArea taskDescriptionArea;
    @FXML private BorderPane mainBorderPane;

    @FXML public void initialize() {
        // Initialize formatter for displayed task date
        formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss");
        // Initial values of taskList
        // List content display
        tasksList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Task> call(ListView<Task> taskListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Task task, boolean b) {
                        super.updateItem(task, b);
                        if(!b) {
                            setText(task.getTitle());
                            // Tasks, which deadline date is tomorrow will be displaying in red color
                            if(task.getDeadline().getYear() == LocalDateTime.now().getYear() &&
                                    task.getDeadline().getMonth() == LocalDate.now().getMonth()) {
                                if(LocalDateTime.now().getDayOfMonth()+1 == task.getDeadline().getDayOfMonth())
                                    setTextFill(Color.ORANGE);
                                else if(LocalDateTime.now().getDayOfMonth() == task.getDeadline().getDayOfMonth())
                                    setTextFill(Color.RED);
                            }
                        } else
                            setText(null);
                    }
                };
            }
        });
        tasksList.setItems(TaskData.getInstance().loadTasks());
        if(tasksList.getItems()!=null) {
            if (tasksList.getItems().isEmpty())
                Alerts.showInformation("Twoja lista zadań jest pusta :(\n" +
                        "Dodaj nowe używając menu");
        }
        // After selection of task, those properties will be displaying in title label, deadline label and
        // description text area
        tasksList.getSelectionModel().selectedItemProperty().addListener((observableValue, task, t1) -> {
            if(t1!=null) {
                Task selectedTask = tasksList.getSelectionModel().getSelectedItem();
                taskDescriptionArea.setText(selectedTask.getDescription());
                taskTitleLabel.setText(selectedTask.getTitle());
                taskDeadLineLabel.setText(selectedTask.getDeadline().format(formatter));
            }
        });
        // Initial selection
        tasksList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tasksList.getSelectionModel().selectFirst();
        // Show tray warnings if tasks is close to actual date
        checkForComingTasks();
    }
    // FXML handlers
    // Top Menubar items handlers
    @FXML public void handleNewTaskMenuItem() { showTaskDialog(); }
    @FXML public void handleModifyMenuItem() {
        Task selectedTask = tasksList.getSelectionModel().getSelectedItem();
        showTaskDialog(selectedTask);
    }
    @FXML public void handleDeleteMenuItem() {
        Task selectedTask = tasksList.getSelectionModel().getSelectedItem();
        if(selectedTask!=null) {
            Alert deleteAlert = new Alert(Alert.AlertType.NONE);
            ButtonType deleteButtonType = new ButtonType("Usuń");
            ButtonType cancelButtonType = new ButtonType("Anuluj");
            deleteAlert.getButtonTypes().addAll(deleteButtonType, cancelButtonType);
            deleteAlert.setTitle("Usuwanie zadania");
            deleteAlert.setHeaderText(null);
            deleteAlert.setContentText("Czy na pewno chcesz usunąć to zadanie "+selectedTask.getTitle()+" ?");
            Optional<ButtonType> result = deleteAlert.showAndWait();
            if (result.isPresent() && result.get() == deleteButtonType) {
                if(TaskData.getInstance().deleteTask(selectedTask.getId())) {
                    loadTasksList();
                    if(tasksList.getItems()!=null)
                        if (!tasksList.getItems().isEmpty())
                            tasksList.getSelectionModel().selectFirst();
                    else {
                        taskTitleLabel.setText("");
                        taskDescriptionArea.setText("");
                        taskDeadLineLabel.setText("");
                    }
                }
            }
        }
    }
    @FXML public void handleAboutMenuItem() {
        Alerts.showInformation("Organizer domowy 1.0\nBy Bartosz Bartosik");
    }
    @FXML public void handleExitMenuItem() {
        // Saving items before exiting
        Platform.exit();
    }
    // Non FXML methods
    //
    // Inducing dialog for adding or modyfing task
    private void showTaskDialog(Task task) {
        Dialog<ButtonType> taskDialog = new Dialog<>();
        taskDialog.setTitle("Dodawanie/Modyfikowanie zadania");
        taskDialog.setHeaderText("Wprowadź dane zadania");
        taskDialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("dialogs/taskDialogWindow.fxml"));
        taskDialog.initStyle(StageStyle.TRANSPARENT);
        try {
            taskDialog.getDialogPane().setContent(loader.load());
            TaskDialogWindowController controller = loader.getController();
            ButtonType cancelButtonType = new ButtonType("Anuluj");
            ButtonType applyButtonType = new ButtonType("Akceptuj");
            taskDialog.getDialogPane().getButtonTypes().addAll(applyButtonType ,cancelButtonType);
            // Checks if dialog window is induced for modyfing item
            if(task!=null)
                controller.fillFields(task);
            Optional<ButtonType> result = taskDialog.showAndWait();
            if(result.isPresent() && result.get() == applyButtonType) {
                // Task returned from controller method
                Task newTask = controller.getTaskFromFields();
                //If goal of dialog is not to modify the task
                    if (task == null) {
                        if (newTask != null) {
                            if(TaskData.getInstance().saveNewTask(newTask))
                                loadTasksList();
                        }
                        else
                            showTaskDialog();
                        tasksList.getSelectionModel().select(newTask);
                    } else {
                        if(newTask != null) {
                            task.setTitle(newTask.getTitle());
                            task.setDescription(newTask.getDescription());
                            task.setDeadline(newTask.getDeadline());
                            if(TaskData.getInstance().updateTask(task)) {
                                loadTasksList();
                                tasksList.getSelectionModel().clearSelection();
                                tasksList.getSelectionModel().select(task);
                            }
                        } else showTaskDialog(task);
                    }
                }
        } catch (IOException e) {
            Alerts.showError("Nie można otworzyć okna dialogowego\n" +
                    "Na komputerze nie znaleziono odpowidniego pliku fxml\n" +
                    "Spróbuj ponownie zainstalować program aby naprawić ten problem");
            //e.printStackTrace();
        }
    }
    // Overloaded dialog method designed for adding new task
    private void showTaskDialog() { showTaskDialog(null); }
    // Returns menuBar for window dragging
    public MenuBar getMenuBar() { return  mainMenuBar; }
    // Checks if there are task to do
    private void checkForComingTasks() {
        if(tasksList.getItems()!=null) {
            for (Task iterTask : tasksList.getItems()) {
                if (iterTask.getDeadline().getYear() == LocalDateTime.now().getYear()
                        && iterTask.getDeadline().getMonth() == LocalDateTime.now().getMonth()) {
                    if (iterTask.getDeadline().getDayOfMonth() == LocalDateTime.now().getDayOfMonth()) {
                        Alerts.showTrayWarning("Dzisiejsze zadania czekają na wykonanie :)");
                        break;
                    } else if (iterTask.getDeadline().getDayOfMonth() == LocalDateTime.now().getDayOfMonth() + 1) {
                        Alerts.showTrayWarning("Jutrzejsze zadania czekają na wykonanie :)");
                        break;
                    }
                }
            }
        }
    }
    // Fill list of tasks by quering the database in separate thread
    private void loadTasksList() {
        QueryThread thread = new QueryThread();
        tasksList.itemsProperty().bind(thread.valueProperty());
        new Thread(thread).start();
    }
}
