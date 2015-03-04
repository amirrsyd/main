package STORAGE;

import MODEL.Task;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * FileStorage class
 *
 * @author Qiyuan
 */
public class TaskStorage extends Storage {
	
	private static final int ZERO = 0;
	
	public TaskStorage(String filePath) throws IOException {
		Path storePath = Paths.get(filePath + "taskList.txt").toAbsolutePath();
		setStorePath(storePath);
		checkAndCreateFile(storePath);
		openFile(getStorePath());
	}

	public boolean createTask(String taskName, String taskDescription, 
		                      LocalDate startDate, LocalTime startTime, 
	                          LocalDate endDate, LocalTime endTime) {
		Task task = new Task(taskName, taskDescription, startDate, startTime, 
			                 endDate, endTime);
		storeTask(task);
		return true;
	}

	public Task getNextTask() {
		return getList().get(ZERO);
	}

	public boolean completeTask(String taskName, CompletedTaskStorage completedTasks) {
		Task task = search(taskName);
		getList().remove(task);
		completedTasks.storeTask(task);
		return true;
	}

	public ObservableList<Task> getTasks() {
		return getList();
	}
}