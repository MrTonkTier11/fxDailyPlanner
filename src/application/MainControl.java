package application;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox; // Import HBox
import javafx.scene.layout.VBox; // Import VBox
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class MainControl {
	private Stage stage;
	private Scene scene;
	private Parent root;

    // FXML INJECTIONS (Add these fields)
	@FXML
    private TextField taskNameField;

    @FXML
    private HBox taskMenuOne; // The HBox on the dashboard

    @FXML
    private VBox taskMenuTwo; // The VBox in the menu pane
    
    @FXML
    private TextField searchField; // NEW!! (For searching)
    
    @FXML
    public static Deque<Task> allTasks = new ArrayDeque<>(); //NEW !! (Double-ended Queue)
    /**
     * Handles adding a new task button to both HBox (taskMenuOne) 
     * and VBox (taskMenuTwo) when the 'Add' button is clicked.
     */
    @FXML
    public void addTask(ActionEvent event) {
        String taskName = taskNameField.getText().trim();

        if (!taskName.isEmpty()) {
            //NEW!! Create Task object and store in deque
            Task task = new Task(taskName);
            allTasks.addLast(task); // <-- important: store Task in deque

            // Add buttons to UI
            addTaskButtonsToUI(task);

            // Clear the TextField
            taskNameField.clear();
        } else {
            System.out.println("Task name cannot be empty!");
        }
    }

    //NEW!!  ADDED SEARCH TASK PACHECK NALANG PO
    @FXML
    public void searchTasks(ActionEvent event) {
        String query = searchField.getText().toLowerCase().trim();

        // Clear current buttons
        taskMenuOne.getChildren().clear();
        taskMenuTwo.getChildren().clear();

        // Query Condition: show all tasks if search is empty
        if (query.isEmpty()) {
            for (Task task : allTasks) {
                addTaskButtonsToUI(task);
            }
            return;
        }

        // Filter Task
        for (Task task : allTasks) {
            if (task.matches(query)) {
                addTaskButtonsToUI(task);
            }
        }
    }

    private void addTaskButtonsToUI(Task task) {
        // --- 1. Button for taskMenuOne (HBox - Small/Square-ish) ---
        Button buttonOne = new Button(task.getName());
        buttonOne.getStyleClass().add("icons-bilog");
        buttonOne.setPrefHeight(54.0); 
        buttonOne.setPrefWidth(65.0);

        // --- 2. Button for taskMenuTwo (VBox - Long/Full Width) ---
        Button buttonTwo = new Button(task.getName());
        buttonTwo.getStyleClass().add("icons-bilog");
        buttonTwo.setMaxWidth(Double.MAX_VALUE);

        taskMenuOne.getChildren().add(buttonOne);
        taskMenuTwo.getChildren().add(buttonTwo);
    }
//Newly added task deque
    //store tasks
    public void setAllTasks(Deque<Task> tasks) {
        this.allTasks = tasks;
        refreshTaskList();  
    }
    //Instead of creating a new homeview this refresh the added tasks
    public void refreshTaskList() {
        // Re-add all tasks from deque
        for (Task task : allTasks) {
            addTaskButtonsToUI(task);
        }
    }

	//Existing scene switching methods...
    //revised action + added refresh 
    public void switchToHomeView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("homeView.fxml"));
        Parent root = loader.load();
//--
        MainControl controller = loader.getController();
        controller.refreshTaskList();   //shown again using static tasks
//--
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
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