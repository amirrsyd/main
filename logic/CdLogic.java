package logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Stack;
import java.util.regex.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import vault.CompletedTaskVault;
import vault.HistoryVault;
import vault.TaskVault;
import vault.TrashVault;
import model.Task;

public class CdLogic {
	private static final String USER_DIR = "user.dir";
	private static final String DATE_REGEX = "([1-9]|[012][0-9]|3[01])[-/]\\s*(0[1-9]|1[012])[-/]\\s*((19|20)?[0-9]{2})";
	private static final String TIME_REGEX = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

	private static final String MESSAGE_INVALID_FORMAT = "invalid command "
			+ "format :%1$s";
	private static final String MESSAGE_ERROR = "Unrecognized command type" ;
	private static final String MESSAGE_NONEXIST = "Directory doesnt exist" ;
	private static final String MESSAGE_NO_NEXT_TASK = "no next tasks" ;
	private static final String MESSAGE_SHOW_NEXT_TASK = "next task shown" ;
	private static final String MESSAGE_INVALID_EDIT = "invalid edit command" ;
	private static final String MESSAGE_INVALID_TIME = "new time not valid" ;
	private static final String MESSAGE_TRASH_CLEARED = "trash emptied successfully" ;
	private static final String MESSAGE_TRASH_UNCLEARED = "trash can't be emptied" ;
	private static final String MESSAGE_NOT_CHRON = "new date not chronologically correct" ;
	private static final String MESSAGE_EDIT_SUCCESS = "edit complete" ;
	private static final String MESSAGE_INVALID_DATE = "new date not valid" ;
	private static final String MESSAGE_DELETE_UNSUCCESS = "Delete not successful" ;
	private static final String MESSAGE_INVALID_COMMAND = "command type string cannot be null!" ;

	private static TaskVault taskVault;
	private static TrashVault trashVault;
	private static HistoryVault historyVault;
	private static CompletedTaskVault completedTaskVault;
	private static Stack<UNDOABLE> commandStack;
	private static String vaultPath;

	private static ObservableList<Task> toDisplay;
	private static ObservableList<Task> tasks;
	private static ObservableList<Task> history;
	
	
	enum UNDOABLE {
		ADD, DELETE, COMPLETE, EDIT, CHANGEDIR
	}


	enum COMMAND_TYPE {
		ADD, DELETE, LIST, EMPTY, SEARCH, COMPLETE, EDIT, INVALID, EXIT, CHANGEDIR, UNDO, NEXT, GETDIR
	}

	enum TaskType {
		FLOAT, EVENT, DATELINE
	}
	
	enum Day {
		MON, TUE, WED, THU, FRI, SAT, SUN
	}

	public CdLogic() throws IOException {
		initializeFromConfig();
		initializeVaults();
		history = FXCollections.observableArrayList();
		commandStack = new Stack<UNDOABLE>();
		tasks = taskVault.getList();
		toDisplay = copyList(tasks);
	}

	/**
	 * @throws IOException
	 */
	private void initializeFromConfig() throws IOException {
		File config = new File("config.txt");
		/*
		 * Warning: If config.txt exists but is empty,
		 * a nullpointerexception will be thrown. If it exists but
	 	 * the path inside is not a directory, tasks will not be
	 	 * saved.
		 */
		if (!config.exists()){
			config.createNewFile();
			writeToConfig(System.getProperty(USER_DIR));
			vaultPath = System.getProperty(USER_DIR);
		}else{
			vaultPath = getVaultPath("config.txt").trim();
		}
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

	public ObservableList<Task> getTaskList() {
		tasks = FXCollections.observableArrayList(taskVault.getList());

		return FXCollections.observableArrayList(toDisplay);
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
		case UNDO:
			return undo();
		case CHANGEDIR:
			return changeDirectory(userCommand);
		case GETDIR:
			return getDirectory();
		default:
			// throw an error if the command is not recognized
			throw new Error(MESSAGE_ERROR);
		}
	}

	private String getDirectory() {
		// TODO Auto-generated method stub
		
		return "Working directory: " + vaultPath;
	}

	private String changeDirectory(String userCommand) throws IOException {
		String newPathString = removeFirstWord(userCommand).trim();
	
		File newPath = new File(newPathString);
		newPath = newPath.getAbsoluteFile();
		if (newPath.isDirectory()) {
			Task oldDirTask = new Task(vaultPath);
			history.add(oldDirTask);
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
			return "files moved to \"" + newPathString + "\"";
		}
		
		return MESSAGE_NONEXIST ;
		
	}

	/**
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
	 * @param newPathString
	 * @throws IOException
	 */
	private void copyToNewPath(String newPathString) throws IOException {
		String oldPathString = vaultPath;
		Path taskVaultPath = Paths.get(oldPathString + "/taskList.txt").toAbsolutePath();
		Path completedVaultPath = Paths.get(oldPathString + "/completed.txt").toAbsolutePath();
		Path trashVaultPath = Paths.get(oldPathString + "/trash.txt").toAbsolutePath();
		Path historyVaultPath = Paths.get(oldPathString + "/history.txt").toAbsolutePath();

		Path newTaskVaultPath = Paths.get(newPathString + "/taskList.txt").toAbsolutePath();
		Path newCompletedVaultPath = Paths.get(newPathString + "/completed.txt").toAbsolutePath();
		Path newTrashVaultPath = Paths.get(newPathString+ "/trash.txt").toAbsolutePath();
		Path newHistoryVaultPath = Paths.get(newPathString + "/history.txt").toAbsolutePath();

		Files.copy(taskVaultPath, newTaskVaultPath, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(completedVaultPath, newCompletedVaultPath, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(trashVaultPath, newTrashVaultPath, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(historyVaultPath, newHistoryVaultPath, StandardCopyOption.REPLACE_EXISTING);

		File oldTaskFile = new File(taskVaultPath.toString());
		File oldTrashFile = new File(completedVaultPath.toString());
		File oldCompletedFile = new File(trashVaultPath.toString());
		File oldHistoryFile = new File(historyVaultPath.toString());

		oldTaskFile.delete();
		oldTrashFile.delete();
		oldCompletedFile.delete();
		oldHistoryFile.delete();
	}

	private String undo() throws IOException {
		// TODO Auto-generated method stub
		if(history.isEmpty()){
			return "No more undo left";
		}
		switch (commandStack.peek()){
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
			return undoChangeDir();
		default:
			break;

		}
		return null;
	}

	private String undoChangeDir() throws IOException {
		String newPathString = history.remove(history.size()-1).getTaskName();
		
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
			return "files moved to \"" + newPathString + "\"";
		}
		
		return MESSAGE_NONEXIST ;
	}

	private String undoEdit() {
//		ObservableList<Task> historyList = historyVault.getList();
//		Task newTask = historyList.get(historyList.size() - 1);
//		Task oldTask = historyList.get(historyList.size() - 2);
		Task newTask = history.remove(history.size()-1);
		Task oldTask = history.remove(history.size()-1);
		trashVault.remove(oldTask.getTaskName());
		taskVault.remove(newTask.getTaskName());
		taskVault.storeTask(oldTask);
//		historyVault.remove(oldTask.getTaskName());
//		historyVault.remove(newTask.getTaskName());
		updateDisplay();
		saveVaults();
		return "Undo: \"" + oldTask.getTaskName() + "\" has been restored to its original";
	}

	private String undoDelete() {
//		ObservableList<Task> historyList = historyVault.getList();
//		Task historyTask = historyList.get(historyList.size() - 1);
		Task historyTask = history.remove(history.size()-1);
		taskVault.storeTask(historyTask);
		trashVault.remove(historyTask.getTaskName());
//		historyVault.remove(historyTask.getTaskName());
		updateDisplay();
		saveVaults();
		return "Undo: \"" + historyTask.getTaskName()
				+ "\" moved back from trash to tasks";
	}

	private String undoComplete() {
//		ObservableList<Task> historyList = historyVault.getList();
//		Task historyTask = historyList.get(historyList.size() - 1);
		Task historyTask = history.remove(history.size()-1);
		taskVault.storeTask(historyTask);
		completedTaskVault.remove(historyTask.getTaskName());
//		historyVault.remove(historyTask.getTaskName());
		updateDisplay();
		saveVaults();
		return "Undo: \"" + historyTask.getTaskName() + "\" moved back from completed to tasks";
	}

	private String undoAdd() {
//		ObservableList<Task> historyList = historyVault.getList();
//		Task historyTask = historyList.get(historyList.size()-1);
		Task historyTask = history.remove(history.size()-1);
		taskVault.remove(historyTask.getTaskName());
//		historyVault.remove(historyTask.getTaskName());
		updateDisplay();
		saveVaults();
		return "Undo: \"" + historyTask.getTaskName() + "\" removed from tasks";
	}

	private String next() {
		// TODO Auto-generated method stub

		if (tasks.isEmpty()) {
			return MESSAGE_NO_NEXT_TASK;
		}

		toDisplay.clear();
		toDisplay.add(taskVault.getNextTask());

		assert(toDisplay.size() <= 1);

		saveVaults();

		return MESSAGE_SHOW_NEXT_TASK;

	}

	/**
	 * 
	 */
	private void saveVaults() {
		taskVault.save();
		trashVault.save();
		completedTaskVault.save();
	}

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

	private String addComment(String userCommand) {
		// TODO Auto-generated method stub
		String[] editArguments = parseEdit(userCommand, "addcomment");
		String taskName = editArguments[0].trim();
		String newComment = editArguments[1].trim();

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);

			taskVault.remove(taskName);
			taskVault.createTask(oldTask.getTaskName(), newComment,
					oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), oldTask.getEndTime());

//			historyVault.storeTask(oldTask);
//			historyVault.storeTask(taskVault.getTask(taskName));	
			history.add(oldTask);
			history.add(taskVault.getTask(oldTask.getTaskName()));
			commandStack.push(UNDOABLE.EDIT);			
			updateDisplay();
			saveVaults();
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
			return MESSAGE_INVALID_TIME;
		}
		if (taskExists(taskName)) {			
			Task oldTask = taskVault.getTask(taskName);
			

			if (oldTask.getEndTime() == null) {
				if (oldTask.getStartTime() == null) {
					return "Cannot edit end time of floating task";
				} else {
					return "Cannot edit end time of deadline";
				}
			}
			if(!isInOrder(oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), newEndTime)){
				return MESSAGE_NOT_CHRON;
			}
			taskVault.remove(taskName);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), newEndTime);

//			historyVault.storeTask(oldTask);
//			historyVault.storeTask(taskVault.getTask(taskName));		
			history.add(oldTask);
			history.add(taskVault.getTask(oldTask.getTaskName()));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
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
			return MESSAGE_INVALID_TIME;
		}

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);
			if(oldTask.getStartTime()==null){
				return "Cannot edit start time of floating task";
			}
			if(!isInOrder(oldTask.getStartDate(), newStartTime, oldTask.getEndDate(),
					oldTask.getEndTime())){
				return MESSAGE_NOT_CHRON ;
			}


			taskVault.remove(taskName);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					oldTask.getStartDate(), newStartTime, oldTask.getEndDate(),
					oldTask.getEndTime());

//			historyVault.storeTask(oldTask);
//			historyVault.storeTask(taskVault.getTask(taskName));	
			history.add(oldTask);
			history.add(taskVault.getTask(oldTask.getTaskName()));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
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
			return MESSAGE_INVALID_DATE;
		}

		if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);
			if (oldTask.getEndDate() == null) {
				if (oldTask.getStartTime() == null) {
					return "Cannot edit end date of floating task";
				} else {
					return "Cannot edit end date of deadline";
				}
			}
			if(!isInOrder(oldTask.getStartDate(), oldTask.getStartTime(), newEndDate,
					oldTask.getEndTime())){
				return MESSAGE_NOT_CHRON;
			}
			taskVault.remove(taskName);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					oldTask.getStartDate(), oldTask.getStartTime(), newEndDate,
					oldTask.getEndTime());

//			historyVault.storeTask(oldTask);
//			historyVault.storeTask(taskVault.getTask(taskName));		
			history.add(oldTask);
			history.add(taskVault.getTask(oldTask.getTaskName()));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
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
			return MESSAGE_INVALID_DATE;
		}

		if (newStartDate == null) {
			return "Enter a valid date";
		} else if (taskExists(taskName)) {
			Task oldTask = taskVault.getTask(taskName);
			if(oldTask.getStartDate()==null){
				return "Cannot edit start date of floating task";
			}

			if((oldTask.getEndDate()!=null) && !isInOrder(newStartDate, oldTask.getStartTime(), oldTask.getEndDate(),
					oldTask.getEndTime())){
				return MESSAGE_NOT_CHRON ;
			}
			taskVault.remove(taskName);
			taskVault.createTask(oldTask.getTaskName(), oldTask.getComment(),
					newStartDate, oldTask.getStartTime(), oldTask.getEndDate(),
					oldTask.getEndTime());

//			historyVault.storeTask(oldTask);
//			historyVault.storeTask(taskVault.getTask(taskName));	
			history.add(oldTask);
			history.add(taskVault.getTask(oldTask.getTaskName()));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS ;
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

			taskVault.remove(taskName);
			taskVault.createTask(newTaskName, oldTask.getComment(),
					oldTask.getStartDate(), oldTask.getStartTime(),
					oldTask.getEndDate(), oldTask.getEndTime());

//			historyVault.storeTask(oldTask);
//			historyVault.storeTask(taskVault.getTask(newTaskName));	
			history.add(oldTask);
			history.add(taskVault.getTask(newTaskName));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
		} else {
			return "task " + taskName + " not found";
		}
	}

	private boolean taskExists(String taskName) {
		return taskVault.getTask(taskName) != null;
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
		userCommand = removeFirstWord(userCommand);

		if (taskVault.getTask(userCommand)!=null) {
			commandStack.push(UNDOABLE.COMPLETE);
//			historyVault.storeTask(taskVault.getTask(userCommand));
			history.add(taskVault.getTask(userCommand));
			taskVault.completeTask(userCommand, completedTaskVault);
			updateDisplay();
			saveVaults();
			return "\"" + userCommand + "\"" + " completed successfully";
		} else {
			return "\"" + userCommand + "\"" + "couldn't be completed";
		}
	}

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
			saveVaults();
			return MESSAGE_TRASH_CLEARED;
		} else {
			return MESSAGE_TRASH_UNCLEARED;
		}
	}

	private String list(String userCommand) {
		userCommand = removeFirstWord(userCommand);

		String[] listArguments = parseList(userCommand);
		toDisplay.clear();

		if (listArguments[0].trim().equals("")) {
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
		if (listArguments[0].equalsIgnoreCase("trash")
				|| listArguments[0].equals("deleted")) {
			toDisplay = trashVault.getList();
			return "trash displayed";
		}
		if (listArguments[0].equalsIgnoreCase("completed")) {
			toDisplay = completedTaskVault.getList();
			return "completed tasks displayed";
		}
		if (listArguments[0].equalsIgnoreCase("history")) {
			toDisplay = historyVault.getList();
			return "history displayed";
		}

		try {
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
		} catch (ArrayIndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			updateDisplay();
			return "Invalid list command";
		}

		updateDisplay();
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
		userCommand = removeFirstWord(userCommand);

		String taskName = userCommand;
		if (taskVault.getTask(taskName)!=null) {
			commandStack.push(UNDOABLE.DELETE);
//			historyVault.storeTask(taskVault.getTask(taskName));
			history.add(taskVault.getTask(taskName));
			taskVault.deleteTask(taskName, trashVault);
			updateDisplay();
			saveVaults();
			return "\"" + taskName + "\"" + " deleted successfully";
		}

		return MESSAGE_DELETE_UNSUCCESS ;
	}

	private void updateDisplay() {
		toDisplay.clear();
		toDisplay = copyList(taskVault.getList());
	}

	private String add(String userCommand) {
		userCommand = removeFirstWord(userCommand);

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
		
		if((startDate!=null) && (startTime==null)){
			return ("Start date must be accompanied with start time");
		}
		
		if(endDate!=null){
			if (endTime==null){
				return "End date must be accompanied with an end time";
			}
		
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
			commandStack.push(UNDOABLE.ADD);
//			historyVault.storeTask(taskVault.getTask(addArguments[0]));
			history.add(taskVault.getTask(addArguments[0]));
			updateDisplay();
			saveVaults();
			return "Task \"" + addArguments[0] + "\" successfully added";
		}

		return "Task \"" + addArguments[0] + "\"" + " cannot be added";
	}

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
		if (commandTypeString.equalsIgnoreCase("undo")){
			return COMMAND_TYPE.UNDO;
		}
		if (commandTypeString.equalsIgnoreCase("changedir")){
			return COMMAND_TYPE.CHANGEDIR;
		}
		if (commandTypeString.equalsIgnoreCase("getdir")){
			return COMMAND_TYPE.GETDIR;
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
		if (description.matches(DATE_REGEX) || (split.length == 1)) {
			return null;
		}

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

	private static void writeToConfig(String newString) throws IOException {
		FileWriter fw = new FileWriter("config.txt", true);
		fw.write(newString);
		fw.close();
	}

	private static String getVaultPath(String fileName) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

		try {
			String line = bufferedReader.readLine();
			return line;
		} finally {
			bufferedReader.close();
		}
	}

	private static void clearConfigFile() throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File("config.txt"));
		writer.print("");
		writer.close();
	}
}
