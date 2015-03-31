package logic;
 

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
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
import model.RecurringTask;
import model.RecurringTask.Day;
import model.Task;
/**
* <h1>CdLogic Class</h1>
* The CdLogic class implements the comman.DO application that
* is a task manager. The methods below implement the functionalities
* of commands entered by a user
* 
*/
public class CdLogic {
	
	/**
	 * Listed below are the String constants e.g. Message returns
	 * This is to ensure easier code readability
	 * Start Date & End Date is in the format (DD:MM:YYYY) 
	 * Start Time & End Time is in the format (HH:MM) 
	 */
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
	private static final String MESSAGE_NO_UNDO = "no more undo left" ;
	private static final String MESSAGE_FLOAT_ETIME = "Cannot edit end time of floating task" ;
    private static final String MESSAGE_DEADLINE_ETIME = "Cannot edit end time of deadline" ;
    private static final String MESSAGE_FLOAT_EDATE = "cannot edit end date of floating task" ;
    private static final String MESSAGE_DEADLINE_EDATE = "cannot edit end date of deadline" ;
	/**
	 * We call the classes from Vault (Storage) to use them in our methods as parsers
	 * 
	 */
	private static TaskVault taskVault;
	private static TrashVault trashVault;
	private static HistoryVault historyVault;
	private static CompletedTaskVault completedTaskVault;
	private static Stack<UNDOABLE> commandStack;
	private static String vaultPath;

	private static ObservableList<Task> toDisplay;
	private static ObservableList<Task> tasks;
	//private static ObservableList<Task> history;
	
	
	enum UNDOABLE {
		ADD, DELETE, COMPLETE, EDIT, CHANGEDIR
	}


	enum COMMAND_TYPE {
		ADD, DELETE, LIST, EMPTY, SEARCH, COMPLETE, EDIT, INVALID, EXIT, CHANGEDIR, UNDO, NEXT, GETDIR, ADDRECUR, RECUR
	}

	enum TaskType {
		FLOAT, EVENT, DATELINE
	}
	/**
	 * Method that will work with GUI and TaskVault class.
	 *  Initializes the display of tasks that is retrieved from TaskVault
	 *  @throws I0Exception
	 */

	public CdLogic() throws IOException {
		initializeFromConfig();
		initializeVaults();
		historyVault.clear();
		historyVault = new HistoryVault(vaultPath);
		//history = FXCollections.observableArrayList();
		commandStack = new Stack<UNDOABLE>();
		tasks = taskVault.getList();
		toDisplay = copyList(tasks);
		lookForRecurrence();
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
	 * Obtains tasklist from TaskVault class
	 * Returns tasks to be viewed on the GUI
	 */
	public ObservableList<Task> getTaskList() {
		tasks = FXCollections.observableArrayList(taskVault.getList());

		return FXCollections.observableArrayList(toDisplay);
	}

	public ObservableList<Task> getDisplayList() {
		return toDisplay;
	}
	/**
	 * If command is empty, will display MESSAGE_INVALID_FORMAT
	 * Else, it will check the first word entered by user
	 * followed by obtaining the command type from method determineCommandType
	 * and executes command
	 * add(userCommand), delete(userCommand), list(userCommand), empty(), search(userCommand)
	 * complete(userCommand), edit(userCommand), exit(System.exit(0)), next(), undo(), changedir(userCommand)
	 * getdir() 
	 * @throws Error Message 
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
		case NEXT:
			return next();
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
		default:
			// throw an error if the command is not recognized
			throw new Error(MESSAGE_ERROR);
		}
	}

	private void lookForRecurrence(){
		ObservableList<Task> list = taskVault.getList();
		for (int i = 0; i<list.size(); i++){
			Task currTask = list.get(i);
			if(currTask.taskIsRecurring()){
				RecurringTask recurringTask = (RecurringTask) list.get(i);
				if(recurringTask.getEndDate() != null){
					while(getEndLDT(recurringTask).isBefore(LocalDateTime.now())){
						taskVault.remove(recurringTask.getTaskName());
						setNextRecurrence(recurringTask);
					}
				}else{
					while(getStartLDT(recurringTask).isBefore(LocalDateTime.now())){
						taskVault.remove(recurringTask.getTaskName());
						setNextRecurrence(recurringTask);
						recurringTask = (RecurringTask) taskVault.getTask(currTask.getTaskName());
					}
				}
			}
		}
		updateDisplay();
		saveVaults();
	}
	
	private void setNextRecurrence(RecurringTask recurringTask) {
		if (recurringTask.getRecurrence()==1){
			
		}else if (recurringTask.getRecurrenceDay() != null) {
			setNextWeek(recurringTask);
		} else if(recurringTask.getDayOfMonth()!= 0){
			setNextMonth(recurringTask);
		}
	}

	private void setNextMonth(RecurringTask recurringTask) {
		// TODO Auto-generated method stub
		int dayOfMonth = recurringTask.getDayOfMonth();
		LocalDate oldStartDate = recurringTask.getStartDate();
		LocalDate newStartDate;
		LocalDate oldEndDate = recurringTask.getEndDate();
		LocalDate newEndDate = null;
		RecurringTask newTask;
		
		try{
			newStartDate = oldStartDate.plusMonths(1).withDayOfMonth(dayOfMonth);
		}catch (DateTimeException e){
			newStartDate = oldStartDate.plusMonths(1);
		}
		
		if (oldEndDate != null){
			newEndDate = newStartDate.plusDays(Period.between(oldStartDate, oldEndDate).getDays());
		}
		
		newTask = new RecurringTask(recurringTask.getTaskName(),
				recurringTask.getComment(), newStartDate,
				recurringTask.getStartTime(), newEndDate,
				recurringTask.getEndTime(), recurringTask.getRecurrence() - 1,
				dayOfMonth);

		if ((oldEndDate != null) && hasOverlap(newStartDate, newEndDate)) {
			newTask.setRecurrence(recurringTask.getRecurrence());
			setNextRecurrence(newTask);
		} else {
			taskVault.storeTask(newTask);
		}
		
	}

	/**
	 * @param recurringTask
	 */
	private void setNextWeek(RecurringTask recurringTask) {
		DayOfWeek recurrenceDay = recurringTask.getRecurrenceDay();
		LocalDate oldStartDate = recurringTask.getStartDate();
		LocalDate newStartDate;
		LocalDate oldEndDate = recurringTask.getEndDate();
		LocalDate newEndDate = null;
		RecurringTask newTask;
		
		if (oldStartDate.getDayOfWeek() == recurrenceDay) {
			newStartDate = oldStartDate.plusWeeks(1);
			if (oldEndDate != null) {
				newEndDate = oldEndDate.plusWeeks(1);
			}
		}else {
			newStartDate = oldStartDate.with(TemporalAdjusters.next(recurrenceDay));
			if (oldEndDate != null){
				newEndDate = newStartDate.plusDays(Period.between(oldStartDate, oldEndDate).getDays());
			}
				
		}
		newTask = new RecurringTask(recurringTask.getTaskName(),
				recurringTask.getComment(), newStartDate,
				recurringTask.getStartTime(), newEndDate,
				recurringTask.getEndTime(), recurringTask.getRecurrence() - 1,
				recurrenceDay);

		if ((oldEndDate != null) && hasOverlap(newStartDate, newEndDate)) {
			newTask.setRecurrence(recurringTask.getRecurrence());
			setNextRecurrence(newTask);
		} else {
			taskVault.storeTask(newTask);
		}
			
	}

	private boolean hasOverlap(LocalDate newStartDate, LocalDate newEndDate) {
		// TODO Auto-generated method stub
		for (int i = 0; i < taskVault.getList().size(); i++){
			Task currTask = taskVault.getList().get(i);
			if((currTask.getStartDate()!=null)&&(currTask.getEndDate()!=null)){
				if(newEndDate.isAfter(currTask.getStartDate()) && newEndDate.isBefore(currTask.getEndDate())){
					return true;
				}
				if(newStartDate.isAfter(currTask.getStartDate()) && newStartDate.isBefore(currTask.getEndDate())){
					return true;
				}
			}
		}
		return false;
	}

	private String addrecur(String userCommand) {
		String response = add(userCommand);
		String[] addArguments = parseAdd(removeFirstWord(userCommand));
		if(!response.contains("successfully added")){
			return response;
		}
		
		String taskName = addArguments[0];
		String recurDetails = addArguments[1];
		
		taskVault.getTask(taskName).setComment(null);
		
		if(addArguments[2]==null){
			taskVault.remove(removeFirstWord(userCommand));
			updateDisplay();
			saveVaults();
			return "cannot recur floating task";
		}
		
		if(recurDetails ==null){
			taskVault.remove(taskName);
			updateDisplay();
			saveVaults();
			return "insert recursion details";
		}
		
		String recurResponse = recur("recur " + taskName + " " + recurDetails);
		if(!recurResponse.contains("will recur")){
			taskVault.remove(taskName);
			updateDisplay();
			saveVaults();
			return recurResponse;
		}
		
		updateDisplay();
		saveVaults();
		return recurResponse;
	}

	private String recur(String userCommand) {
		// TODO Auto-generated method stub
		String trimmedCommand = removeFirstWord(userCommand).trim();
		
		String taskName = lookForTaskName(trimmedCommand);
		
		if(taskName.equals("")){
			return "Task not found";
		}
		
		trimmedCommand = trimmedCommand.replaceFirst(taskName, "");
		if (getFirstWord(trimmedCommand).equals("monthly")){
			String recurDetails = removeFirstWord(trimmedCommand);
			return recurMonth(taskName, recurDetails);
		}
		if (getFirstWord(trimmedCommand).equals("weekly")){
			String recurDetails = removeFirstWord(trimmedCommand);
			return recurWeek(taskName, recurDetails);
		}
		
		return null;
	}

	private String recurWeek(String taskName, String recurDetails) {
		// TODO Auto-generated method stub
		DayOfWeek dayOfWeek;
		int recurrence;
		String[] recurDetailsArray = recurDetails.split("\\s+");
		if (recurDetailsArray.length == 2) {
			dayOfWeek = getDay(recurDetailsArray[0]);

			try {
				recurrence = Integer.parseInt(recurDetailsArray[1]);
			} catch (NumberFormatException e) {
				return "insert valid number for number of recurrence";
			}
			
			if(dayOfWeek == null){
				return "insert valid day to recur on";
			}
			
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			Task newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfWeek);
			taskVault.storeTask(newRecurringTask);

			return taskName + " will recur every " + dayOfWeek
					+ " for " + recurrence + " times.";

		}else if(recurDetailsArray.length == 1 && !recurDetailsArray[0].equals("")){
			try {
				recurrence = Integer.parseInt(recurDetailsArray[0]);
			} catch (NumberFormatException e) {
				return "insert valid number for number of recurrence";
			}
			
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			dayOfWeek = toRecur.getStartDate().getDayOfWeek();
			Task newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfWeek);
			taskVault.storeTask(newRecurringTask);
			
			return taskName + " will recur every " + dayOfWeek
					+ " for " + recurrence + " times.";

		}else if(recurDetails.equals("")){
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			dayOfWeek = toRecur.getStartDate().getDayOfWeek();
			recurrence = 10;
			Task newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfWeek);
			taskVault.storeTask(newRecurringTask);
			
			return taskName + " will recur every " + dayOfWeek
					+ " for " + recurrence + " times.";
		}
	
			
		return "invalid recurrence format";
	}

	private DayOfWeek getDay(String dayString) {
		// TODO Auto-generated method stub
		if(dayString.toLowerCase().contains("mon")){
			return DayOfWeek.MONDAY;
		}
		if(dayString.toLowerCase().contains("tue")){
			return DayOfWeek.TUESDAY;
		}
		if(dayString.toLowerCase().contains("wed")){
			return DayOfWeek.WEDNESDAY;
		}
		if(dayString.toLowerCase().contains("thu")){
			return DayOfWeek.THURSDAY;
		}
		if(dayString.toLowerCase().contains("fri")){
			return DayOfWeek.FRIDAY;
		}
		if(dayString.toLowerCase().contains("sat")){
			return DayOfWeek.SATURDAY;
		}
		if(dayString.toLowerCase().contains("sun")){
			return DayOfWeek.SUNDAY;
		}
		return null;
	}

	private String recurMonth(String taskName, String recurDetails) {
		// TODO Auto-generated method stub
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
			} catch (NumberFormatException e) {
				return "insert valid number for number of recurrence";
			}
			
			if(dayOfMonth<1 || dayOfMonth>31){
				return "day of month must be between 1 and 31";
			}
			
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			Task newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfMonth);
			taskVault.storeTask(newRecurringTask);

			return taskName + " will recur every " + dayOfMonth
					+ " of the month " + " for " + recurrence + " times.";

		}else if(recurDetailsArray.length == 1 && !recurDetailsArray[0].equals("")){
			try {
				recurrence = Integer.parseInt(recurDetailsArray[0]);
			} catch (NumberFormatException e) {
				return "insert valid number for number of recurrence";
			}
			
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			dayOfMonth = toRecur.getStartDate().getDayOfMonth();
			Task newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfMonth);
			taskVault.storeTask(newRecurringTask);
			
			return taskName + " will recur every " + dayOfMonth
					+ " of the month " + " for " + recurrence + " times.";

		}else if(recurDetails.equals("")){
			Task toRecur = taskVault.getTask(taskName);
			taskVault.remove(taskName);
			dayOfMonth = toRecur.getStartDate().getDayOfMonth();
			recurrence = 10;
			Task newRecurringTask = new RecurringTask(toRecur, recurrence,
					dayOfMonth);
			taskVault.storeTask(newRecurringTask);
			
			return taskName + " will recur every " + dayOfMonth
					+ " of the month " + " for " + recurrence + " times.";
		}
	
			
		return "invalid recurrence format";
	}

	
	
	private String lookForTaskName(String trimmedCommand) {
		String found = "";
		ObservableList<Task> list = taskVault.getList();
		for (int i = 0; i < list.size(); i++) {
			Task currTask = list.get(i);
			if (trimmedCommand.contains(currTask.getTaskName())
					&& (currTask.getTaskName().length() > found.length())) {
				found = currTask.getTaskName();
			}
		}
		return found;
	}

	/**
	 * Obtain directory
	 */

	private String getDirectory() {
		// TODO Auto-generated method stub
		
		return "Working directory: " + vaultPath;
	}
	/**
     * method that returns the new directory when change directory command is entered by user
     * if directory fails to exist, returns MESSAGE_NONEXIST
	 * @throws IOException
	 */
	
	private String changeDirectory(String userCommand) throws IOException {
		String newPathString = removeFirstWord(userCommand).trim();
	
		File newPath = new File(newPathString);
		newPath = newPath.getAbsoluteFile();
		if (newPath.isDirectory()) {
			Task oldDirTask = new Task(vaultPath);
			//history.add(oldDirTask);
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
			return "files moved to \"" + newPathString + "\"";
		}
		
		return MESSAGE_NONEXIST ;
		
	}

	/**
	 * creates an instance of the different Vault classes 
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
	 * method that performs undo command
	 * if task is empty, returns MESSAGE_NO_UNDO
	 * undoAdd, undoComplete, undoDelete, undoEdit, undoChangedir 
	 * @throws IOException
	 */

	private String undo() throws IOException {
		// TODO Auto-generated method stub
		if(commandStack.isEmpty()){
			return MESSAGE_NO_UNDO ;
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
	/**
	 * performs undoChangedir command
	 * If directory specified does not exit, returns MESSAGE_NONEXIST
	 * @throws IOException
	 */
	private String undoChangeDir() throws IOException {
		//String newPathString = history.remove(history.size()-1).getTaskName();
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
			return "files moved to \"" + newPathString + "\"";
		}
		
		return MESSAGE_NONEXIST ;
	}
	
	/**
	 * @return
	 */
	private String getLastHistoryName() {
		return historyVault.getList().get(historyVault.getList().size() -1).getTaskName();
	}
	
	/**
	 * performs undoEdit command to revert back to original
	 */
	private String undoEdit() {
//		ObservableList<Task> historyList = historyVault.getList();
//		Task newTask = historyList.get(historyList.size() - 1);
//		Task oldTask = historyList.get(historyList.size() - 2);
		Task newTask = historyVault.pop(getLastHistoryName());
		Task oldTask = historyVault.pop(getLastHistoryName());
		trashVault.remove(oldTask.getTaskName());
		taskVault.remove(newTask.getTaskName());
		taskVault.storeTask(oldTask);
//		historyVault.remove(oldTask.getTaskName());
//		historyVault.remove(newTask.getTaskName());
		updateDisplay();
		saveVaults();
		return "Undo: \"" + oldTask.getTaskName() + "\" has been restored to its original";
	}
	/**
	 * performs undoDelete command
	 */

	private String undoDelete() {
//		ObservableList<Task> historyList = historyVault.getList();
//		Task historyTask = historyList.get(historyList.size() - 1);
		System.out.println("Latest deleted:" + getLastHistoryName());
		Task historyTask = historyVault.pop(getLastHistoryName());
		taskVault.storeTask(historyTask);
		trashVault.remove(historyTask.getTaskName());
//		historyVault.remove(historyTask.getTaskName());
		updateDisplay();
		saveVaults();
		return "Undo: \"" + historyTask.getTaskName()
				+ "\" moved back from trash to tasks";
	}
	
	/**
	 * performs undoComplete command
	 */
	private String undoComplete() {
//		ObservableList<Task> historyList = historyVault.getList();
//		Task historyTask = historyList.get(historyList.size() - 1);
		Task historyTask = historyVault.pop(getLastHistoryName());
		taskVault.storeTask(historyTask);
		completedTaskVault.remove(historyTask.getTaskName());
//		historyVault.remove(historyTask.getTaskName());
		updateDisplay();
		saveVaults();
		return "Undo: \"" + historyTask.getTaskName() + "\" moved back from completed to tasks";
	}
	
	/**
	 * performs undoAdd command 
	 */

	private String undoAdd() {
//		ObservableList<Task> historyList = historyVault.getList();
//		Task historyTask = historyList.get(historyList.size()-1);
		Task historyTask = historyVault.pop(getLastHistoryName());
		taskVault.remove(historyTask.getTaskName());
//		historyVault.remove(historyTask.getTaskName());
		updateDisplay();
		saveVaults();
		return "Undo: \"" + historyTask.getTaskName() + "\" removed from tasks";
	}
	/**
	 * performs add command
	 * if task list is empty, return MESSAGE_NO_NEXT_TASK
	 * toDisplay clears the taks list and adds the consecutive task
	 * task is saved in saveVaults() method
	 */

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
		historyVault.save();
	}
	/**
	 * method that performs the respective edit instructions
	 * if invalid String, MESSAGE_INVALID_EDIT will be returned
	 * e.g. tasknam, startdate, starttime, enddate, endtime, addcomment
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
	 * performs commenting of a task
	 * adds the new comment to the existing task and saves the changes
	 * updates display of the task with comment added
	 * @param userCommand
	 */

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

			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(taskName));	
//			history.add(oldTask);
//			history.add(taskVault.getTask(oldTask.getTaskName()));
			commandStack.push(UNDOABLE.EDIT);			
			updateDisplay();
			saveVaults();
			return "comment added";
		} else {
			return "task " + taskName + " not found";
		}
	}
	/**
	 * method that edits endtime of tasks
	 * For floating tasks where endtime is null, 
	 * execution will return message
	 * For deadline tasks, where endtime is null but starttime is not null, 
	 * execution will return message
	 * Method will also not allow if endtime entered is before the stipulated start time of 
	 * a specific task
	 * @param userCommand
	 */

	private String editEndTime(String userCommand) {
		// TODO Auto-generated method stub
		String[] editArguments = parseEdit(userCommand, "endtime");
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
					return MESSAGE_FLOAT_ETIME;
				} else {
					return MESSAGE_DEADLINE_ETIME;
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

			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(taskName));		
//			history.add(oldTask);
//			history.add(taskVault.getTask(oldTask.getTaskName()));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
		} else {
			return "task " + taskName + " not found";
		}

	}

	/**
	 * Method does not allow floating tasks to have starttime edited 
	 * because it was formerly null
	 * @param userCommand
	 */
	private String editStartTime(String userCommand) {
		// TODO Auto-generated method stub
		String[] editArguments = parseEdit(userCommand, "starttime");
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

			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(taskName));	
//			history.add(oldTask);
//			history.add(taskVault.getTask(oldTask.getTaskName()));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
		} else {
			return "task " + taskName + " not found";
		}
	}

	/**
	 * Method does not allow edit of date for floating tasks
	 * Method does not allow edit of date for deadline if endDate is null
	 * Method does not allow endDate to be before Startdate of task
	 * @param userCommand
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
					return MESSAGE_FLOAT_EDATE;
				} else {
					return MESSAGE_DEADLINE_EDATE;
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

			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(taskName));		
//			history.add(oldTask);
//			history.add(taskVault.getTask(oldTask.getTaskName()));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
		} else {
			return "task " + taskName + " not found";
		}
	}

	/**
	 * Method does not allow edit of start date of floating task
	 * @param userCommand
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

			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(taskName));	
//			history.add(oldTask);
//			history.add(taskVault.getTask(oldTask.getTaskName()));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS ;
		} else {
			return "task " + taskName + " not found";
		}
	}

	/**
	 * Method prevents tasks with same name to be stored 
	 * @param userCommand
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

			historyVault.storeTask(oldTask);
			historyVault.storeTask(taskVault.getTask(newTaskName));	
//			history.add(oldTask);
//			history.add(taskVault.getTask(newTaskName));
			commandStack.push(UNDOABLE.EDIT);
			updateDisplay();
			saveVaults();
			return MESSAGE_EDIT_SUCCESS;
		} else {
			return "task " + taskName + " not found";
		}
	}
	/**
	 * @param taskName 	 
	 */

	private boolean taskExists(String taskName) {
		return taskVault.getTask(taskName) != null;
	}
	
	/**
	 * @param userCommand, string
	 */

	private String[] parseEdit(String userCommand, String string) {
		// TODO Auto-generated method stub

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

		if (taskVault.getTask(userCommand)!=null) {
			Task completedTask = taskVault.getTask(userCommand);
			commandStack.push(UNDOABLE.COMPLETE);
			historyVault.storeTask(taskVault.getTask(userCommand));
//			history.add(taskVault.getTask(userCommand));
			taskVault.completeTask(userCommand, completedTaskVault);
			if(completedTask.taskIsRecurring()){
				setNextRecurrence((RecurringTask) completedTask);
			}
			updateDisplay();
			saveVaults();
			return "\"" + userCommand + "\"" + " completed successfully";
		} else {
			return "\"" + userCommand + "\"" + "couldn't be completed";
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
		return found + " tasks found";
	}
	/**
	 * @param taskIndex
	 */
	private boolean isNotYetFound(int taskIndex) {
		return !(toDisplay.contains(tasks.get(taskIndex)));
	}
	/**
	 * @param currentWord, taskIndex
	 */
	private boolean isPartialMatch(String currentWord, int taskIndex) {
		return tasks.get(taskIndex).getTaskName().contains(currentWord);
	}
	/**
	 * @param userCommand, taskIndex
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
			if (trashVault.emptyTrash()) {
				saveVaults();
				return MESSAGE_TRASH_CLEARED;
			} else {
				return MESSAGE_TRASH_UNCLEARED;
			}
		} else if (userCommand.equalsIgnoreCase("completed")){
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
	/**
	 * @param listArguments array
	 */

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
	/**
	 * @param listArguments array
	 * @return
	 */
	private boolean isValidDayPeriod(String[] listArguments) {
		// TODO Auto-generated method stub
		return (listArguments[2] == null && listArguments[0].matches(DATE_REGEX)
				&& listArguments[1].matches(DATE_REGEX));
	}
	/**
	 * @return Weekly sorted tasks
	 */

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
			historyVault.storeTask(taskVault.getTask(taskName));
//			history.add(taskVault.getTask(taskName));
			taskVault.deleteTask(taskName, trashVault);
			updateDisplay();
			saveVaults();
			return "\"" + taskName + "\"" + " deleted successfully";
		}

		return MESSAGE_DELETE_UNSUCCESS ;
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
			historyVault.storeTask(taskVault.getTask(addArguments[0]));
//			history.add(taskVault.getTask(addArguments[0]));
			updateDisplay();
			saveVaults();
			return "Task \"" + addArguments[0] + "\" successfully added";
		}

		return "Task \"" + addArguments[0] + "\"" + " cannot be added";
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
		if (commandTypeString.equalsIgnoreCase("undo")){
			return COMMAND_TYPE.UNDO;
		}
		if (commandTypeString.equalsIgnoreCase("changedir")){
			return COMMAND_TYPE.CHANGEDIR;
		}
		if (commandTypeString.equalsIgnoreCase("getdir")){
			return COMMAND_TYPE.GETDIR;
		}
		if (commandTypeString.equalsIgnoreCase("recur")){
			return COMMAND_TYPE.RECUR;
		}
		if (commandTypeString.equalsIgnoreCase("addrecur")){
			return COMMAND_TYPE.ADDRECUR;
		}
		return COMMAND_TYPE.INVALID;
	}

	// PARSE METHODS
	/**
	 * Method that takes in taskName, taskDescription, Startdate, Startime, Enddate, Endtime
	 * @param command
	 * @return details of task that have been entered by user
	 */

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
	/**
	 * Method to extract dates and times of tasks entered by user
	 * @param data
	 * @return Start date, Start time, End date, End time
	 */

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
	/**
	 * Method to search tasks for specific keyword entered by user
	 * @param data
	 * @return search keyword
	 */

	public String[] parseSearch(String data) {
		return data.trim().split("\\s+");
	}

	// STRING MANIPULATION METHODS
	/**
	 * Method to get task name
	 * @param data
	 * @return taskName
	 */

	public String getTaskName(String data) {
		String task = data.split(DATE_REGEX, 2)[0];
		task = task.trim();

		return task;
	}
	/**
	 * Method to get task description
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
	 * @param userCommand
	 * @return command 
	 */

	private String getFirstWord(String userCommand) {
		return userCommand.trim().split("\\s+")[0];
	}
	/**
	 * Method to remove word from original string and replace with empty space
	 * @param userCommand
	 * @return 
	 */
	private String removeFirstWord(String userCommand) {
		return userCommand.replaceFirst(getFirstWord(userCommand), "").trim();
	}
	/**
	 * Method to standardize date input (DD:MM:YYYY)
	 * @param dataString
	 * @return 
	 */
	private LocalDate toLocalDate(String dateString) throws DateTimeParseException{
		if ((dateString == null) || (!dateString.matches(DATE_REGEX))) {
			return null;
		}
		DateTimeFormatter dateFormatter = DateTimeFormatter
				.ofPattern("dd/MM/yyyy");
		LocalDate date = LocalDate.parse(dateString, dateFormatter);

		return date;
	}
	/**
	 * Method to standardize time (HH:MM)
	 * @param data
	 * @return
	 */

	private LocalTime toLocalTime(String timeString) throws DateTimeParseException{
		if ((timeString == null) || (!timeString.matches(TIME_REGEX))) {
			return null;
		}
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		LocalTime time = LocalTime.parse(timeString, timeFormatter);

		return time;
	}
	/**
	 * Method to return Start Date & Start Time
	 * @param task
	 * @return Start date, Start time
	 */

	public LocalDateTime getStartLDT(Task task) {
		return LocalDateTime.of(task.getStartDate(), task.getStartTime());
	}
	/**
	 * Method to return End Date & End Time
	 * @param task
	 * @return End date, End time
	 */

	public LocalDateTime getEndLDT(Task task) {
		return LocalDateTime.of(task.getEndDate(), task.getEndTime());
	}
	/**
	 * Method to check if start date & start time is before end date and end time
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
	 * @param fileName
	 * @return 
	 * @throws IOException
	 */

	private static String getVaultPath(String fileName) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

		try {
			String line = bufferedReader.readLine();
			return line;
		} finally {
			bufferedReader.close();
		}
	}
	/**
	 * Method to clear file
	 * @return config.txt
	 * @throws IOException
	 */

	private static void clearConfigFile() throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File("config.txt"));
		writer.print("");
		writer.close();
	}
}