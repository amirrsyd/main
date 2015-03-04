package STORAGE;

import MODEL.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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
import java.io.FileWriter;


/**
 * Storage class
 *
 * @author Qiyuan
 */
public class Storage {
	private static final Charset CHAR_SET = Charset.forName("US-ASCII");
	private static final int ZERO = 0;
	private static final int FIELD_NUMBER = 6;
	private static DateTimeFormatter timeFormat = DateTimeFormatter.ISO_LOCAL_TIME;
	private static DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;
	
	private ObservableList<Task> list;
	private Path storePath;

	//Default filePath = ""
	protected Storage() {
		//this("");
	}


	public Storage(String filePath) throws IOException {
		storePath = Paths.get(filePath + "storage.txt").toAbsolutePath();
		setStorePath(storePath);
		checkAndCreateFile(storePath);
		openFile(storePath);
	}

	protected void openFile(Path storePath) {
		list = FXCollections.observableArrayList();
		try {

			BufferedReader reader = new BufferedReader(new FileReader(storePath.toFile()));
			//BufferedReader reader = Files.newBufferedReader(storePath);
			
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
			error.printStackTrace();
			//System.err.format("%s%n", error);
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

	public boolean deleteTask(String taskName, TrashStorage trash) {
		Task task = search(taskName);
		if(search(taskName)==null) {
			System.out.println("BOOM");
		}
		trash.storeTask(task);
		return list.remove(task);
	}

	protected ObservableList<Task> getList() {
		return list;
	}
	
	//???
	/*	public void save() {
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
		}*/
	
		protected Task search(String taskName) {
			//search task
			for (int i = 0; i < list.size(); i++) {
				System.out.println(list.get(i).getTaskName().equals(taskName));
				System.out.println(list.get(i).getTaskName().length());
				System.out.println(taskName.length());
				if (list.get(i).getTaskName().equals(taskName)) {
					return list.get(i);
				}
			}
			return null;
		}


	protected Path getStorePath() {
		return storePath;
	}
	
	protected void setStorePath(Path storePath) {
		this.storePath = storePath;
	}
	public void save() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(storePath.toFile()));
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
			writer.close();
		} catch (IOException error) {
			System.err.format("%s%n", error);
		}
	}
//???
/*	public void save() {
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
	}*/

	/*
	 * This method checks whether the file has been created or not 
	 */
	protected static void checkAndCreateFile(Path storePath) throws IOException {
		File toCheck = storePath.toFile();
		
		if(!toCheck.exists()) {
			toCheck.createNewFile();
		}
/*		System.out.println(toCheck.exists());
		System.out.println(toCheck.canWrite());
		System.out.println(toCheck.canRead());*/
	}
	
}