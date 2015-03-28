package vault;

//import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import model.Task;

/**
 * HistoryVault class.
 * The list within HistoryVault must be unsorted and treated 
 * to be Stack-like with a Last-In-First Out priority.
 *
 * @author Qiyuan
 */
public class HistoryVault extends Vault {

	/**
	 * Constructs a HistoryVault object and use the dirPath to open an existing 
	 * file in the specified directory if possible. If the file does not exist, 
	 * it creates an empty list. The path to the directory must be valid and the 
	 * directory must exist. Otherwise it will throw an IOException.
	 * 
	 * @param dirPath         path to the directory for file access.
	 * @throws IOException    if path is invalid or directory does not exist.
	 */
	public HistoryVault(String dirPath) throws IOException {
		filePath = Paths.get(dirPath + "/history.txt").toAbsolutePath();
		// setFilePath(filePath);
		fileName = "/history.txt";
		if (canFindFile(filePath)) {
			openFile(filePath);
		}
		else {
			list = FXCollections.observableArrayList();
		}
	}

	/**
	 * This method overrides the same method inherited from Vault.
	 * Inserts the specified Task object into the list and return true 
	 * if it is successful. Also, The list must not already contain the 
	 * object and the task object must not have a null taskName.
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
		return list.add(newTask);
	}
	
	/**
	 * This method overrides the same method inherited from Vault.
	 * Removes the last occurrence of the Task object with the specified 
	 * taskName from the list WITHOUT moving it to trash.
	 * 
	 * @param taskName    name of the task.
	 * @return            true if simple removal is successful.
	 */
	public boolean remove(String taskName) {
		Task task = backSearch(taskName);
		if (task == null) {
			return false;
		}
		// task object must be unique as it removes first occurrence
		return list.remove(task); 
	}
	/**
	 * Pops the last found occurrence of the specified task.
	 * 
	 * @param taskName    name of the task.
	 * @return            the task if successful else null.
	 */
	public Task pop(String taskName) {
		Task task = backSearch(taskName);
		return task;
	}
	
	/**
	 * This method overrides the same method inherited from Vault.
	 * Does nothing since this method should not be used on HistoryVault
	 * as designed-by-contract.
	 */
	public void save() {
	}
}