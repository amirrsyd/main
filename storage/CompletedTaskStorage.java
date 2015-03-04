package STORAGE;

import MODEL.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.collections.ObservableList;

/**
 * CompletedTaskStorage class
 *
 * @author Qiyuan
 */
public class CompletedTaskStorage extends Storage {

	public CompletedTaskStorage(String filePath) throws IOException {
		Path storePath = Paths.get(filePath + "/completed.txt").toAbsolutePath();
		setStorePath(storePath);
		checkAndCreateFile(storePath);
	}

	public ObservableList<Task> getCompletedTasks() {
		return getList();
	}
}