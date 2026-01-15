package application;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class MainControl {
    private Stage stage;
    private Scene scene;
    private Parent root;

    // FXML INJECTIONS
    @FXML
    private TextField taskNameField;

    @FXML
    private HBox taskMenuOne;

    @FXML
    private VBox taskMenuTwo;

    @FXML
    private TextField searchField;

    @FXML
    public static Deque<Task> allTasks = new ArrayDeque<>();

    @FXML
    public void addTask(ActionEvent event) {
        String taskName = taskNameField.getText().trim();

        if (!taskName.isEmpty()) {
            Task task = new Task(taskName);
            allTasks.addLast(task);
            addTaskButtonsToUI(task);
            taskNameField.clear();
        } else {
            System.out.println("Task name cannot be empty!");
        }
    }

    @FXML
    public void searchTasks(ActionEvent event) {
        String query = searchField.getText().toLowerCase().trim();

        taskMenuOne.getChildren().clear();
        taskMenuTwo.getChildren().clear();

        if (query.isEmpty()) {
            for (Task task : allTasks) {
                addTaskButtonsToUI(task);
            }
            return;
        }

        for (Task task : allTasks) {
            if (task.matches(query)) {
                addTaskButtonsToUI(task);
            }
        }
    }

    private void addTaskButtonsToUI(Task task) {
        Button buttonOne = new Button(task.getName());
        buttonOne.getStyleClass().add("icons-bilog");
        buttonOne.setPrefHeight(54.0); 
        buttonOne.setPrefWidth(65.0);
        buttonOne.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button buttonTwo = new Button(task.getName());
        buttonTwo.getStyleClass().add("icons-bilog");
        buttonTwo.setMaxWidth(Double.MAX_VALUE);
        buttonTwo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        taskMenuOne.getChildren().add(buttonOne);
        taskMenuTwo.getChildren().add(buttonTwo);
    }

    public void setAllTasks(Deque<Task> tasks) {
        this.allTasks = tasks;
        refreshTaskList();
    }

    public void refreshTaskList() {
        for (Task task : allTasks) {
            addTaskButtonsToUI(task);
        }
    }

    // Scene switching methods
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

    public void switchPrioTask(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("prioTask.fxml"));
        Parent root = loader.load();

        PriorityController prioCtrl = loader.getController();
        prioCtrl.loadPriorityTasks();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
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