package application;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL; 
import java.util.ResourceBundle; 

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; 
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox; 
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox; 
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ToggleButton; 
import javafx.scene.control.Label; 
import javafx.geometry.Pos; 
import javafx.application.Platform; 

// Imports for real-time and date/time
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;


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
    @FXML private TextField monthField;  // Used for Start Hour (0-23)
    @FXML private TextField dayField;    // Used for Start Minute (0-59)
    
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
    
    @FXML private Button search;
    @FXML private TextField searchField;
    
    private Map<String, DayOfWeek> dayMap;
    private Timeline timeline; // for real-time updates
    private TaskScheduler selectedTask; // ⭐️ FIX: Holds the manually selected task to prevent flicker ⭐️

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
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterTasks(newValue);
            });
        }
        updateDashboardMain(); 
        startTaskMonitor();
    }
    
    /** Initializes and starts the Timeline for continuous task monitoring. */
    private void startTaskMonitor() {
        timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                updateTaskUI(); 
                checkAndStartTasks(); 
                updateDashboardMain(); 
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // ----------------------------------------------------
    // TASK MONITORING LOGIC 
    // ----------------------------------------------------

    /** Checks if any recurring task should start now and monitors its duration. */
    private void checkAndStartTasks() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        
        if (GlobalData.schedules == null) return;
        
        for (TaskScheduler task : GlobalData.schedules) {
            
            // 1. Check if the task is due to START now 
            if (task.getCurrentStartTime() == null) {
                
                LocalDateTime nextDueTime = calculateNextDueTime(task);

                if (nextDueTime != null && ChronoUnit.MINUTES.between(now, nextDueTime) == 0 && nextDueTime.isAfter(now.minusSeconds(1))) {
                    
                    // START THE TASK! (This resets alarmTriggered to false in TaskScheduler)
                    task.setCurrentStartTime(now.withSecond(0).withNano(0)); 
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

        for (TaskScheduler task : GlobalData.schedules) {
            String taskName = task.getNote();
            
            if (taskName.toLowerCase().contains(lowerCaseQuery) || lowerCaseQuery.isEmpty()) {
                
                HBox taskRow = new HBox(10); 
                taskRow.setAlignment(Pos.CENTER_LEFT);
                taskRow.setMaxWidth(Double.MAX_VALUE);
                taskRow.getStyleClass().add("icons-bilog"); 
                taskRow.setPrefHeight(60.0);
                
                // ⭐️ UPDATED CLICK HANDLER: Set the selectedTask field ⭐️
                taskRow.setOnMouseClicked(event -> {
                    this.selectedTask = task; // Set the selected task
                    displayTaskOnDashboard(task);
                    
                    // Highlight the selected row
                    taskMenuTwo.getChildren().forEach(node -> node.setStyle(null)); 
                    taskRow.setStyle("-fx-background-color: #3498db;"); 
                });
                
                Label nameLabel = new Label(taskName);
                nameLabel.getStyleClass().add("task-name-label");
                nameLabel.setPrefWidth(200); 
                
                Label timeLabel = new Label();
                timeLabel.getStyleClass().add("time-status-label");
                
                String statusText;
                if (task.getCurrentStartTime() != null) {
                    // Task is running, show duration remaining
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
                    // Task is scheduled, show time until next start
                    statusText = calculateTimeRemaining(task);
                    timeLabel.setStyle("-fx-text-fill: green;");
                }
                //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------
                timeLabel.setText(statusText);
                Button prioBtn = new Button("Priority"); 
                prioBtn.setStyle("-fx-font-size: 16px; -fx-background-color: gold;");
                prioBtn.setOnAction(e -> {
                    if (!GlobalData.prioTasks.contains(task)) {
                        GlobalData.prioTasks.add(task);
                        showAlert("Priority Added", task.getNote() + " is now a priority task!");
                    } else {
                        showAlert("Already Priority", "This task is already marked as priority.");
                    }
                });

                // ✅ ✅ AUTO DELETE BUTTON
                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-font-size: 16px; -fx-background-color: red; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> {
                    GlobalData.schedules.remove(task);
                    GlobalData.prioTasks.remove(task); // also remove from priority if needed
                    taskMenuTwo.getChildren().remove(taskRow);

                    // Clear dashboard if deleted task is selected
                    if (task == selectedTask) {
                        selectedTask = null;
                        dashBoardMain.getChildren().clear();
                    }
                });

                taskRow.getChildren().addAll(nameLabel, timeLabel, prioBtn, deleteBtn);
                taskMenuTwo.getChildren().add(taskRow);

                // Maintain highlight if this task is currently selected
                if (task == this.selectedTask) {
                    taskRow.setStyle("-fx-background-color: #3498db;"); 
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
            return;
        }
        
        // 1. Update the UI
        dashBoardMain.getChildren().clear();

        // Title Label
        Label titleLabel = new Label(task.getNote());
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        titleLabel.setLayoutX(20);
        titleLabel.setLayoutY(20);
        
        // Timer Label
        Label timerLabel = new Label();
        String timerText;
        
        // Check if the task is currently running (live countdown)
        if (task.getCurrentStartTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            long totalDurationSeconds = (task.getDurationHours() * 3600) + 
                                        (task.getDurationMinutes() * 60) + 
                                        task.getDurationSeconds();
            long secondsLeft = ChronoUnit.SECONDS.between(now, task.getCurrentStartTime().plusSeconds(totalDurationSeconds));
            
            if (secondsLeft <= 0) {
                timerText = "Status: TIME'S UP!";
                timerLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: orange; -fx-font-weight: bold;");
            } else {
                long hrs = secondsLeft / 3600;
                long mins = (secondsLeft % 3600) / 60;
                long secs = secondsLeft % 60;
                
                timerText = String.format("RUNNING: %02d:%02d:%02d left", hrs, mins, secs);
                timerLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: red; -fx-font-weight: bold;");
            }
        } else {
            // Task is scheduled, show time until next start
            timerText = "Next Start: " + calculateTimeRemaining(task);
            timerLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: green;");
        }
        
        timerLabel.setText(timerText);
        timerLabel.setLayoutX(20);
        timerLabel.setLayoutY(70);

        // Note Detail
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
        
        dashBoardMain.getChildren().addAll(titleLabel, timerLabel, noteHeader, noteLabel);
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
            createSchedulePane.setVisible(true);
            createSchedulePane.toFront();
        }
    }
    
    @FXML
    public void hideScheduleForm(ActionEvent event) {
        if (createSchedulePane != null) {
            createSchedulePane.setVisible(false);
            
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

    /** Creates a TaskScheduler object and saves it to GlobalData. */
    @FXML
    public void saveSchedule(ActionEvent event) {
    	String title = titleField.getText().trim(); 
    	String noteDetailText = noteField.getText().trim(); 
        
        try {
            
            int startHour = Integer.parseInt(monthField.getText().trim());
            int startMinute = Integer.parseInt(dayField.getText().trim());
            
            int durationHours = Integer.parseInt(hourField.getText().trim());
            int durationMinutes = Integer.parseInt(minuteField.getText().trim());
            
            int durationSeconds = secondsField != null && !secondsField.getText().trim().isEmpty() ? Integer.parseInt(secondsField.getText().trim()) : 0;

            if (title.isEmpty() || selectedDays.isEmpty()) {
                showAlert("Input Error", "Please enter a title and select at least one day.");
                return;
            }
            
            if (startHour < 0 || startHour > 23 || startMinute < 0 || startMinute > 59) {
                showAlert("Input Error", "Start Hour must be 0-23 and Minute 0-59.");
                return;
            }

            TaskScheduler newSchedule = new TaskScheduler(
                title, 
                startHour, 
                startMinute, 
                durationHours, 
                durationMinutes, 
                durationSeconds, 
                noteDetailText, 
                new ArrayList<>(selectedDays) 
            );
            
            GlobalData.schedules.add(newSchedule);
            GlobalData.taskNames.add(title); 
            
            // Immediately select the new task for dashboard display
            this.selectedTask = newSchedule;
            
            displayTaskOnDashboard(newSchedule); 
            
            loadSavedTasks();
            hideScheduleForm(event);
            
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please ensure all time fields contain valid whole numbers.");
            System.err.println("NumberFormatException: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred during scheduling.");
            e.printStackTrace();
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
                scheduledTime = nextDay.with(TemporalAdjusters.next(targetDay))
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
            return "Starts Now"; 
        }

        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("Starts in: %dD %02d:%02d:%02d", days, hours, minutes, seconds);
    }
    
	// ----------------------------------------------------
	// SCENE SWITCHING METHODS
	// ----------------------------------------------------
	
	public void switchToHomeView (ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("homeView.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	
	public void switchToHomeSchedule (ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("homeSched.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	
	public void switchToSchoolSchedule (ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("schoolView.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	public void switchToWorkSchedule (ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("workSchedule.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	public void switchToDailyPlan (ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("dailyPlan.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
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