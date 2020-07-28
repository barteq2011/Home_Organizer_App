package homeOrganiserApp;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import homeOrganiserApp.alerting.Alerts;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class TaskDialogWindowController {
    @FXML private TextField titleField, hourField, minuteField;
    @FXML private DatePicker deadlinePicker;
    @FXML private TextArea descriptionArea;

    @FXML public void initialize() {
        // Initial apperance properties
        deadlinePicker.getEditor().setDisable(true);
        hourField.setEditable(false);
        minuteField.setEditable(false);
    }

    // FXML handlers
    // Handling time operation buttons for datetime picker
    @FXML public void handleHoursPlusButton() {
        int hours = Integer.parseInt(hourField.getText()) + 1;
        if(hours<24) {
            hourField.setText(checkZeroNeeded(hours)+hours);
        }
        else
            hourField.setText("00");
    }
    @FXML public void handleHoursMinusButton() {
        int hours = Integer.parseInt(hourField.getText()) - 1;
        if(hours>=0) {
            hourField.setText(checkZeroNeeded(hours) + hours);
        }
        else
            hourField.setText("23");
    }
    @FXML public void handleMinutesPlusButton() {
        int minutes = Integer.parseInt(minuteField.getText()) + 1;
        if(minutes<=59) {
            minuteField.setText(checkZeroNeeded(minutes) + minutes);
        }
        else
            minuteField.setText("00");
    }
    @FXML public void handleMinutesMinusButton() {
        int minutes = Integer.parseInt(minuteField.getText()) - 1;
        if(minutes>=0) {
            minuteField.setText(checkZeroNeeded(minutes)+minutes);
            minuteField.setText(checkZeroNeeded(minutes)+minutes);
        }
        else
            minuteField.setText("59");
    }
    // Non FXML functions
    public Task getTaskFromFields() {
        try {
            String title = titleField.getText();
            LocalDate selectedDate = deadlinePicker.getValue();
            int hours = Integer.parseInt(hourField.getText());
            int minutes = Integer.parseInt(minuteField.getText());
            LocalDateTime finalDateTime = selectedDate.atTime(hours, minutes);
            String description = descriptionArea.getText();
            if(title.isEmpty() || title.contains("#") || title.contains(";") || description.isEmpty()
                || description.contains("#") || description.contains(";")) throw new Exception();
            return Task.newTask(title, description, finalDateTime);
        } catch (Exception e) {
            Alerts.showError("Wszystkie pola są wymagane.\nSprawdź czy w nazwa i opis nie zawierają '#' lub ';'");
        }
        return null;
    }
    public void fillFields(Task task) {
        String title = task.getTitle();
        LocalDate deadlineDate = task.getDeadline().toLocalDate();
        String deadlineHours = String.valueOf(task.getDeadline().getHour());
        if(deadlineHours.length()<=1)
            deadlineHours = "0".concat(deadlineHours);
        String deadlineMinutes = String.valueOf(task.getDeadline().getMinute());
        if(deadlineMinutes.length()<=1)
            deadlineMinutes = "0".concat(deadlineMinutes);
        String description = task.getDescription();
        titleField.setText(title);
        deadlinePicker.setValue(deadlineDate);
        hourField.setText(deadlineHours);
        minuteField.setText(deadlineMinutes);
        descriptionArea.setText(description);
    }
    private String checkZeroNeeded(int time) {
        String neededZero = "";
        if(time/10 < 1)
            neededZero = "0";
        return neededZero;
    }
}
