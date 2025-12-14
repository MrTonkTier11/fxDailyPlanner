package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load csv
            TaskDatabase.loadTasks();

            Parent root = FXMLLoader.load(getClass().getResource("homeView.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("style.css").toExternalForm()
            );

            primaryStage.setTitle("JavaFX Template App");
            primaryStage.setScene(scene);

            // save 
            primaryStage.setOnCloseRequest(event -> {
                TaskDatabase.saveTasks();
            });

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
