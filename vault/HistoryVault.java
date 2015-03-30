package vault;

//import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import model.Task;

/**
 * HistoryVault class.
 * The list within HistoryVault will be unsorted and treated 
 * to be Stack-like with a Last-In-First Out priority. It will also 
 * allow duplicates within it.
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
	 * if it is successful WITHOUT sorting. Also, the task object must 
	 * not have a null taskName.
	 * 
	 * @param newTask    Task object.
	 * @return           true if this is successful.
	 */
	public boolean storeTask(Task newTask) {
		if (newTask == null) {
			return false;
		}
		if (newTask.getTaskName() == null) {
			return false;
		}
		return list.add(newTask);
	}
	
	/**
	 * Removes the last found occurrence of the specified task and 
	 * return it.
	 * 
	 * @param taskName    name of the task.
	 * @return            the task if it is successful else null.
	 */
	public Task pop(String taskName) {
		int index = backSearchIndex(taskName);
		if (index == INVALID) {
			return null;
		}
		return list.remove(index); 
	}
	
	/**
	 * This method overrides the same method inherited from Vault.
	 * Always returns false since this method should not be used on HistoryVault
	 * as designed-by-contract.
	 * 
	 * @param taskName    name of the task.
	 * @return            false.
	 */
	public boolean remove(String taskName) {
		return false;
	}
	
	/**
	 * This method overrides the same method inherited from Vault.
	 * Does nothing since this method should not be used on HistoryVault
	 * as designed-by-contract.
	 */
	public void save() {
	}
}