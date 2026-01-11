package application;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.geometry.Pos;
import javafx.application.Platform;

// Imports for real-time and date/time
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
//imports for printing
import javafx.print.PrinterJob;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;


public class HomeViewControl implements Initializable {

    private List<String> selectedDays = new ArrayList<>();
	private Stage stage;
	private Scene scene;
	private Parent root;

    // FXML INJECTIONS
    @FXML private HBox taskMenuOne;
    @FXML private VBox taskMenuTwo;

    // Main Display Pane
    @FXML private Pane dashBoardMain;

    // SCHEDULE CREATION PANE INJECTIONS
    @FXML private TextField noteField;
    @FXML private Pane createSchedulePane;
    @FXML private TextField titleField;

    // Start Time Fields (Hour/Minute)
    @FXML private TextField monthField; // Used for Start Hour (0-23)
    @FXML private TextField dayField;   // Used for Start Minute (0-59)

    // Duration Timer Fields
    @FXML private TextField hourField;   // Used for Duration Hours
    @FXML private TextField minuteField; // Used for Duration Minutes
    @FXML private TextField secondsField;

    // Day Buttons
    @FXML private ToggleButton sunButton;
    @FXML private ToggleButton monButton;
    @FXML private ToggleButton tueButton;
    @FXML private ToggleButton wedButton;
    @FXML private ToggleButton thuButton;
    @FXML private ToggleButton friButton;
    @FXML private ToggleButton satButton;
    
    // --- NEW ACTION BUTTONS FROM FXML ---
    @FXML private Button editDetails;
    @FXML private Button editSchedule;
    @FXML private Button pauseResumeTask;

    @FXML private Button search;
    @FXML private TextField searchField;

    private Map<String, DayOfWeek> dayMap;
    private Timeline timeline; // for real-time updates
    private TaskScheduler selectedTask; // ⭐️ Holds the manually selected task ⭐️
    private boolean isEditingMode = false; // 🌟 NEW STATE VARIABLE 🌟

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dayMap = Map.of(
            sunButton.getId(), DayOfWeek.SUNDAY,
            monButton.getId(), DayOfWeek.MONDAY,
            tueButton.getId(), DayOfWeek.TUESDAY,
            wedButton.getId(), DayOfWeek.WEDNESDAY,
            thuButton.getId(), DayOfWeek.THURSDAY,
            friButton.getId(), DayOfWeek.FRIDAY,
            satButton.getId(), DayOfWeek.SATURDAY
        );

        loadSavedTasks();

        if (searchField != null) {
            // Use change listener for real-time filtering as the user types
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterTasks(newValue);
            });
        }
        // Hide the action buttons until the user selects a task from the list
        if (taskMenuOne != null) {
            taskMenuOne.setVisible(false);
        }
        updateDashboardMain();
        startTaskMonitor();
    }

    /** Initializes and starts the Timeline for continuous task monitoring. */
    private void startTaskMonitor() {
        timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
            	updateTimeLabelsOnly();
                checkAndStartTasks();
                updateDashboardMain();
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
   
    private void updateTimeLabelsOnly() {
        if (taskMenuTwo == null || GlobalData.schedules == null) return;

        for (Node node : taskMenuTwo.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;

                Label timeLabel = null;

                // find the time-status-label within the row
                for (Node child : row.getChildren()) {
                    if (child instanceof Label) {
                        Label lbl = (Label) child;
                        if (lbl.getStyleClass().contains("time-status-label")) {
                            timeLabel = lbl;
                            break;
                        }
                    }
                }

                if (timeLabel == null) continue; // nothing to update for this row

                TaskScheduler task = findTaskByRow(row);
                if (task == null) {
                    // safety fallback: don't modify label if we can't find the task
                    continue;
                }

                // update text (and optionally style)
                String status = calculateStatus(task);
                timeLabel.setText(status);

                // optional: update inline style color like before
                if (task.getCurrentStartTime() != null) {
                    long secondsLeft = ChronoUnit.SECONDS.between(
                            LocalDateTime.now(),
                            task.getCurrentStartTime().plusSeconds(task.getTotalDurationSeconds())
                    );
                    if (secondsLeft <= 0) {
                        timeLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        timeLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                } else {
                    timeLabel.setStyle("-fx-text-fill: green;");
                }
            }
        }
    }

    // Safer findTaskByRow: finds the label with style class "task-name-label" and matches by text
    private TaskScheduler findTaskByRow(HBox row) {
        String name = null;

        for (Node child : row.getChildren()) {
            if (child instanceof Label) {
                Label lbl = (Label) child;
                if (lbl.getStyleClass().contains("task-name-label")) {
                    name = lbl.getText();
                    break;
                }
            }
        }

        if (name == null || GlobalData.schedules == null) return null;

        for (TaskScheduler t : GlobalData.schedules) {
            if (t.getNote() != null && t.getNote().equals(name)) {
                return t;
            }
        }
        return null;
    }

    // calculate status string (uses TaskScheduler.getTotalDurationSeconds())
    private String calculateStatus(TaskScheduler task) {
        if (task == null) return "";

        // Assume TaskScheduler has isPaused() method
        if (task.isPaused()) {
            return "PAUSED";
        }
        
        if (task.getCurrentStartTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            long totalDurationSeconds = task.getTotalDurationSeconds();
            long secondsLeft = ChronoUnit.SECONDS.between(now, task.getCurrentStartTime().plusSeconds(totalDurationSeconds));

            if (secondsLeft <= 0) {
                return "TIME'S UP!";
            } else {
                long hrs = secondsLeft / 3600;
                long mins = (secondsLeft % 3600) / 60;
                long secs = secondsLeft % 60;
                return String.format("RUNNING: %02d:%02d:%02d left", hrs, mins, secs);
            }
        } else {
            // scheduled (not running) case
            return calculateTimeRemaining(task);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ----------------------------------------------------
    // ACTION HANDLERS FOR DASHBOARD BUTTONS
    // ----------------------------------------------------

    /**
     * Edits ONLY Title and Notes. 
     * Disables the Time, Duration, and Day selection sections.
     */
    private void editTaskDetails(TaskScheduler task) {
        this.selectedTask = task;
        isEditingMode = true;

        // 1. Populate only text fields
        titleField.setText(task.getNote());
        noteField.setText(task.getNoteDetail());

        // 2. Lock the scheduling sections so the user can't change them
        // (Assuming the Panes are children of createSchedulePane)
        // We use setDisable(true) to make them look "grayed out" or read-only
        // Show Details, Hide Schedule
        setTitleNoteSectionVisible(true);
        setSchedulingSectionsVisible(false);

        // 3. Show the form
        showScheduleForm(null);
    }
    
    //helper methods for the three buttons:
    private void setTitleNoteSectionVisible(boolean visible) {
        titleField.setVisible(visible);
        titleField.setManaged(visible);
        noteField.setVisible(visible);
        noteField.setManaged(visible);
        
        // Also toggle the disabled state based on visibility
        titleField.setDisable(!visible);
        noteField.setDisable(!visible);
    }

    private void setSchedulingSectionsVisible(boolean visible) {
        Node[] scheduleNodes = {
            monthField, dayField, hourField, minuteField, secondsField,
            sunButton, monButton, tueButton, wedButton, thuButton, friButton, satButton
        };

        for (Node node : scheduleNodes) {
            if (node != null) {
                node.setVisible(visible);
                node.setManaged(visible);
                node.setDisable(!visible); // If it's hidden, it's also disabled
            }
        }
    }

    /**
     * Edits ONLY Scheduling (Time, Duration, Days).
     * Disables the Title and Note fields.
     */
    private void editTaskSchedule(TaskScheduler task) {
        this.selectedTask = task;
        isEditingMode = true;

     // Populate existing time data
        monthField.setText(String.valueOf(task.getStartHour()));
        dayField.setText(String.valueOf(task.getStartMinute()));
        hourField.setText(String.valueOf(task.getDurationHours()));
        minuteField.setText(String.valueOf(task.getDurationMinutes()));
        if (secondsField != null) secondsField.setText(String.valueOf(task.getDurationSeconds()));

        // 2. Reset and Populate Days
        for (ToggleButton b : new ToggleButton[]{sunButton, monButton, tueButton, wedButton, thuButton, friButton, satButton}) {
            if (b != null) b.setSelected(false);
        }
        selectedDays.clear();
        
        for (String dayId : task.getRecurringDays()) {
            Node node = createSchedulePane.lookup("#" + dayId);
            if (node instanceof ToggleButton) {
                ToggleButton btn = (ToggleButton) node;
                btn.setSelected(true);
                selectedDays.add(dayId);
            }
        }

     // ONLY show scheduling info
        setTitleNoteSectionVisible(false);
        setSchedulingSectionsVisible(true);

        showScheduleForm(null);
    }

    /**
     * Handles the action for pausing/resuming the task countdown.
     * Assumes TaskScheduler has isPaused() and setPaused(boolean) methods.
     */
    private void toggleTaskPause(TaskScheduler task) {
        if (task.isPaused()) {
            task.setPaused(false);
            showAlert("Task Resumed", task.getNote() + " has been resumed.");
        } else {
            task.setPaused(true);
            showAlert("Task Paused", task.getNote() + " has been paused.");
        }
        // Force the dashboard and UI to update immediately
        updateDashboardMain();
        updateTaskUI(); 
    }
    
    // ----------------------------------------------------
    // TASK MONITORING LOGIC
    // ----------------------------------------------------

    /** Checks if any recurring task should start now and monitors its duration. */
    private void checkAndStartTasks() {
        // We check against the minute for recurring tasks, then against seconds for duration
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        if (GlobalData.schedules == null) return;

        for (TaskScheduler task : GlobalData.schedules) {
            
            // ⭐ Skip processing if the task is currently paused
            if (task.isPaused()) {
                continue;
            }

            // 1. Check if the task is due to START now
            if (task.getCurrentStartTime() == null) {

                LocalDateTime nextDueTime = calculateNextDueTime(task);

                // Use 'between' for minute check, and check if it's not in the past
                if (nextDueTime != null && ChronoUnit.MINUTES.between(now, nextDueTime) == 0 && nextDueTime.isAfter(now.minusSeconds(1))) {

                    // START THE TASK!
                    task.setCurrentStartTime(now.withSecond(0).withNano(0));
                    task.setAlarmTriggered(false); // Reset alarm for the new run
                    System.out.println("TASK STARTED: " + task.getNote() + " at " + now.toLocalTime());
                }
            }

            // 2. Check if the currently running task is due to STOP (Duration Check)
            if (task.getCurrentStartTime() != null) {

                long totalDurationSeconds = (task.getDurationHours() * 3600) +
                                            (task.getDurationMinutes() * 60) +
                                            task.getDurationSeconds();

                LocalDateTime endTime = task.getCurrentStartTime().plusSeconds(totalDurationSeconds);

                if (LocalDateTime.now().isAfter(endTime) || LocalDateTime.now().isEqual(endTime)) {

                    // 3. Only trigger the alarm if it hasn't been triggered for this run
                    if (!task.isAlarmTriggered()) {

                        Platform.runLater(() -> {
                            showAlert("ALARM! Time's Up!", "The scheduled task: " + task.getNote() + " has finished its duration.");
                        });

                        task.setAlarmTriggered(true); // Set the flag to true
                        System.out.println("TASK STOPPED: " + task.getNote() + " and Alarm Triggered.");
                        // Keep currentStartTime set so UI can show "TIME'S UP!" until the next run loop rebuilds the UI
                    }
                }
            }
        }
    }

    private void updateTaskUI() {
        filterTasks(searchField != null ? searchField.getText().trim() : "");
    }

    // ----------------------------------------------------
    // TASK DISPLAY AND REBUILD LOGIC
    // ----------------------------------------------------

    public boolean filterTasks(String query) {
        if (taskMenuTwo == null || GlobalData.schedules == null) return false;

        String lowerCaseQuery = query.toLowerCase().trim();
        int tasksFound = 0;

        taskMenuTwo.getChildren().clear();
        
        // Iterate backwards to put newest tasks at the top of the VBox (if you want LIFO display)
        for (int i = GlobalData.schedules.size() - 1; i >= 0; i--) {
            TaskScheduler task = GlobalData.schedules.get(i);
            String taskName = task.getNote();

            if (taskName.toLowerCase().contains(lowerCaseQuery) || lowerCaseQuery.isEmpty()) {

                HBox taskRow = new HBox(10);
                taskRow.setAlignment(Pos.CENTER_LEFT);
                taskRow.setMaxWidth(Double.MAX_VALUE);
                taskRow.getStyleClass().add("icons-bilog");
                taskRow.setPrefHeight(60.0);

                // Click handler to select task
                taskRow.setOnMouseClicked(event -> {
                    this.selectedTask = task;
                    displayTaskOnDashboard(task);

                    // Deselect all others, select this one
                    taskMenuTwo.getChildren().forEach(node -> node.setStyle(null));
                    taskRow.setStyle("-fx-background-color: #616161;");
                });

                Label nameLabel = new Label(taskName);
                nameLabel.getStyleClass().add("task-name-label");

                Label timeLabel = new Label();
                timeLabel.getStyleClass().add("time-status-label");

                HBox.setHgrow(nameLabel, Priority.ALWAYS); // allow label to take space and push menu right

                // Set status text
                String statusText;
                // Handle paused state if TaskScheduler supports it
                if (task.isPaused()) {
                    statusText = "PAUSED";
                    timeLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                } else if (task.getCurrentStartTime() != null) {
                    // Running/Time's Up status
                    LocalDateTime now = LocalDateTime.now();
                    long totalDurationSeconds = (task.getDurationHours() * 3600) +
                                                (task.getDurationMinutes() * 60) +
                                                task.getDurationSeconds();
                    long secondsLeft = ChronoUnit.SECONDS.between(now, task.getCurrentStartTime().plusSeconds(totalDurationSeconds));

                    if (secondsLeft <= 0) {
                        statusText = "TIME'S UP!";
                        timeLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        long hrs = secondsLeft / 3600;
                        long mins = (secondsLeft % 3600) / 60;
                        long secs = secondsLeft % 60;

                        statusText = String.format("RUNNING: %02d:%02d:%02d left", hrs, mins, secs);
                        timeLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                } else {
                    // Scheduled status
                    statusText = calculateTimeRemaining(task);
                    timeLabel.setStyle("-fx-text-fill: green;");
                }
                timeLabel.setText(statusText);

                // -----------------Option Menu per Task ------------------------------------------------------------------------------------------------------------------------------------------
                MenuButton optionsMenu = new MenuButton("⋮");
                optionsMenu.setStyle("-fx-font-size: 20px; -fx-background-color: TRANSPARENT ; -fx-background-radius: 50px;");

                //INSIDE OF MENU BUTTON
                MenuItem prioItem = new MenuItem("Add Priority");
                prioItem.setOnAction(e -> {
                    if (!GlobalData.prioTasks.contains(task)) {
                        GlobalData.prioTasks.add(task);
                        task.setPriority(true);            // Set priority
                        TaskDatabase.saveTasks();          // Save changes
                        showAlert("Priority Added", task.getNote() + " is now a priority task!");
                    } else {
                        showAlert("Already Priority", "This task is already marked as priority.");
                    }
                    optionsMenu.hide(); // Manually hide the menu after action is taken
                });

                MenuItem deleteItem = new MenuItem("Delete Task");
                deleteItem.setOnAction(e -> {
                    GlobalData.schedules.remove(task);
                    GlobalData.prioTasks.remove(task);
                    taskMenuTwo.getChildren().remove(taskRow);

                    TaskDatabase.saveTasks();//database
                    
                    if (task == selectedTask) {
                        selectedTask = null;
                        dashBoardMain.getChildren().clear();
                    }
                    optionsMenu.hide(); 
                    updateTaskUI();
                });

           
                optionsMenu.setOnMousePressed(e -> {
                    if(optionsMenu.isShowing()) {
                        optionsMenu.hide();
                    } else {
                        optionsMenu.show();
                    }
                    e.consume(); // Prevents the default click action from interfering
                });


                optionsMenu.getItems().addAll(prioItem, deleteItem);

                // Add components to HBox with spacing
                taskRow.getChildren().addAll(nameLabel, timeLabel, optionsMenu);
                taskMenuTwo.getChildren().add(taskRow);

                // Highlight if it's the currently selected task
                if (task == this.selectedTask) {
                    taskRow.setStyle("-fx-background-color: #616161;");
                }

                tasksFound++;
            }
        }

        return tasksFound > 0;
    }

    public void loadSavedTasks() {
        if (taskMenuOne == null || taskMenuTwo == null) {
            System.out.println("FXML components (taskMenuOne/taskMenuTwo) not yet loaded for this scene.");
            return;
        }
        updateTaskUI();
    }

    // ----------------------------------------------------
    // DASHBOARD DISPLAY LOGIC
    // ----------------------------------------------------

    /**
     * Updates dashBoardMain to display the details of a specific TaskScheduler object.
     */
    private void displayTaskOnDashboard(TaskScheduler task) {
        if (dashBoardMain == null || task == null) {
            if (dashBoardMain != null) dashBoardMain.getChildren().clear();
            // Hide the top menu if no task is selected
            if (taskMenuOne != null) taskMenuOne.setVisible(false);
            return;
        }

        // 1. Clear the main area and show the action menu
        dashBoardMain.getChildren().clear();
        if (taskMenuOne != null) taskMenuOne.setVisible(true);

        // --- RENDER CONTENT LABELS ---

        // Title Label
        Label titleLabel = new Label(task.getNote());
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        titleLabel.setLayoutX(20);
        titleLabel.setLayoutY(20);

        // Timer Label Logic
        Label timerLabel = new Label();
        String timerText;
        String timerStyle;

        if (task.isPaused()) {
             timerText = "Status: PAUSED";
             timerStyle = "-fx-font-size: 24px; -fx-text-fill: blue; -fx-font-weight: bold;";
        } else if (task.getCurrentStartTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            long totalSeconds = (task.getDurationHours() * 3600) + (task.getDurationMinutes() * 60) + task.getDurationSeconds();
            long secondsLeft = ChronoUnit.SECONDS.between(now, task.getCurrentStartTime().plusSeconds(totalSeconds));

            if (secondsLeft <= 0) {
                timerText = "Status: TIME'S UP!";
                timerStyle = "-fx-font-size: 24px; -fx-text-fill: orange; -fx-font-weight: bold;";
            } else {
                long hrs = secondsLeft / 3600;
                long mins = (secondsLeft % 3600) / 60;
                long secs = secondsLeft % 60;
                timerText = String.format("RUNNING: %02d:%02d:%02d left", hrs, mins, secs);
                timerStyle = "-fx-font-size: 24px; -fx-text-fill: red; -fx-font-weight: bold;";
            }
        } else {
            timerText = "Next Start: " + calculateTimeRemaining(task);
            timerStyle = "-fx-font-size: 24px; -fx-text-fill: green;";
        }

        timerLabel.setText(timerText);
        timerLabel.setStyle(timerStyle);
        timerLabel.setLayoutX(20);
        timerLabel.setLayoutY(70);

        // Note Details
        Label noteHeader = new Label("Details:");
        noteHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        noteHeader.setLayoutX(20);
        noteHeader.setLayoutY(120);

        Label noteLabel = new Label(task.getNoteDetail());
        noteLabel.setWrapText(true);
        noteLabel.setPrefWidth(740);
        noteLabel.setPrefHeight(400);
        noteLabel.setLayoutX(20);
        noteLabel.setLayoutY(150);
        noteLabel.setAlignment(Pos.TOP_LEFT);

        // Add all labels to the dashboard
        dashBoardMain.getChildren().addAll(titleLabel, timerLabel, noteHeader, noteLabel);

        // --- CONFIGURE THE NEW ACTION BUTTONS ---
        // 1. Edit Details Action
        //editDetails.setText("Edit Info"); // Or use a graphic/icon
        editDetails.setGraphic(createIcon("edit-info.png")); // Your filename here
        editDetails.setOnAction(e -> editTaskDetails(task));


        // 2. Edit Schedule Action
        //editSchedule.setText("Schedule");
        editSchedule.setGraphic(createIcon("edit-schedule.png"));
        editSchedule.setOnAction(e -> editTaskSchedule(task));

        // 3. Pause/Resume Action
        // Update button text based on current state
        //pauseResumeTask.setText(task.isPaused() ? "Resume" : "Pause");
        String statusIcon = task.isPaused() ? "resume.png" : "pause.png";
        pauseResumeTask.setGraphic(createIcon(statusIcon));
        pauseResumeTask.setOnAction(e -> {
            toggleTaskPause(task);
            // Immediately refresh the dashboard and button text after toggling
            displayTaskOnDashboard(task); 
        });
        
        
    }
    /**
     * Helper method to create formatted ImageViews
     */
    private ImageView createIcon(String iconName) {
        try {
            Image img = new Image(getClass().getResourceAsStream(iconName));
            ImageView view = new ImageView(img);
            view.setFitHeight(50);
            view.setFitWidth(50);
            view.setPreserveRatio(true);
            return view;
        } catch (Exception e) {
            System.out.println("Could not load icon: " + iconName);
            return null;
        }
    
    }
         

    /**
     * Updates dashBoardMain. Prioritizes displaying the user's manually selected task.
     */
    private void updateDashboardMain() {
        if (dashBoardMain == null || GlobalData.schedules == null || GlobalData.schedules.isEmpty()) {
            if (dashBoardMain != null) dashBoardMain.getChildren().clear();
            return;
        }

        TaskScheduler taskToDisplay = null;

        // 1. Check if the user has manually selected a task. If so, display it.
        if (this.selectedTask != null) {
            taskToDisplay = this.selectedTask;
        } else {
            // 2. If no task is selected, find the most relevant task (running or soonest)
            taskToDisplay = GlobalData.schedules.stream()
                .sorted((t1, t2) -> {
                    // Running tasks have highest priority
                    if (t1.getCurrentStartTime() != null && t2.getCurrentStartTime() == null) return -1;
                    if (t1.getCurrentStartTime() == null && t2.getCurrentStartTime() != null) return 1;

                    // If neither is running, prioritize the one starting soonest
                    LocalDateTime next1 = calculateNextDueTime(t1);
                    LocalDateTime next2 = calculateNextDueTime(t2);

                    if (next1 == null && next2 == null) return 0;
                    if (next1 == null) return 1;
                    if (next2 == null) return -1;

                    return next1.compareTo(next2);
                })
                .findFirst().orElse(null);
        }

        if (taskToDisplay == null) return;

        // 3. Display the determined task.
        displayTaskOnDashboard(taskToDisplay);
    }

    // ----------------------------------------------------
    // SCENE/FORM MANAGEMENT
    // ----------------------------------------------------

    @FXML
    public void searchTasks(ActionEvent event) {
        if (searchField != null) {
            String query = searchField.getText().trim();
            System.out.println("Search executed for query: " + query);

            boolean tasksFound = filterTasks(query);

            if (!tasksFound) {
                    showAlert("Search Result", "\"" + query + "\" Not Found :(");
            }
        }
    }

    @FXML
    public void showScheduleForm(ActionEvent event) {
        if (createSchedulePane != null) {
        	// If event is not null, it means the user clicked the "+" button, not an edit button
            if (event != null) {
                setTitleNoteSectionVisible(true);
                setSchedulingSectionsVisible(true);
                isEditingMode = false; 
                // clear fields here if needed
            }        	
            createSchedulePane.setVisible(true);
            createSchedulePane.toFront();
        }
    }

    @FXML
    public void hideScheduleForm(ActionEvent event) {
        if (createSchedulePane != null) {
            createSchedulePane.setVisible(false);
            // RE-ENABLE EVERYTHING for the next use
            setTitleNoteSectionVisible(true);
            setSchedulingSectionsVisible(true);
            isEditingMode = false;
            
            titleField.clear();
            noteField.clear();
            monthField.clear();
            dayField.clear();
            hourField.clear();
            minuteField.clear();
            if (secondsField != null) secondsField.clear();

            for (ToggleButton b : new ToggleButton[]{sunButton, monButton, tueButton, wedButton, thuButton, friButton, satButton}) {
                if (b != null) {
                    b.setSelected(false);
                }
            }
            selectedDays.clear();
            
            //  FIX: Reset Editing Mode 
            isEditingMode = false;
        }
    }

    @FXML
    public void toggleDaySelection(ActionEvent event) {
        ToggleButton source = (ToggleButton) event.getSource();

        String dayId = source.getId();

        if (source.isSelected()) {
            selectedDays.add(dayId);
        } else {
            selectedDays.remove(dayId);
        }
    }

    /** Creates a TaskScheduler object or Updates the selected task. */
    @FXML
    public void saveSchedule(ActionEvent event) {
        // 1. Get Title and Note (Use existing if field is disabled)
        String title = titleField.isDisabled() ? selectedTask.getNote() : titleField.getText().trim();
        String noteDetailText = noteField.isDisabled() ? selectedTask.getNoteDetail() : noteField.getText().trim();

        try {
            int startHour, startMinute, durationHours, durationMinutes, durationSeconds;
            List<String> days;

            // 2. Conditional Logic: If Scheduling is disabled, keep existing values
            if (monthField.isDisabled()) {
                // Keep existing schedule data
                startHour = selectedTask.getStartHour();
                startMinute = selectedTask.getStartMinute();
                durationHours = selectedTask.getDurationHours();
                durationMinutes = selectedTask.getDurationMinutes();
                durationSeconds = selectedTask.getDurationSeconds();
                days = new ArrayList<>(selectedTask.getRecurringDays());
            } else {
                // Validate and parse new schedule data
                if (monthField.getText().isEmpty() || dayField.getText().isEmpty() || 
                    hourField.getText().isEmpty() || minuteField.getText().isEmpty()) {
                    showAlert("Input Error", "Please fill in all time and duration fields.");
                    return;
                }
                
                startHour = Integer.parseInt(monthField.getText().trim());
                startMinute = Integer.parseInt(dayField.getText().trim());
                durationHours = Integer.parseInt(hourField.getText().trim());
                durationMinutes = Integer.parseInt(minuteField.getText().trim());
                durationSeconds = (secondsField != null && !secondsField.getText().trim().isEmpty()) 
                                   ? Integer.parseInt(secondsField.getText().trim()) : 0;
                days = new ArrayList<>(selectedDays);

                if (days.isEmpty()) {
                    showAlert("Input Error", "Please select at least one day.");
                    return;
                }
                
                if (startHour < 0 || startHour > 23 || startMinute < 0 || startMinute > 59) {
                    showAlert("Input Error", "Start Hour must be 0-23 and Minute 0-59.");
                    return;
                }
            }

            // 3. Final Title Check
            if (title.isEmpty()) {
                showAlert("Input Error", "Please enter a title.");
                return;
            }

         // 4. Save or Update Logic
            if (isEditingMode && this.selectedTask != null) {
                // UPDATE EXISTING: Use setters to modify the object directly
                selectedTask.setNote(title);
                selectedTask.setNoteDetail(noteDetailText);
                selectedTask.setStartHour(startHour);
                selectedTask.setStartMinute(startMinute);
                selectedTask.setDurationHours(durationHours);
                selectedTask.setDurationMinutes(durationMinutes);
                selectedTask.setDurationSeconds(durationSeconds);
                selectedTask.setRecurringDays(days);
                
                showAlert("Success", "Task updated successfully!");
            } else {
                // CREATE NEW: Using your specific constructor order
                // Order: note, startHour, startMinute, durHours, durMins, durSecs, noteDetail, days
                TaskScheduler newTask = new TaskScheduler(
                    title, 
                    startHour, 
                    startMinute, 
                    durationHours, 
                    durationMinutes, 
                    durationSeconds, 
                    noteDetailText, 
                    days
                );
                
                GlobalData.schedules.add(newTask);
                showAlert("Success", "New task added!");
            }

            // 5. Cleanup and UI Refresh
            TaskDatabase.saveTasks();
            hideScheduleForm(null);
            updateTaskUI();
            displayTaskOnDashboard(selectedTask);

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for time and duration.");
        }
    }

    // ----------------------------------------------------
    // CALCULATIONS
    // ----------------------------------------------------

    /** Finds the absolute soonest time this task is scheduled to run. */
    private LocalDateTime calculateNextDueTime(TaskScheduler task) {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> nextCandidates = new ArrayList<>();

        List<DayOfWeek> targetDays = task.getRecurringDays().stream()
            .map(dayMap::get)
            .filter(day -> day != null)
            .collect(Collectors.toList());

        for (DayOfWeek targetDay : targetDays) {

            LocalDateTime nextDay = now.with(TemporalAdjusters.nextOrSame(targetDay));

            LocalDateTime scheduledTime = nextDay
                .withHour(task.getStartHour())
                .withMinute(task.getStartMinute())
                .withSecond(0)
                .withNano(0);

            // If the time is in the past TODAY, schedule it for next week
            if (scheduledTime.isBefore(now.withSecond(0).withNano(0))) {
                scheduledTime = now.with(TemporalAdjusters.next(targetDay)) // Calculate next instance of the day
                .withHour(task.getStartHour())
                .withMinute(task.getStartMinute())
                .withSecond(0)
                .withNano(0);
            }

            nextCandidates.add(scheduledTime);
        }

        if (nextCandidates.isEmpty()) {
            return null;
        }

        return nextCandidates.stream()
            .min(Comparator.naturalOrder())
            .orElse(null);
    }

    /** Calculates the time remaining until the next due time for a recurring task (Not running). */
    public String calculateTimeRemaining(TaskScheduler task) {
        LocalDateTime nextDueTime = calculateNextDueTime(task);
        if (nextDueTime == null) {
            return "No days selected.";
        }

        LocalDateTime now = LocalDateTime.now();

        long totalSeconds = ChronoUnit.SECONDS.between(now, nextDueTime);

        if (totalSeconds < 0) {
            // This case should ideally not happen if calculateNextDueTime is perfect,
            // but it serves as a good safety net when the task is due right now.
            return "Starts Now";
        }

        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("Starts in: %dD %02d:%02d:%02d", days, hours, minutes, seconds);
    }
    
    //PRINT METHOD
    @FXML
    private void printTodoList() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) return;

        if (!job.showPrintDialog(null)) return;

        VBox page = buildTodoPrintPage();

        Group printableGroup = new Group(page);
        printableGroup.applyCss();
        printableGroup.layout();

        Printer printer = job.getPrinter();
        PageLayout pageLayout = printer.createPageLayout(
                Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

        double scaleX = pageLayout.getPrintableWidth() / printableGroup.getBoundsInParent().getWidth();
        double scaleY = pageLayout.getPrintableHeight() / printableGroup.getBoundsInParent().getHeight();
        double scale = Math.min(scaleX, scaleY);

        printableGroup.getTransforms().add(new Scale(scale, scale));

        boolean success = job.printPage(pageLayout, printableGroup);
        if (success) job.endJob();

        printableGroup.getTransforms().clear();
    }
    //Print page
    private VBox buildTodoPrintPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(40));
        page.setPrefSize(595, 842); // A4 size
        page.setStyle("-fx-background-color: white;");

        // Title
        Label title = new Label("TO DO LIST");
        title.setStyle("-fx-font-size: 42px; -fx-font-weight: bold;");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        // Date
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("MMMM - dd - yyyy"));
        Label dateLabel = new Label("DATE: " + formattedDate);
        dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        dateLabel.setAlignment(Pos.CENTER);
        dateLabel.setMaxWidth(Double.MAX_VALUE);

        page.getChildren().addAll(title, dateLabel);
        
        HBox sections = new HBox(30);

        // To do Tasks
        VBox tasksColumn = new VBox(10);
        tasksColumn.setPrefWidth(270);
        tasksColumn.getChildren().add(sectionHeader("TO DO TASKS"));

        if (GlobalData.schedules.isEmpty()) {
            tasksColumn.getChildren().add(new Label("No tasks assigned yet."));
        } else {
            for (TaskScheduler task : GlobalData.schedules) {
                VBox taskRow = buildTaskRowWithLine(task);
                tasksColumn.getChildren().add(taskRow);
            }
        }

        // Priorities 
        VBox prioColumn = new VBox(10);
        prioColumn.setPrefWidth(270);
        prioColumn.getChildren().add(sectionHeader("PRIORITIES"));

        List<TaskScheduler> prioTasks = GlobalData.getPriorityTasks();
        if (prioTasks.isEmpty()) {
            prioColumn.getChildren().add(new Label("No priority tasks assigned yet."));
        } else {
            for (TaskScheduler task : prioTasks) {
                VBox taskRow = buildTaskRowWithLine(task);
                prioColumn.getChildren().add(taskRow);
            }
        }

        sections.getChildren().addAll(tasksColumn, prioColumn);
        page.getChildren().add(sections);

        return page;
    }

    private VBox buildTaskRowWithLine(TaskScheduler task) {
        VBox container = new VBox(5);

        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

   
        CheckBox check = new CheckBox();

       //Task note
        Label noteLabel = new Label(task.getNote());
        noteLabel.setStyle("-fx-font-weight: bold;");
        noteLabel.setWrapText(true);
        noteLabel.setPrefWidth(200);

        row.getChildren().addAll(check, noteLabel);

        container.getChildren().add(row);

        // get notes and deadline
        String detailsText = "";
        if (task.getNoteDetail() != null && !task.getNoteDetail().isEmpty()) {
            detailsText += task.getNoteDetail();
        }
        if (task.getCurrentStartTime() != null) {
            LocalDateTime deadline = task.getCurrentStartTime().plusSeconds(task.getTotalDurationSeconds());
            if (!detailsText.isEmpty()) detailsText += "\n";
            detailsText += "Deadline: " + deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        if (!detailsText.isEmpty()) {
            Label detailsLabel = new Label(detailsText);
            detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
            detailsLabel.setWrapText(true);
            detailsLabel.setPadding(new Insets(0, 0, 0, 20)); // indent under note
            container.getChildren().add(detailsLabel);
        }

        // HORIZONTAL LINE SEPARATOR
        Line separator = new Line(0, 0, 270, 0); // width matches column
        separator.setStyle("-fx-stroke: gray; -fx-stroke-width: 1;");
        container.getChildren().add(separator);

        return container;
    }

    // SECTION HEADER
    private Label sectionHeader(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-border-color: black;" +
                "-fx-border-radius: 5;" +
                "-fx-padding: 4 8;"
        );
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    
	// ----------------------------------------------------
	// SCENE SWITCHING METHODS
	// ----------------------------------------------------

	public void switchPrioTask (ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("prioTask.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	public void switchToNotice (ActionEvent event) throws IOException{
		Parent root = FXMLLoader.load(getClass().getResource("noticeFunction.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
}