package logic;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Stack;
import java.util.regex.*;
import java.io.*;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sound.Sound;
import vault.CompletedTaskVault;
import vault.HistoryVault;
import vault.TaskVault;
import vault.TrashVault;
import model.IdGenerator;
import model.RecurringTask;
import model.Task;

/**
 * <h1>CdLogic Class</h1> The CdLogic class implements the comman.DO application
 * that is a task manager. The methods below implement the functionalities of
 * commands entered by a user
 * 
 */
public class CdLogic {
	

	private static final String MESSAGE_EMPTY_TASKNAME = "Task name cannot be empty";
	/**
	 * Listed below are the String constants e.g. Message returns This is to
	 * ensure easier code readability Start Date & End Date is in the format
	 * (DD:MM:YYYY) Start Time & End Time is in the format (HH:MM)
	 */
	
    //date and time format
	private static final String DATE_REGEX = "([0-9][0-9])[-/]\\s*([0-9][0-9])[-/]"
			+ "\\s*([0-9][0-9][0-9][0-9])";
	private static final String TIME_REGEX = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	private static final String TIME_PATTERN = "HH:mm";
	private static final String DATE_PATTERN = "dd/MM/yyyy";
	private static final String DATE_TIME_PATTERN = "HH:mm dd/MM/yyyy";
	
	//constants generated for add method
	private static final String MESSAGE_END_BEFORE_START = "Task cannot end before it starts";
	private static final String MESSAGE_NO_ENDTIME = "End date must be accompanied with an end time";
	private static final String MESSAGE_NO_STARTTIME = "Start date must be accompanied with start time";
	private static final String MESSAGE_INVALID_TASKNAME = "Task Name cannot start with \"@\"";
	private static final String MESSAGE_TIME_MISSING = "Enter a valid time";
	
	//constants generated for delete method
	private static final String MESSAGE_DELETE_UNSUCCESS = "Delete not successful";
	
	//constants generated for search method
	private static final String MESSAGE_TASKS_FOUND = " tasks found";
	
	//constants generated for complete method
	private static final String MESSAGE_COMPLETE_FAIL = "\"%s\" could not be completed";
	private static final String MESSAGE_COMPLETE_SUCCESS = "\"%s\" completed successfully";
	
	//constants generated for edit method
	private static final String MESSAGE_INVALID_DATE = "Date %s is not valid";
    private static final String MESSAGE_INVALID_TIME = "Time %s is not valid";
	private static final String MESSAGE_NOT_CHRON = "new date not chronologically correct";
	private static final String MESSAGE_EDIT_SUCCESS = "edit complete";
	private static final String MESSAGE_INVALID_EDIT = "invalid edit command";
	private static final String MESSAGE_NO_UNDO = "no more undo left";
	private static final String MESSAGE_FLOAT_ETIME = "Cannot edit end time of floating task";
	private static final String MESSAGE_DEADLINE_ETIME = "Cannot edit end time of deadline";
	private static final String MESSAGE_FLOAT_EDATE = "cannot edit end date of floating task";
	private static final String MESSAGE_DEADLINE_EDATE = "cannot edit end date of deadline";
	private static final String MESSAGE_ALREADY_EXISTS = "\"%s\" already exists";
	private static final String MESSAGE_DATE_MISSING = "Enter a valid date";
	private static final String MESSAGE_FLOAT_SDATE = "Cannot edit start date of floating task";
	private static final String MESSAGE_FLOAT_STIME = "Cannot edit start time of floating task";
	
	//constants generated for adding comment
	private static final String MESSAGE_COMMENT_ADDED = "comment added";
	
	//constants generated for undo methods
	private static final String MESSAGE_UNDO_ADD = "Undo add: \"%s\" removed from tasks";
	private static final String MESSAGE_UNDO_COMPLETE = "Undo complete: \"%s\" moved back from completed to tasks";
	private static final String MESSAGE_UNDO_DELETE = "Undo delete: \"%s\" moved back from trash to tasks";
	private static final String MESSAGE_UNDO_EDIT = "Undo edit: Change made to \"%s\" discarded";
	
	//contants generated for file directory methods
	private static final String MESSAGE_NONEXIST = "Directory doesnt exist";
	private static final String MESSAGE_FILES_MOVED = "Files moved to \"%s\"";
	private static final String MESSAGE_WORKING_DIRECTORY = "Working directory: ";
	private static final String USER_DIR = "user.dir";
	
	//constants generated for recurring methods
	private static final String MESSAGE_WILL_RECUR_FOREVER = "%s will recur on %s %s";
	private static final String MESSAGE_WILL_RECUR = "%s will recur on %s %s for %d times";
	private static final String MESSAGE_INVALID_RECURRENCES = "insert valid number for number of recurrence";
	private static final String MESSAGE_INVALID_DAYMONTH = "insert valid day and month (dd/mm)";
	private static final String DATEMONTH_PATTERN = "dd/MM";
	private static final String MESSAGE_NOT_FOUND = "task %s not found";
	private static final String MESSAGE_RECUR_FLOATING = "cannot recur floating task";
	private static final String MESSAGE_RECURSION_DETAILS = "insert recursion details";
	private static final String MESSAGE_SPECIFY_RECURRENCE = "specify to recur daily, weekly, monthly, or yearly";
	private static final String MESSAGE_INVALID_RECURFORMAT = "invalid recurrence format";
	private static final String MESSAGE_INVALID_DAYOFWEEK = "insert valid day to recur on";
	private static final String MESSAGE_RECUR_DAILY_FOREVER = "%s will recur %s forever";
	private static final String MESSAGE_RECUR_DAILY = "%s will recur %s for %d times";
	private static final String YEARLY = "yearly";
	private static final String MONTHLY = "monthly";
	private static final String WEEKLY = "weekly";
	private static final String DAILY = "daily";
	
	//constants generated for empty method
	private static final String MESSAGE_TRASH_CLEARED = "trash emptied successfully";
	private static final String MESSAGE_TRASH_UNCLEARED = "trash can't be emptied";
	
	//constants generated for execCommand method
	private static final String MESSAGE_INVALID_FORMAT = "invalid command "
			+ "format :%1$s";
	private static final String MESSAGE_ERROR = "Unrecognized command type";
	
	//constants generated for detCommand method
	private static final String MESSAGE_INVALID_COMMAND = "command type string cannot be null!";
	
	//constants generated for help command
	private static final String MESSAGE_HELP_COMMANDS = "A list of commands that you can use:\n"
			+ "add, list, edit, delete, search, undo, help, addrecur, recur, complete, empty, getdir, changedir, exit\n"
			+ "Enter help [command] for help with specific command syntax.\n";
	private static final String MESSAGE_HELP_ADD = "Action: Creates a task\n"
			+ "Syntax: add [TaskName] {StartDate StartTime} {EndDate EndTime}\n";
	private static final String MESSAGE_HELP_DELETE = "Action: Deletes a task\nSyntax: delete [TaskName]\n";
	private static final String MESSAGE_HELP_LIST = "Action: List out tasks\n"
			+ "Syntax: list {today/week/date/completed/trash}\n";
	private static final String MESSAGE_HELP_EMPTY = "Action: Empty a list\n"
			+ "Syntax: empty {completed/trash}\n";
	private static final String MESSAGE_HELP_SEARCH = "Action: Search for tasks through keywords\n"
			+ "Syntax: search [keywords]\n";
	private static final String MESSAGE_HELP_COMPLETE = "Action: Sets a task as complete\n"
			+ "Syntax: complete [TaskName]\n";
	private static final String MESSAGE_HELP_EDIT = "Action: Edit a field in a task\n"
			+ "Syntax: edit [TaskName] [Field To Edit] [New Value]\n";
	private static final String MESSAGE_HELP_EXIT = "Action: Exits Comman.Do\n"
			+ "Syntax: exit\n";
	private static final String MESSAGE_HELP_CHANGEDIR = "Action: Change the directory that stores your data\n"
			+ "Syntax: changedir [PATH]\n";
	private static final String MESSAGE_HELP_UNDO = "Action: Undo the last command\n"
			+ "Syntax: undo\n";
	private static final String MESSAGE_HELP_GETDIR = "Action: Get the current directory that stores your data\n"
			+ "Syntax: getdir\n";
	private static final String MESSAGE_HELP_ADDRECUR = "Action: Creates a recurring task\n"
			+ "Syntax: addrecur [same as adding tasks] {daily/weekly/monthly/yearly} [day of week/day of month/day and month] [recurrence]\n";
	private static final String MESSAGE_HELP_RECUR = "Action: Recur a task\n"
			+ "Syntax: recur [TaskName] {daily/weekly/monthly/yearly} [day of week/day of month/ day and month] [recurrence]\n";
	private static final String MESSAGE_HELP_INVALID = "Invalid help command: Enter \"help\" for list of commands\n";
	
	//constants generated for list command
	private static final String MESSAGE_INVALID_LIST = "Invalid list command";
	private static final String MESSAGE_HISTORY_DISPLAYED = "history displayed";
	private static final String MESSAGE_COMPLETED_DISPLAYED = "completed tasks displayed";
	private static final String MESSAGE_TRASH_DISPLAYED = "trash displayed";
	private static final String MESSAGE_ALL_DISPLAYED = "All tasks displayed";
	private static final String MESSAGE_NUM_DISPLAYED = " tasks displayed";
	
	//constants generated for show command
	private static final String MESSAGE_SHOW_YEARLY = "Recurs every %d of the year";
	private static final String MESSAGE_SHOW_MONTHLY = "Recurs every %d of the month";
	private static final String MESSAGE_SHOW_WEEKLY = "Recurs every %s";
	private static final String MESSAGE_SHOW_DAILY = "Recurs daily";
	private static final String MESSAGE_SHOW_TASK = "Task Name: %s\nStart: %s\nEnd: %s\nComment: %s\n";
	
	//integer constants for parsing of details into an array
	private static final int INDEX_FIRST_WORD = 0;
	private static final int LIST_ARGUMENTS_LENGTH = 4;
	private static final int ADD_ARGUMENTS_LENGTH = 6;
	private static final int INDEX_TASKNAME = 0;
	private static final int INDEX_COMMENT = 1;
	private static final int INDEX_STARTDATE = 2;
	private static final int INDEX_STARTTIME = 3;
	private static final int INDEX_ENDDATE = 4;
	private static final int INDEX_ENDTIME = 5;
	private static final int INDEX_PARSED_DATE_1 = 0;
	private static final int INDEX_PARSED_DATE_2 = 1;
	private static final int INDEX_PARSED_TIME_1 = 0;
	private static final int INDEX_PARSED_TIME_2 = 1;
	private static final int LIST_DATE_1 = 0;
	private static final int LIST_TIME_1 = 1;
	private static final int LIST_DATE_2 = 2;
	private static final int LIST_TIME_2 = 3;
	
	//CdLogic Field
	private static TaskVault taskVault;
	private static TrashVault trashVault;
	private static HistoryVault historyVault;
	private static CompletedTaskVault completedTaskVault;
	private static Stack<UNDOABLE> commandStack;
	private static String vaultPath;

	private static ObservableList<Task> toDisplay;
	private static ObservableList<Task> tasks;

	private Sound sound;

	enum UNDOABLE {
		ADD, DELETE, COMPLETE, EDIT, CHANGEDIR
	}

	enum COMMAND_TYPE {
		ADD, DELETE, LIST, EMPTY, SEARCH, COMPLETE, EDIT, INVALID, EXIT, CHANGEDIR, UNDO, NEXT, 
		GETDIR, ADDRECUR, RECUR, HELP, SHOW
	}

	enum TaskType {
		FLOAT, EVENT, DATELINE
	}

	/**
	 * Method that will work with GUI and TaskVault class. Initializes the
	 * display of tasks that is retrieved from TaskVault
	 * 
	 * @throws I0Exception
	 */

	public CdLogic() throws IOException {
		initializeFromConfig();
		initializeVaults();
		historyVault.clear();
		historyVault = new HistoryVault(vaultPath);
		commandStack = new Stack<UNDOABLE>();
		tasks = taskVault.getList();
		toDisplay = copyList(tasks);
		lookForRecurrence();
		sound = new Sound();
	}

	/**
	 * For testing purposes: Clears all lists and all data from the files.
	 */
	public void clear() {
		taskVault.clear();
		trashVault.clear();
		historyVault.clear();
		completedTaskVault.clear();
	}

	/**
	 * initializes config.txt to save to the working directory
	 * 
	 * @throws IOException
	 */
	private void initializeFromConfig() throws IOException {
		File config = new File("config.txt");
		/*
		 * Warning: If config.txt exists but is empty, a nullpointerexception
		 * will be thrown. If it exists but the path inside is not a directory,
		 * tasks will not be saved.
		 */
		if (!config.exists()) {
			config.createNewFile();
			writeToConfig(System.getProperty(USER_DIR));
			vaultPath = System.getProperty(USER_DIR);
		} else {
			vaultPath = getVaultPath("config.txt").trim();
		}
	}

	/**
	 * Obtains tasklist from TaskVault class Returns tasks to be viewed on the
	 * GUI
	 */
	public ObservableList<Task> getTaskList() {
		tasks = FXCollections.observableArrayList(taskVault.getList());

		return FXCollections.observableArrayList(toDisplay);
	}

	public ObservableList<Task> getDisplayList() {
		return toDisplay;
	}

	/**
	 * If command is empty, will display MESSAGE_INVALID_FORMAT Else, it will
	 * check the first word entered by user followed by obtaining the command
	 * type from method determineCommandType and executes command
	 * add(userCommand), delete(userCommand), list(userCommand), empty(),
	 * search(userCommand) complete(userCommand), edit(userCommand),
	 * exit(System.exit(0)), next(), undo(), changedir(userCommand) getdir()
	 * 
	 * @throws Error
	 *             Message
	 */

	public String executeCommand(String userCommand) throws IOException {
		tasks = taskVault.getList();
		lookForRecurrence();

		if (userCommand.trim().equals(""))
			return String.format(MESSAGE_INVALID_FORMAT, userCommand);

		String commandTypeString = getFirstWord(userCommand);
		COMMAND_TYPE commandType = determineCommandType(commandTypeString);

		switch (commandType) {
		case ADD:
			return add(userCommand);
		case DELETE:
			return delete(userCommand);
		case LIST:
			return list(userCommand);
		case EMPTY:
			return empty(userCommand);
		case SEARCH:
			return search(userCommand);
		case COMPLETE:
			return complete(userCommand);
		case EDIT:
			return edit(userCommand);
		case INVALID:
			return String.format(MESSAGE_INVALID_FORMAT, userCommand);
		case EXIT:
			System.exit(0);
		case UNDO:
			return undo();
		case CHANGEDIR:
			return changeDirectory(userCommand);
		case GETDIR:
			return getDirectory();
		case RECUR:
			return recur(userCommand);
		case ADDRECUR:
			return addrecur(userCommand);
		case HELP:
			return help(userCommand);
		case SHOW:
			return show(userCommand);
		default:
			// throw an error if the command is not recognized
			throw new Error(MESSAGE_ERROR);
		}
	}

	/**
	 * Shows the details of the task specified in user command
	 * 
	 * @param userCommand
	 * @return string containing details of task
	 */
	private String show(String userCommand) {
		userCommand = removeFirstWord(userCommand);
		if (!taskExists(userCommand)){
			return String.format(MESSAGE_NOT_FOUND, userCommand);
		}
		Task toShow = taskVault.getTask(userCommand);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
		String startDateTimeString = "";
		String endDateTimeString = "";
		String comment = "";
		if(toShow.getStartDate()!=null){
			startDateTimeString = getStartLDT(toShow).format(formatter);
		}
		if(toShow.getEndDate()!=null){
			endDateTimeString = getEndLDT(toShow).format(formatter);
		}
		if(toShow.getComment()!=null){
			comment = toShow.getComment();
		}
		
		String details = String.format(MESSAGE_SHOW_TASK,toShow.getTaskName(), startDateTimeString, endDateTimeString, comment);
		 
		if(toShow.isRecurring()){
			RecurringTask toShowRecur = (RecurringTask) toShow;
			if(toShowRecur.isDaily()){
				details += MESSAGE_SHOW_DAILY;
			}else if(toShowRecur.getRecurrenceDay()!=null){
				details += String.format(MESSAGE_SHOW_WEEKLY, toShowRecur.getRecurrenceDay());
			}else if(toShowRecur.getDayOfMonth()!=0){
				details += String.format(MESSAGE_SHOW_MONTHLY, toShowRecur.getDayOfMonth());
			}else if(toShowRecur.getMonthDay()!=null){
				DateTimeFormatter monthDayFormat = DateTimeFormatter.ofPattern("dd/MM");
				details += String.format(MESSAGE_SHOW_YEARLY, toShowRecur.getMonthDay().format(monthDayFormat));
			}			
		}

		return details;
	}

	private String help(String userCommand) {
		userCommand = removeFirstWord(userCommand).trim();
		
		if(userCommand.equals("")){
			return MESSAGE_HELP_COMMANDS;
		}
		if(userCommand.equalsIgnoreCase("add")){
			return MESSAGE_HELP_ADD;
		}
		if(userCommand.equalsIgnoreCase("delete")){
			return MESSAGE_HELP_DELETE;
		}
		if(userCommand.equalsIgnoreCase("edit")){
			return MESSAGE_HELP_EDIT;
		}
		if(userCommand.equalsIgnoreCase("list")){
			return MESSAGE_HELP_LIST;
		}
		if(userCommand.equalsIgnoreCase("search")){
			return MESSAGE_HELP_SEARCH;
		}
		if(userCommand.equalsIgnoreCase("complete")){
			return MESSAGE_HELP_COMPLETE;
		}
		if(userCommand.equalsIgnoreCase("empty")){
			return MESSAGE_HELP_EMPTY;
		}
		if(userCommand.equalsIgnoreCase("undo")){
			return MESSAGE_HELP_UNDO;
		}
		if(userCommand.equalsIgnoreCase("changedir")){
			return MESSAGE_HELP_CHANGEDIR;
		}
		if(userCommand.equalsIgnoreCase("getdir")){
			return MESSAGE_HELP_GETDIR;
		}
		if(userCommand.equalsIgnoreCase("recur")){
			return MESSAGE_HELP_RECUR;
		}
		if(userCommand.equalsIgnoreCase("addrecur")){
			return MESSAGE_HELP_ADDRECUR;
		}
		if(userCommand.equalsIgnoreCase("exit")){
			return MESSAGE_HELP_EXIT;
		}
		return MESSAGE_HELP_INVALID;
	}

	/**
	 * Looks for recurring tasks that have passed the current time and
	 * sets its next recurrence
	 */
	private void lookForRecurrence() {
		ObservableList<Task> list = taskVault.getList();
		for (int i = 0; i < list.size(); i++) {
			Task currTask = list.get(i);
			if (currTask.isRecurring()) {
				RecurringTask recurringTask = (RecurringTask) list.get(i);
				if (recurringTask.getEndDate() != null) {
					while (getEndLDT(recurringTask).isBefore(
							LocalDateTime.now())) {
						taskVault.remove(recurringTask.getTaskName());
						setNextRecurrence(recurringTask);
						recurringTask = (RecurringTask) taskVault
								.getTask(currTask.getTaskName());
					}
				} else {
					while (getStartLDT(recurringTask).isBefore(
							LocalDateTime.now())) {
						taskVault.remove(recurringTask.getTaskName());
						setNextRecurrence(recurringTask);
						recurringTask = (RecurringTask) taskVault
								.getTask(currTask.getTaskName());
					}
				}
			}
		}
		updateDisplay();
		saveVaults();
	}

	
	/**
	 * Sets the next recurrence of the given recurring task.
	 * 
	 * @param recurringTask
	 */
	private void setNextRecurrence(RecurringTask recurringTask) {
		if (recurringTask.getRecurrence() == 1) {

		} else if (recurringTask.getRecurrenceDay() != null) {
			setNextWeek(recurringTask);
		} else if (recurringTask.getDayOfMonth() != 0) {
			setNextMonth(recurringTask);
		} else if (recurringTask.isDaily()) {
			setNextDay(recurringTask);
		} else if (recurringTask.getMonthDay() != null) {
			setNextYear(recurringTask);
		}
	}

	/**
	 * Sets the next recurrence of the given recurring task to the next year
	 * 
	 * @param recurringTask
	 */
	private void setNextYear(RecurringTask recurringTask) {
		MonthDay monthDay = recurringTask.getMonthDay();
		LocalDate oldStartDate = recurringTask.getStartDate();
		LocalDate newStartDate;
		LocalDate oldEndDate = recurringTask.getEndDate();
		LocalDate newEndDate = null;
		RecurringTask newTask;
		int dayOfMonth = monthDay.getDayOfMonth();

		newStartDate = oldStartDate.plusYears(1).withMonth(
				monthDay.getMonthValue());

		while (true) {
			try {
				newStartDate = newStartDate.withDayOfMonth(dayOfMonth);
				break;
			} catch (DateTimeException e) {
				dayOfMonth--;
				continue;
			}
		}

		if (oldEndDate != null) {
			newEndDate = newStartDate.plusDays(Period.between(oldStartDate,
					oldEndDate).getDays());
		}

		newTask = new RecurringTask(recurringTask.getTaskName(),
				recurringTask.getComment(), newStartDate,
				recurringTask.getStartTime(), newEndDate,
				recurringTask.getEndTime(), recurringTask.getRecurrence() - 1,
				monthDay);

		copyId(recurringTask, newTask);

		if ((oldEndDate != null)
				&& hasOverlap(getStartLDT(newTask), getEndLDT(newTask))) {
			newTask.setRecurrence(recurringTask.getRecurrence());
			setNextRecurrence(newTask);
		} else {
			taskVault.storeTask(newTask);
		}
	}

	/**
	 * Sets the next recurrence of the given recurring task to the next day
	 * 
	 * @param recurringTask
	 */
	private void setNextDay(RecurringTask recurringTask) {
		LocalDate oldStartDate = recurringTask.getStartDate();
		LocalDate newStartDate;
		LocalDate oldEndDate = recurringTask.getEndDate();
		LocalDate newEndDate = null;
		RecurringTask newTask;

		newStartDate = oldStartDate.plusDays(1);

		if (oldEndDate != null) {
			newEndDate = newStartDate.plusDays(Period.between(oldStartDate,
					oldEndDate).getDays());
		}

		newTask = new RecurringTask(recurringTask.getTaskName(),
				recurringTask.getComment(), newStartDate,
				recurringTask.getStartTime(), newEndDate,
				recurringTask.getEndTime(), recurringTask.getRecurrence() - 1);

		copyId(recurringTask, newTask);

		if ((oldEndDate != null)
				&& hasOverlap(getStartLDT(newTask), getEndLDT(newTask))) {
			newTask.setRecurrence(recurringTask.getRecurrence());
			setNextRecurrence(newTask);
		} else {
			taskVault.storeTask(newTask);
		}
	}

	/**
	 * Sets the next recurrence of the given recurring task to the next month
	 * 
	 * @param recurringTask
	 */
	private void setNextMonth(RecurringTask recurringTask) {
		int dayOfMonth = recurringTask.getDayOfMonth();
		LocalDate oldStartDate = recurringTask.getStartDate();
		LocalDate newStartDate;
		LocalDate oldEndDate = recurringTask.getEndDate();
		LocalDate newEndDate = null;
		RecurringTask newTask;

		while (true) {
			try {
				newStartDate = oldStartDate.plusMonths(1).withDayOfMonth(
						dayOfMonth);
				break;
			} catch (DateTimeException e) {
				dayOfMonth--;
				continue;
			}
		}

		if (oldEndDate != null) {
			newEndDate = newStartDate.plusDays(Period.between(oldStartDate,
					oldEndDate).getDays());
		}

		newTask = new RecurringTask(recurringTask.getTaskName(),
				recurringTask.getComment(), newStartDate,
				recurringTask.getStartTime(), newEndDate,
				recurringTask.getEndTime(), recurringTask.getRecurrence() - 1,
				recurringTask.getDayOfMonth());

		copyId(recurringTask, newTask);


		if ((oldEndDate != null)
				&& hasOverlap(getStartLDT(newTask), getEndLDT(newTask))) {
			newTask.setRecurrence(recurringTask.getRecurrence());
			setNextRecurrence(newTask);
		} else {
			taskVault.storeTask(newTask);
		}

	}

	/**
	 * Sets the next recurrence of the given recurring task to the next week
	 * 
	 * @param recurringTask
	 */
	private void setNextWeek(RecurringTask recurringTask) {
		DayOfWeek recurrenceDay = recurringTask.getRecurrenceDay();
		LocalDate oldStartDate = recurringTask.getStartDate();
		LocalDate newStartDate;
		LocalDate oldEndDate = recurringTask.getEndDate();
		LocalDate newEndDate = null;
		RecurringTask newTask;

		if (oldStartDate.getDayOfWeek().getValue() < recurrenceDay.getValue()) {
			newStartDate = oldStartDate.plusWeeks(1).with(
					TemporalAdjusters.next(recurrenceDay));
		} else {
			newStartDate = oldStartDate.with(TemporalAdjusters
					.next(recurrenceDay));
		}


		if (oldEndDate != null) {
			newEndDate = newStartDate.plusDays(Period.between(oldStartDate,
					oldEndDate).getDays());
		}

		newTask = new RecurringTask(recurringTask.getTaskName(),
				recurringTask.getComment(), newStartDate,
				recurringTask.getStartTime(), newEndDate,
				recurringTask.getEndTime(), recurringTask.getRecurrence() - 1,
				recurrenceDay);

		copyId(recurringTask, newTask);

		if ((oldEndDate != null)
				&& hasOverlap(getStartLDT(newTask), getEndLDT(newTask))) {
			newTask.setRecurrence(recurringTask.getRecurrence());
			setNextRecurrence(newTask);
		} else {
			taskVault.storeTask(newTask);
		}

	}

	/**
	 * Checks if the given set of dates and times overlap with any existing task 
	 * currently in the tasks list.
	 * 
	 * @param localDateTime
	 * @param localDateTime2
	 * @return true if there is overlap, false if none
	 */
	private boolean hasOverlap(LocalDateTime localDateTime,
			LocalDateTime localDateTime2) {
		for (int i = 0; i < taskVault.getList().size(); i++) {
			Task currTask = taskVault.getList().get(i);
			if ((currTask.getStartDate() != null)
					&& (currTask.getEndDate() != null)) {
				if (localDateTime2.isAfter(getStartLDT(currTask))
						&& localDateTime2.isBefore(getEndLDT(currTask))) {
					return true;
				}
				if (localDateTime.isAfter(getStartLDT(currTask))
						&& localDateTime.isBefore(getEndLDT(currTask))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * creates a new task given the user's command and sets the task
	 * for recurrence
	 * 
	 * @param userCommand
	 * @return message containing success or failure of executing the command
	 */
	private String addrecur(String userCommand) {
		String response = add(userCommand);
		String[] addArguments = parseAdd(removeFirstWord(userCommand));
		if (!response.contains("successfully added")) {
			return response;
		}

		String taskName = addArguments[0];
		String recurDetails = addArguments[1];

		taskVault.getTask(taskName).setComment(null);

		if (addArguments[2] == null) {
			taskVault.remove(removeFirstWord(userCommand));
			updateDisplay();
			saveVaults();
			return MESSAGE_RECUR_FLOATING;
		}

		if (recurDetails == null) {
			taskVault.remove(taskName);
			updateDisplay();
			saveVaults();
			return MESSAGE_RECURSION_DETAILS;
		}

		String recurResponse = recur("recur " + taskName + " " + recurDetails);
		if (!recurResponse.contains("will recur")) {
			taskVault.remove(taskName);
			lookForRecurrence();
			updateDisplay();
			saveVaults();
			return recurResponse;
		}

		lookForRecurrence();
		updateDisplay();
		saveVaults();
		return recurResponse;
	}

	/**
	 * recurs the given, existing task in the user command
	 * 
	 * @param userCommand
	 * @return message containing success or failure of executing the command
	 */
	private String recur(String userCommand) {
		String trimmedCommand = removeFirstWord(userCommand).trim();

		String taskName = lookForTaskName(trimmedCommand);

		if (taskName.equals("")) {
			return MESSAGE_NOT_FOUND;
		}
		
		if(taskVault.getTask(taskName).getStartDate()==null){
			return MESSAGE_RECUR_FLOATING;
		}

		trimmedCommand = trimmedCommand.replaceFirst(taskName, "");
		if (getFirstWord(trimmedCommand).equals("monthly")) {
			String recurDetails = removeFirstWord(trimmedCommand);
			return recurMonth(taskName, recurDetails);
		}
		if (getFirstWord(trimmedCommand).equals("weekly")) {
			String recurDetails = removeFirstWord(trimmedCommand);
			return recurWeek(taskName, recurDetails);
		}
		if (getFirstWord(trimmedCommand).equals("daily")) {
			String recurDetails = removeFirstWord(trimmedCommand);
			return recurDaily(taskName, recurDetails);
		}
		if (getFirstWord(trimmedCommand).equals("yearly")) {
			String recurDetails = removeFirstWord(trimmedCommand);
			return recurYearly(taskName, recurDetails);
		}

		return MESSAGE_SPECIFY_RECURRENCE;
	}

	/**
	 * recurs the task represented by taskName yearly according to details
	 * in recurDetails
	 * 
	 * @param taskName
	 * @param recurDetails
	 * @return message containing success or failure of executing the command
	 */
	private String recurYearly(String taskName, String recurDetails) {
		int recurrence;
		MonthDay monthDay;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEMONTH_PATTERN);

		String[] recurDetailsArray = recurDetails.split("\\s+");
		if (recurDetailsArray.length == 2) {
			try {
				monthDay = MonthDay.parse(recurDetailsArray[0], formatter);
			} catch (NumberFormatException e) {
				return MESSAGE_INVALID_DAYMONTH;
			}

			try {
				recurrence = Integer.parseInt(recurDetailsArray[1]);
			} catch (NumberFormatException e) {
				return MESSAGE_INVALID_RECURRENCES;
			}

			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence,
					monthDay);
			copyId(toRecur, newRecurringTask);

			taskVault.storeTask(newRecurringTask);
			saveVaults();
			return String.format(MESSAGE_WILL_RECUR, taskName, monthDay, YEARLY, recurrence);

		} else if (recurDetailsArray.length == 1
				&& !recurDetailsArray[0].equals("")) {
			try {
				recurrence = Integer.parseInt(recurDetailsArray[0]);
				if (recurrence < 1) {
					return MESSAGE_INVALID_RECURRENCES;
				}
			} catch (NumberFormatException e) {
				return MESSAGE_INVALID_RECURRENCES;
			}

			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			monthDay = MonthDay.from(toRecur.getStartDate());
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence,
					monthDay);
			copyId(toRecur, newRecurringTask);

			taskVault.storeTask(newRecurringTask);
			saveVaults();
			return String.format(MESSAGE_WILL_RECUR, taskName, monthDay, YEARLY, recurrence);

		} else if (recurDetails.equals("")) {
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			monthDay = MonthDay.from(toRecur.getStartDate());
			recurrence = Integer.MAX_VALUE;
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence,
					monthDay);
			copyId(toRecur, newRecurringTask);

			taskVault.storeTask(newRecurringTask);
			saveVaults();
			DateTimeFormatter monthDayFormat = DateTimeFormatter.ofPattern("dd/MM");
			return String.format("%s will recur on %s %s", taskName, monthDay.format(monthDayFormat), YEARLY);
		}

		return null;
	}

	/**
	 * recurs the task represented by taskName daily according to details
	 * in recurDetails
	 * 
	 * @param taskName
	 * @param recurDetails
	 * @return message containing success or failure of executing the command
	 */
	private String recurDaily(String taskName, String recurDetails) {
		int recurrence;

		String[] recurDetailsArray = recurDetails.split("\\s+");

		if (recurDetailsArray.length == 1 && !recurDetailsArray[0].equals("")) {
			try {
				recurrence = Integer.parseInt(recurDetailsArray[0]);
				if (recurrence < 1) {
					return MESSAGE_INVALID_RECURRENCES;
				}
			} catch (NumberFormatException e) {
				return MESSAGE_INVALID_RECURRENCES;
			}

			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence);
			copyId(toRecur, newRecurringTask);

			taskVault.storeTask(newRecurringTask);
			saveVaults();
			return String.format(MESSAGE_RECUR_DAILY, taskName, DAILY, recurrence);

		} else if (recurDetails.equals("")) {
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			recurrence = Integer.MAX_VALUE;
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence);
			copyId(toRecur, newRecurringTask);

			taskVault.storeTask(newRecurringTask);
			saveVaults();
			return String.format(MESSAGE_RECUR_DAILY_FOREVER, taskName, DAILY);
		}

		return MESSAGE_INVALID_RECURFORMAT;
	}

	/**
	 * recurs the task represented by taskName weekly according to details
	 * in recurDetails
	 * 
	 * @param taskName
	 * @param recurDetails
	 * @return message containing success or failure of executing the command
	 */
	private String recurWeek(String taskName, String recurDetails) {
		DayOfWeek dayOfWeek;
		int recurrence;
		String[] recurDetailsArray = recurDetails.split("\\s+");
		if (recurDetailsArray.length == 2) {
			dayOfWeek = getDay(recurDetailsArray[0]);

			try {
				recurrence = Integer.parseInt(recurDetailsArray[1]);
				if (recurrence < 1) {
					return MESSAGE_INVALID_RECURRENCES;
				}
			} catch (NumberFormatException e) {
				return MESSAGE_INVALID_RECURRENCES;
			}

			if (dayOfWeek == null) {
				return MESSAGE_INVALID_DAYOFWEEK;
			}

			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfWeek);
			copyId(toRecur, newRecurringTask);

			taskVault.storeTask(newRecurringTask);
			saveVaults();
			return String.format(MESSAGE_WILL_RECUR, taskName, dayOfWeek, WEEKLY, recurrence);

		} else if (recurDetailsArray.length == 1
				&& !recurDetailsArray[0].equals("")) {
			try {
				recurrence = Integer.parseInt(recurDetailsArray[0]);
				if (recurrence < 1) {
					return MESSAGE_INVALID_RECURRENCES;
				}
			} catch (NumberFormatException e) {
				return MESSAGE_INVALID_RECURRENCES;
			}
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			dayOfWeek = toRecur.getStartDate().getDayOfWeek();
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfWeek);
			copyId(toRecur, newRecurringTask);

			taskVault.storeTask(newRecurringTask);
			saveVaults();
			return String.format(MESSAGE_WILL_RECUR, taskName, dayOfWeek, WEEKLY, recurrence);

		} else if (recurDetails.equals("")) {
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			dayOfWeek = toRecur.getStartDate().getDayOfWeek();
			recurrence = Integer.MAX_VALUE;
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfWeek);
			copyId(toRecur, newRecurringTask);
			saveVaults();
			taskVault.storeTask(newRecurringTask);
			return String.format(MESSAGE_WILL_RECUR_FOREVER, taskName, dayOfWeek, WEEKLY);
		}

		return MESSAGE_INVALID_RECURFORMAT;
	}

	/**
	 * sets the id of the old task to the new recurring task then adds the id
	 * to the id bank
	 * 
	 * @param toRecur
	 * @param newRecurringTask
	 */
	private void copyId(Task toRecur, RecurringTask newRecurringTask) {
		newRecurringTask.setId(toRecur.getId());
		IdGenerator idGenerator = new IdGenerator();
		idGenerator.addId(
				Integer.parseInt(toRecur.getId().substring(1), 36),
				toRecur.getTaskName());
	}

	
	/**
	 * returns the DayOfWeek object represented by the given string
	 * 
	 * @param dayString
	 * @return DayOfWeek, or null if String does not represent a valid day
	 */
	private DayOfWeek getDay(String dayString) {
		if (dayString.toLowerCase().contains("mon")) {
			return DayOfWeek.MONDAY;
		}
		if (dayString.toLowerCase().contains("tue")) {
			return DayOfWeek.TUESDAY;
		}
		if (dayString.toLowerCase().contains("wed")) {
			return DayOfWeek.WEDNESDAY;
		}
		if (dayString.toLowerCase().contains("thu")) {
			return DayOfWeek.THURSDAY;
		}
		if (dayString.toLowerCase().contains("fri")) {
			return DayOfWeek.FRIDAY;
		}
		if (dayString.toLowerCase().contains("sat")) {
			return DayOfWeek.SATURDAY;
		}
		if (dayString.toLowerCase().contains("sun")) {
			return DayOfWeek.SUNDAY;
		}
		return null;
	}

	/**
	 * recurs the task represented by taskName monthly according to details
	 * in recurDetails
	 * 
	 * @param taskName
	 * @param recurDetails
	 * @return message containing success or failure of executing the command
	 */
	private String recurMonth(String taskName, String recurDetails) {
		int dayOfMonth;
		int recurrence;
		String[] recurDetailsArray = recurDetails.split("\\s+");
		if (recurDetailsArray.length == 2) {
			try {
				dayOfMonth = Integer.parseInt(recurDetailsArray[0]);
			} catch (NumberFormatException e) {
				return "insert valid number for day of the month";
			}

			try {
				recurrence = Integer.parseInt(recurDetailsArray[1]);
				if (recurrence < 1) {
					return "Insert valid number for number of recurrence";
				}
			} catch (NumberFormatException e) {
				return MESSAGE_INVALID_RECURRENCES;
			}

			if (dayOfMonth < 1 || dayOfMonth > 31) {
				return "day of month must be between 1 and 31";
			}

			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfMonth);
			copyId(toRecur, newRecurringTask);

			taskVault.storeTask(newRecurringTask);

			return String.format(MESSAGE_WILL_RECUR, taskName, dayOfMonth, MONTHLY, recurrence);

		} else if (recurDetailsArray.length == 1
				&& !recurDetailsArray[0].equals("")) {
			try {
				recurrence = Integer.parseInt(recurDetailsArray[0]);
				if (recurrence < 1) {
					return "Insert valid number for number of recurrence";
				}
			} catch (NumberFormatException e) {
				return MESSAGE_INVALID_RECURRENCES;
			}

			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			dayOfMonth = toRecur.getStartDate().getDayOfMonth();
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfMonth);
			copyId(toRecur, newRecurringTask);

			taskVault.storeTask(newRecurringTask);

			return String.format(MESSAGE_WILL_RECUR, taskName, dayOfMonth, MONTHLY, recurrence);

		} else if (recurDetails.equals("")) {
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			dayOfMonth = toRecur.getStartDate().getDayOfMonth();
			recurrence = Integer.MAX_VALUE;
			RecurringTask newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfMonth);
			copyId(toRecur, newRecurringTask);

			taskVault.storeTask(newRecurringTask);

			return String.format(MESSAGE_WILL_RECUR_FOREVER, taskName, dayOfMonth, MONTHLY);
		}

		return MESSAGE_INVALID_RECURFORMAT;
	}

	/**
	 * looks for an existing task name or id in the given string and returns it
	 * 
	 * @param trimmedCommand
	 * @return taskName or id of task contained in the string
	 */
	private String lookForTaskName(String trimmedCommand) {
		String found = "";
		ObservableList<Task> list = taskVault.getList();

		if (trimmedCommand.startsWith("@")) {
			for (int i = 0; i < list.size(); i++) {
				Task currTask = list.get(i);
				if (trimmedCommand.contains(currTask.getId())
						&& (currTask.getId().length() > found.length())) {
					found = currTask.getTaskName();
				}
			}
			return found;
		} else {

			for (int i = 0; i < list.size(); i++) {
				Task currTask = list.get(i);
				if (trimmedCommand.contains(currTask.getTaskName())
						&& (currTask.getTaskName().length() > found.length())) {
					found = currTask.getTaskName();
				}
			}
			return found;
		}
	}

	/**
	 * Obtain directory
	 */

	private String getDirectory() {
		return MESSAGE_WORKING_DIRECTORY + vaultPath;
	}

	/**
	 * method that returns the new directory when change directory command is
	 * entered by user if directory fails to exist, returns MESSAGE_NONEXIST
	 * 
	 * @throws IOException
	 */

	private String changeDirectory(String userCommand) throws IOException {
		String newPathString = removeFirstWord(userCommand).trim();

		File newPath = new File(newPathString);
		newPath = newPath.getAbsoluteFile();
		if (newPath.isDirectory()) {
			Task oldDirTask = new Task(vaultPath);
			historyVault.storeTask(oldDirTask);

			commandStack.push(UNDOABLE.CHANGEDIR);
			taskVault.deleteFile();
			trashVault.deleteFile();
			completedTaskVault.deleteFile();
			historyVault.deleteFile();
			taskVault.setFilePath(Paths.get(newPathString).toAbsolutePath());
			trashVault.setFilePath(Paths.get(newPathString).toAbsolutePath());
			completedTaskVault.setFilePath(Paths.get(newPathString)
					.toAbsolutePath());
			historyVault.setFilePath(Paths.get(newPathString).toAbsolutePath());
			clearConfigFile();
			writeToConfig(Paths.get(newPathString).toAbsolutePath().toString());
			vaultPath = Paths.get(newPathString).toAbsolutePath().toString();
			saveVaults();
			updateDisplay();
			return String.format(MESSAGE_FILES_MOVED, newPathString);
		}

		return MESSAGE_NONEXIST;

	}

	/**
	 * creates an instance of the different Vault classes
	 * 
	 * @throws IOException
	 */
	private void initializeVaults() throws IOException {
		String vaultPath = System.getProperty(USER_DIR);
		taskVault = new TaskVault(vaultPath);
		trashVault = new TrashVault(vaultPath);
		historyVault = new HistoryVault(vaultPath);
		completedTaskVault = new CompletedTaskVault(vaultPath);
	}

	/**
	 * method that performs undo command if task is empty, returns
	 * MESSAGE_NO_UNDO undoAdd, undoComplete, undoDelete, undoEdit,
	 * undoChangedir
	 * 
	 * @throws IOException
	 */

	private String undo(){
		if (commandStack.isEmpty()) {
			return MESSAGE_NO_UNDO;
		}
		switch (commandStack.peek()) {
		case ADD:
			commandStack.pop();
			return undoAdd();
		case COMPLETE:
			commandStack.pop();
			return undoComplete();
		case DELETE:
			commandStack.pop();
			return undoDelete();
		case EDIT:
			commandStack.pop();
			return undoEdit();
		case CHANGEDIR:
			commandStack.pop();
			try {
				return undoChangeDir();
			} catch (IOException e) {
			}
		default:
			break;

		}
		return null;
	}

	/**
	 * performs undoChangedir command If directory specified does not exit,
	 * returns MESSAGE_NONEXIST
	 * 
	 * @throws IOException
	 */
	private String undoChangeDir() throws IOException {
		String newPathString = getLastHistoryName();
		historyVault.pop(newPathString);

		File newPath = new File(newPathString);
		newPath = newPath.getAbsoluteFile();
		if (newPath.isDirectory()) {
			taskVault.deleteFile();
			trashVault.deleteFile();
			completedTaskVault.deleteFile();
			historyVault.deleteFile();
			taskVault.setFilePath(Paths.get(newPathString).toAbsolutePath());
			trashVault.setFilePath(Paths.get(newPathString).toAbsolutePath());
			completedTaskVault.setFilePath(Paths.get(newPathString)
					.toAbsolutePath());
			historyVault.setFilePath(Paths.get(newPathString).toAbsolutePath());
			clearConfigFile();
			writeToConfig(Paths.get(newPathString).toAbsolutePath().toString());
			vaultPath = Paths.get(newPathString).toAbsolutePath().toString();
			saveVaults();
			updateDisplay();
			return String.format(MESSAGE_FILES_MOVED, newPathString);
		}

		return MESSAGE_NONEXIST;
	}

	/**
	 * @return
	 */
	private String getLastHistoryName() {
		return historyVault.getList().get(historyVault.getList().size() - 1)
				.getTaskName();
	}

	/**
	 * performs undoEdit command to revert back to original
	 */
	private String undoEdit() {
		Task newTask = historyVault.pop(getLastHistoryName());
		Task oldTask = historyVault.pop(getLastHistoryName());
		
		IdGenerator idGenerator = new IdGenerator();
		if(!idGenerator.isExistingId(newTask.getId()) || newTask.getTaskName().equals(idGenerator.getTaskName(newTask.getId()))){
			return undo();
		}
		
		taskVault.remove(newTask.getTaskName());
		taskVault.storeTask(oldTask);
		updateDisplay();
		saveVaults();
		return String.format(MESSAGE_UNDO_EDIT, oldTask.getTaskName());
	}

	/**
	 * performs undoDelete command
	 */

	private String undoDelete() {
		Task historyTask = historyVault.pop(getLastHistoryName());
		IdGenerator idGenerator = new IdGenerator();
		if(!idGenerator.isExistingId(historyTask.getId()) || historyTask.getTaskName().equals(idGenerator.getTaskName(historyTask.getId()))){
			return undo();
		}
		taskVault.storeTask(historyTask);
		trashVault.remove(historyTask.getTaskName());
		updateDisplay();
		saveVaults();
		return String.format(MESSAGE_UNDO_DELETE, historyTask.getTaskName());
	}

	/**
	 * performs undoComplete command
	 */
	private String undoComplete() {
		Task historyTask = historyVault.pop(getLastHistoryName());
		IdGenerator idGenerator = new IdGenerator();
		if(!idGenerator.isExistingId(historyTask.getId())){
			return undo();
		}
		taskVault.storeTask(historyTask);
		completedTaskVault.remove(historyTask.getTaskName());
		updateDisplay();
		saveVaults();
		return String.format(MESSAGE_UNDO_COMPLETE, historyTask.getTaskName());
	}

	/**
	 * performs undoAdd command
	 */

	private String undoAdd() {
		Task historyTask = historyVault.pop(getLastHistoryName());
		IdGenerator idGenerator = new IdGenerator();
		if(!idGenerator.isExistingId(historyTask.getId())){
			return undo();
		}
		taskVault.remove(historyTask.getTaskName());
		updateDisplay();
		saveVaults();
		return String.format(MESSAGE_UNDO_ADD, historyTask.getTaskName());
	}

	/**
	 *
	 */
	private void saveVaults() {
		taskVault.save();
		trashVault.save();
		completedTaskVault.save();
		historyVault.save();
	}

	/**
	 * method that performs the respective edit instructions if invalid String,
	 * MESSAGE_INVALID_EDIT will be returned e.g. tasknam, startdate, starttime,
	 * enddate, endtime, addcomment
	 * 
	 * @param userCommand
	 * @throws IOException
	 */

	private String edit(String userCommand) throws IOException {
		userCommand = removeFirstWord(userCommand);

		if (userCommand.contains("taskname")) {
			return editTaskName(userCommand);
		}
		if (userCommand.contains("startdate")) {
			return editStartDate(userCommand);
		}
		if (userCommand.contains("starttime")) {
			return editStartTime(userCommand);
		}
		if (userCommand.contains("enddate")) {
			return editEndDate(userCommand);
		}
		if (userCommand.contains("endtime")) {
			return editEndTime(userCommand);
		}

		if (userCommand.contains("addcomment")) {
			return addComment(userCommand);
		}
		return MESSAGE_INVALID_EDIT;

	}

	/**
	 * performs commenting of a task adds the new comment to the existing task
	 * and saves the changes updates display of the task with comment added
	 * 
	 * @param userCommand
	 */

	private String addComment(String userCommand) {
		String[] editArguments = parseEdit(userCommand, "addcomment");
		String taskName = editArguments[0].trim();
		String newComment = editArguments[1].trim();

		
		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);

			taskVault.remove(taskName);
			taskVault.createTask(oldTask.getTaskName(), newComment,
					oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), oldTask.getEndTime());
			
			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(taskName));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_COMMENT_ADDED;
		} else {
			return String.format(MESSAGE_NOT_FOUND, taskName);
		}
	}

	/**
	 * method that edits endtime of tasks For floating tasks where endtime is
	 * null, execution will return message For deadline tasks, where endtime is
	 * null but starttime is not null, execution will return message Method will
	 * also not allow if endtime entered is before the stipulated start time of
	 * a specific task
	 * 
	 * @param userCommand
	 */

	private String editEndTime(String userCommand) {
		String[] editArguments = parseEdit(userCommand, "endtime");
		if(editArguments.length==1){
			return MESSAGE_TIME_MISSING;
		}
		String[] endTimes = extractTimes(editArguments[1]);
		String taskName = editArguments[0].trim();
		LocalTime newEndTime;
		try {
			newEndTime = toLocalTime(endTimes[0]);
		} catch (DateTimeParseException e) {
			return String.format(MESSAGE_INVALID_TIME, endTimes[0]);
		}
		
		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);

			if (oldTask.getEndTime() == null) {
				if (oldTask.getStartTime() == null) {
					return MESSAGE_FLOAT_ETIME;
				} else {
					return MESSAGE_DEADLINE_ETIME;
				}
			}
			if ((oldTask.getEndDate() != null)
					&&!isInOrder(oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), newEndTime)) {
				return MESSAGE_NOT_CHRON;
			}
			taskVault.remove(taskName);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), newEndTime);

			if(oldTask.isRecurring()){
				replaceWithRecurringTask(taskName, oldTask);
			}
			
			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(taskName));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
		} else {
			return String.format(MESSAGE_NOT_FOUND, taskName);
		}

	}

	/**
	 * Method does not allow floating tasks to have starttime edited because it
	 * was formerly null
	 * 
	 * @param userCommand
	 */
	private String editStartTime(String userCommand) {
		String[] editArguments = parseEdit(userCommand, "starttime");
		if(editArguments.length==1){
			return MESSAGE_TIME_MISSING;
		}
		String[] startTimes = extractTimes(editArguments[1]);
		String taskName = editArguments[0].trim();
		LocalTime newStartTime;
		try {
			newStartTime = toLocalTime(startTimes[0]);
		} catch (DateTimeParseException e) {
			return String.format(MESSAGE_INVALID_TIME, startTimes[0]);
		}

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);

			if (oldTask.getStartTime() == null) {
				return MESSAGE_FLOAT_STIME;
			}

			if ((oldTask.getEndDate() != null)
					&&!isInOrder(oldTask.getStartDate(), newStartTime,
					oldTask.getEndDate(), oldTask.getEndTime())) {
				return MESSAGE_NOT_CHRON;
			}
			
			taskVault.remove(taskName);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					oldTask.getStartDate(), newStartTime, oldTask.getEndDate(),
					oldTask.getEndTime());
			
			if(oldTask.isRecurring()){
				replaceWithRecurringTask(taskName, oldTask);
			}
			
			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(taskName));;
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
		} else {
			return String.format(MESSAGE_NOT_FOUND, taskName);
		}
	}

	/**
	 * Method does not allow edit of date for floating tasks Method does not
	 * allow edit of date for deadline if endDate is null Method does not allow
	 * endDate to be before Startdate of task
	 * 
	 * @param userCommand
	 */
	private String editEndDate(String userCommand) {
		String[] editArguments = parseEdit(userCommand, "enddate");
		if(editArguments.length==1){
			return MESSAGE_DATE_MISSING;
		}
		String[] endDates = extractDates(editArguments[1]);
		String taskName = editArguments[0].trim();
		LocalDate newEndDate;
		try {
			newEndDate = toLocalDate(endDates[0]);
		} catch (DateTimeParseException e) {
			return String.format(MESSAGE_INVALID_DATE, endDates[0]);
		}

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);
			if (oldTask.getEndDate() == null) {
				if (oldTask.getStartTime() == null) {
					return MESSAGE_FLOAT_EDATE;
				} else {
					return MESSAGE_DEADLINE_EDATE;
				}
			}
			if ((oldTask.getEndDate() != null)
					&& !isInOrder(oldTask.getStartDate(), oldTask.getStartTime(),
					newEndDate, oldTask.getEndTime())) {
				return MESSAGE_NOT_CHRON;
			}
			taskVault.remove(taskName);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					oldTask.getStartDate(), oldTask.getStartTime(), newEndDate,
					oldTask.getEndTime());

			if(oldTask.isRecurring()){
				replaceWithRecurringTask(taskName, oldTask);
			}
			
			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(taskName));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
		} else {
			return String.format(MESSAGE_NOT_FOUND, taskName);
		}
	}

	/**
	 * Method does not allow edit of start date of floating task
	 * 
	 * @param userCommand
	 */
	private String editStartDate(String userCommand) {
		String[] editArguments = parseEdit(userCommand, "startdate");
		if(editArguments.length==1){
			return MESSAGE_DATE_MISSING;
		}
		String[] startDates = extractDates(editArguments[1]);
		String taskName = editArguments[0].trim();
		LocalDate newStartDate;
		try {
			newStartDate = toLocalDate(startDates[0]);
		} catch (DateTimeParseException e) {
			return String.format(MESSAGE_INVALID_DATE, startDates[0]);
		}

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);
			if (oldTask.getStartDate() == null) {
				return MESSAGE_FLOAT_SDATE;
			}

			if ((oldTask.getEndDate() != null)
					&& !isInOrder(newStartDate, oldTask.getStartTime(),
							oldTask.getEndDate(), oldTask.getEndTime())) {
				return MESSAGE_NOT_CHRON;
			}
			taskVault.remove(taskName);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					newStartDate, oldTask.getStartTime(), oldTask.getEndDate(),
					oldTask.getEndTime());
			
			if(oldTask.isRecurring()){
				replaceWithRecurringTask(taskName, oldTask);
			}

			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(taskName));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
		} else {
			return String.format(MESSAGE_NOT_FOUND, taskName);
		}
	}

	/**
	 * replaces the task in taskVault with a recurringTask with recurring details from
	 * its previous task representative
	 * 
	 * @param taskName
	 * @param oldTask
	 */
	private void replaceWithRecurringTask(String taskName, Task oldTask) {
		RecurringTask oldRecurringTask = (RecurringTask) oldTask;
		RecurringTask newRecurringTask = null;
		if(oldRecurringTask.isDaily()){
			newRecurringTask = new RecurringTask(taskVault.getTask(taskName), oldRecurringTask.getRecurrence());
			newRecurringTask.setId(taskVault.getTask(oldRecurringTask.getTaskName()).getId());
		}else if(oldRecurringTask.getRecurrenceDay()!=null){
			newRecurringTask = new RecurringTask(taskVault.getTask(taskName), oldRecurringTask.getRecurrence(), oldRecurringTask.getRecurrenceDay());
			newRecurringTask.setId(taskVault.getTask(oldRecurringTask.getTaskName()).getId());
		}else if(oldRecurringTask.getDayOfMonth()!=0){
			newRecurringTask = new RecurringTask(taskVault.getTask(taskName), oldRecurringTask.getRecurrence(), oldRecurringTask.getDayOfMonth());
			newRecurringTask.setId(taskVault.getTask(oldRecurringTask.getTaskName()).getId());
		}else if(oldRecurringTask.getMonthDay()!=null){
			newRecurringTask = new RecurringTask(taskVault.getTask(taskName), oldRecurringTask.getRecurrence(), oldRecurringTask.getMonthDay());
			newRecurringTask.setId(taskVault.getTask(oldRecurringTask.getTaskName()).getId());
		}
		taskVault.remove(oldRecurringTask.getTaskName());
		IdGenerator idGenerator = new IdGenerator();
		idGenerator.addId(
				Integer.parseInt(newRecurringTask.getId().substring(1), 36),
				newRecurringTask.getTaskName());
		taskVault.storeTask(newRecurringTask);
	}

	/**
	 * Method prevents tasks with same name to be stored
	 * 
	 * @param userCommand
	 */
	private String editTaskName(String userCommand) {
		String[] editArguments = parseEdit(userCommand, "taskname");
		String taskName = editArguments[0].trim();
		String newTaskName = editArguments[1].trim();

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);

			for (int i = 0; i < tasks.size(); i++) {
				if (tasks.get(i).getTaskName().equals(newTaskName)) {
					return String.format(MESSAGE_ALREADY_EXISTS, newTaskName);
				}
			}

			taskVault.remove(taskName);
			taskVault.createTask(newTaskName, oldTask.getComment(),
					oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), oldTask.getEndTime());
			
			if(oldTask.isRecurring()){
				replaceWithRecurringNewName(newTaskName, oldTask);
			}

			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(newTaskName));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
		} else {
			return String.format(MESSAGE_NOT_FOUND, taskName);
		}
	}

	/**
	 * replaces the task in taskVault with a recurringTask with recurring details from
	 * its previous task representative. For tasks which names have changed.
	 * 
	 * @param taskName
	 * @param oldTask
	 */
	private void replaceWithRecurringNewName(String newTaskName, Task oldTask) {
		RecurringTask oldRecurringTask = (RecurringTask) oldTask;
		RecurringTask newRecurringTask = null;
		
		if(oldRecurringTask.isDaily()){
			newRecurringTask = new RecurringTask(taskVault.getTask(newTaskName), oldRecurringTask.getRecurrence());
			newRecurringTask.setId(taskVault.getTask(newTaskName).getId());
		}else if(oldRecurringTask.getRecurrenceDay()!=null){
			newRecurringTask = new RecurringTask(taskVault.getTask(newTaskName), oldRecurringTask.getRecurrence(), oldRecurringTask.getRecurrenceDay());
			newRecurringTask.setId(taskVault.getTask(newTaskName).getId());
		}else if(oldRecurringTask.getDayOfMonth()!=0){
			newRecurringTask = new RecurringTask(taskVault.getTask(newTaskName), oldRecurringTask.getRecurrence(), oldRecurringTask.getDayOfMonth());
			newRecurringTask.setId(taskVault.getTask(newTaskName).getId());
		}else if(oldRecurringTask.getMonthDay()!=null){
			newRecurringTask = new RecurringTask(taskVault.getTask(newTaskName), oldRecurringTask.getRecurrence(), oldRecurringTask.getMonthDay());
			newRecurringTask.setId(taskVault.getTask(newTaskName).getId());
		}
		taskVault.remove(newTaskName);
		IdGenerator idGenerator = new IdGenerator();
		idGenerator.addId(
				Integer.parseInt(newRecurringTask.getId().substring(1), 36),
				newRecurringTask.getTaskName());
		taskVault.storeTask(newRecurringTask);
	}

	/**
	 * @param taskName
	 */

	private boolean taskExists(String taskName) {
		return taskVault.getTask(taskName) != null;
	}

	/**
	 * @param userCommand
	 *            , string
	 */

	private String[] parseEdit(String userCommand, String string) {
		return userCommand.trim().split(string);
	}

	/**
	 * @param toCopy
	 */

	private ObservableList<Task> copyList(ObservableList<Task> toCopy) {
		ObservableList<Task> newList = FXCollections.observableArrayList();
		for (int i = 0; i < toCopy.size(); i++) {
			newList.add(i, toCopy.get(i));
		}
		return newList;
	}

	/**
	 * param userCommand
	 */

	private String complete(String userCommand) {
		userCommand = removeFirstWord(userCommand);

		if (taskVault.getTask(userCommand) != null) {
			Task completedTask = taskVault.getTask(userCommand);
			commandStack.push(UNDOABLE.COMPLETE);
			historyVault.storeTask(taskVault.getTask(userCommand));
			taskVault.completeTask(userCommand, completedTaskVault);
			if (completedTask.isRecurring()) {
				setNextRecurrence((RecurringTask) completedTask);
			}
			updateDisplay();
			saveVaults();

			return String.format(MESSAGE_COMPLETE_SUCCESS, userCommand);
		} else {
			return String.format(MESSAGE_COMPLETE_FAIL, userCommand);
		}
	}

	/**
	 * @param userCommand
	 */

	private String search(String userCommand) {
		userCommand = removeFirstWord(userCommand);

		String[] searchWords = parseSearch(userCommand);
		toDisplay.clear();
		int found = 0;

		for (int taskIndex = 0; taskIndex < tasks.size(); taskIndex++) {
			if (isCompleteMatch(userCommand, taskIndex)) {
				toDisplay.add(tasks.get(taskIndex));
				found++;
			}
		}
		for (int wordIndex = 0; wordIndex < searchWords.length; wordIndex++) {
			String currentWord = searchWords[wordIndex];
			for (int taskIndex = 0; taskIndex < tasks.size(); taskIndex++) {
				if (isPartialMatch(currentWord, taskIndex)
						&& isNotYetFound(taskIndex)) {
					toDisplay.add(tasks.get(taskIndex));
					found++;
				}
			}
		}
		return found + MESSAGE_TASKS_FOUND;
	}

	/**
	 * @param taskIndex
	 */
	private boolean isNotYetFound(int taskIndex) {
		return !(toDisplay.contains(tasks.get(taskIndex)));
	}

	/**
	 * @param currentWord
	 *            , taskIndex
	 */
	private boolean isPartialMatch(String currentWord, int taskIndex) {
		return tasks.get(taskIndex).getTaskName().contains(currentWord);
	}

	/**
	 * @param userCommand
	 *            , taskIndex
	 */
	private boolean isCompleteMatch(String userCommand, int taskIndex) {
		return tasks.get(taskIndex).getTaskName().equals(userCommand);
	}

	/**
	 * @throws IOException
	 * 
	 */
	private String empty(String userCommand) throws IOException {
		userCommand = removeFirstWord(userCommand).trim();
		if (userCommand.equalsIgnoreCase("trash")) {
			if (trashVault.clear()) {
				saveVaults();
				return MESSAGE_TRASH_CLEARED;
			} else {
				return MESSAGE_TRASH_UNCLEARED;
			}
		} else if (userCommand.equalsIgnoreCase("completed")) {
			completedTaskVault.clear();
			completedTaskVault = new CompletedTaskVault(vaultPath);
			return "Completed tasks cleared";
		} else {
			return "Specify to empty trash or completed tasks";
		}
	}

	/**
	 * @param userCommand
	 */
	private String list(String userCommand) {
		userCommand = removeFirstWord(userCommand);

		String[] listArguments = parseList(userCommand);
		toDisplay.clear();

		if (listArguments[0].trim().equals("")) {
			updateDisplay();
			return MESSAGE_ALL_DISPLAYED;
		}

		if (listArguments[0].equalsIgnoreCase("today")) {
			return listToday() + MESSAGE_NUM_DISPLAYED;
		}

		if (listArguments[0].equalsIgnoreCase("week")) {
			return listWeek() + MESSAGE_NUM_DISPLAYED;
		}
		if (listArguments[0].equalsIgnoreCase("trash")
				|| listArguments[0].equals("deleted")) {
			toDisplay = trashVault.getList();
			return MESSAGE_TRASH_DISPLAYED;
		}
		if (listArguments[0].equalsIgnoreCase("completed")) {
			toDisplay = completedTaskVault.getList();
			return MESSAGE_COMPLETED_DISPLAYED;
		}
		if (listArguments[0].equalsIgnoreCase("history")) {
			toDisplay = historyVault.getList();
			return MESSAGE_HISTORY_DISPLAYED;
		}

		try {
			if (isValidListDate(listArguments)) {
				return listDate(listArguments[0]) + MESSAGE_NUM_DISPLAYED;
			} else if (isValidListDateTime(listArguments)) {
				return listDateTime(listArguments) + MESSAGE_NUM_DISPLAYED;
			} else if (isValidDayPeriod(listArguments)) {
				return listDayPeriod(listArguments) + MESSAGE_NUM_DISPLAYED;
			} else if (isValidListPeriod(listArguments)) {
				return listPeriod(listArguments) + MESSAGE_NUM_DISPLAYED;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			updateDisplay();
			return MESSAGE_INVALID_LIST;
		}

		updateDisplay();
		return MESSAGE_INVALID_LIST;

	}

	/**
	 * @param listArguments
	 *            array
	 */

	private int listDayPeriod(String[] listArguments) {
		int tasksFound = 0;
		LocalDate date1 = toLocalDate(listArguments[0]);
		LocalDate date2 = toLocalDate(listArguments[1]);

		for (int tasksIndex = 0; tasksIndex < tasks.size(); tasksIndex++) {
			Task currTask = tasks.get(tasksIndex);
			if (currTask.getEndDate() != null) {
				if (currTask.getStartDate().equals(date1)
						|| currTask.getEndDate().equals(date1)
						|| currTask.getStartDate().equals(date2)
						|| currTask.getEndDate().equals(date2)) {
					toDisplay.add(currTask);
					tasksFound++;
				} else if (!(currTask.getStartDate().isAfter(date2) || currTask
						.getEndDate().isBefore(date1))) {
					toDisplay.add(currTask);
					tasksFound++;
				}
			} else if (currTask.getStartDate() != null) {
				if (!(currTask.getStartDate().isBefore(date1) || currTask
						.getStartDate().isAfter(date2))) {
					toDisplay.add(currTask);
					tasksFound++;
				}
			}
		}
		return tasksFound;
	}

	/**
	 * @param listArguments
	 *            array
	 * @return
	 */
	private boolean isValidDayPeriod(String[] listArguments) {
		return (listArguments[2] == null
				&& listArguments[0].matches(DATE_REGEX) && listArguments[1]
					.matches(DATE_REGEX));
	}

	/**
	 * @return Weekly sorted tasks
	 */

	private int listWeek() {
		String[] listWeekArguments = new String[2];
		DateTimeFormatter dateFormatter = DateTimeFormatter
				.ofPattern(DATE_PATTERN);
		LocalDate todayDate = LocalDate.now();
		LocalDate laterDate = todayDate.plusWeeks(1);

		listWeekArguments[0] = todayDate.format(dateFormatter);
		listWeekArguments[1] = laterDate.format(dateFormatter);

		return listDayPeriod(listWeekArguments);
	}

	/**
	 * @return
	 */
	private int listToday() {
		DateTimeFormatter dateFormatter = DateTimeFormatter
				.ofPattern(DATE_PATTERN);
		LocalDate todayDate = LocalDate.now();

		return listDate(todayDate.format(dateFormatter));
	}

	/**
	 * @param listArguments
	 * @return
	 */
	private boolean isValidListPeriod(String[] listArguments) {
		LocalDateTime dateTime1 = LocalDateTime.of(
				toLocalDate(listArguments[0]), toLocalTime(listArguments[1]));
		LocalDateTime dateTime2 = LocalDateTime.of(
				toLocalDate(listArguments[2]), toLocalTime(listArguments[3]));

		return listArguments[0].matches(DATE_REGEX)
				&& listArguments[1].matches(TIME_REGEX)
				&& listArguments[2].matches(DATE_REGEX)
				&& listArguments[3].matches(TIME_REGEX)
				&& dateTime1.isBefore(dateTime2);
	}

	/**
	 * @param listArguments
	 * @return
	 */
	private boolean isValidListDateTime(String[] listArguments) {
		return listArguments[2] == null && listArguments[0].matches(DATE_REGEX)
				&& listArguments[1].matches(TIME_REGEX);
	}

	/**
	 * @param listArguments
	 * @return
	 */
	private boolean isValidListDate(String[] listArguments) {
		return listArguments[1] == null && listArguments[0].matches(DATE_REGEX);
	}

	/**
	 * @param listArguments
	 * @return
	 */
	private int listPeriod(String[] listArguments) {
		int tasksFound = 0;
		LocalDateTime dateTime1 = LocalDateTime.of(
				toLocalDate(listArguments[0]), toLocalTime(listArguments[1]));
		LocalDateTime dateTime2 = LocalDateTime.of(
				toLocalDate(listArguments[2]), toLocalTime(listArguments[3]));

		for (int taskIndex = 0; taskIndex < tasks.size(); taskIndex++) {
			Task currTask = tasks.get(taskIndex);
			if (currTask.getStartDate() != null) {
				if ((getStartLDT(currTask).compareTo(dateTime1) >= 0)
						&& (getStartLDT(currTask).compareTo(dateTime2) <= 0)) {
					toDisplay.add(currTask);
					tasksFound++;
				} else if (currTask.getEndDate() != null) {
					if ((getEndLDT(currTask).compareTo(dateTime1) >= 0)
							&& (getEndLDT(currTask).compareTo(dateTime2) <= 0)) {
						toDisplay.add(currTask);
						tasksFound++;
					}
				}
			}
		}
		return tasksFound;
	}

	/**
	 * @param listArguments
	 * @return
	 */
	private int listDateTime(String[] listArguments) {
		int tasksFound = 0;
		LocalDateTime dateTime1 = LocalDateTime.of(
				toLocalDate(listArguments[0]), toLocalTime(listArguments[1]));

		for (int tasksIndex = 0; tasksIndex < tasks.size(); tasksIndex++) {
			Task currTask = tasks.get(tasksIndex);
			if (currTask.getEndDate() != null) {
				if (getStartLDT(currTask).equals(dateTime1)
						|| getEndLDT(currTask).equals(dateTime1)) {
					toDisplay.add(currTask);
					tasksFound++;
				} else if (getStartLDT(currTask).isBefore(dateTime1)
						&& getStartLDT(currTask).isAfter(dateTime1)) {
					toDisplay.add(currTask);
					tasksFound++;
				}
			} else if (currTask.getStartDate() != null) {
				if (getStartLDT(currTask).equals(dateTime1)) {
					toDisplay.add(currTask);
					tasksFound++;
				}
			}
		}
		return tasksFound;
	}

	/**
	 * @param dateString
	 * @return
	 */
	private int listDate(String dateString) {
		int tasksFound = 0;
		LocalDate date1 = toLocalDate(dateString);
		for (int tasksIndex = 0; tasksIndex < tasks.size(); tasksIndex++) {
			Task currTask = tasks.get(tasksIndex);
			if (currTask.getEndDate() != null) {
				if (currTask.getStartDate().equals(date1)
						|| currTask.getEndDate().equals(date1)) {
					toDisplay.add(currTask);
					tasksFound++;
				} else if (currTask.getStartDate().isBefore(date1)
						&& currTask.getEndDate().isAfter(date1)) {
					toDisplay.add(currTask);
					tasksFound++;
				}
			} else if (currTask.getStartDate() != null) {
				if (currTask.getStartDate().equals(date1)) {
					toDisplay.add(currTask);
					tasksFound++;
				}
			}
		}
		return tasksFound;
	}

	/**
	 * 
	 * 
	 * @param userCommand
	 * @return
	 */
	private String delete(String userCommand) {
		userCommand = removeFirstWord(userCommand);

		String taskName = userCommand;
		if (taskExists(taskName)) {
			commandStack.push(UNDOABLE.DELETE);
			historyVault.storeTask(taskVault.getTask(taskName));
			taskVault.deleteTask(taskName, trashVault);
			updateDisplay();
			saveVaults();
			return String.format("\"%s\" deleted successfully", taskName);
		}

		return MESSAGE_DELETE_UNSUCCESS;
	}

	/**
	 * 
	 */

	private void updateDisplay() {
		toDisplay.clear();
		toDisplay = copyList(taskVault.getList());
	}

	/**
	 * @param userCommand
	 */

	private String add(String userCommand) {
		userCommand = removeFirstWord(userCommand);

		String[] addArguments = parseAdd(userCommand);

		LocalDate startDate;
		try {
			startDate = toLocalDate(addArguments[INDEX_STARTDATE]);
		} catch (DateTimeParseException e) {
			return String.format(MESSAGE_INVALID_DATE, addArguments[INDEX_STARTDATE]);
		}
		LocalTime startTime;
		try {
			startTime = toLocalTime(addArguments[INDEX_STARTTIME]);
		} catch (DateTimeParseException e) {
			return String.format(MESSAGE_INVALID_TIME, addArguments[INDEX_STARTTIME]);
		}
		LocalDate endDate;
		try {
			endDate = toLocalDate(addArguments[INDEX_ENDDATE]);
		} catch (DateTimeParseException e) {
			return String.format(MESSAGE_INVALID_DATE, addArguments[INDEX_ENDDATE]);
		}
		LocalTime endTime;
		try {
			endTime = toLocalTime(addArguments[INDEX_ENDTIME]);
		} catch (DateTimeParseException e) {
			return String.format(MESSAGE_INVALID_TIME, addArguments[INDEX_ENDTIME]);
		}

		if ((taskVault.getTask(addArguments[INDEX_TASKNAME]) != null)) {
			return String.format(MESSAGE_ALREADY_EXISTS, addArguments[INDEX_TASKNAME]);
		}

		if (addArguments[INDEX_TASKNAME].startsWith("@")) {
			return MESSAGE_INVALID_TASKNAME;
		}
		
		if (addArguments[INDEX_TASKNAME].equals("")){
			return MESSAGE_EMPTY_TASKNAME;
		}

		if ((startDate != null) && (startTime == null)) {
			return MESSAGE_NO_STARTTIME;
		}

		if (endDate != null) {
			if (endTime == null) {
				return MESSAGE_NO_ENDTIME;
			}

			LocalDateTime startDateTime = LocalDateTime
					.of(startDate, startTime);
			LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
			if (endDateTime.isBefore(startDateTime)) {
				return MESSAGE_END_BEFORE_START;
			}

			for (int i = 0; i < tasks.size(); i++) {
				Task currTask = tasks.get(i);
				if (currTask.getEndDate() != null) {
					if (startDateTime.isAfter(getStartLDT(currTask))
							&& startDateTime.isBefore(getEndLDT(currTask))) {
						return "\"" + addArguments[INDEX_TASKNAME]
								+ "\" cannot overlap with \""
								+ currTask.getTaskName() + "\"";
					}
					if (endDateTime.isAfter(getStartLDT(currTask))
							&& endDateTime.isBefore(getEndLDT(currTask))) {
						return "\"" + addArguments[INDEX_TASKNAME]
								+ "\" cannot overlap with \""
								+ currTask.getTaskName() + "\"";
					}
					if (getStartLDT(currTask).isAfter(startDateTime)
							&& getStartLDT(currTask).isBefore(endDateTime)) {
						return "\"" + addArguments[INDEX_TASKNAME]
								+ "\" cannot overlap with \""
								+ currTask.getTaskName() + "\"";
					}
					if (getEndLDT(currTask).isAfter(startDateTime)
							&& getEndLDT(currTask).isBefore(endDateTime)) {
						return "\"" + addArguments[INDEX_TASKNAME]
								+ "\" cannot overlap with \""
								+ currTask.getTaskName() + "\"";
					}
				}
			}
		}

		if (taskVault.createTask(addArguments[INDEX_TASKNAME], addArguments[INDEX_COMMENT], startDate,
				startTime, endDate, endTime)) {
			commandStack.push(UNDOABLE.ADD);
			historyVault.storeTask(taskVault.getTask(addArguments[INDEX_TASKNAME]));
			updateDisplay();
			saveVaults();
			return "Task \"" + addArguments[INDEX_TASKNAME] + "\" successfully added";
		}

		return "Task \"" + addArguments[INDEX_TASKNAME] + "\"" + " cannot be added";
	}

	/**
	 * @param commandTypeString
	 */

	private COMMAND_TYPE determineCommandType(String commandTypeString) {
		if (commandTypeString == null) {
			throw new Error(MESSAGE_INVALID_COMMAND);
		}
		if (commandTypeString.equalsIgnoreCase("add")) {
			return COMMAND_TYPE.ADD;
		}
		if (commandTypeString.equalsIgnoreCase("delete")) {
			return COMMAND_TYPE.DELETE;
		}
		if (commandTypeString.equalsIgnoreCase("list")) {
			return COMMAND_TYPE.LIST;
		}
		if (commandTypeString.equalsIgnoreCase("empty")) {
			return COMMAND_TYPE.EMPTY;
		}
		if (commandTypeString.equalsIgnoreCase("search")) {
			return COMMAND_TYPE.SEARCH;
		}
		if (commandTypeString.equalsIgnoreCase("complete")) {
			return COMMAND_TYPE.COMPLETE;
		}
		if (commandTypeString.equalsIgnoreCase("edit")) {
			return COMMAND_TYPE.EDIT;
		}
		if (commandTypeString.equalsIgnoreCase("exit")) {
			return COMMAND_TYPE.EXIT;
		}
		if (commandTypeString.equalsIgnoreCase("next")) {
			return COMMAND_TYPE.NEXT;
		}
		if (commandTypeString.equalsIgnoreCase("undo")) {
			return COMMAND_TYPE.UNDO;
		}
		if (commandTypeString.equalsIgnoreCase("changedir")) {
			return COMMAND_TYPE.CHANGEDIR;
		}
		if (commandTypeString.equalsIgnoreCase("getdir")) {
			return COMMAND_TYPE.GETDIR;
		}
		if (commandTypeString.equalsIgnoreCase("recur")) {
			return COMMAND_TYPE.RECUR;
		}
		if (commandTypeString.equalsIgnoreCase("addrecur")) {
			return COMMAND_TYPE.ADDRECUR;
		}
		if (commandTypeString.equalsIgnoreCase("help")) {
			return COMMAND_TYPE.HELP;
		} 
		if (commandTypeString.equalsIgnoreCase("show")) {
			return COMMAND_TYPE.SHOW;
		}
		return COMMAND_TYPE.INVALID;
	}

	// PARSE METHODS
	/**
	 * Method that takes in taskName, taskDescription, Startdate, Startime,
	 * Enddate, Endtime
	 * 
	 * @param command
	 * @return details of task that have been entered by user
	 */

	public String[] parseAdd(String command) {

		String[] arguments = new String[ADD_ARGUMENTS_LENGTH];
		arguments[INDEX_TASKNAME] = getTaskName(command);
		arguments[INDEX_COMMENT] = getDescription(command);
		String[] dates = extractDates(command);
		String[] times = extractTimes(command);

		arguments[INDEX_STARTDATE] = dates[INDEX_PARSED_DATE_1];
		arguments[INDEX_ENDDATE] = dates[INDEX_PARSED_DATE_2];
		arguments[INDEX_STARTTIME] = times[INDEX_PARSED_TIME_1];
		arguments[INDEX_ENDTIME] = times[INDEX_PARSED_TIME_2];

		return arguments;
	}

	/**
	 * Method to extract dates and times of tasks entered by user
	 * 
	 * @param data
	 * @return Start date, Start time, End date, End time
	 */

	public String[] parseList(String data) {
		String[] arguments;
		arguments = data.trim().split("\\s+");
		if (!(arguments[INDEX_FIRST_WORD].matches(DATE_REGEX) || arguments[INDEX_FIRST_WORD]
				.matches(TIME_REGEX))) {
			return arguments;
		}

		String[] dates = extractDates(data);
		String[] times = extractTimes(data);
		arguments = new String[LIST_ARGUMENTS_LENGTH];
		arguments[LIST_DATE_1] = dates[INDEX_PARSED_DATE_1];
		arguments[LIST_DATE_2] = dates[INDEX_PARSED_DATE_2];
		arguments[LIST_TIME_1] = times[INDEX_PARSED_TIME_1];
		arguments[LIST_TIME_2] = times[INDEX_PARSED_TIME_2];

		return arguments;
	}

	/**
	 * Method to search tasks for specific keyword entered by user
	 * 
	 * @param data
	 * @return search keyword
	 */

	public String[] parseSearch(String data) {
		return data.trim().split("\\s+");
	}

	// STRING MANIPULATION METHODS
	/**
	 * Method to get task name
	 * 
	 * @param data
	 * @return taskName
	 */

	public String getTaskName(String data) {
		String task = data.split(DATE_REGEX, 2)[0];
		task = task.trim();
		
		if(data.split(TIME_REGEX, 2)[0].trim().length()<task.length()){
			task = data.split(TIME_REGEX, 2)[0].trim();
		}
		
		return task;
	}

	/**
	 * Method to get task description
	 * 
	 * @param data
	 * @return description of task
	 */

	public String getDescription(String data) {
		String[] split = data.split(TIME_REGEX);
		String description = split[split.length - 1].trim();
		if (description.matches(DATE_REGEX) || (split.length == 1)) {
			return null;
		}

		return description;
	}

	/**
	 * Method to extract dates
	 * 
	 * @param data
	 * @return specific date
	 */

	public String[] extractDates(String data) {
		Pattern datePattern = Pattern.compile(DATE_REGEX);
		Matcher dateMatcher = datePattern.matcher(data);
		String[] result = new String[2];
		int index = 0;
		while (dateMatcher.find()) {
			result[index++] = dateMatcher.group();
		}
		return result;
	}

	/**
	 * Method to extract times entered by user
	 * 
	 * @param data
	 * @return specific time
	 */

	public String[] extractTimes(String data) {
		Pattern timePattern = Pattern.compile(TIME_REGEX);
		Matcher timeMatcher = timePattern.matcher(data);
		String[] result = new String[2];
		int index = 0;
		while (timeMatcher.find()) {
			result[index++] = timeMatcher.group();
		}
		return result;
	}

	/**
	 * Method to extract command from original String
	 * 
	 * @param userCommand
	 * @return command
	 */

	private String getFirstWord(String userCommand) {
		return userCommand.trim().split("\\s+")[0];
	}

	/**
	 * Method to remove word from original string and replace with empty space
	 * 
	 * @param userCommand
	 * @return
	 */
	private String removeFirstWord(String userCommand) {
		return userCommand.replaceFirst(getFirstWord(userCommand), "").trim();
	}

	/**
	 * Method to standardize date input (DD:MM:YYYY)
	 * 
	 * @param dataString
	 * @return
	 */
	private LocalDate toLocalDate(String dateString)
			throws DateTimeParseException {
		if ((dateString == null) || (!dateString.matches(DATE_REGEX))) {
			return null;
		}
		DateTimeFormatter dateFormatter = DateTimeFormatter
				.ofPattern(DATE_PATTERN);
		LocalDate date = LocalDate.parse(dateString, dateFormatter);

		return date;
	}

	/**
	 * Method to standardize time (HH:MM)
	 * 
	 * @param data
	 * @return
	 */

	private LocalTime toLocalTime(String timeString)
			throws DateTimeParseException {
		if ((timeString == null) || (!timeString.matches(TIME_REGEX))) {
			return null;
		}
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
		LocalTime time = LocalTime.parse(timeString, timeFormatter);

		return time;
	}

	/**
	 * Method to return Start Date & Start Time
	 * 
	 * @param task
	 * @return Start date, Start time
	 */

	public LocalDateTime getStartLDT(Task task) {
		return LocalDateTime.of(task.getStartDate(), task.getStartTime());
	}

	/**
	 * Method to return End Date & End Time
	 * 
	 * @param task
	 * @return End date, End time
	 */

	public LocalDateTime getEndLDT(Task task) {
		return LocalDateTime.of(task.getEndDate(), task.getEndTime());
	}

	/**
	 * Method to check if start date & start time is before end date and end
	 * time
	 * 
	 * @param task
	 * @return true/false
	 */

	public boolean isInOrder(LocalDate startDate, LocalTime startTime,
			LocalDate endDate, LocalTime endTime) {
		return (LocalDateTime.of(startDate, startTime).isBefore(LocalDateTime
				.of(endDate, endTime)));
	}

	/**
	 * Method to open and write to file
	 * 
	 * @param newString
	 * @return config.txt
	 * @throws IOException
	 */

	private static void writeToConfig(String newString) throws IOException {
		FileWriter fw = new FileWriter("config.txt", true);
		fw.write(newString);
		fw.close();
	}

	/**
	 * Method that includes bufferedReader
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */

	private static String getVaultPath(String fileName) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(
				fileName));

		try {
			String line = bufferedReader.readLine();
			return line;
		} finally {
			bufferedReader.close();
		}
	}

	/**
	 * Method to clear file
	 * 
	 * @return config.txt
	 * @throws IOException
	 */

	private static void clearConfigFile() throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File("config.txt"));
		writer.print("");
		writer.close();
	}
}