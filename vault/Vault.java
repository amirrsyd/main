package vault;

import model.Task;
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
import java.time.format.DateTimeFormatter;

/**
 * Vault class
 *
 * @author Qiyuan
 */
public class Vault {

	protected static final int DATE_TIME_LENGTH = 3;
	protected static final int OFFSET_10 = 10;
	protected static final int OFFSET_8 = 8;
	protected static final int ZERO = 0;
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
	protected static final String NULL = "null";
	protected static final String END_TIME = "endtime";
	protected static final String END_DATE = "enddate";
	protected static final String START_TIME = "starttime";
	protected static final String START_DATE = "startdate";
	protected static final String COMMENT = "comment";
	protected static final Charset CHAR_SET = Charset.forName("US-ASCII");
	protected static DateTimeFormatter timeFormat = DateTimeFormatter.ISO_LOCAL_TIME;
	protected static DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;
	
	protected ObservableList<Task> list;
	protected Path storePath;


	//Default Constructor
	public Vault() {
	}

	public Vault(String filePath) throws IOException {
		storePath = Paths.get(filePath + "vault.txt").toAbsolutePath();
		//setStorePath(storePath);
		// If file exists, open it. If it doesn't, create empty list.
		if (canFindFile(storePath)) {
			openFile(storePath);
		}
		else {
			list = FXCollections.observableArrayList();
		}
	}

	public Task getTask(String taskName) {
		Task task = search(taskName);
		return task;
	}

	public boolean storeTask(Task newTask) {
		list.add(newTask);
		System.out.println("Sorted");
		FXCollections.sort(list);	//Task implements comparable
		return true;
	}

	public boolean deleteTask(String taskName, TrashVault trash) {
		Task task = search(taskName);
		if(search(taskName)==null) {
			System.out.println("BOOM");
		}
		trash.storeTask(task);
		return list.remove(task);
	}

	public ObservableList<Task> getList() {
		ObservableList<Task> listCopy = FXCollections.observableArrayList();;
		for (int i = 0; i < list.size(); i++) {
			listCopy.add(list.get(i));
		}
		return listCopy;
	}
	
	
	public void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(storePath, 
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
				i++;
				writer.write(NULL);
				writer.newLine();
			}
		} catch (IOException error) {
			System.err.format("%s%n", error);
		}
	}

	/*protected Path getStorePath() {
		return storePath;
	}*/
	
	/*protected void setStorePath(Path storePath) {
		this.storePath = storePath;
	}*/
	
	protected Task search(String taskName) {
		//search task
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getTaskName().equals(taskName)) {
				return list.get(i);
			}			
		}		
		return null;
	}

	/*
	 * This method checks whether the file exists
	 */
	protected static boolean canFindFile(Path storePath) throws IOException {
		File file = storePath.toFile();
		if(!file.exists()) {
			return false;
		}
		return true;
	}
	
	protected void openFile(Path storePath) {
		list = FXCollections.observableArrayList();
		try {

			//BufferedReader reader = new BufferedReader(new FileReader(storePath.toFile()));
			BufferedReader reader = Files.newBufferedReader(storePath);
			
			String line = reader.readLine();
		    while (line != null) {
		    	Task task = new Task(line);
	    		line = reader.readLine();
		    	while (line != null) {
		    		if (line.startsWith(COMMENT)) {
		    			task.setComment(line.substring(OFFSET_8));
		    		}
		    		if (line.startsWith(START_DATE)) {
		    			task.setStartDate(changeStringToDate(line.substring(OFFSET_10)));
		    		}
		    		if (line.startsWith(START_TIME)) {
		    			task.setStartTime(changeStringToTime(line.substring(OFFSET_10)));
		    		}
		    		if (line.startsWith(END_DATE)) {
		    			task.setEndDate(changeStringToDate(line.substring(OFFSET_8)));
		    		}
		    		if (line.startsWith(END_TIME)) {
		    			task.setEndTime(changeStringToTime(line.substring(OFFSET_8)));
		    		}
		    		line = reader.readLine();
		    		if (line.startsWith(NULL)) {
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
	 * Changes the string passed to a LocalTime object
	 * @param timeString
	 * @return
	 */
	private LocalTime changeStringToTime(String timeString) {
		if (timeString.length() < DATE_TIME_LENGTH) {
			return null;
		}
		return LocalTime.parse(timeString, timeFormat);
	}

	/**
	 * Changes the string passed to a LocalDate object
	 * @param dateString
	 * @return
	 */
	private LocalDate changeStringToDate(String dateString) {
		if (dateString.length() < DATE_TIME_LENGTH) {
			return null;
		}
		return LocalDate.parse(dateString, dateFormat);
	}
}