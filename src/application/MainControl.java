package application;

import javafx.event.ActionEvent;
import java.io.IOException;

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

    /**
     * Handles adding a new task button to both HBox (taskMenuOne) 
     * and VBox (taskMenuTwo) when the 'Add' button is clicked.
     */
    @FXML
    public void addTask(ActionEvent event) {
        String taskName = taskNameField.getText().trim();
        
        if (!taskName.isEmpty()) {
            
            // --- 1. Button for taskMenuOne (HBox - Small/Square-ish) ---
            Button buttonOne = new Button(taskName);
            buttonOne.getStyleClass().add("icons-bilog");
            // Let the HBox manage the size, but we'll set a preferred size based on your existing buttons
            buttonOne.setPrefHeight(54.0); 
            buttonOne.setPrefWidth(65.0); // Based on existing 'home' and 'school' buttons
            
            // --- 2. Button for taskMenuTwo (VBox - Long Oblong/Full Width) ---
            Button buttonTwo = new Button(taskName);
            buttonTwo.getStyleClass().add("icons-bilog");
            
            // KEY TWEAK: Set the maximum width to fill the VBox container
            buttonTwo.setMaxWidth(Double.MAX_VALUE);
            // Optional: Match the height of the existing buttons if they were tall
            // For now, we'll let the VBox and default button styling manage height.
            
            // Add to containers
            taskMenuOne.getChildren().add(buttonOne);
            taskMenuTwo.getChildren().add(buttonTwo);
            
            // Clear the TextField
            taskNameField.clear();
        } else {
            System.out.println("Task name cannot be empty!");
        }
    }
	
	//Existing scene switching methods...
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