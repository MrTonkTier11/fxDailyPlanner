package application;

import javafx.event.ActionEvent;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class MainControl {
	private Stage stage;
	private Scene scene;
	private Parent root;
	
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
		Parent root = FXMLLoader.load(getClass().getResource("workView.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}


}
