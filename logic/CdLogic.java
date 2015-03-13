package logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.*;
import java.io.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import vault.CompletedTaskVault;
import vault.HistoryVault;
import vault.Vault;
import vault.TaskVault;
import vault.TrashVault;
import model.Task;

public class CdLogic {
	private static final String DATE_REGEX = "([1-9]|[012][0-9]|3[01])[-/]\\s*(0[1-9]|1[012])[-/]\\s*((19|20)?[0-9]{2})";
	private static final String TIME_REGEX = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

	private static final String MESSAGE_INVALID_FORMAT = "invalid command "
			+ "format :%1$s";

	private static Vault vault;
	private static TaskVault taskVault;
	private static TrashVault trashVault;
	private static HistoryVault historyVault;
	private static CompletedTaskVault completedTaskVault;

	private static ObservableList<Task> toDisplay;
	private static ObservableList<Task> tasks;

	enum COMMAND_TYPE {
		ADD, DELETE, LIST, EMPTY, SEARCH, COMPLETE, EDIT, INVALID, EXIT, CHANGEDIR, UNDO, NEXT
	}

	enum TaskType {
		FLOAT, EVENT, DATELINE
	}

	public CdLogic() throws IOException {
		String vaultPath = "";
		taskVault = new TaskVault(vaultPath);
		trashVault = new TrashVault(vaultPath);
		historyVault = new HistoryVault(vaultPath);
		completedTaskVault = new CompletedTaskVault(vaultPath);
		tasks = taskVault.getList();
		toDisplay = copyList(tasks);
	}

	public ObservableList<Task> getTaskList() {
		tasks = taskVault.getList();

		return tasks;
	}

	public ObservableList<Task> getDisplayList() {
		return toDisplay;
	}

	public String executeCommand(String userCommand) throws IOException {
		tasks = taskVault.getList();

		if (userCommand.trim().equals(""))
			return String.format(MESSAGE_INVALID_FORMAT, userCommand);

		String commandTypeString = getFirstWord(userCommand);
		COMMAND_TYPE commandType = determineCommandType(commandTypeString);
		userCommand = removeFirstWord(userCommand);

		switch (commandType) {
		case ADD:
			return add(userCommand);
		case DELETE:
			return delete(userCommand);
		case LIST:
			return list(userCommand);
		case EMPTY:
			return empty();
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
		case NEXT:
			return next();
		default:
			// throw an error if the command is not recognized
			throw new Error("Unrecognized command type");
		}
	}

	private String next() {
		// TODO Auto-generated method stub
		if (tasks.isEmpty()) {
			return "no next tasks";
		}

		toDisplay.clear();
		toDisplay.add(taskVault.getNextTask());
		
		//saveVaults();

		return "next task shown";

	}

	/**
	 * 
	 */
	private void saveVaults() {
		historyVault.save();
		taskVault.save();
		trashVault.save();
		completedTaskVault.save();
	}

	private String edit(String userCommand) throws IOException {

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
		return "invalid edit command";

	}

	private String addComment(String userCommand) {
		// TODO Auto-generated method stub
		String[] editArguments = parseEdit(userCommand, "addcomment");
		String taskName = editArguments[0].trim();
		String newComment = editArguments[1].trim();

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);

			taskVault.deleteTask(taskName, trashVault);
			taskVault.createTask(oldTask.getTaskName(), newComment,
					oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), oldTask.getEndTime());
			updateDisplay();
			//saveVaults();
			return "comment added";
		} else {
			return "task " + taskName + " not found";
		}
	}

	private String editEndTime(String userCommand) {
		// TODO Auto-generated method stub
		String[] editArguments = parseEdit(userCommand, "enddate");
		String[] endTimes = extractTimes(editArguments[1]);
		String taskName = editArguments[0].trim();
		LocalTime newEndTime;
		try {
			newEndTime = toLocalTime(endTimes[0]);
		} catch (DateTimeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "New time not valid";
		}
		
<<<<<<< HEAD
		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);
			if(!isInOrder(oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), newEndTime)){
				return "New date is not chronologically correct";
			}
			taskVault.deleteTask(taskName, trashVault);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), newEndTime);
			updateDisplay();
			//saveVaults();
			return "edit done";
		} else {
			return "task " + taskName + " not found";
		}

	}

	/**
	 * @param userCommand
	 * @return
	 */
	private String editStartTime(String userCommand) {
		// TODO Auto-generated method stub
		String[] editArguments = parseEdit(userCommand, "enddate");
		String[] startTimes = extractTimes(editArguments[1]);
		String taskName = editArguments[0].trim();
		LocalTime newStartTime;
		try {
			newStartTime = toLocalTime(startTimes[0]);
		} catch (DateTimeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "New time not valid";
		}

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);
			if(!isInOrder(oldTask.getStartDate(), newStartTime, oldTask.getEndDate(),
					oldTask.getEndTime())){
				return "New date is not chronologically correct";
			}
			
			
			taskVault.deleteTask(taskName, trashVault);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					oldTask.getStartDate(), newStartTime, oldTask.getEndDate(),
					oldTask.getEndTime());
			updateDisplay();
			//saveVaults();
			return "edit done";
		} else {
			return "task " + taskName + " not found";
		}
	}

	/**
	 * @param userCommand
	 * @return
	 */
	private String editEndDate(String userCommand) {
		String[] editArguments = parseEdit(userCommand, "enddate");
		String[] endDates = extractDates(editArguments[1]);
		String taskName = editArguments[0].trim();
		LocalDate newEndDate;
		try {
			newEndDate = toLocalDate(endDates[0]);
		} catch (DateTimeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "New date not valid";
		}

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);
			if(!isInOrder(oldTask.getStartDate(), oldTask.getStartTime(), newEndDate,
					oldTask.getEndTime())){
				return "New date is not chronologically correct";
			}
			taskVault.deleteTask(taskName, trashVault);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					oldTask.getStartDate(), oldTask.getStartTime(), newEndDate,
					oldTask.getEndTime());
			updateDisplay();
			//saveVaults();
			return "edit done";
		} else {
			return "task " + taskName + " not found";
		}
	}

	/**
	 * @param userCommand
	 * @return
	 */
	private String editStartDate(String userCommand) {
		String[] editArguments = parseEdit(userCommand, "startdate");
		String[] startDates = extractDates(editArguments[1]);
		String taskName = editArguments[0].trim();
		LocalDate newStartDate;
		try {
			newStartDate = toLocalDate(startDates[0]);
		} catch (DateTimeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "New date not valid";
		}

		if (newStartDate == null) {
			return "Enter a valid date";
		} else if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);
			
			
			if(!isInOrder(newStartDate, oldTask.getStartTime(), oldTask.getEndDate(),
					oldTask.getEndTime())){
				return "New date is not chronologically correct";
			}
			taskVault.deleteTask(taskName, trashVault);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					newStartDate, oldTask.getStartTime(), oldTask.getEndDate(),
					oldTask.getEndTime());
			updateDisplay();
			//saveVaults();
			return "edit done";
		} else {
			return "task " + taskName + " not found";
		}
	}

	/**
	 * @param userCommand
	 * @return
	 */
	private String editTaskName(String userCommand) {
		String[] editArguments = parseEdit(userCommand, "taskname");
		String taskName = editArguments[0].trim();
		String newTaskName = editArguments[1].trim();

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);
			
			for(int i = 0; i<tasks.size(); i++){
				if(tasks.get(i).getTaskName().equals(newTaskName)){
					return "\"" + newTaskName + "\" already exists";
				}
			}
			
			taskVault.deleteTask(taskName, trashVault);
			taskVault.createTask(newTaskName, oldTask.getComment(),
					oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), oldTask.getEndTime());
			updateDisplay();
			//saveVaults();
			return "edit done";
		} else {
			return "task " + taskName + " not found";
		}
	}

	private boolean taskExists(String taskName) {
		return taskVault.getTask(taskName) != null;
=======
    	if(userCommand.contains("taskname")){
    		String[] editArguments = parseEdit(userCommand, "taskname");
    		String taskName = editArguments[0].trim();
    		String newTaskName = editArguments[1].trim();
    		
    		
    		if(taskVault.getTask(taskName)!=null){	
    			
    			Task oldTask = taskVault.getTask(taskName);
    			oldTaskName = oldTask.getTaskName();
    			oldTaskDescription = oldTask.getComment();
    			oldStartDate = oldTask.getStartDate();
    			oldStartTime = oldTask.getStartTime();
    			oldEndDate = oldTask.getEndDate();
    			oldEndTime = oldTask.getEndTime();
    			
    			taskVault.deleteTask(taskName, trashVault);
    			taskVault.createTask(newTaskName, oldTaskDescription, oldStartDate, oldStartTime, oldEndDate, oldEndTime);
    			updateDisplay();
    			return "edit done";
    		}else{ 
    			return "task " + taskName + " not found";
    		}
    		
    	}else if(userCommand.contains("startdate")){
    		String[] editArguments = parseEdit(userCommand, "startdate");
    		String[] startDates = extractDates(editArguments[1]);
    		String[] startTimes = extractTimes(editArguments[1]);
    		
    		LocalDate newStartDate = toLocalDate(startDates[0]);
    		LocalTime newStartTime = toLocalTime(startTimes[0]);
    		
    		if(taskVault.getTask(editArguments[0].trim())!=null){
    			
    			Task oldTask = taskVault.getTask(editArguments[0].trim());
    			oldTaskName = oldTask.getTaskName();
    			oldTaskDescription = oldTask.getComment();
    			oldStartDate = oldTask.getStartDate();
    			oldStartTime = oldTask.getStartTime();
    			oldEndDate = oldTask.getEndDate();
    			oldEndTime = oldTask.getEndTime();
    			
    			taskVault.deleteTask(oldTaskName, trashVault);
    			taskVault.createTask(oldTaskName, oldTaskDescription, newStartDate, newStartTime, oldEndDate, oldEndTime);
    			updateDisplay();
    			return "edit done";
    		}else{ 
    			return "task " + editArguments[0].trim() + " not found";
    		}
    	}else if(userCommand.contains("enddate")){
    		String[] editArguments = parseEdit(userCommand, "enddate");
    		
    		String[] startDates = extractDates(editArguments[1]);
    		String[] startTimes = extractTimes(editArguments[1]);
    		
    		LocalDate newEndDate = toLocalDate(startDates[0]);
    		LocalTime newEndTime = toLocalTime(startTimes[0]);
    		
    		if(taskVault.getTask(editArguments[0].trim())!=null){
    			Task oldTask = taskVault.getTask(editArguments[0].trim());
    			oldTaskName = oldTask.getTaskName();
    			oldTaskDescription = oldTask.getComment();
    			oldStartDate = oldTask.getStartDate();
    			oldStartTime = oldTask.getStartTime();
    			oldEndDate = oldTask.getEndDate();
    			oldEndTime = oldTask.getEndTime();
    			
    			taskVault.deleteTask(oldTaskName, trashVault);
    			taskVault.createTask(oldTaskName, oldTaskDescription, oldStartDate, oldStartTime, newEndDate, newEndTime);
    			updateDisplay();
    			
    			return "edit done";
    		}else{ 
    			return "task " + editArguments[0] + " not found";
    		}
    	}else return "invalid edit command";

    	
>>>>>>> origin/master
	}

	private String[] parseEdit(String userCommand, String string) {
		// TODO Auto-generated method stub

		return userCommand.trim().split(string);
	}

	private ObservableList<Task> copyList(ObservableList<Task> toCopy) {
		ObservableList<Task> newList = FXCollections.observableArrayList();
		for (int i = 0; i < toCopy.size(); i++) {
			newList.add(i, toCopy.get(i));
		}
		return newList;
	}

	private String complete(String userCommand) {
		if (taskVault.completeTask(userCommand, completedTaskVault)) {
			updateDisplay();
			//saveVaults();
			return "\"" + userCommand + "\"" + " completed successfully";
		} else {
			return "\"" + userCommand + "\"" + "couldn't be completed";
		}
	}

	private String search(String userCommand) {
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
		return found + " tasks found";
	}

	private boolean isNotYetFound(int taskIndex) {
		return !(toDisplay.contains(tasks.get(taskIndex)));
	}

	private boolean isPartialMatch(String currentWord, int taskIndex) {
		return tasks.get(taskIndex).getTaskName().contains(currentWord);
	}

	private boolean isCompleteMatch(String userCommand, int taskIndex) {
		return tasks.get(taskIndex).getTaskName().equals(userCommand);
	}

	private String empty() {
		if (trashVault.emptyTrash()) {
			//saveVaults();
			return "trash emptied successfully";
		} else {
			return "trash can't be emptied";
		}
	}

	private String list(String userCommand) {
		String[] listArguments = parseList(userCommand);
		toDisplay.clear();

		if (listArguments[0] == null) {
			System.out.println("Updating display");
			updateDisplay();
			return "All tasks displayed";
		}
		
		if(listArguments[0].equalsIgnoreCase("today")){
			return listToday() + " tasks displayed";
		}

		if(listArguments[0].equalsIgnoreCase("week")){
			return listWeek() + " tasks displayed";
		}
		
		if (isValidListDate(listArguments)) {
			return listDate(listArguments[0]) + "tasks displayed";
		} else if (isValidListDateTime(listArguments)) {
			return listDateTime(listArguments) + "tasks displayed";
		} else if (isValidDayPeriod(listArguments)){
			return listDayPeriod(listArguments) + " tasks displayed";
		}
		else if (isValidListPeriod(listArguments)) {
			return listPeriod(listArguments) + "tasks displayed";
		}

		return "Invalid list command";

	}
	
	private int listDayPeriod(String[] listArguments){
		int tasksFound = 0;
		LocalDate date1 = toLocalDate(listArguments[0]);
		LocalDate date2 = toLocalDate(listArguments[1]);

		for (int tasksIndex = 0; tasksIndex < tasks.size(); tasksIndex++) {
			Task currTask = tasks.get(tasksIndex);
			if (currTask.getEndDate() != null) {
				if (currTask.getStartDate().equals(date1)
						|| currTask.getEndDate().equals(date1) || currTask.getStartDate().equals(date2)
						|| currTask.getEndDate().equals(date2) ) {
					toDisplay.add(currTask);
					tasksFound++;
				} else if (!(currTask.getStartDate().isAfter(date2)
						|| currTask.getEndDate().isBefore(date1))) {
					toDisplay.add(currTask);
					tasksFound++;
				}
			} else if (currTask.getStartDate() != null) {
				if (!(currTask.getStartDate().isBefore(date1)||currTask.getStartDate().isAfter(date2))) {
					toDisplay.add(currTask);
					tasksFound++;
				}
			}
		}
		return tasksFound;		
	}

	private boolean isValidDayPeriod(String[] listArguments) {
		// TODO Auto-generated method stub
		return (listArguments[2] == null && listArguments[0].matches(DATE_REGEX)
				&& listArguments[1].matches(DATE_REGEX));
	}

	private int listWeek() {
		// TODO Auto-generated method stub
		String[] listWeekArguments = new String[2];
		DateTimeFormatter dateFormatter = DateTimeFormatter
				.ofPattern("dd/MM/yyyy");
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
				.ofPattern("dd/MM/yyyy");
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
		String taskName = userCommand;
		if (taskVault.deleteTask(taskName, trashVault)) {
			updateDisplay();
			//saveVaults();
			return "\"" + taskName + "\"" + " deleted successfully";
		}

		return "delete not successful";
	}

	private void updateDisplay() {
		toDisplay.clear();
		toDisplay = copyList(taskVault.getList());
	}

	private String add(String userCommand) {
		String[] addArguments = parseAdd(userCommand);
		
		LocalDate startDate;
		try {
			startDate = toLocalDate(addArguments[2]);
		} catch (DateTimeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Date " + addArguments[2] + " is not valid";
		}
		LocalTime startTime;
		try {
			startTime = toLocalTime(addArguments[3]);
		} catch (DateTimeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Time " + addArguments[3] + " is not valid";
		}
		LocalDate endDate;
		try {
			endDate = toLocalDate(addArguments[4]);
		} catch (DateTimeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Date " + addArguments[4] + " is not valid";
		}
		LocalTime endTime;
		try {
			endTime = toLocalTime(addArguments[5]);
		} catch (DateTimeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Date " + addArguments[5] + " is not valid";
		}

		if((taskVault.getTask(addArguments[0])!=null)){
			return "\"" + addArguments[0] + "\" already exists";
		}
		
		
		if(endDate!=null){
			LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
			LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
			if(endDateTime.isBefore(startDateTime)){
				return "Task cannot end before it starts";
			}
			
			for(int i = 0; i<tasks.size(); i++){
				Task currTask = tasks.get(i);
				if (currTask.getEndDate()!=null){
					if(startDateTime.isAfter(getStartLDT(currTask)) && startDateTime.isBefore(getEndLDT(currTask))){
						return "\"" + addArguments[0] + "\" cannot overlap with \"" + currTask.getTaskName() + "\"";
					}
					if(endDateTime.isAfter(getStartLDT(currTask)) && endDateTime.isBefore(getEndLDT(currTask))){
						return "\"" + addArguments[0] + "\" cannot overlap with \"" + currTask.getTaskName() + "\"";
					}					
				}
			}
		}
		
		if (taskVault.createTask(addArguments[0], addArguments[1], startDate,
				startTime, endDate, endTime)) {
			updateDisplay();
			//saveVaults();
			return " Task \"" + addArguments[0] + "\" successfully added";
		}

		return " Task \"" + addArguments[0] + "\"" + " cannot be added";
	}

	private COMMAND_TYPE determineCommandType(String commandTypeString) {
		if (commandTypeString == null) {
			throw new Error("command type string cannot be null!");
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
		return COMMAND_TYPE.INVALID;
	}

	// PARSE METHODS

	public String[] parseAdd(String command) {
		System.out.println(command);

		String[] arguments = new String[6];
		arguments[0] = getTaskName(command);
		arguments[1] = getDescription(command);
		String[] dates = extractDates(command);
		String[] times = extractTimes(command);

		arguments[2] = dates[0];
		arguments[4] = dates[1];
		arguments[3] = times[0];
		arguments[5] = times[1];

		return arguments;
	}

	public String[] parseList(String data) {
		String[] arguments;
		arguments = data.trim().split("\\s+");
		if(!(arguments[0].matches(DATE_REGEX)||arguments[0].matches(TIME_REGEX))){
			return arguments;
		}
	
		String[] dates = extractDates(data);
		String[] times = extractTimes(data);
		arguments = new String[4];
		arguments[0] = dates[0];
		arguments[2] = dates[1];
		arguments[1] = times[0];
		arguments[3] = times[1];

		return arguments;
	}

	public String[] parseSearch(String data) {
		return data.trim().split("\\s+");
	}

	// STRING MANIPULATION METHODS

	public String getTaskName(String data) {
		String task = data.split(DATE_REGEX, 2)[0];
		task = task.trim();

		return task;
	}

	public String getDescription(String data) {
		String[] split = data.split(TIME_REGEX);
		String description = split[split.length - 1].trim();

		return description;
	}

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

	private String getFirstWord(String userCommand) {
		return userCommand.trim().split("\\s+")[0];
	}

	private String removeFirstWord(String userCommand) {
		return userCommand.replaceFirst(getFirstWord(userCommand), "").trim();
	}

	private LocalDate toLocalDate(String dateString) throws DateTimeParseException{
		if ((dateString == null) || (!dateString.matches(DATE_REGEX))) {
			return null;
		}
		DateTimeFormatter dateFormatter = DateTimeFormatter
				.ofPattern("dd/MM/yyyy");
		LocalDate date = LocalDate.parse(dateString, dateFormatter);

		return date;
	}

	private LocalTime toLocalTime(String timeString) throws DateTimeParseException{
		if ((timeString == null) || (!timeString.matches(TIME_REGEX))) {
			return null;
		}
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		LocalTime time = LocalTime.parse(timeString, timeFormatter);

		return time;
	}

	public LocalDateTime getStartLDT(Task task) {
		return LocalDateTime.of(task.getStartDate(), task.getStartTime());
	}

	public LocalDateTime getEndLDT(Task task) {
		return LocalDateTime.of(task.getEndDate(), task.getEndTime());
	}

	public boolean isInOrder(LocalDate startDate, LocalTime startTime,
			LocalDate endDate, LocalTime endTime) {
		return (LocalDateTime.of(startDate, startTime).isBefore(LocalDateTime
				.of(endDate, endTime)));
	}
}