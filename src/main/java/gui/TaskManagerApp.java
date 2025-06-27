package gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Priority;
import model.Section;
import model.Task;
import model.TaskManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TaskManagerApp extends Application {

    private TaskManager taskManager;
    private ListView<String> sectionListView;
    private ListView<Task> taskListView;

    private TextField newSectionNameField;
    private TextField taskNameField;
    private ComboBox<Priority> priorityComboBox;
    private DatePicker dueDatePicker;
    private TextField searchField;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("TaskManagerApp: Starting application...");
        taskManager = new TaskManager(); // Initialize TaskManager
        System.out.println("TaskManagerApp: TaskManager initialized.");


        primaryStage.setTitle("Personal Task Manager");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // --- Section Panel (Left) ---
        VBox sectionPanel = new VBox(10);
        sectionPanel.setPadding(new Insets(10));
        sectionPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        sectionPanel.setPrefWidth(200);

        Label sectionLabel = new Label("Sections");
        sectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        newSectionNameField = new TextField();
        newSectionNameField.setPromptText("New section name");

        Button addSectionButton = new Button("Add Section");
        addSectionButton.setMaxWidth(Double.MAX_VALUE);
        addSectionButton.setOnAction(e -> addSection());

        Button removeSectionButton = new Button("Remove Section");
        removeSectionButton.setMaxWidth(Double.MAX_VALUE);
        removeSectionButton.setOnAction(e -> removeSelectedSection());

        sectionListView = new ListView<>();
        sectionListView.setPrefHeight(300);
        sectionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateTaskListView(newVal);
            }
        });

        sectionPanel.getChildren().addAll(sectionLabel, newSectionNameField, addSectionButton, removeSectionButton, new Separator(), sectionListView);
        root.setLeft(sectionPanel);

        // --- Task Panel (Center) ---
        VBox taskPanel = new VBox(10);
        taskPanel.setPadding(new Insets(10));
        taskPanel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label taskLabel = new Label("Tasks");
        taskLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        taskNameField = new TextField();
        taskNameField.setPromptText("Task name");

        priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll(Priority.values());
        priorityComboBox.setPromptText("Select Priority");
        priorityComboBox.setMaxWidth(Double.MAX_VALUE);

        dueDatePicker = new DatePicker();
        dueDatePicker.setPromptText("Due Date");
        dueDatePicker.setMaxWidth(Double.MAX_VALUE);

        Button addTaskButton = new Button("Add Task");
        addTaskButton.setMaxWidth(Double.MAX_VALUE);
        addTaskButton.setOnAction(e -> addTask());

        Button removeTaskButton = new Button("Remove Task");
        removeTaskButton.setMaxWidth(Double.MAX_VALUE);
        removeTaskButton.setOnAction(e -> removeSelectedTask());

        taskListView = new ListView<>();
        taskListView.setPrefHeight(300);

        taskPanel.getChildren().addAll(taskLabel, taskNameField, priorityComboBox, dueDatePicker, addTaskButton, removeTaskButton, new Separator(), taskListView);
        root.setCenter(taskPanel);

        // --- Search and Sort Panel (Bottom) ---
        HBox searchSortPanel = new HBox(10);
        searchSortPanel.setPadding(new Insets(10));
        searchSortPanel.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        searchField = new TextField();
        searchField.setPromptText("Search tasks by name");
        searchField.setPrefWidth(250);

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchTasks());

        Button showAllTasksButton = new Button("Show All Tasks");
        showAllTasksButton.setOnAction(e -> displayAllTasks()); // Corrected line

        Button sortTasksButton = new Button("Sort All Tasks (Due Date & Priority)");
        sortTasksButton.setOnAction(e -> sortAndDisplayAllTasks());

        searchSortPanel.getChildren().addAll(searchField, searchButton, showAllTasksButton, sortTasksButton);
        root.setBottom(searchSortPanel);


        // Initial population of section list
        updateSectionListView();
        System.out.println("TaskManagerApp: Initial section list updated.");


        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        System.out.println("TaskManagerApp: GUI displayed.");
    }

    private void updateSectionListView() {
        System.out.println("TaskManagerApp: Updating section list view...");
        sectionListView.getItems().setAll(taskManager.getSectionNames());
        taskListView.getItems().clear();
        sectionListView.getSelectionModel().clearSelection();
        System.out.println("TaskManagerApp: Section list view updated. Number of sections: " + taskManager.getSectionNames().size());
    }

    private void updateTaskListView(String sectionName) {
        System.out.println("TaskManagerApp: Updating task list view for section: " + sectionName);
        List<Task> tasks = taskManager.getTasksForSection(sectionName);
        taskListView.getItems().setAll(tasks);
        System.out.println("TaskManagerApp: Task list view updated. Number of tasks for '" + sectionName + "': " + tasks.size());
    }

    private void addSection() {
        String sectionName = newSectionNameField.getText().trim();
        if (!sectionName.isEmpty()) {
            taskManager.addSection(sectionName);
            updateSectionListView();
            newSectionNameField.clear();
            System.out.println("TaskManagerApp: Section '" + sectionName + "' added.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Section name cannot be empty.");
        }
    }

    private void removeSelectedSection() {
        String selectedSection = sectionListView.getSelectionModel().getSelectedItem();
        if (selectedSection != null) {
            Optional<ButtonType> result = showAlert(Alert.AlertType.CONFIRMATION, "Confirm Deletion",
                    "Are you sure you want to delete section '" + selectedSection + "' and all its tasks?");
            if (result.isPresent() && result.get() == ButtonType.OK) {
                taskManager.removeSection(selectedSection);
                updateSectionListView();
                System.out.println("TaskManagerApp: Section '" + selectedSection + "' removed.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a section to remove.");
        }
    }

    private void addTask() {
        String selectedSection = sectionListView.getSelectionModel().getSelectedItem();
        if (selectedSection == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a section to add the task to.");
            return;
        }

        String taskName = taskNameField.getText().trim();
        Priority priority = priorityComboBox.getSelectionModel().getSelectedItem();
        LocalDate localDueDate = dueDatePicker.getValue();

        if (taskName.isEmpty() || priority == null || localDueDate == null) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill in all task details: Name, Priority, and Due Date.");
            return;
        }

        Date dueDate = Date.from(localDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Task newTask = new Task(taskName, priority, dueDate);
        taskManager.addTask(selectedSection, newTask);
        updateTaskListView(selectedSection);
        clearTaskInputFields();
        System.out.println("TaskManagerApp: Task '" + taskName + "' added to section '" + selectedSection + "'.");
    }

    private void removeSelectedTask() {
        String selectedSection = sectionListView.getSelectionModel().getSelectedItem();
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();

        if (selectedSection != null && selectedTask != null) {
            Optional<ButtonType> result = showAlert(Alert.AlertType.CONFIRMATION, "Confirm Deletion",
                    "Are you sure you want to delete task '" + selectedTask.getName() + "' from section '" + selectedSection + "'?");
            if (result.isPresent() && result.get() == ButtonType.OK) {
                taskManager.removeTask(selectedSection, selectedTask);
                updateTaskListView(selectedSection);
                System.out.println("TaskManagerApp: Task '" + selectedTask.getName() + "' removed from section '" + selectedSection + "'.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select both a section and a task to remove.");
        }
    }

    private void clearTaskInputFields() {
        taskNameField.clear();
        priorityComboBox.getSelectionModel().clearSelection();
        dueDatePicker.setValue(null);
    }

    private void searchTasks() {
        System.out.println("TaskManagerApp: Search button clicked.");
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Search", "Please enter a search term.");
            String selectedSection = sectionListView.getSelectionModel().getSelectedItem();
            if (selectedSection != null) {
                updateTaskListView(selectedSection);
            } else {
                displayAllTasks(); // Show all if no section selected and search is empty
            }
            return;
        }

        List<Task> results = taskManager.searchTasksByName(searchTerm);
        taskListView.getItems().setAll(results);
        sectionListView.getSelectionModel().clearSelection();
        System.out.println("TaskManagerApp: Search results displayed. Found " + results.size() + " tasks for '" + searchTerm + "'.");
    }

    private void sortAndDisplayAllTasks() {
        System.out.println("TaskManagerApp: Sort All Tasks button clicked.");
        List<Task> sortedTasks = taskManager.getSortedTasksByDueDateAndPriority();
        taskListView.getItems().setAll(sortedTasks);
        sectionListView.getSelectionModel().clearSelection();
        System.out.println("TaskManagerApp: Sorted tasks displayed. Total tasks: " + sortedTasks.size());
    }

    private void displayAllTasks() {
        System.out.println("TaskManagerApp: Display All Tasks button clicked.");
        List<Task> allTasks = taskManager.getAllTasks();
        taskListView.getItems().setAll(allTasks);
        sectionListView.getSelectionModel().clearSelection();
        System.out.println("TaskManagerApp: All tasks displayed. Total tasks: " + allTasks.size());
    }

    private Optional<ButtonType> showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
