package application;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL; // Added required import
import java.util.ResourceBundle; // Added required import

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // Added required import
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox; 
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox; 
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;


// ⭐️ IMPLEMENT INITIALIZABLE INTERFACE ⭐️
public class HomeViewControl implements Initializable {
    
    // REMOVED: public static List<String> taskNames = new ArrayList<>(); (Now in GlobalData)
    private List<String> selectedDays = new ArrayList<>(); // Already existed, kept here for day tracking
	private Stage stage;
	private Scene scene;
	private Parent root;

    // FXML INJECTIONS (Existing)
	@FXML private TextField taskNameField;
    @FXML private HBox taskMenuOne;
    @FXML private VBox taskMenuTwo;
    
    // ⭐️ NEW FXML INJECTIONS FOR SCHEDULE CREATION PANE ⭐️
    @FXML private Pane createSchedulePane;
    @FXML private TextField noteField;
    @FXML private TextField monthField;
    @FXML private TextField dayField;
    @FXML private TextField hourField;
    @FXML private TextField minuteField;
    
    // Days of Week Buttons (7 of them)
    @FXML private Button sunButton;
    @FXML private Button monButton;
    @FXML private Button tueButton;
    @FXML private Button wedButton;
    @FXML private Button thuButton;
    @FXML private Button friButton;
    @FXML private Button satButton;

    // 1. UPDATED: The initialize method (Must implement the Initializable signature)
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // This ensures that whenever any scene using this controller is loaded, 
        // the buttons from the static list are rebuilt.
        loadSavedTasks();
    }
    
    // 2. UPDATED: The loadSavedTasks method (Now uses GlobalData.taskNames)
    public void loadSavedTasks() {
    	// Only proceed if the injected FXML containers are not null
        if (taskMenuOne == null || taskMenuTwo == null) {
            System.out.println("FXML components (taskMenuOne/taskMenuTwo) not yet loaded for this scene.");
            return;
        }
        
        taskMenuOne.getChildren().clear();
        taskMenuTwo.getChildren().clear();

        // Rebuild buttons from the GlobalData list
        for (String taskName : GlobalData.taskNames) {
            
            // --- 1. Button for taskMenuOne (HBox - Small/Square-ish) ---
            Button buttonOne = new Button(taskName);
            buttonOne.getStyleClass().add("icons-bilog");
            buttonOne.setPrefHeight(54.0); 
            buttonOne.setPrefWidth(65.0); 
            
            // --- 2. Button for taskMenuTwo (VBox - Long Oblong/Full Width) ---
            Button buttonTwo = new Button(taskName);
            buttonTwo.getStyleClass().add("icons-bilog");
            buttonTwo.setMaxWidth(Double.MAX_VALUE); // Ensures button fills VBox
            
            // Add to containers
            taskMenuOne.getChildren().add(buttonOne);
            taskMenuTwo.getChildren().add(buttonTwo);
        }
    }
    
    // 3. UPDATED: Your addTask method (Now uses GlobalData.taskNames)
    @FXML
    public void addTask(ActionEvent event) {
        // This method can now be used for simple tasks, 
        // but for schedules, users should use the new form.
        String taskName = taskNameField.getText().trim();
        
        if (!taskName.isEmpty()) {
        	// Store the task name in GlobalData
        	GlobalData.taskNames.add(taskName);
            
            loadSavedTasks();
            taskNameField.clear();
        } else {
            System.out.println("Task name cannot be empty!");
        }
    }

    // ⭐️ NEW SCHEDULER FUNCTIONALITY ⭐️

    /** Shows the Schedule creation form pane and brings it to the front. 
     * (Linked to the 'All Tasks' button in the FXML) */
    @FXML
    public void showScheduleForm(ActionEvent event) {
        if (createSchedulePane != null) {
            createSchedulePane.setVisible(true);
            createSchedulePane.toFront();
        }
    }
    
    /** Hides the Schedule creation form pane. 
     * (Linked to the 'Cancel' button in the FXML) */
    @FXML
    public void hideScheduleForm(ActionEvent event) {
        if (createSchedulePane != null) {
            createSchedulePane.setVisible(false);
            
            // Optional: Reset form fields and selections
            noteField.clear();
            monthField.clear();
            dayField.clear();
            hourField.clear();
            minuteField.clear();
            
            // Clear selected days and reset button styles
            for (Button b : new Button[]{sunButton, monButton, tueButton, wedButton, thuButton, friButton, satButton}) {
                if (b != null) {
                    b.getStyleClass().remove("selected-day");
                }
            }
            selectedDays.clear();
        }
    }
    
    /** Handles toggling day buttons and updating the selectedDays list. */
    @FXML
    public void toggleDaySelection(ActionEvent event) {
        Button source = (Button) event.getSource();
        String day = source.getText();
        
        // Use the 'selected-day' style class to visually indicate selection
        if (selectedDays.contains(day)) {
            selectedDays.remove(day);
            source.getStyleClass().remove("selected-day"); 
        } else {
            selectedDays.add(day);
            source.getStyleClass().add("selected-day"); 
        }
    }

    /** Creates a TaskScheduler object and saves it to GlobalData. */
    @FXML
    public void saveSchedule(ActionEvent event) {
        String note = noteField.getText().trim();
        String month = monthField.getText().trim();
        String day = dayField.getText().trim();
        String hour = hourField.getText().trim();
        String minute = minuteField.getText().trim();
        
        if (note.isEmpty() || month.isEmpty() || day.isEmpty() || hour.isEmpty() || minute.isEmpty() || selectedDays.isEmpty()) {
            System.out.println("Error: Please fill all fields and select at least one day.");
            return;
        }

        // 1. Compile Data
        String date = month + "/" + day;
        String time = hour + ":" + minute;
        
        // 2. Create the new Schedule Object
        TaskScheduler newSchedule = new TaskScheduler(note, date, time, new ArrayList<>(selectedDays));
        
        // 3. Save it to the Global Data Lists
        GlobalData.schedules.add(newSchedule);
        GlobalData.taskNames.add(note); // Use the note as the button name for the menu
        
        // 4. Reload UI and Reset State
        loadSavedTasks();
        hideScheduleForm(event);
    }
	
	//Existing scene switching methods... (Using FXMLLoader.load() as before)
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