package gui.view;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

import javafx.collections.ObservableList;
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
import java.util.Collections;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.geometry.HPos;

import java.time.temporal.ChronoUnit;

public class TaskOverviewController{
	
	public static class TaskOverviewControllerLogger {
		static FileHandler fileHandler;
		
		//Use the classname for the logger
		private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		
		private final static void initializeLogger() {
		    try {  
		        // This block configure the logger with handler and formatter 
		    	System.out.println("Creating logger at: " + System.getProperty("user.dir") + "\\logFile.txt");
		        fileHandler = new FileHandler(System.getProperty("user.dir") + "logFile.txt");
		        logger.addHandler(fileHandler);
		        SimpleFormatter formatter = new SimpleFormatter();  
		        fileHandler.setFormatter(formatter);
		    } catch (SecurityException securityException) {  
		    	securityException.printStackTrace();  
		    } catch (IOException iOException) {  
		        iOException.printStackTrace();  
		    }
		}
	}
	
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
	private static final String MESSAGE_LOG_ERROR = "An error has occured, please try again.";
	private static final String MESSAGE_EMPTY_USER_INPUT_ERROR = "Please key in a command first.";
	private static final String MESSAGE_WELCOME = "Hello! Welcome to Comman.Do, your to-do manager.\n"
												+ "Please note that the directories to save your tasks are by default in the same directory as the application.\n" 
												+ "Start by entering a command into the box below.\n";
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
	
	private ObservableList<Task> taskList; 
	
	private Label[][][] calendarTasks = new Label[6][7][3];
	
	private LocalDate startingDate;
	
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
		
		TaskOverviewControllerLogger.initializeLogger();
		output.setEditable(false);
		initializeLogic();
		outputToTextArea(MESSAGE_WELCOME);
		
		prepareLabelsForHeader(headerDays);
		prepareLabelsForCalendar(getCurrentMonth(), getCurrentYear());
		setStartingDate(computeStartDate(getCurrentMonth(), getCurrentYear()));
		prepareTaskLabelsForCalendar(getStartingDate().getDayOfMonth(), getStartingDate().getMonth(), getStartingDate().getYear());
		initializeCalendar();
		initializeCellFormat();
		setCellFormat();
		fillHeader();
		fillCells();
		fillCalendar();
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
	public void prepareLabelsForHeader(Label[] headerDays) {
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
		
		getMonthHeader().setText(getCurrentMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)+ " " + String.valueOf(getCurrentYear()));
	}

	/**
	 * Prepare the label array with values of current month.
	 * 
	 * @param inputMonth inputYear
	 */
	public void prepareLabelsForCalendar(Month inputMonth, int inputYear) {
		LocalDate monthToShow = computeStartDate(inputMonth, inputYear);
		
		for(int i = 0; i < NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER; i++) {
			for(int j = 0; j < NUMBER_OF_COLS_IN_CALENDAR; j++) {
				getDateNumbers()[i][j] = new Label(String.valueOf(monthToShow.getDayOfMonth()));
				monthToShow = monthToShow.plusDays(1);
			}
		}
	}
	
	/**
	 * Computes the starting date of the calendar
	 * @param inputMonth
	 * @param inputYear
	 * @return
	 */
	private LocalDate computeStartDate(Month inputMonth, int inputYear) {
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
		return monthToShow;
	}
	
	/**
	 * Prepare the labels from tasks to be inserted into the calendar.
	 * 
	 */
	public void prepareTaskLabelsForCalendar(int inputDay, Month inputMonth, int inputYear) {
		
		/*
		 * Pseudo-code
		 * sort taskList <should be in chronological order>
		 * determine the start calendar and end calendar
		 * find the point where we can show a task
		 * make labels for up to 3 per day, the rest are ignored
		 * stop when we reach the end of calendar 
		 * 
		 */
		updateTaskList();
		calendarTasks = new Label[6][7][3];
		Collections.sort(taskList);
		LocalDate calendarStartDate = LocalDate.of(inputYear, inputMonth, inputDay);
		LocalDate calendarEndDate = calendarStartDate.plusDays(41);
		for(int i = 0; i < taskList.size(); i++) {
			//If startDate of task is earlier than calendarEndDate
			if(taskList.get(i).getStartDate() != null &&
				taskList.get(i).getStartDate().compareTo(calendarEndDate) <= 0) {
				//Get which row and which col is the event on
				long difference = calendarStartDate.until(taskList.get(i).getStartDate(), ChronoUnit.DAYS);
				LocalDate startDateToAdd;
				
				if(difference < 0) {
					difference = 0;
					startDateToAdd = calendarStartDate;
				}
				else {
					startDateToAdd = taskList.get(i).getStartDate();
				}
				
				int row = (int) (difference / 7);
				int col = (int) (difference % 7);
				System.out.println(difference + " " + row + " and " + col);
				//Check if for that particular day, we already have 3 labels to show
				if(calendarTasks[row][col][0] == null)
					addLabelToCalendarTasks(startDateToAdd, calendarEndDate, taskList.get(i), row, col, 0);
				else if(calendarTasks[row][col][1] == null) {
					addLabelToCalendarTasks(startDateToAdd, calendarEndDate, taskList.get(i), row, col, 1);
				}
				else if(calendarTasks[row][col][2] == null) {
					addLabelToCalendarTasks(startDateToAdd, calendarEndDate, taskList.get(i), row, col, 2);
				}
			}
		}
		
		
		
	}

	private void addLabelToCalendarTasks(LocalDate startDateToAdd, LocalDate calendarEndDate, Task toAdd, int row, int col, int taskNumber) {
		LocalDate endDateToStopAdding = toAdd.getEndDate();
		if(endDateToStopAdding != null) {
			while(startDateToAdd.until(endDateToStopAdding, ChronoUnit.DAYS) >= 0 && startDateToAdd.compareTo(calendarEndDate) <= 0) {
				calendarTasks[row][col][taskNumber] = new Label(toAdd.getTaskName());
				col++;
				if(col > 6) {
					col = 0;
					row++;
				}
				startDateToAdd = startDateToAdd.plusDays(1);
			}
		}
		else {
			calendarTasks[row][col][taskNumber] = new Label(toAdd.getTaskName());
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
		basicCellColumnConstraints[PREVIEW_COLUMN].setPercentWidth(100);
		
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

	/**
	 * This method fills in the labels created for each tasks into the calendar
	 */
	private void fillTasksIntoCells() {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 7; j++) {
				for(int k = 0; k < 3; k++) {
					if(calendarTasks[i][j][k] != null) {
						cellFormat[i][j].add(new Label(Integer.toString(k+1) + ": "), 1, k+1);
						cellFormat[i][j].add(calendarTasks[i][j][k], 2, k+1);
					}
				}
			}
		}
	}

	private void fillCells() {
		
		//Prepare the empty cell format for display
		for(int i = 0; i < NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER; i++) {
			for(int j = 0; j < NUMBER_OF_COLS_IN_CALENDAR; j++) {
				cellFormat[i][j].add(getDateNumbers()[i][j], 1, 0);
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
				GridPane.setHalignment(getDateNumbers()[i][j], HPos.CENTER);
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
		prepareLabelsForCalendar(getCurrentMonth(), getCurrentYear());
		setStartingDate(computeStartDate(currentMonth, currentYear));
		prepareTaskLabelsForCalendar(getStartingDate().getDayOfMonth(), getStartingDate().getMonth(), getStartingDate().getYear());
		fillCells();
		fillTasksIntoCells();
		fillCalendar();
		getMonthHeader().setText(getCurrentMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + String.valueOf(getCurrentYear()));
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
		prepareLabelsForCalendar(getCurrentMonth(), getCurrentYear());
		setStartingDate(computeStartDate(currentMonth, currentYear));
		prepareTaskLabelsForCalendar(startingDate.getDayOfMonth(), startingDate.getMonth(), startingDate.getYear());
		fillCells();
		fillTasksIntoCells();
		fillCalendar();
		getMonthHeader().setText(getCurrentMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + String.valueOf(getCurrentYear()));
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
			if(getCurrentMonth().getValue() == 1) {
				setCurrentYear(getCurrentYear() - 1);
			}
			setCurrentMonth(getCurrentMonth().minus(1));
		}
		else if(plusOrMinus == PLUS) {
			if(getCurrentMonth().getValue() == 12) {
				setCurrentYear(getCurrentYear() + 1);
			}
			setCurrentMonth(getCurrentMonth().plus(1));
		}
	}

	/**
	 * Takes in the user input from the textfield after he press 'enter'
	 * @throws IOException 
	 */
	@FXML
	private void executeUserInput(KeyEvent event) throws IOException, EmptyUserInputException {
		
		if (event.getCode() == KeyCode.ENTER) {
			if(input.getText() != null && !input.getText().isEmpty()) {
				userInput = input.getText();
				input.setText("");
				String response = "";
				try {
					response = logic.executeCommand(userInput);
				}
				catch(Exception e) {
					TaskOverviewControllerLogger.logger.log(Level.SEVERE, "Executed: {0}, Error in logic has occured", userInput);
				}
				assert !response.equals("") : MESSAGE_LOG_ERROR;
				
				//Logging what the user is executing and the response received
				TaskOverviewControllerLogger.logger.log(Level.INFO, "Executed: {0}, Response {1}",  new Object[] {userInput, response});
				userInput = "";
				outputToTextArea(response);
				
				updateTaskTable();
				removeCellsFromCalendar();
				setCellFormat();
				setStartingDate(computeStartDate(currentMonth, currentYear));
				prepareTaskLabelsForCalendar(startingDate.getDayOfMonth(), startingDate.getMonth(), startingDate.getYear());
				fillCells();
				fillTasksIntoCells();
				fillCalendar();
			}
			else {
				throw new EmptyUserInputException();
			}
		}
	}
	
	@SuppressWarnings("serial")
	private class EmptyUserInputException extends Exception{
		private EmptyUserInputException() {
			super(MESSAGE_EMPTY_USER_INPUT_ERROR);
		}
	}
	
	/**
	 * Takes the display list from logic and update the GUI
	 */
	public void updateTaskTable() {
		mainApp.setTaskData(logic.getDisplayList());
		taskTable.setItems(mainApp.getTaskData());
	}
	
	/**
	 * Takes the full taskList from logic
	 */
	private void updateTaskList() {
		taskList = logic.getTaskList();
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

	public Month getCurrentMonth() {
		return currentMonth;
	}

	public void setCurrentMonth(Month currentMonth) {
		this.currentMonth = currentMonth;
	}

	public int getCurrentYear() {
		return currentYear;
	}

	public void setCurrentYear(int currentYear) {
		this.currentYear = currentYear;
	}

	public Label getMonthHeader() {
		return monthHeader;
	}

	public void setMonthHeader(Label monthHeader) {
		this.monthHeader = monthHeader;
	}

	public Label[][] getDateNumbers() {
		return dateNumbers;
	}

	public void setDateNumbers(Label[][] dateNumbers) {
		this.dateNumbers = dateNumbers;
	}

	private LocalDate getStartingDate() {
		return startingDate;
	}

	private void setStartingDate(LocalDate startingDate) {
		this.startingDate = startingDate;
	}
}
