package vault;

import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;

import model.Task;

/**
 * taskVault class
 *
 * @author Qiyuan
 */
public class TaskVault extends Vault {

	public TaskVault(String filePath) throws IOException {
		storePath = Paths.get(filePath + "/taskList.txt").toAbsolutePath();
		// setStorePath(storePath);
		if (canFindFile(storePath)) {
			openFile(storePath);
		}
		else {
			list = FXCollections.observableArrayList();
		}
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
		if (list.size() == ZERO) {
			return null;
		}
		return list.get(ZERO);
	}

	public boolean completeTask(String taskName, 
			                    CompletedTaskVault completedTasks) {
		Task task = search(taskName);
		list.remove(task);
		completedTasks.storeTask(task);
		return true;
	}
}