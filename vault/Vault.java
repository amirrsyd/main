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
import java.util.Vector;
import java.time.format.DateTimeFormatter;

/**
 * Vault class
 *
 * @author Qiyuan
 */
public class Vault {

	protected static final Charset CHAR_SET = Charset.forName("US-ASCII");
	protected static final int ZERO = 0;
	protected static final int FIELD_NUMBER = 6;
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
		return list;
	}
	
	
	public void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(storePath, 
				                         CHAR_SET, StandardOpenOption.CREATE, 
				                         StandardOpenOption.WRITE, 
				                         StandardOpenOption.SYNC)) {
			while (!list.isEmpty()) {
				Task task = list.remove(ZERO);
				writer.write(task.getTaskName());
				if (task.getComment() != null) {
					writer.write(task.getComment());
				}
				if (task.getStartDate() != null) {
					writer.write(task.getStartDate().toString());
					writer.write(task.getStartTime().toString());
				}
				if (task.getEndDate() != null) {
					writer.write(task.getEndDate().toString());
					writer.write(task.getEndTime().toString());
				}
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
		    	Vector<String> taskLines = new Vector<String>();
		    	while (line != "") {
		    		line = reader.readLine();
		    		taskLines.add(line);
		    	}
		    	if (taskLines.size() < FIELD_NUMBER) {
		    		taskLines.add(null);
		    	}
		    	task.setComment(taskLines.remove(ZERO));
		    	
		    	task.setStartDate(changeStringToDate(taskLines.remove(ZERO)));
		    	task.setStartTime(changeStringToTime(taskLines.remove(ZERO)));
		    	task.setEndDate(changeStringToDate(taskLines.remove(ZERO)));
		    	task.setEndTime(changeStringToTime(taskLines.remove(ZERO)));
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
		return LocalTime.parse(timeString, timeFormat);
	}

	/**
	 * Changes the string passed to a LocalDate object
	 * @param dateString
	 * @return
	 */
	private LocalDate changeStringToDate(String dateString) {
		return LocalDate.parse(dateString, dateFormat);
	}
}