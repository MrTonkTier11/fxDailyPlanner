package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class PriorityController implements Initializable {

    private Timeline timeline;
    private Stage stage;
    private Scene scene;

    @FXML private VBox prioListBox;
    @FXML private Pane taskwindow;

    @FXML private Button prioritiesBtn;
    @FXML private Button homeBtn;

    // --------------------------
    // INITIALIZATION
    // --------------------------
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TaskDatabase.loadPriorityTasks();
        loadPriorityTasks();
        startLiveCountdown();

        // Tooltips for main buttons
        if (prioritiesBtn != null) Tooltip.install(prioritiesBtn, new Tooltip("View Priority Tasks"));
        if (homeBtn != null) Tooltip.install(homeBtn, new Tooltip("Go back to Home"));
    }

  
    // LIVE COUNTDOWN
    private void startLiveCountdown() {
        timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> updatePriorityCountdown())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updatePriorityCountdown() {
        for (Node node : prioListBox.getChildren()) {
            if (node instanceof HBox row) {
                Label nameLabel = null;
                Label timeLabel = null;

                for (Node child : row.getChildren()) {
                    if (child instanceof Label lbl) {
                        if (nameLabel == null) nameLabel = lbl;
                        else timeLabel = lbl; // second Label is timer
                    }
                }

                if (nameLabel != null && timeLabel != null) {
                    for (TaskScheduler task : GlobalData.prioTasks) {
                        if (task.getNote().equals(nameLabel.getText())) {
                            updateTimerLabel(task, timeLabel);
                            break;
                        }
                    }
                }
            }
        }
    }

    
    // LOAD PRIORITY TASKS
    HBox[] selectedRow = new HBox[1];

    public void loadPriorityTasks() {
        prioListBox.getChildren().clear();

        if (GlobalData.prioTasks == null) return;

        for (TaskScheduler task : GlobalData.prioTasks) {

            HBox taskRow = new HBox(10);
            taskRow.setStyle("-fx-padding:5; -fx-alignment:CENTER_LEFT; -fx-background-color:#f4f4f4;");
            taskRow.setMaxWidth(Double.MAX_VALUE);

            Label nameLabel = new Label(task.getNote());
            nameLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold;");
            Tooltip.install(nameLabel, new Tooltip("Task: " + task.getNote()));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label timeLabel = new Label();
            updateTimerLabel(task, timeLabel);
            timeLabel.setStyle("-fx-font-size:14px; -fx-text-fill:#333333;");
            Tooltip.install(timeLabel, new Tooltip("Status for '" + task.getNote() + "'"));

            Button removeBtn = new Button("❌");
            removeBtn.setStyle("-fx-background-color:#ff4d4d; -fx-text-fill:white; -fx-font-weight:bold;");
            Tooltip.install(removeBtn, new Tooltip("Remove '" + task.getNote() + "'"));

            removeBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Task");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to remove '" + task.getNote() + "' from Priority?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    task.setPriority(false);
                    GlobalData.prioTasks.remove(task);
                    TaskDatabase.saveTasks();
                    loadPriorityTasks();
                }
            });
            

            // Blinking HBox for status
            Timeline blinkTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), ev -> {
                if (selectedRow[0] == taskRow) return; // don't blink if selected

                String blinkColor = "#f4f4f4"; // default
                if (task.isPaused()) blinkColor = "#2980b9"; // blue
                else if (task.getCurrentStartTime() != null) {
                    long totalSeconds = task.getDurationHours() * 3600
                                      + task.getDurationMinutes() * 60
                                      + task.getDurationSeconds();
                    long secondsLeft = ChronoUnit.SECONDS.between(
                            LocalDateTime.now(),
                            task.getCurrentStartTime().plusSeconds(totalSeconds)
                    );
                    blinkColor = (secondsLeft <= 0) ? "#e67e22" : "#27ae60"; // orange if time's up, green if running
                }

                String currentBg = taskRow.getStyle();
                if (currentBg.contains(blinkColor)) {
                    taskRow.setStyle("-fx-padding:5; -fx-alignment:CENTER_LEFT; -fx-background-color:#f4f4f4;");
                } else {
                    taskRow.setStyle("-fx-padding:5; -fx-alignment:CENTER_LEFT; -fx-background-color:" + blinkColor + ";");
                }
            }));
            blinkTimeline.setCycleCount(Timeline.INDEFINITE);
            blinkTimeline.play();

           
            // Row click highlights
            taskRow.setOnMouseClicked(e -> {
                displayFullTaskDetails(task);

                // Reset other rows
                prioListBox.getChildren().forEach(node -> {
                    if (node instanceof HBox row && row != taskRow) {
                        row.setStyle("-fx-padding:5; -fx-alignment:CENTER_LEFT; -fx-background-color:#f4f4f4;");
                    }
                });

                // Highlight selected row
                taskRow.setStyle("-fx-padding:5; -fx-alignment:CENTER_LEFT; -fx-background-color:#616161; -fx-text-fill:white;");
                selectedRow[0] = taskRow; // mark this row as selected
            });

            taskRow.getChildren().addAll(nameLabel, spacer, timeLabel, removeBtn);
            prioListBox.getChildren().add(taskRow);
        }
    }

    private void updateTimerLabel(TaskScheduler task, Label timerLabel) {
        LocalDateTime now = LocalDateTime.now();

        if (task.isPaused()) {
            timerLabel.setText("⏸ PAUSED");
            timerLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
            return;
        }

        if (task.getCurrentStartTime() != null) {
            long totalSeconds = task.getDurationHours() * 3600 + task.getDurationMinutes() * 60 + task.getDurationSeconds();
            long secondsLeft = ChronoUnit.SECONDS.between(now, task.getCurrentStartTime().plusSeconds(totalSeconds));

            if (secondsLeft <= 0) {
                timerLabel.setText("⏰ TIME'S UP!");
                timerLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
            } else {
                long hrs = secondsLeft / 3600;
                long mins = (secondsLeft % 3600) / 60;
                long secs = secondsLeft % 60;
                timerLabel.setText(String.format("⏳ %02d:%02d:%02d", hrs, mins, secs));
                timerLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
            return;
        }

        // Scheduled
        timerLabel.setText("🟢 SCHEDULED");
        timerLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

  
    // TASK DETAILS VIEW
    private void displayFullTaskDetails(TaskScheduler task) {
        if (taskwindow == null || task == null) return;

        taskwindow.getChildren().clear();

        // Title
        Label title = new Label(task.getNote());
        title.setLayoutX(10);
        title.setLayoutY(10);
        title.setStyle("-fx-font-size: 50px; -fx-font-weight: bold; -fx-text-fill: yellow;");
        Tooltip.install(title, new Tooltip("Task Title: " + task.getNote()));

        // Details
        Label details = new Label(task.getNoteDetail());
        details.setLayoutX(10);
        details.setLayoutY(80);
        details.setWrapText(true);
        details.setPrefWidth(taskwindow.getPrefWidth() - 20);
        details.setStyle("-fx-font-size: 30px; -fx-text-fill: white;");
        Tooltip.install(details, new Tooltip("Task Details"));

        // Start Section
        VBox startBox = new VBox(5);
        startBox.setLayoutX(75);
        startBox.setLayoutY(450);
        startBox.setStyle("-fx-padding: 10; -fx-border-color: green; -fx-border-width: 2; -fx-border-radius: 5;");
        Label startLabel = new Label("START");
        startLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: violet;");
        Label startTime = new Label(String.format("%02d:%02d", task.getStartHour(), task.getStartMinute()));
        startTime.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: violet;");
        Tooltip.install(startTime, new Tooltip("Task Start Time"));
        startBox.getChildren().addAll(startLabel, startTime);

        // Duration Section
        VBox durationBox = new VBox(5);
        durationBox.setLayoutX(275);
        durationBox.setLayoutY(450);
        durationBox.setStyle("-fx-padding: 10; -fx-border-color: green; -fx-border-width: 2; -fx-border-radius: 5;");
        Label durationLabel = new Label("DURATION");
        durationLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: violet;");
        Label durationTime = new Label(task.getDurationHours() + "h " + task.getDurationMinutes() + "m " + task.getDurationSeconds() + "s");
        durationTime.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: violet;");
        Tooltip.install(durationTime, new Tooltip("Task Duration"));
        durationBox.getChildren().addAll(durationLabel, durationTime);

        // Status Section
        VBox statusBox = new VBox(5);
        statusBox.setLayoutX(500);
        statusBox.setLayoutY(450);
        statusBox.setStyle("-fx-padding: 10; -fx-border-color: green; -fx-border-width: 2; -fx-border-radius: 5;");
        Label statusLabel = new Label("STATUS");
        statusLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: violet;");
        String statusText = (task.getCurrentStartTime() != null) ? "ON GOING" : "WAITING";
        Label taskStatus = new Label(statusText);
        taskStatus.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: violet;");
        Tooltip.install(taskStatus, new Tooltip("Current task status"));
        statusBox.getChildren().addAll(statusLabel, taskStatus);

        taskwindow.getChildren().addAll(title, details, startBox, durationBox, statusBox);
    }

    // --------------------------
    // SCENE SWITCHING
    // --------------------------
    public void switchToHomeView(ActionEvent event) throws IOException { switchScene(event, "homeView.fxml"); }
    public void switchToHomeSchedule(ActionEvent event) throws IOException { switchScene(event, "homeSched.fxml"); }
    public void switchToSchoolSchedule(ActionEvent event) throws IOException { switchScene(event, "schoolView.fxml"); }
    public void switchToWorkSchedule(ActionEvent event) throws IOException { switchScene(event, "workSchedule.fxml"); }
    public void switchToDailyPlan(ActionEvent event) throws IOException { switchScene(event, "dailyPlan.fxml"); }
    public void switchToNotice(ActionEvent event) throws IOException { switchScene(event, "noticeFunction.fxml"); }

    @FXML
    public void switchPrioTask(ActionEvent event) throws IOException { switchScene(event, "prioTask.fxml"); }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}


/*New Features:
 * Displayed live status in task buttons
 * Details of notes 
 * popup when deleting a task
 * blinking based on status (blue green orange)
 * separated details for the task duration, deadline, status
 * tooltip for details when cursor meets an element
 * 
 */

