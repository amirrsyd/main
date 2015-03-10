package gui.view;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import gui.MainApp;
import model.Task;
import logic.CdLogic;

import java.time.format.TextStyle;
import java.util.Locale;

import javafx.geometry.HPos;

public class TaskOverviewController{
	
	private static final int NUMBER_OF_COLUMNS_IN_CELL = 3;
	private static final int NUMBER_OF_ROWS_IN_CALENDAR = 7;
	private static final int NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER = 6;
	private static final int NUMBER_OF_COLS_IN_CALENDAR = 7;
	private static final int NUMBER_OF_DAYS_IN_A_WEEK = 7;
	private static final int PLUS = 1;
	private static final int MINUS = 0;
	private static final int MARGIN_COLUMN = 0;
	private static final int INDEX_COLUMN = 1;
	private static final int PREVIEW_COLUMN = 2;
	private static CdLogic logic;

	
	@FXML
	private TableView<Task> taskTable;
	@FXML
	private TableColumn<Task, String> taskNameColumn;
	@FXML
	private TableColumn<Task, LocalDate> startDateColumn;
	@FXML
	private TableColumn<Task, LocalTime> startTimeColumn;
	@FXML
	private TableColumn<Task, LocalDate> endDateColumn;
	@FXML
	private TableColumn<Task, LocalTime> endTimeColumn;
	@FXML
	private GridPane calendar;
	@FXML
	private Label monthHeader;
	@FXML
	private TextArea output;
	@FXML
	private TextField input;
	
	private String userInput;
	
	private Month currentMonth = LocalDate.now().getMonth();
	
	private int currentYear = LocalDate.now().getYear();
	
	private Label[] headerDays = new Label[7];
	
	private Label[][] dateNumbers = new Label[NUMBER_OF_ROWS_IN_CALENDAR][NUMBER_OF_COLS_IN_CALENDAR];
	
	private GridPane[][] cellFormat = new GridPane[NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER][NUMBER_OF_COLS_IN_CALENDAR];
	
	private ColumnConstraints[] basicCellColumnConstraints = new ColumnConstraints[NUMBER_OF_COLUMNS_IN_CELL];
	
	private RowConstraints basicCellRowConstraints = new RowConstraints();
	
	// Reference to the main application
	private MainApp mainApp;
	
	/**
	 * The constructor is called before the initialize() method.
	 */
	public TaskOverviewController() {
	}
	
	@FXML
	private void initialize() {
		//Initialize the task table with the five columns.
		taskNameColumn.setCellValueFactory(cellData -> cellData.getValue().taskNameProperty());
		startDateColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());
		startTimeColumn.setCellValueFactory(cellData -> cellData.getValue().startTimeProperty());
		endDateColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty());
		endTimeColumn.setCellValueFactory(cellData -> cellData.getValue().endTimeProperty());
		prepareLabelsForHeader(headerDays);
		prepareLabelsForCalendar(currentMonth, currentYear);
		initializeLogic();
		initializeCalendar();
		initializeCellFormat();
		setCellFormat();
		fillHeader();
		fillCells();
		fillCalendar();
		output.setEditable(false);
	}
	
	
	private static void initializeLogic() {
		try {
			logic = new CdLogic();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * This is a constructor for the labels require to act as header
	 *
	 * @param headerDays
	 */
	private void prepareLabelsForHeader(Label[] headerDays) {
		headerDays[0] = new Label("Sun");
		GridPane.setHalignment(headerDays[0], HPos.CENTER);
		headerDays[1] = new Label("Mon");
		GridPane.setHalignment(headerDays[1], HPos.CENTER);
		headerDays[2] = new Label("Tues");
		GridPane.setHalignment(headerDays[2], HPos.CENTER);
		headerDays[3] = new Label("Wed");
		GridPane.setHalignment(headerDays[3], HPos.CENTER);
		headerDays[4] = new Label("Thurs");
		GridPane.setHalignment(headerDays[4], HPos.CENTER);
		headerDays[5] = new Label("Fri");
		GridPane.setHalignment(headerDays[5], HPos.CENTER);
		headerDays[6] = new Label("Sat");
		GridPane.setHalignment(headerDays[6], HPos.CENTER);
		
		monthHeader.setText(currentMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH)+ " " + String.valueOf(currentYear));
	}

	/**
	 * Prepare the label array with values of current month.
	 * 
	 * @param inputMonth inputYear
	 */
	private void prepareLabelsForCalendar(Month inputMonth, int inputYear) {
		LocalDate monthToShow = LocalDate.of(inputYear, inputMonth, 1);
		int offset = 0;
		
		//If monday we offset by 1 day to fill the calendar and same for tuesday, wednesday, thursday and so on...
		if(monthToShow.getDayOfWeek().getValue() > 1) {
			offset = monthToShow.getDayOfWeek().getValue();
		}
		//However, on sunday the offset is 7 so that we show 1 row of the previous month on top
		else {
			offset = 7;
		}
		monthToShow = monthToShow.minusDays(offset);
		
		for(int i = 0; i < NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER; i++) {
			for(int j = 0; j < NUMBER_OF_COLS_IN_CALENDAR; j++) {
				dateNumbers[i][j] = new Label(String.valueOf(monthToShow.getDayOfMonth()));
				monthToShow = monthToShow.plusDays(1);
			}
		}
	}

	/**
	 * Set the constraints of the gridpane such that each column and row is properly spread out
	 */
	private void initializeCalendar() {
		ColumnConstraints columnWeight = new ColumnConstraints();
		columnWeight.setPercentWidth(50);
		RowConstraints rowWeight = new RowConstraints();
		RowConstraints rowDayHeaderWeight = new RowConstraints();
		rowWeight.setPercentHeight(50);
		rowDayHeaderWeight.setPercentHeight(30);
		for(int i = 0; i < NUMBER_OF_COLS_IN_CALENDAR; i++) {
			calendar.getColumnConstraints().set(i, columnWeight);
		}
		calendar.getRowConstraints().set(0, rowDayHeaderWeight);
		for(int i = 1; i < NUMBER_OF_ROWS_IN_CALENDAR; i++) {
			calendar.getRowConstraints().set(i, rowWeight);
		}
	}

	/**
	 * Initialize the format of the cells within calendars
	 */
	private void initializeCellFormat() {
	
		basicCellColumnConstraints[MARGIN_COLUMN] = new ColumnConstraints();
		basicCellColumnConstraints[MARGIN_COLUMN].setPercentWidth(2);
		
		basicCellColumnConstraints[INDEX_COLUMN] = new ColumnConstraints();
		basicCellColumnConstraints[INDEX_COLUMN].setPercentWidth(25);
		
		basicCellColumnConstraints[PREVIEW_COLUMN] = new ColumnConstraints();
		basicCellColumnConstraints[PREVIEW_COLUMN].setPercentWidth(70);
		
		basicCellRowConstraints = new RowConstraints();
		basicCellRowConstraints.setPercentHeight(50);
		
		
	}

	/**
	 * Clear cells of all labels
	 */
	private void setCellFormat() {
	
		for(int i = 0; i < NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER; i++) {
			for(int j = 0; j < NUMBER_OF_COLS_IN_CALENDAR; j++) {
				cellFormat[i][j] = new GridPane();
				cellFormat[i][j].getColumnConstraints().addAll(basicCellColumnConstraints);
				cellFormat[i][j].getRowConstraints().addAll(basicCellRowConstraints,basicCellRowConstraints,basicCellRowConstraints,basicCellRowConstraints);
			}
		}
	}

	/**
	 * Fills the first 7 grids with Sun, Mon, Tues, Wed, Thurs, Fri, Sat respectively
	 * 
	 */
	private void fillHeader() {
		//Fill in the headers in monthView
		for(int i = 0; i < NUMBER_OF_DAYS_IN_A_WEEK; i++) {
			calendar.add(headerDays[i], i, 0);
		}
		
	}

	private void fillCells() {
		
		//Prepare the empty cell format for display
		for(int i = 0; i < NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER; i++) {
			for(int j = 0; j < NUMBER_OF_COLS_IN_CALENDAR; j++) {
				cellFormat[i][j].add(dateNumbers[i][j], 1, 0);
			}
		}
	}

	/**
	 * Fills the gridpane: monthview with labels for the current month
	 * 
	 * @param dateNumbers
	 */
	
	private void fillCalendar() {
		
		//Fill in the rest of the days as prepared previously 
		for(int i = 0; i < NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER; i++) {
			for(int j = 0; j < NUMBER_OF_COLS_IN_CALENDAR; j++) {
	
				//monthView.add(dateNumbers[i][j], j, i+1);
				calendar.add(cellFormat[i][j], j, i+1);
				GridPane.setHalignment(dateNumbers[i][j], HPos.CENTER);
			}
		}
		
	}

	/**
	 * Update the calendar with the next month.
	 */
	@FXML
	private void handleNextMonthButton() {
		removeCellsFromCalendar();
		setCellFormat();
		updateMonth(PLUS);
		prepareLabelsForCalendar(currentMonth, currentYear);
		fillCells();
		fillCalendar();
		monthHeader.setText(currentMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + String.valueOf(currentYear));
		//outputToTextArea("MONTH UP BY 1");
	}
	
	/**
	 * Update the calendar with the previous month.
	 */
	@FXML
	private void handlePreviousMonthButton() {
		removeCellsFromCalendar();
		setCellFormat();
		updateMonth(MINUS);
		prepareLabelsForCalendar(currentMonth, currentYear);
		fillCells();
		fillCalendar();
		monthHeader.setText(currentMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + String.valueOf(currentYear));
		//outputToTextArea("MONTH DROP BY 1");
	}
	
	/**
	 * Clear the current labels in monthView
	 * Must be called before updating the dateNumbers array
	 */
	private void removeCellsFromCalendar() {
		for(int i = 0; i < NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER; i++) {
			for(int j = 0; j < NUMBER_OF_COLS_IN_CALENDAR; j++) {
				calendar.getChildren().remove(cellFormat[i][j]);
			}
		}
	}

	/**
	 * The function update the currentMonth and currentYear variable
	 * 
	 * @param plusOrMinus
	 */
	private void updateMonth(int plusOrMinus) {
		if(plusOrMinus == MINUS) {
			if(currentMonth.getValue() == 1) {
				currentYear--;
			}
			currentMonth = currentMonth.minus(1);
		}
		else if(plusOrMinus == PLUS) {
			if(currentMonth.getValue() == 12) {
				currentYear++;
			}
			currentMonth = currentMonth.plus(1);
		}
	}

	/**
	 * Takes in the user input from the textfield after he press 'enter'
	 * @throws IOException 
	 */
	@FXML
	private void getUserInput(KeyEvent event) throws IOException {
		if (event.getCode() == KeyCode.ENTER) {
			if(input.getText() != null && !input.getText().isEmpty())
			userInput = input.getText();
			
			String response = logic.executeCommand(userInput);
			
			outputToTextArea(response);
			
			mainApp.setTaskData(logic.getDisplayList());
			taskTable.setItems(mainApp.getTaskData());
			
			userInput = "";
			input.setText("");
		}
	}

	/**
	 * Takes in a string and displays it to the user.
	 * 
	 * @param text
	 */
	@FXML
	private void outputToTextArea(String text) {
		output.appendText(text+"\n");
	}
	
	/**
	 * Is called by the main application to give a reference back to itself.
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;

		//Add observable list data to the table 
		taskTable.setItems(mainApp.getTaskData());	
	}
}
