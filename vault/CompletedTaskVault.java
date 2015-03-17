package vault;

//import MODEL.Task;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.collections.FXCollections;

//import javafx.collections.ObservableList;

/**
 * CompletedTaskVault class
 *
 * @author Qiyuan
 */
public class CompletedTaskVault extends Vault {

	public CompletedTaskVault(String filePath) throws IOException {
		storePath = Paths.get(filePath + "/completed.txt").toAbsolutePath();
		// setStorePath(storePath);
		if (canFindFile(storePath)) {
			openFile(storePath);
		}
		else {
			list = FXCollections.observableArrayList();
		}
	}
}