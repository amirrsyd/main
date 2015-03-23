package vault;

import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;

import model.Task;

/**
 * taskVault class.
 *
 * @author Qiyuan
 */
public class TaskVault extends Vault {
	
	/**
	 * Constructs a TaskVault object and use the dirPath to open an existing 
	 * file in the specified directory if possible. If the file does not exist, 
	 * it creates an empty list. The path to the directory must be valid and the 
	 * directory must exist. Otherwise it will throw an IOException.
	 * 
	 * @param dirPath         path to the directory for file access.
	 * @throws IOException    if path is invalid or directory does not exist.
	 */
	public TaskVault(String dirPath) throws IOException {
		filePath = Paths.get(dirPath + "/taskList.txt").toAbsolutePath();
		// setFilePath(filePath);
		if (canFindFile(filePath)) {
			openFile(filePath);
		}
		else {
			list = FXCollections.observableArrayList();
		}
	}
	
	/**
	 * Creates a Task object with the specified taskName, taskDescription,
	 * startDate, startTime, endDate and endTime. Then returns true if it
	 * is successful.
	 * 
	 * @param taskName           name of the task.
	 * @param taskDescription    description of the task.
	 * @param startDate          starting date of the task.
	 * @param startTime          starting time of the task.
	 * @param endDate            ending date of the task.
	 * @param endTime            ending time of the tasl.
	 * @return                   true if it is successful.
	 */
	public boolean createTask(String taskName, String taskDescription,
			                  LocalDate startDate, LocalTime startTime, 
			                  LocalDate endDate, LocalTime endTime) {
		Task task = new Task(taskName, taskDescription, startDate, startTime,
				             endDate, endTime);
		storeTask(task);
		return true;
	}
	
	/**
	 * Returns the first Task object in the list. It returns null if the list
	 * is empty.
	 * 
	 * @return    first Task object in the list if list is not empty else null.
	 */
	public Task getNextTask() {
		if (list.size() == ZERO) {
			return null;
		}
		return list.get(ZERO);
	}
	
	/**
	 * Removes the first Task object with the same name from the list and 
	 * move it to completedTasks and return true.
	 * 
	 * @param taskName          name of the task.
	 * @param completedTasks    CompletedTaskVault object.
	 * @return                  true if it is successful.
	 */
	public boolean completeTask(String taskName, 
			                    CompletedTaskVault completedTasks) {
		Task task = search(taskName);
		list.remove(task);
		completedTasks.storeTask(task);
		return true;
	}
}