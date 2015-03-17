package vault;

//import MODEL.Task;
//import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.collections.FXCollections;

/**
 * TrashVault class
 *
 * @author Qiyuan
 */
public class TrashVault extends Vault {

	public TrashVault(String filePath) throws IOException {
		storePath = Paths.get(filePath + "/trash.txt").toAbsolutePath();
		// setStorePath(storePath);
		if (canFindFile(storePath)) {
			openFile(storePath);
		}
		else {
			list = FXCollections.observableArrayList();
		}
	}

	public boolean emptyTrash() {
		list.clear();
		return true;
	}
}