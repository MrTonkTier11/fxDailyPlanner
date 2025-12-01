package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
import java.util.ResourceBundle;

public class PriorityController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML private VBox prioListBox;
    @FXML private Pane taskwindow;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadPriorityTasks();
    }

    /** Load all priority tasks in HomeView style */
    public void loadPriorityTasks() {
        prioListBox.getChildren().clear();

        if (GlobalData.prioTasks != null) {
            for (TaskScheduler task : GlobalData.prioTasks) {

                // 1️⃣ Create task row HBox
                HBox taskRow = new HBox(10);
                taskRow.setStyle("-fx-padding: 5; -fx-alignment: CENTER_LEFT; -fx-background-color: #f4f4f4;");
                taskRow.setMaxWidth(Double.MAX_VALUE);

                // 2️⃣ Task name label
                Label nameLabel = new Label(task.getNote());
                nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                nameLabel.setPrefWidth(200);

                // 3️⃣ Timer / status label
                Label timeLabel = new Label();
                updateTimerLabel(task, timeLabel);

                // 4️⃣ REMOVE PRIORITY button (new)
                Button prioBtn = new Button("✖ Remove");
                prioBtn.setStyle("-fx-background-color: #ff4d4d; -fx-font-weight: bold;");

                prioBtn.setOnAction(e -> {
                    // Remove task from priority list
                    GlobalData.prioTasks.remove(task);

                    // Refresh view instantly
                    loadPriorityTasks();
                });

                // 5️⃣ Add elements to HBox
                taskRow.getChildren().addAll(nameLabel, timeLabel, prioBtn);

                // 6️⃣ Click row to show full details in taskwindow
                taskRow.setOnMouseClicked(e -> {
                    displayFullTaskDetails(task);
                    // Highlight selected row
                    prioListBox.getChildren().forEach(node -> node.setStyle("-fx-background-color: #f4f4f4;"));
                    taskRow.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                });

                prioListBox.getChildren().add(taskRow);
            }
        }
    }

    /** Updates the timerLabel with countdown or status */
    private void updateTimerLabel(TaskScheduler task, Label timerLabel) {
        LocalDateTime now = LocalDateTime.now();

        if (task.getCurrentStartTime() != null) {
            long totalSeconds =
                    (task.getDurationHours() * 3600) +
                    (task.getDurationMinutes() * 60) +
                    task.getDurationSeconds();

            long secondsLeft = ChronoUnit.SECONDS.between(
                    now,
                    task.getCurrentStartTime().plusSeconds(totalSeconds)
            );

            if (secondsLeft <= 0) {
                timerLabel.setText("TIME'S UP!");
                timerLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            } else {
                long hrs = secondsLeft / 3600;
                long mins = (secondsLeft % 3600) / 60;
                long secs = secondsLeft % 60;

                timerLabel.setText(
                        String.format("RUNNING: %02d:%02d:%02d left", hrs, mins, secs)
                );
                timerLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        } else {
            timerLabel.setText("SCHEDULED");
            timerLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }
    }

    /** Display full task attributes in taskwindow (like HomeView dashboard) */
    private void displayFullTaskDetails(TaskScheduler task) {
        if (taskwindow == null || task == null) return;

        taskwindow.getChildren().clear(); // Clear previous display

        // Directly add labels into the existing pane
        Label title = new Label("Title: " + task.getNote());
        title.setLayoutX(10);
        title.setLayoutY(10);
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        Label details = new Label("Details: " + task.getNoteDetail());
        details.setLayoutX(10);
        details.setLayoutY(50);
        details.setWrapText(true);
        details.setPrefWidth(taskwindow.getPrefWidth() - 20);
        details.setStyle("-fx-font-size: 25px;");

        Label startTime = new Label("Start: " + String.format("%02d:%02d", task.getStartHour(), task.getStartMinute()));
        startTime.setLayoutX(10);
        startTime.setLayoutY(120);

        Label duration = new Label("Duration: " + task.getDurationHours() + "h " + task.getDurationMinutes() + "m " + task.getDurationSeconds() + "s");
        duration.setLayoutX(10);
        duration.setLayoutY(150);

        
        Label repeatDays = new Label("Repeats On: " + task.getRecurringDays());
        repeatDays.setLayoutX(10);
        repeatDays.setLayoutY(180);

        String status = (task.getCurrentStartTime() != null) ? "RUNNING" : "WAITING";
        Label taskStatus = new Label("Status: " + status);
        taskStatus.setLayoutX(10);
        taskStatus.setLayoutY(210);

        // Add labels directly to the pane
        taskwindow.getChildren().addAll(title, details, startTime, duration, repeatDays, taskStatus);
    }

    // ----------------------------------------------------
    // SCENE SWITCHING METHODS
    // ----------------------------------------------------

    public void switchToHomeView(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("homeView.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToHomeSchedule(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("homeSched.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToSchoolSchedule(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("schoolView.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToWorkSchedule(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("workSchedule.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToDailyPlan(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("dailyPlan.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void switchPrioTask(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("prioTask.fxml"));
        Parent root = loader.load();

        // Get the controller instance
        PriorityController prioCtrl = loader.getController();
        prioCtrl.loadPriorityTasks(); // Refresh the list

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToNotice(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("noticeFunction.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
