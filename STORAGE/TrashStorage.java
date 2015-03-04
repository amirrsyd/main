package STORAGE;

import MODEL.Task;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * TrashStorage class
 *
 * @author Qiyuan
 */
public class TrashStorage extends Storage {

	public TrashStorage(String filePath) throws IOException {
		Path storePath = Paths.get(filePath + "/trash.txt").toAbsolutePath();
		setStorePath(storePath);
		checkAndCreateFile(storePath);
		openFile(getStorePath());
	}

	public boolean emptyTrash() {
		getTrash().clear();
		return true;
	}

	public ObservableList<Task> getTrash() {
		return getList();
	}
}