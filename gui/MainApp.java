package gui;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import model.Task;
import gui.view.TaskOverviewController;

public class MainApp extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
	
	/**
	 * The data as an observable list of Tasks.
	 */
	private ObservableList<Task> taskData = FXCollections.observableArrayList();
	
	/**
	 * Constructor
	 */
	public MainApp() {
		
		//Dummy data
/*		taskData.add(new Task("Do homework 1"));
		taskData.add(new Task("Do homework 2"));
		taskData.add(new Task("Do homework 3"));
		taskData.add(new Task("Do homework 4"));
		taskData.add(new Task("Do homework 5"));
		taskData.add(new Task("Do homework 6"));
		taskData.add(new Task("Do homework 7"));
		taskData.add(new Task("Do homework 8"));*/
	}
	
	/**
	 * Returns the data as an observable list of Tasks.
	 * @return
	 */
	
	public ObservableList<Task> getTaskData() {
		return taskData;
	}
	
	public void setTaskData(ObservableList<Task> taskData) {
		this.taskData = taskData;
	}
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Comman.Do");
		
		initRootLayout();
		
		showMainOverview();
	}

	/**
	 * Initializes the root layout
	 */
	public void initRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();
			
			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			scene.getStylesheets().add(MainApp.class.getResource("commandoskin.css").toString());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Shows the main overview inside the root layout
	 */
	public void showMainOverview() {
		try {
			// Load main overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/MainOverview.fxml"));
			AnchorPane mainOverview = (AnchorPane) loader.load();
			
			// Set main overview into the center of root layout.
			rootLayout.setCenter(mainOverview);
			
			// Give the controller access to the main app.
			TaskOverviewController controller = loader.getController();
			controller.setMainApp(this);
			controller.setup();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the main stage.
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}
		
	public static void main(String[] args) {
		launch(args);
	}
}
