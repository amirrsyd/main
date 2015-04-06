package vault;

import model.IdGenerator;
import model.Task;
import model.RecurringTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

/**
 * Vault class.
 *
 * @author Qiyuan
 */
public class Vault {

	private static final String YEARLY_SPACE = "YEARLY ";
	private static final String DAILY = "DAILY";
	private static final String MONTHLY_SPACE = "MONTHLY ";
	private static final String WEEKLY_SPACE = "WEEKLY ";
	private static final String RECURRING_SPACE = "RECURRING ";
	protected static final int DATE_TIME_LENGTH = 3;
	protected static final int OFFSET_10 = 10;
	protected static final int OFFSET_8 = 8;
	private static final int OFFSET_7 = 7;
	protected static final int ZERO = 0;
	protected static final int INVALID = -1;
	protected static final String END_TIME_DOUBLE_SPACE = "endtime  ";
	protected static final String END_DATE_DOUBLE_SPACE = "enddate  ";
	protected static final String END_TIME_SPACE = "endtime ";
	protected static final String END_DATE_SPACE = "enddate ";
	protected static final String START_TIME_SPACE = "starttime ";
	protected static final String START_DATE_SPACE = "startdate ";
	protected static final String COMMENT_SPACE = "comment ";
	protected static final String COMMENT_DOUBLE_SPACE = "comment  ";
	protected static final String START_TIME_DOUBLE_SPACE = "starttime  ";
	protected static final String START_DATE_DOUBLE_SPACE = "startdate  ";
	protected static final String END_OF_TASK = "end of task";
	protected static final Charset CHAR_SET = Charset.forName("US-ASCII");
	protected static DateTimeFormatter timeFormat = DateTimeFormatter.ISO_LOCAL_TIME;
	protected static DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;
	
	protected ObservableList<Task> list;
	protected Path filePath;
	protected String fileName;

	/**
	 * Default constructor.
	 */
	public Vault() {
	}
	
	/**
	 * Constructs a Vault object and use the dirPath to open an existing file 
	 * in the specified directory if possible. If the file does not exist, it 
	 * creates an empty list. The path to the directory must be valid and the 
	 * directory must exist. Otherwise it will throw an IOException.
	 * 
	 * @param dirPath         path to the directory for file access.
	 * @throws IOException    if path is invalid or directory does not exist.
	 */
	public Vault(String dirPath) throws IOException {
		filePath = Paths.get(dirPath + "/vault.txt").toAbsolutePath();
		fileName = "/vault.txt";
		//setFilePath(filePath);
		// If file exists, open it. If it doesn't, create empty list.
		if (canFindFile(filePath)) {
			openFile(filePath);
		}
		else {
			list = FXCollections.observableArrayList();
		}
	}
	
	/**
	 * Obtains the first occurrence of the Task object with the specified 
	 * taskName in the list and returns it.
	 * 
	 * @param taskName    name of the task.
	 * @return            the Task object.
	 */
	public Task getTask(String taskName) {
		Task task = search(taskName);
		return task;
	}
	
	/**
	 * Inserts the specified Task object into the list, sort the list and 
	 * return true if it is successful. Also, The list must not already 
	 * contain the object and the task object must not have a null taskName.
	 * 
	 * @param newTask    Task object.
	 * @return           true if this is successful.
	 */
	public boolean storeTask(Task newTask) {
		if (newTask == null || list.contains(newTask)) {
			return false;
		}
		if (newTask.getTaskName() == null) {
			return false;
		}
		list.add(newTask);
		FXCollections.sort(list);	//Task implements comparable
		return true;
	}
	
	/**
	 * Removes the first occurrence of the Task object with the specified 
	 * taskName from the list and move it to trash.
	 * 
	 * @param taskName    name of the task.
	 * @param trash       TrashVault object.
	 * @return            true if deletion is successful.
	 */
	public boolean deleteTask(String taskName, TrashVault trash) {
		if (taskName == null || trash == null) {
			return false;
		}
		Task task = search(taskName);
		if(task == null) {
			return false;
		}
		trash.storeTask(task);
		return list.remove(task);
	}
	
	/**
	 * Removes the first occurrence of the Task object with the specified 
	 * taskName from the list WITHOUT moving it to trash.
	 * 
	 * @param taskName    name of the task.
	 * @return            true if simple removal is successful.
	 */
	public boolean remove(String taskName) {
		Task task = search(taskName);
		if (task == null) {
			return false;
		}
		IdGenerator idGenerator = new IdGenerator();
		idGenerator.removeId(task.getId());
		return list.remove(task);
	}
	
	/**
	 * Returns a copy of the list.
	 * 
	 * @return    the copied list.
	 */
	public ObservableList<Task> getList() {
		ObservableList<Task> listCopy = FXCollections.observableArrayList();;
		for (int i = 0; i < list.size(); i++) {
			listCopy.add(list.get(i));
		}
		return listCopy;
	}
	
	/**
	 * Saves the list in a special string format into the relevant file.
	 */	
	public void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(filePath, 
				                         CHAR_SET, StandardOpenOption.CREATE, 
				                         StandardOpenOption.WRITE, 
				                         StandardOpenOption.SYNC,
				                         StandardOpenOption.TRUNCATE_EXISTING)) {
			int i = 0;
			while (i < list.size()) {
				Task task = list.get(i);
				writer.write(task.getTaskName());
				writer.newLine();
				if (task.getComment() != null) {
					writer.write(COMMENT_SPACE + task.getComment());
					writer.newLine();
				}
				else {
					writer.write(COMMENT_DOUBLE_SPACE);
					writer.newLine();
				}
				if (task.getStartDate() != null) {
					writer.write(START_DATE_SPACE + task.getStartDate().toString());
					writer.newLine();
					writer.write(START_TIME_SPACE + task.getStartTime().toString());
					writer.newLine();
				}
				else {
					writer.write(START_DATE_DOUBLE_SPACE);
					writer.newLine();
					writer.write(START_TIME_DOUBLE_SPACE);
					writer.newLine();
				}
				if (task.getEndDate() != null) {
					writer.write(END_DATE_SPACE + task.getEndDate().toString());
					writer.newLine();
					writer.write(END_TIME_SPACE + task.getEndTime().toString());
					writer.newLine();
				}
				else {
					writer.write(END_DATE_DOUBLE_SPACE);
					writer.newLine();
					writer.write(END_TIME_DOUBLE_SPACE);
					writer.newLine();
				}
				if (task.isRecurring()) {
					RecurringTask recurringTask = (RecurringTask) task;
					writer.write(RECURRING_SPACE + recurringTask.getRecurrence());
					writer.newLine();
					if (recurringTask.isDaily()) {
						writer.write(DAILY);
					}
					else if (recurringTask.getRecurrenceDay() != null) {
						writer.write(WEEKLY_SPACE + recurringTask.getRecurrenceDay().toString());
					}
					else if (recurringTask.getDayOfMonth() > ZERO) {
						writer.write(MONTHLY_SPACE + recurringTask.getDayOfMonth());
					}
					else {
						writer.write(YEARLY_SPACE + recurringTask.getMonthDay().toString());
					}
					writer.newLine();
				}
				if (task.getId() != null) {
					writer.write("ID " + task.getId());
					writer.newLine();
				}
				else {
					writer.write("ID");
					writer.newLine();
				}
				i++;
				writer.write(END_OF_TASK);
				writer.newLine();
			}
		} catch (IOException error) {
			System.err.format("%s%n", error);
		}
	}

	/**
	 * Returns the filePath.
	 * 
	 * @return    the path to the file.
	 */
	public Path getFilePath() {
		return filePath;
	}
	
	/**
	 * Sets the filePath for file access in the directory path.
	 * 
	 * @param dirPath    the path to the directory.    
	 */
	public void setFilePath(Path dirPath) {
		assert dirPath != null;
		this.filePath = Paths.get(dirPath + fileName).toAbsolutePath();
	}
	
	/**
	 * Deletes the associated file.
	 */
	public void deleteFile(){
		filePath.toFile().delete();
	}
	
	/**
	 * Clears the list and the data in the file.
	 */
	public boolean clear() {
		IdGenerator idGenerator = new IdGenerator();
		for (Task task : list) {
			idGenerator.removeId(task.getId());
		}
		list.clear();
		save();
		return true;
	}

	/**
	 * Search for the first Task object in the list based on taskName.
	 * 
	 * @param taskName    name of the task.
	 * @return            Task object if found else null.
	 */
	protected Task search(String taskName) {
		if (taskName == null) {
			return null;
		}
		if (taskName.startsWith("@")) {
			String idString = taskName;
			// Search from the front for the task using id
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getId().equals(idString)) {
					return list.get(i);
				}			
			}		
			return null;
		}
		else {
			// Search from the front for the task using taskName
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getTaskName().equals(taskName)) {
					return list.get(i);
				}			
			}		
			return null;
		}
	}

	/**
	 * Search for the last Task object in the list based on taskName.
	 * 
	 * @param taskName    name of the task.
	 * @return            index of the Task object if found else .
	 */
	protected int backSearchIndex(String taskName) {
		if (taskName == null) {
			return INVALID;
		}
		// Search from the back for the task
		for (int i = list.size() - 1; i >= 0; i--) {
			if (list.get(i).getTaskName().equals(taskName)) {
				return i;
			}			
		}		
		return INVALID;
	}

	/**
	 * Checks whether the file exists and return true if it does.
	 * 
	 * @param filePath        the path to the file.
	 * @return                true if the file exists.
	 * @throws IOException    if path is invalid.
	 */
	protected static boolean canFindFile(Path filePath) throws IOException {
		File file = filePath.toFile();
		if(!file.exists()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Opens the file, located by filePath, to read it and store the data 
	 * into list.
	 *  
	 * @param filePath    the path to the file.
	 */
	protected void openFile(Path filePath) {
		list = FXCollections.observableArrayList();
		try {

			//BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
			BufferedReader reader = Files.newBufferedReader(filePath);
			
			String line = reader.readLine();
		    while (line != null) {
		    	Task task = new Task(line);
	    		line = reader.readLine();
		    	while (line != null) {
		    		if (line.startsWith(COMMENT_SPACE)) {
		    			task.setComment(line.substring(OFFSET_8));
		    		}
		    		if (line.startsWith(START_DATE_SPACE)) {
		    			task.setStartDate(changeStringToDate(line.substring(OFFSET_10)));
		    		}
		    		if (line.startsWith(START_TIME_SPACE)) {
		    			task.setStartTime(changeStringToTime(line.substring(OFFSET_10)));
		    		}
		    		if (line.startsWith(END_DATE_SPACE)) {
		    			task.setEndDate(changeStringToDate(line.substring(OFFSET_8)));
		    		}
		    		if (line.startsWith(END_TIME_SPACE)) {
		    			task.setEndTime(changeStringToTime(line.substring(OFFSET_8)));
		    		}
		    		if (line.startsWith(RECURRING_SPACE)) {
		    			int recurrence = Integer.parseInt(line.substring(OFFSET_10));
		    			line = reader.readLine();
		    			if (line.startsWith(DAILY)) {
		    				task = new RecurringTask(task, recurrence);
		    			}
		    			else if (line.startsWith(WEEKLY_SPACE)) {
		    				DayOfWeek dayOfWeek = DayOfWeek.valueOf(line.substring(OFFSET_7));
		    				task = new RecurringTask(task, recurrence, dayOfWeek);
		    			}
		    			else if (line.startsWith(MONTHLY_SPACE)) {
		    				int dayOfMonth = Integer.parseInt(line.substring(OFFSET_8));
		    				task = new RecurringTask(task, recurrence, dayOfMonth);
		    			}
		    			else if (line.startsWith(YEARLY_SPACE)){
		    				MonthDay monthDay = MonthDay.parse(line.substring(OFFSET_7));
		    				task = new RecurringTask(task, recurrence, monthDay);
		    			}
		    			else {
		    				task = new RecurringTask(task, recurrence);
		    			}
		    		}
		    		if (line.startsWith("ID")) {
			    		if (line.length() > 2) {
			    			task.setId(line.substring(3).trim());
			    		}
			    		else {
			    			task.setId(null);
			    		}
		    		}
		    		line = reader.readLine();
		    		if (line.startsWith(END_OF_TASK)) {
		    			break;
		    		}
		    	}
			    storeTask(task);
		    	line = reader.readLine();
		    }
		    reader.close();
		} catch (IOException error) {
			//error.printStackTrace();
			System.err.format("%s%n", error);
		}
	}
	
	/**
	 * Changes the string passed to a LocalTime object.
	 * 
	 * @param timeString    the time in String format.
	 * @return              the time represented by a LocalTime instance.
	 */
	private LocalTime changeStringToTime(String timeString) {
		if (timeString.length() < DATE_TIME_LENGTH) {
			return null;
		}
		return LocalTime.parse(timeString, timeFormat);
	}

	/**
	 * Changes the string passed to a LocalDate object.
	 * 
	 * @param dateString    the date in String format.
	 * @return              the date represented by a LocalDate instance.
	 */
	private LocalDate changeStringToDate(String dateString) {
		if (dateString.length() < DATE_TIME_LENGTH) {
			return null;
		}
		return LocalDate.parse(dateString, dateFormat);
	}
}