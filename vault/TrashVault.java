package vault;

//import MODEL.Task;
//import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.collections.FXCollections;

/**
 * TrashVault class.
 *
 * @author Qiyuan
 */
public class TrashVault extends Vault {

	/**
	 * Constructs a TrashVault object and use the dirPath to open an existing 
	 * file in the specified directory if possible. If the file does not exist, 
	 * it creates an empty list. The path to the directory must be valid and the 
	 * directory must exist. Otherwise it will throw an IOException.
	 * 
	 * @param dirPath         path to the directory for file access.
	 * @throws IOException    if path is invalid or directory does not exist.
	 */
	public TrashVault(String dirPath) throws IOException {
		filePath = Paths.get(dirPath + "/trash.txt").toAbsolutePath();
		fileName = "/trash.txt";
		// setFilePath(filePath);
		if (canFindFile(filePath)) {
			openFile(filePath);
		}
		else {
			list = FXCollections.observableArrayList();
		}
	}
	
	/**
	 * Removes all elements from the list.
	 * 
	 * @return    true if it is successful.
	 */
	public boolean emptyTrash() {
		list.clear();
		return true;
	}
}