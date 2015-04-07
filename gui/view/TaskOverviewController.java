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
import javafx.scene.layout.StackPane;
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
import java.util.LinkedList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.geometry.HPos;

import java.time.temporal.ChronoUnit;

public class TaskOverviewController{
	
	private static final int NUMBER_OF_COLUMNS_IN_CELL = 2;
	private static final int NUMBER_OF_ROWS_IN_CELL = 5;
	private static final int NUMBER_OF_ROWS_IN_CALENDAR = 7;
	private static final int NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER = 6;
	private static final int NUMBER_OF_COLS_IN_CALENDAR = 7;
	private static final int NUMBER_OF_DAYS_IN_A_WEEK = 7;
	private static final int PLUS = 1;
	private static final int MINUS = -1;
	private static final int NOW = 0;
	private static final int MARGIN_COLUMN = 0;
	private static final int CONTENT_COLUMN = 1;
	private static final int MARGIN_ROW = 0;
	private static final int CONTENT_ROW = 1;
	private static final int ROW_1 = 0;
	private static final int ROW_2 = 1;
	private static final int ROW_3 = 2;
	
	private static final String MESSAGE_LOG_ERROR = "An error has occured, please try again.";
	private static final String MESSAGE_EMPTY_USER_INPUT_ERROR = "Please key in a command first.";
	private static final String MESSAGE_WELCOME = "Hello! Welcome to Comman.Do, your to-do manager.\n"
												+ "Please note that the directories to save your tasks are by default in the same directory as the application.\n" 
												+ "Start by entering a command into the box below.\n";
	private static final String ODD_ROW_CLASS = "oddRow";
	private static final String EVEN_ROW_CLASS = "evenRow";
	private static final String ROW_1_CLASS = "row1";
	private static final String ROW_2_CLASS = "row2";
	private static final String ROW_3_CLASS = "row3";
	private static final String NOT_IN_MONTH_FADE_DATE_CLASS = "fadeDate";
	private static final String NOT_IN_MONTH_FADE_CELL_CLASS = "fadeCell";
	private static final String TODAY_CLASS = "todayClass";
	private static final String EVENT = "event";
	private static final String DATELINE = "dateline";
	
	private static CdLogic logic;
	
	@FXML
	private TableView<Task> taskTable;
	@FXML
	private TableColumn<Task, String> taskIdColumn;
	@FXML
	private TableColumn<Task, String> taskNameColumn;
	@FXML
	private TableColumn<Task, String> taskCommentColumn;
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
	
	private StackPane[] headerDaysContainers = new StackPane[7];
	
	private Label[][] dateNumbers = new Label[NUMBER_OF_ROWS_IN_CALENDAR][NUMBER_OF_COLS_IN_CALENDAR];
	
	private GridPane[][] cellFormat = new GridPane[NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER][NUMBER_OF_COLS_IN_CALENDAR];
	
	private ColumnConstraints[] basicCellColumnConstraints = new ColumnConstraints[NUMBER_OF_COLUMNS_IN_CELL];

	private RowConstraints[] basicCellRowConstraints = new RowConstraints[NUMBER_OF_ROWS_IN_CELL];
	
	ObservableList<Task> taskList; 
	
	private Label[][][] calendarTasks = new Label[6][7][3];
	
	private LocalDate startingDate;
	
	private LinkedList<String> upperCache;
	
	private LinkedList<String> lowerCache;
	
	private KeyEvent prevEvent;
	
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
		taskIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		startDateColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());
		startTimeColumn.setCellValueFactory(cellData -> cellData.getValue().startTimeProperty());
		endDateColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty());
		endTimeColumn.setCellValueFactory(cellData -> cellData.getValue().endTimeProperty());
		
		//TaskOverviewControllerLogger.initializeLogger();
		output.setEditable(false);
		initializeLogic();
		outputToTextArea(MESSAGE_WELCOME);
		setStartingDate(computeStartDate(getCurrentMonth(), getCurrentYear()));
		setUpStack();
	}
	
	/**
	 * Initialize one instance of logic
	 */
	static void initializeLogic() {
		try {
			setLogic(new CdLogic());
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
		for(int i = 0; i < NUMBER_OF_DAYS_IN_A_WEEK; i++) {
			headerDaysContainers[i] = new StackPane();
			headerDaysContainers[i].getChildren().add(headerDays[i]);
			headerDaysContainers[i].getStyleClass().add("headerDays");
		}
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
				if(monthToShow.getMonthValue() != inputMonth.getValue()) {
					getDateNumbers()[i][j].getStyleClass().add(NOT_IN_MONTH_FADE_DATE_CLASS);
				}
				
				if(monthToShow.getYear() == LocalDate.now().getYear() && monthToShow.getDayOfYear() == LocalDate.now().getDayOfYear()) {
					getDateNumbers()[i][j].getStyleClass().add(TODAY_CLASS);
				}
				
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
		LocalDate startDateToAdd = calendarStartDate;
		LocalDate endDateToAdd = calendarEndDate;
		Task toAdd;
		for(int i = 0; i < taskList.size(); i++) {
			toAdd = taskList.get(i);
			
			if(toAdd.getType().equals(EVENT)) {
				//O||O
				if(toAdd.getStartDate().until(calendarStartDate, ChronoUnit.DAYS) >= 0 
						&& calendarEndDate.until(toAdd.getEndDate(), ChronoUnit.DAYS) >= 0) {
					startDateToAdd = calendarStartDate;
					endDateToAdd = calendarEndDate;
					computeAndAddLabel(calendarStartDate, startDateToAdd, endDateToAdd, toAdd);
				}
				//O|O|
				else if(toAdd.getStartDate().until(calendarStartDate, ChronoUnit.DAYS) >= 0
						&& calendarStartDate.until(toAdd.getEndDate(), ChronoUnit.DAYS) >= 0
						&& calendarEndDate.until(toAdd.getEndDate(), ChronoUnit.DAYS) <= 0) {
					startDateToAdd = calendarStartDate;
					endDateToAdd = toAdd.getEndDate();
					computeAndAddLabel(calendarStartDate, startDateToAdd, endDateToAdd, toAdd);
				}
				//|O|O
				else if(toAdd.getStartDate().until(calendarStartDate, ChronoUnit.DAYS) <= 0
						&& toAdd.getStartDate().until(calendarEndDate, ChronoUnit.DAYS) >= 0
						&& calendarEndDate.until(toAdd.getEndDate(), ChronoUnit.DAYS) >= 0) {
					startDateToAdd = toAdd.getStartDate();
					endDateToAdd = calendarEndDate;
					computeAndAddLabel(calendarStartDate, startDateToAdd, endDateToAdd, toAdd);
				}
				//|OO|
				else if(toAdd.getStartDate().until(calendarStartDate, ChronoUnit.DAYS) <= 0 
						&& calendarEndDate.until(toAdd.getEndDate(), ChronoUnit.DAYS) <= 0) {
					startDateToAdd = toAdd.getStartDate();
					endDateToAdd = toAdd.getEndDate();
					computeAndAddLabel(calendarStartDate, startDateToAdd, endDateToAdd, toAdd);
				}

			}
			
			
			else if(toAdd.getType().equals(DATELINE)) {
				if(toAdd.getStartDate().until(calendarStartDate, ChronoUnit.DAYS) <= 0 &&
						toAdd.getStartDate().until(calendarEndDate, ChronoUnit.DAYS) >= 0) {		
					startDateToAdd = toAdd.getStartDate();
					endDateToAdd = toAdd.getStartDate();
					computeAndAddLabel(calendarStartDate, startDateToAdd, endDateToAdd, toAdd);
				}
			}
		}
	}

	private void computeAndAddLabel(LocalDate calendarStartDate,
			LocalDate startDateToAdd, LocalDate endDateToAdd, Task toAdd) {
		//Compute the difference, row and col to start adding
		long difference = calendarStartDate.until(startDateToAdd, ChronoUnit.DAYS);
		
		int row = (int) (difference / 7);
		int col = (int) (difference % 7);
		
		
		//Check if for that particular day, we already have 3 labels to show
		
		if(calendarTasks[row][col][0] == null) {
			addLabelToCalendarTasks(startDateToAdd, endDateToAdd, toAdd, row, col, 0);
		}	
		else if(calendarTasks[row][col][1] == null) {
			addLabelToCalendarTasks(startDateToAdd, endDateToAdd, toAdd, row, col, 1);
		}
		else if(calendarTasks[row][col][2] == null) {
			addLabelToCalendarTasks(startDateToAdd, endDateToAdd, toAdd, row, col, 2);
		}
	}

	private void addLabelToCalendarTasks(LocalDate startDateToAdd, LocalDate endDateToAdd, Task toAdd, int row, int col, int taskNumber) {
		
		while(startDateToAdd.until(endDateToAdd, ChronoUnit.DAYS) >= 0) {
			//System.out.println("Adding: "+toAdd.getTaskName());
			calendarTasks[row][col][taskNumber] = new Label(toAdd.getTaskName());
			addClassToLabel(row, col, taskNumber);
			
			col++;
			if(col > 6) {
				col = 0;
				row++;
			}
			
			startDateToAdd = startDateToAdd.plusDays(1);
		}
	}

	private void addClassToLabel(int row, int col, int taskNumber) {
		switch(taskNumber) {
		case ROW_1 :
			calendarTasks[row][col][taskNumber].getStyleClass().add(ROW_1_CLASS);
			//System.out.println("Adding Class 1");
			break;
		case ROW_2 :
			calendarTasks[row][col][taskNumber].getStyleClass().add(ROW_2_CLASS);
			//System.out.println("Adding Class 2");
			break;
		case ROW_3 :
			calendarTasks[row][col][taskNumber].getStyleClass().add(ROW_3_CLASS);
			//System.out.println("Adding Class 3");
			break;
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
		basicCellColumnConstraints[MARGIN_COLUMN].setPercentWidth(5);
		
		basicCellColumnConstraints[CONTENT_COLUMN] = new ColumnConstraints();
		basicCellColumnConstraints[CONTENT_COLUMN].setPercentWidth(95);
		
		basicCellRowConstraints[MARGIN_ROW] = new RowConstraints();
		basicCellRowConstraints[MARGIN_ROW].setPercentHeight(5);
		
		basicCellRowConstraints[CONTENT_ROW] = new RowConstraints();
		basicCellRowConstraints[CONTENT_ROW].setPercentHeight(25);
	}

	/**
	 * Clear cells of all labels
	 */
	private void setCellFormat() {
	
		for(int i = 0; i < NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER; i++) {
			for(int j = 0; j < NUMBER_OF_COLS_IN_CALENDAR; j++) {
				cellFormat[i][j] = new GridPane();
				cellFormat[i][j].getColumnConstraints().addAll(basicCellColumnConstraints);
				cellFormat[i][j].getRowConstraints().addAll(basicCellRowConstraints[MARGIN_ROW],basicCellRowConstraints[CONTENT_ROW],basicCellRowConstraints[CONTENT_ROW],basicCellRowConstraints[CONTENT_ROW],basicCellRowConstraints[CONTENT_ROW]);
				//Even week add class
				if(i % 2 == 0) {
					cellFormat[i][j].getStyleClass().add(EVEN_ROW_CLASS);
				}
				else {
					cellFormat[i][j].getStyleClass().add(ODD_ROW_CLASS);
				}
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
			calendar.add(headerDaysContainers[i], i, 0);
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
						cellFormat[i][j].add(calendarTasks[i][j][k], 1, k+2);
					}
				}
			}
		}
	}

	private void fillCells() {
		
		//Prepare the empty cell format for display
		for(int i = 0; i < NUMBER_OF_ROWS_IN_CALENDAR_WITHOUT_HEADER; i++) {
			for(int j = 0; j < NUMBER_OF_COLS_IN_CALENDAR; j++) {
				cellFormat[i][j].add(getDateNumbers()[i][j], 1, 1);
				if(getDateNumbers()[i][j].getStyleClass().contains(NOT_IN_MONTH_FADE_DATE_CLASS)) {
					cellFormat[i][j].getStyleClass().add(NOT_IN_MONTH_FADE_CELL_CLASS);
				}
				if(getDateNumbers()[i][j].getStyleClass().contains(TODAY_CLASS)) {
					cellFormat[i][j].getStyleClass().add(TODAY_CLASS);
				}
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
				if(cellFormat[i][j] == null) {
					//System.out.println(i + ", " + j);
				}
				else {
					calendar.add(cellFormat[i][j], j, i+1);
				}
			}
			
		}
		
	}
	
	/**
	 * Update the calendar with the next month.
	 */
	@FXML
	private void goBackNow() {
		removeCellsFromCalendar();
		setCellFormat();
		updateMonth(NOW);
		prepareLabelsForCalendar(getCurrentMonth(), getCurrentYear());
		setStartingDate(computeStartDate(currentMonth, currentYear));
		prepareTaskLabelsForCalendar(getStartingDate().getDayOfMonth(), getStartingDate().getMonth(), getStartingDate().getYear());
		fillCells();
		fillTasksIntoCells();
		fillCalendar();
		getMonthHeader().setText(getCurrentMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + String.valueOf(getCurrentYear()));
	}

	/**
	 * Update the calendar with the next month.
	 */
	@FXML
	private void changeNextMonth() {
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
	private void changePreviousMonth() {
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
		else if(plusOrMinus == NOW) {
			setCurrentYear(LocalDate.now().getYear());
			setCurrentMonth(LocalDate.now().getMonth());
		}
	}

	/**
	 * Takes in the KeyEvent and execute the appropriate actions
	 * CTRL -> Shifts focus to textField
	 * @throws IOException 
	 */
	@FXML
	private void executeKeyEvents(KeyEvent event) throws IOException, EmptyUserInputException {
		
		if (event.getCode() == KeyCode.CONTROL) {
			if(!input.isFocused()) {
				input.requestFocus();
				input.end();
			}
			else {
				calendar.requestFocus();
			}
		}
		else if (event.getCode() == KeyCode.ESCAPE) {
			calendar.requestFocus();
		}
		else if(event.getCode() == KeyCode.LEFT) {
			changePreviousMonth();
		}
		else if(event.getCode() == KeyCode.RIGHT) {
			changeNextMonth();
		}
		else if(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
			goBackNow();
		}
		event.consume();
	}
	
	/**
	 * Takes in the user input from the textfield after he press 'enter'
	 * @throws IOException 
	 */
	@FXML
	private void executeKeyEventsInInput(KeyEvent event) throws IOException, EmptyUserInputException {
		if (event.getCode() == KeyCode.ENTER) {
			executeCommandUserEntered();
		}
		
		//Alternate behaviour for UP and DOWN
		else if(event.getCode() == KeyCode.UP) {
			if(input.isFocused()) {
				String previousCommand = peekPreviousCommand();
				if(previousCommand != null && !previousCommand.equals("")) {
					if(prevEvent == null || prevEvent.getCode() == KeyCode.UP) {
						getInput().setText(previousCommand);
						pushDownCommand(popPreviousCommand());
						input.end();
					}
					else if(prevEvent.getCode() == KeyCode.DOWN && peekNextCommand() != null) {
						System.out.println("PD");
						pushDownCommand(popPreviousCommand());
						previousCommand = peekPreviousCommand();
						getInput().setText(previousCommand);
						pushDownCommand(popPreviousCommand());
						input.end();
					}
					else if(prevEvent.getCode() == KeyCode.DOWN) {
						getInput().setText(previousCommand);
						pushDownCommand(popPreviousCommand());
						input.end();
						
					}
				}
				prevEvent = event;
			}
			event.consume();
		}
		else if(event.getCode() == KeyCode.DOWN) {
			if(input.isFocused()) {
				String nextCommand = peekNextCommand();
				if(nextCommand != null && !nextCommand.equals("")) {
					if(prevEvent == null || prevEvent.getCode() == KeyCode.DOWN) {
						getInput().setText(nextCommand);
						pushUpCommand(popNextCommand());
					}
					else if(prevEvent.getCode() == KeyCode.UP) {
						pushUpCommand(popNextCommand());
						nextCommand = peekNextCommand();
						getInput().setText(nextCommand);
						pushUpCommand(popNextCommand());
					}
				}
				else {
					getInput().setText("");
				}
				prevEvent = event;
			}
		}
	}

	private void executeCommandUserEntered() throws EmptyUserInputException {
		if(getInput().getText() != null && !getInput().getText().isEmpty()) {
			userInput = getInput().getText();
			pushUpCommand(userInput);
			lowerCache = new LinkedList<String>();
			getInput().setText("");
			String response = "";
			try {
				response = getLogic().executeCommand(userInput);
			}
			catch(Exception e) {
				//TaskOverviewControllerLogger.logger.log(Level.SEVERE, "Executed: {0}, Error in logic has occured", userInput);
			}

			assert !response.equals("") : MESSAGE_LOG_ERROR;
			
			//Logging what the user is executing and the response received
			//TaskOverviewControllerLogger.logger.log(Level.INFO, "Executed: {0}, Response {1}",  new Object[] {userInput, response});
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
		mainApp.setTaskData(getLogic().getDisplayList());
		taskTable.setItems(mainApp.getTaskData());
	}
	
	/**
	 * Takes the full taskList from logic
	 */
	void updateTaskList() {
		taskList = getLogic().getTaskList();
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
	
	/**
	 * Set up the stacks for storing previous user inputs
	 * @return 
	 */
	private void setUpStack() {
		upperCache = new LinkedList<String>();
		lowerCache = new LinkedList<String>();
	}
	
	/**
	 * Get previous command from stack from up key pressed
	 */
	private String peekPreviousCommand() {
		if(upperCache.size() > 0) {
			return upperCache.peek();
		}
		return null;
	}
	
	/**
	 * Get next command from stack from down key pressed
	 */
	private String peekNextCommand() {
		if(lowerCache.size() > 0) {
			return lowerCache.peek();
		}
		return null;
	}
	
	/**
	 * Push in command to bottom stack
	 */
	private void pushDownCommand(String command){
		lowerCache.push(command);
	}
	
	/**
	 * Push in command to upper stack
	 */
	private void pushUpCommand(String command){
		upperCache.push(command);
	}
	
	/**
	 * Pop the command in the upper stack
	 */
	private String popPreviousCommand() {
		return upperCache.pop();
	}
	
	/**
	 * Pop the command in the lower stack
	 */
	private String popNextCommand() {
		return lowerCache.pop();
	}
	
	/**
	 * The setup() method initializes the data only after the reference to mainApp is obtained
	 */
	public void setup() {
		updateTaskTable();
		prepareLabelsForHeader(headerDays);
		prepareLabelsForCalendar(getCurrentMonth(), getCurrentYear());
		prepareTaskLabelsForCalendar(getStartingDate().getDayOfMonth(), getStartingDate().getMonth(), getStartingDate().getYear());
		initializeCalendar();
		initializeCellFormat();
		setCellFormat();
		fillHeader();
		fillCells();
		fillTasksIntoCells();
		fillCalendar();
		input.requestFocus();
	}

	public TextField getInput() {
		return input;
	}

	public void setInput(TextField input) {
		this.input = input;
	}

	public CdLogic getLogic() {
		return logic;
	}

	public static void setLogic(CdLogic logic) {
		TaskOverviewController.logic = logic;
	}
}
