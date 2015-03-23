package vault;

//import MODEL.Task;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.collections.FXCollections;

//import javafx.collections.ObservableList;

/**
 * CompletedTaskVault class.
 *
 * @author Qiyuan
 */
public class CompletedTaskVault extends Vault {

	/**
	 * Constructs a CompletedTaskVault object and use the dirPath to open an 
	 * existing file in the specified directory if possible. If the file does 
	 * not exist, it creates an empty list. The path to the directory must be 
	 * valid and the directory must exist. Otherwise it will throw an 
	 * IOException.
	 * 
	 * @param dirPath         path to the directory for file access.
	 * @throws IOException    if path is invalid or directory does not exist.
	 */
	public CompletedTaskVault(String dirPath) throws IOException {
		filePath = Paths.get(dirPath + "/completed.txt").toAbsolutePath();
		// setFilePath(filePath);
		if (canFindFile(filePath)) {
			openFile(filePath);
		}
		else {
			list = FXCollections.observableArrayList();
		}
	}
}