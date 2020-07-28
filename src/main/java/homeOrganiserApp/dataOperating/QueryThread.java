package homeOrganiserApp.dataOperating;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public final class QueryThread extends Task<ObservableList<homeOrganiserApp.Task>> {
    @Override
    protected ObservableList<homeOrganiserApp.Task> call() {
        return TaskData.getInstance().loadTasks();
    }
}
