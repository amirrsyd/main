package vault;

//import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import model.Task;

/**
 * HistoryVault class.
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
	 * Inserts the specified Task object into the list and return true.
	 * 
	 * @param newTask    Task object.
	 * @return           true if this is successful.
	 */
	public boolean storeTask(Task newTask) {
		list.add(newTask);
		return true;
	}
}