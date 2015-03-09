package vault;

//import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import model.Task;

/**
 * HistoryVault class
 *
 * @author Qiyuan
 */
public class HistoryVault extends Vault {

	public HistoryVault(String filePath) throws IOException {
		Path storePath = Paths.get(filePath + "/history.txt").toAbsolutePath();
		// setStorePath(storePath);
		if (canFindFile(storePath)) {
			openFile(storePath);
		}
		else {
			list = FXCollections.observableArrayList();
		}
	}

	// Overriding method to remove sorting
	public boolean storeTask(Task newTask) {
		getList().add(newTask);
		return true;
	}
}